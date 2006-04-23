package edu.stanford.smi.protege.server.framestore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.server.framestore.background.FrameEvaluationEvent;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class ServerFrameStore extends UnicastRemoteObject implements RemoteServerFrameStore {
    private static transient Logger log = Log.getLogger(ServerFrameStore.class);
  
    private FrameStore _delegate;
    private KnowledgeBase _kb;
    private FifoWriter<ServerEventWrapper> _eventWriter = new FifoWriter<ServerEventWrapper>();
    private Map<RemoteSession, Registration> _sessionToRegistrationMap 
      = new HashMap<RemoteSession, Registration>();
    private boolean _isDirty;
    private Object _kbLock;

    private static Map<Thread,RemoteSession> sessionMap = new HashMap<Thread, RemoteSession>();
    
    private FrameCalculator frameCalculator;

    private static final int DELAY_MSEC = Integer.getInteger("server.delay", 0).intValue();
    private static final int MIN_PRELOAD_FRAMES = Integer.getInteger("preload.frame.limit", 5000).intValue();
    /*
     * A performance hack Identical copies of the same sft are reduced to the same object so that only a single copy
     * needs to be sent over the wire.
     */
    private Map<Sft,Sft> sftMap = new HashMap<Sft,Sft>();

    
    //ESCA-JAVA0160 
    public ServerFrameStore(FrameStore delegate, 
                            KnowledgeBase kb,
                            Object kbLock) throws RemoteException {
        _delegate = delegate;
        _kb = kb;
        _kbLock = kbLock;
        kb.setDispatchEventsEnabled(false);
        frameCalculator = new FrameCalculator(_delegate, 
                                              _kbLock, 
                                              _eventWriter, 
                                              _sessionToRegistrationMap);
        frameCalculator.start();
        // kb.setJournalingEnabled(true);
        if (DELAY_MSEC != 0) {
            //used for simulating slow network response time
            Log.getLogger().config("Simulated delay of " + DELAY_MSEC + " msec/call");
        }
    }

    private FrameStore getDelegate() {
        return _delegate;
    }

    private static int nDelayedCalls = 0;

    private static void delay() {
        if (DELAY_MSEC != 0) {
            SystemUtilities.sleepMsec(DELAY_MSEC);
            if (++nDelayedCalls % 10 == 0) {
                Log.getLogger().info(nDelayedCalls + " delayed calls");
            }
        }
    }

    public static void recordCall(RemoteSession session) {
        delay();
        synchronized(sessionMap) {
          sessionMap.put(Thread.currentThread(), session);
        }
    }

    public static RemoteSession getCurrentSession() {
      synchronized(sessionMap) {
        return sessionMap.get(Thread.currentThread());
      }
    }

    public int getClsCount(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsCount();
      }
    }

    public int getSlotCount(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getSlotCount();
      }
    }

    public int getFacetCount(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFacetCount();
      }
    }

    public int getSimpleInstanceCount(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getSimpleInstanceCount();
      }
    }

    public int getFrameCount(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFrameCount();
      }
    }

    public List getDirectTemplateSlots(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectTemplateSlots(cls);
      }
    }

    public ValueUpdate removeDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate moveDirectTemplateSlot(Cls cls, Slot slot, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate addDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectSuperclass(cls, superclass);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate removeDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectSuperslot(slot, superslot);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate removeDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectSuperclass(cls, superclass);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate moveDirectSubclass(Cls cls, Cls subclass, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public RemoteResponse<List> getDirectTemplateSlotValues(Cls cls, 
                                                            Slot slot, 
                                                            RemoteSession session) {
      recordCall(session);
      LocalizeUtils.localize(cls, _kb);
      frameCalculator.addRequest(cls, session);
      synchronized(_kbLock) {
        List values = getDelegate().getDirectTemplateSlotValues(cls, slot);
        return new RemoteResponse<List>(values, getEvents(session));
      }
    }

    public Set getSuperslots(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getSuperslots(slot);
      }
    }

    public Set<Slot> getOwnSlots(Frame frame, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOwnSlots(frame);
      }
    }

    public Set getInstances(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getInstances(cls);
      }
    }

    public Set getFramesWithDirectOwnSlotValue(Slot slot, Object value, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
      }
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
      }
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value);
      }
    }

    public Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
      }
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value,
            int maxMatches, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
      }
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
      }
    }

    public List getDirectSuperclasses(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectSuperclasses(cls);
      }
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTemplateSlotValues(cls, slot);
      }
    }

    public RemoteResponse<List> getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) {
      recordCall(session);
      frameCalculator.addRequest(cls, session);
      synchronized(_kbLock) {
        List values = getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
        return new RemoteResponse<List>(values, getEvents(session));
      }
    }

    public Set<Cls> getClses(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClses();
      }
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTemplateFacets(cls, slot);
      }
    }

    public RemoteResponse<Frame> getFrame(String name, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        Frame frame = getDelegate().getFrame(name);
        if (frame != null) {
          frameCalculator.addRequest(frame, session);
        }
        return new RemoteResponse(frame, getEvents(session));
      }
    }

    public Frame getFrame(FrameID id, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFrame(id);
      }
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOwnSlotValues(frame, slot);
      }
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot, RemoteSession session) {
      recordCall(session);
      frameCalculator.addRequest(frame, session);
      synchronized(_kbLock) {
        return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
      }
    }

    public RemoteResponse<List> getDirectOwnSlotValues(Frame frame, Slot slot, RemoteSession session) {
      recordCall(session);
      if (log.isLoggable(Level.FINE)) {
        log.fine("getDirectOwnSlotValues for frame " + frame.getFrameID() + " slot " + slot.getFrameID());
      }
      LocalizeUtils.localize(frame, _kb);
      LocalizeUtils.localize(slot, _kb);
      synchronized(_kbLock) {
        List values = getDelegate().getDirectOwnSlotValues(frame, slot);
        frameCalculator.addRequest(frame, session);
        if (slot.getFrameID().equals(Model.SlotID.DIRECT_SUBCLASSES)) {
          for (Object o : values) {
            if (o instanceof Frame) {
              frameCalculator.addRequest((Frame) o, session);
            }
          }
        }
        return new RemoteResponse<List>(values, getEvents(session));
      }
    }

    public List getDirectInstances(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectInstances(cls);
      }
    }

    public Set<Cls> getSubclasses(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        Set<Cls> subclasses = getDelegate().getSubclasses(cls);
        for (Cls subclass : subclasses) {
          frameCalculator.addRequest(subclass, session);
        }
        return subclasses;
      }
    }

    public Set<Slot> getSlots(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getSlots();
      }
    }

    public Set getSuperclasses(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getSuperclasses(cls);
      }
    }

    public Set getSubslots(Slot slot, RemoteSession session) {
      synchronized(_kbLock) {
        return getDelegate().getSubslots(slot);
      }
    }

    public ValueUpdate setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public RemoteResponse<Facet> createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaults,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Facet facet = getDelegate().createFacet(id, name, directTypes, loadDefaults);
        return new RemoteResponse<Facet>(facet, getEvents(session));
      }
    }

    public List<Cls> getDirectSubclasses(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        List<Cls> subclasses = getDelegate().getDirectSubclasses(cls);
        for (Cls subclass : subclasses) {
          frameCalculator.addRequest(subclass, session);
        }
        return subclasses;
      }
    }

    public Set<Frame> getFrames(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFrames();
      }
    }

    public ValueUpdate setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        return new ValueUpdate(getEvents(session));
      }
    }

    public Set getTypes(Instance instance, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTypes(instance);
      }
    }

    public Set getTemplateSlots(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTemplateSlots(cls);
      }
    }

    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
      }
    }

    public ValueUpdate deleteCls(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteCls(cls);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate deleteSlot(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteSlot(slot);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate deleteFacet(Facet facet, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteFacet(facet);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate deleteSimpleInstance(SimpleInstance simpleInstance, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteSimpleInstance(simpleInstance);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

  public RemoteResponse<Slot> createSlot(FrameID id, String name, Collection directTypes,
                                 Collection directSuperslots,
                                 boolean loadDefaults, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Slot slot =  getDelegate().createSlot(id, name, directTypes, directSuperslots, loadDefaults);
        return new RemoteResponse<Slot>(slot, getEvents(session));
      }
    }

    public List getDirectTypes(Instance instance, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectTypes(instance);
      }
    }

    public List getDirectSubslots(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectSubslots(slot);
      }
    }

    public ValueUpdate addDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectSuperslot(slot, superslot);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate addDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectTemplateSlot(cls, slot);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public RemoteResponse<Cls> createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaults, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Cls cls = getDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaults);
        return new RemoteResponse(cls, getEvents(session));
      }
    }

    public Set<Facet> getFacets(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFacets();
      }
    }

    public Set executeQuery(Query query, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().executeQuery(query);
      }
    }

    public ValueUpdate removeDirectType(Instance instance, Cls directType, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectType(instance, directType);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public Set getReferences(Object value, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getReferences(value);
      }
    }

    public Set getMatchingReferences(String value, int maxMatches, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getMatchingReferences(value, maxMatches);
      }
    }

    public Set getClsesWithMatchingBrowserText(String value, Collection superclasses, int maxMatches,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsesWithMatchingBrowserText(value, superclasses, maxMatches);
      }
    }

    public RemoteResponse<SimpleInstance> createSimpleInstance(FrameID id, String name, Collection directTypes,
            boolean loadDefaults, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        SimpleInstance si = getDelegate().createSimpleInstance(id, name, directTypes, loadDefaults);
        return new RemoteResponse(si, getEvents(session));
      }
    }

    public ValueUpdate addDirectType(Instance instance, Cls type, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectType(instance, type);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public ValueUpdate moveDirectType(Instance instance, Cls cls, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectType(instance, cls, index);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public List getDirectSuperslots(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectSuperslots(slot);
      }
    }

    public String getFrameName(Frame frame, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFrameName(frame);
      }
    }

    public ValueUpdate setFrameName(Frame frame, String name, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
          getDelegate().setFrameName(frame, name);
          markDirty();
          return new ValueUpdate(getEvents(session));
      }
    }

    public Set getOwnFacets(Frame frame, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOwnFacets(frame, slot);
      }
    }

    public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOwnFacetValues(frame, slot, facet);
      }
    }

    public Set getOverriddenTemplateSlots(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOverriddenTemplateSlots(cls);
      }
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
      }
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
      }
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
      }
    }

    public ValueUpdate removeDirectTemplateFacetOverrides(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        markDirty();
        return new ValueUpdate(getEvents(session));
      }
    }

    public void close(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        // do nothing
      }
    }

    public void register(RemoteSession session) {
      synchronized(_kbLock) {
        Registration registration = new Registration(_eventWriter);
        _sessionToRegistrationMap.put(session, registration);
      }
    }
    
    public void deregister(RemoteSession session) {
      synchronized(_kbLock) {
        _sessionToRegistrationMap.remove(session);
        if (_sessionToRegistrationMap.isEmpty()) {
          frameCalculator.dispose();
        }
      }
    }

    public String toString() {
        return "ServerFrameStoreImpl";
    }

    public List<EventObject> getEvents(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        for (EventObject eo : getDelegate().getEvents()) {
          _eventWriter.write(new ServerEventWrapper(eo));
        }
        List<EventObject> events = new ArrayList<EventObject>();
        Registration reg = _sessionToRegistrationMap.get(session);
        if (reg == null) {
          throw new IllegalStateException("Not registered");
        }
        FifoReader<ServerEventWrapper> clientEvents = reg.getEvents();
        ServerEventWrapper eventWrapper;
        while ((eventWrapper = clientEvents.read()) != null) {
          EventObject event = eventWrapper.getEvent();
          if (log.isLoggable(Level.FINEST)) {
            log.finest("Found event " + event);
          }
          if (event instanceof FrameEvaluationEvent) {
            FrameEvaluationEvent frameValue = (FrameEvaluationEvent) event;
            if (frameCalculator.checkInterest(frameValue, session)) {
              events.add(frameValue);
            }
          } else {
            events.add(eventWrapper.getEvent());
          }
        }
        return events;
      }
    }
    
    public boolean beginTransaction(String name, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().beginTransaction(name);
      }
    }

    public boolean commitTransaction(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().commitTransaction();
      }
    }

    public boolean rollbackTransaction(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().rollbackTransaction();
      }
    }

    public boolean isDirty() {
        return _isDirty;
    }

    private void markDirty() {
        _isDirty = true;
    }

    public void markClean() {
        _isDirty = false;
    }

    public List getDirectDomain(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectDomain(slot);
      }
    }

    public Set getDomain(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDomain(slot);
      }
    }

    public ValueUpdate moveDirectSubslot(Slot slot, Slot subslot, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
        return new ValueUpdate(getEvents(session));
      }
    }

    public Set getFramesWithAnyDirectOwnSlotValue(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
      }
    }

    public Set getClsesWithAnyDirectTemplateSlotValue(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
      }
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
      }
    }

    /*
     * Avoid sending the very common "empty list" over the wire.
     */
    private void insertValues(Map map, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        if (values == null || !values.isEmpty()) {
            if (values != null) {
                values = new ArrayList(values);
            }
            map.put(getSft(slot, facet, isTemplate), values);
        }
    }


    private Sft getSft(Slot slot, Facet facet, boolean isTemplate) {
        Sft sft = new Sft(slot, facet, isTemplate);
        Sft mapSft = (Sft) sftMap.get(sft);
        if (mapSft == null) {
            sftMap.put(sft, sft);
            mapSft = sft;
        }
        return mapSft;
    }

    private void insertNullValues(Map map, Slot slot, Facet facet, boolean isTemplate) {
        insertValues(map, slot, facet, isTemplate, null);
    }

    public RemoteResponse<List> preload(Set<String> userFrames, boolean all, RemoteSession session) {
      recordCall(session);
      Set<Frame> frames;
      int frameCount = 0;
      synchronized (_kbLock) {
        frameCount = getDelegate().getFrameCount();
      }
      if (all ||  frameCount < MIN_PRELOAD_FRAMES) {
        synchronized (_kbLock) {
          frames = getDelegate().getFrames();
        }
      } else {
        frames = new LinkedHashSet<Frame>();
        Cls rootClass = null;
        synchronized (_kbLock) {
          rootClass = _kb.getRootCls();
        }
        addSystemClasses(frames, rootClass);
        List<Cls> subClasses = null;
        synchronized (_kbLock) {
          subClasses = _delegate.getDirectSubclasses(rootClass);
        }
        for (Cls subClass : subClasses) {
          frames.add(subClass);
        }
        for (String frameName : userFrames) {
          Frame frame = null;
          synchronized(_kbLock) {
            frame = _delegate.getFrame(frameName);
          }
          frames.add(frame);
        }
      }
      frameCalculator.preLoadFrames(frames, session);
      return new RemoteResponse<List>(null, getEvents(session));
    }
    
    private void addSystemClasses(Collection<Frame> classes, Cls cls)  {
      if (!cls.getFrameID().isSystem() || classes.contains(cls)) {
        return;
      }
      List<Cls> subClasses = null;
      synchronized (_kbLock) {
        subClasses = _delegate.getDirectSubclasses(cls);
      }
      for (Cls subclass : subClasses) {
        addSystemClasses(classes, subclass);
      }
    }

    private void addFrame(Collection<Frame> frames, String className) {
      Cls cls;
      synchronized (_kbLock) {
        cls = (Cls) getDelegate().getFrame(className);
      }
      frames.add(cls);
    }

    private void addHierarchy(Collection<Frame> frames, String className) {
      Cls cls;
      synchronized (_kbLock) {
        cls= (Cls) getDelegate().getFrame(className);
      }
      frames.add(cls);
      synchronized (_kbLock) {
        frames.addAll(getDelegate().getSubclasses(cls));
      }
    }

    private void addRootHierarchy(Collection<Frame> frames, Cls rootCls) {
      frames.add(rootCls);
      Iterator<Cls> i;
      synchronized (_kbLock) {
        i = getDelegate().getDirectSubclasses(rootCls).iterator();
      }
      while (i.hasNext()) {
        Cls subclass = i.next();
        if (!subclass.isSystem()) {
          frames.add(subclass);
          synchronized (_kbLock) {
            frames.addAll(getDelegate().getDirectSubclasses(subclass));
          }
        }
      }
    }
}
