package edu.stanford.smi.protege.server.framestore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.EventGeneratorFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.server.update.InvalidateCacheUpdate;
import edu.stanford.smi.protege.server.update.OntologyUpdate;
import edu.stanford.smi.protege.server.update.RemoteResponse;
import edu.stanford.smi.protege.server.update.RemoveCacheUpdate;
import edu.stanford.smi.protege.server.update.RemoveFrameCache;
import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.TransactionMonitor;

public class ServerFrameStore extends UnicastRemoteObject implements RemoteServerFrameStore {
    private static transient Logger log = Log.getLogger(ServerFrameStore.class);
  
    private FrameStore _delegate;
    private KnowledgeBase _kb;
    
    private FifoWriter<AbstractEvent> _eventWriter = new FifoWriter<AbstractEvent>();
    private FifoWriter<ValueUpdate> _updateWriter = new FifoWriter<ValueUpdate>();
    private List<AbstractEvent> transactionEvents = new ArrayList<AbstractEvent>();
    
    private Map<RemoteSession, Registration> _sessionToRegistrationMap 
      = new HashMap<RemoteSession, Registration>();
    private boolean _isDirty;
    private Object _kbLock;

    private Slot nameSlot;
    private Facet valuesFacet;


    private static Map<Thread,RemoteSession> sessionMap = new HashMap<Thread, RemoteSession>();
    private TransactionMonitor transactionMonitor;
    
    private FrameCalculator frameCalculator;

    private static final int DELAY_MSEC = Integer.getInteger("server.delay", 0).intValue();
    private static final int MIN_PRELOAD_FRAMES = Integer.getInteger("preload.frame.limit", 5000).intValue();
    /*
     * A performance hack Identical copies of the same sft are reduced
     * to the same object so that only a single copy needs to be sent
     * over the wire.
     */
    private Map<Sft,Sft> sftMap = new HashMap<Sft,Sft>();

    
    //ESCA-JAVA0160 
    public ServerFrameStore(FrameStore delegate, 
                            KnowledgeBase kb,
                            Object kbLock) throws RemoteException {
        _delegate = delegate;
        _kb = kb;
        _kbLock = kbLock;
        transactionMonitor = new TransactionMonitor(kbLock);
        kb.setDispatchEventsEnabled(false);
        serverMode();
        nameSlot = _kb.getSystemFrames().getNameSlot();
        valuesFacet = _kb.getSystemFrames().getValuesFacet();
        frameCalculator = new FrameCalculator(_delegate, 
                                              _kbLock, 
                                              _updateWriter, 
                                              this);
        // kb.setJournalingEnabled(true);
        if (DELAY_MSEC != 0) {
            //used for simulating slow network response time
            Log.getLogger().config("Simulated delay of " + DELAY_MSEC + " msec/call");
        }
    }

    private FrameStore getDelegate() {
        return _delegate;
    }
    
    private void serverMode() {
      for (FrameStore fs = _delegate; fs != null; fs = fs.getDelegate()) {
        if (fs instanceof EventGeneratorFrameStore) {
          ((EventGeneratorFrameStore) fs).serverMode();
        }
      }
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

    public OntologyUpdate removeDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate moveDirectTemplateSlot(Cls cls, Slot slot, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate addDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectSuperclass(cls, superclass);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate removeDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectSuperslot(slot, superslot);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate removeDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectSuperclass(cls, superclass);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate moveDirectSubclass(Cls cls, Cls subclass, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
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
        return new RemoteResponse<List>(values, getValueUpdates(session));
      }
    }

    public Set getSuperslots(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getSuperslots(slot);
      }
    }

    public RemoteResponse<Set> getInstances(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return new  RemoteResponse<Set>(
                            getDelegate().getInstances(cls),
                            getValueUpdates(session));
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
        return new RemoteResponse<List>(values, getValueUpdates(session));
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
        return new RemoteResponse(frame, getValueUpdates(session));
      }
    }

    public Frame getFrame(FrameID id, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getFrame(id);
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
        return new RemoteResponse<List>(values, getValueUpdates(session));
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

    public OntologyUpdate setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public RemoteResponse<Facet> createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaults,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Facet facet = getDelegate().createFacet(id, name, directTypes, loadDefaults);
        return new RemoteResponse<Facet>(facet, getValueUpdates(session));
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

    public OntologyUpdate setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public Set getTypes(Instance instance, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTypes(instance);
      }
    }


    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
      }
    }

    public OntologyUpdate deleteCls(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteCls(cls);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate deleteSlot(Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteSlot(slot);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate deleteFacet(Facet facet, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteFacet(facet);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate deleteSimpleInstance(SimpleInstance simpleInstance, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().deleteSimpleInstance(simpleInstance);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

  public RemoteResponse<Slot> createSlot(FrameID id, String name, Collection directTypes,
                                 Collection directSuperslots,
                                 boolean loadDefaults, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Slot slot =  getDelegate().createSlot(id, name, directTypes, directSuperslots, loadDefaults);
        return new RemoteResponse<Slot>(slot, getValueUpdates(session));
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

    public OntologyUpdate addDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectSuperslot(slot, superslot);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate addDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectTemplateSlot(cls, slot);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public RemoteResponse<Cls> createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaults, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Cls cls = getDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaults);
        return new RemoteResponse(cls, getValueUpdates(session));
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

    public OntologyUpdate removeDirectType(Instance instance, Cls directType, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectType(instance, directType);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
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
        return new RemoteResponse(si, getValueUpdates(session));
      }
    }

    public OntologyUpdate addDirectType(Instance instance, Cls type, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().addDirectType(instance, type);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public OntologyUpdate moveDirectType(Instance instance, Cls cls, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectType(instance, cls, index);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
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

    public OntologyUpdate setFrameName(Frame frame, String name, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
          getDelegate().setFrameName(frame, name);
          markDirty();
          return new OntologyUpdate(getValueUpdates(session));
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

    public OntologyUpdate removeDirectTemplateFacetOverrides(Cls cls, Slot slot, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        markDirty();
        return new OntologyUpdate(getValueUpdates(session));
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
        Registration registration = new Registration(_eventWriter, _updateWriter);
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

    public List<AbstractEvent> getEvents(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        updateEvents();
        List<AbstractEvent> events = new ArrayList<AbstractEvent>();
        Registration reg = _sessionToRegistrationMap.get(session);
        if (reg == null) {
          throw new IllegalStateException("Not registered");
        }
        FifoReader<AbstractEvent> clientEvents = reg.getEvents();
        AbstractEvent eo = null;
        while ((eo = clientEvents.read()) != null) {
          events.add(eo);
        }
        return events;
      }
    }

    public void updateEvents() {
      if (!existsTransaction() && !transactionEvents.isEmpty()) {
        for (AbstractEvent eo : transactionEvents) {
          _eventWriter.write(eo);
        }
        transactionEvents = new ArrayList<AbstractEvent>();
      }
      for (AbstractEvent eo : getDelegate().getEvents()) {
        addEvent(eo);
      }
    }

    private void addEvent(AbstractEvent eo) {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Server Processing event " + eo);
      }
      processEvent(eo);
      if (existsTransaction()) {
        transactionEvents.add(eo);
      } else {
        _eventWriter.write(eo);
      }
    }
  
    private List<ValueUpdate> getValueUpdates(RemoteSession session) {
      updateEvents();
      FifoReader<ValueUpdate> valueUpdates = _sessionToRegistrationMap.get(session).getUpdates();
      ValueUpdate vu = null;
      List<ValueUpdate> ret = new ArrayList<ValueUpdate>();
      while ((vu = valueUpdates.read()) != null) {
        Set<RemoteSession> interestedParties = vu.getClients();
        if (interestedParties == null || interestedParties.contains(session)) {
          ret.add(vu);
        }
      }
      return ret;
    }
    
    private void processEvent(AbstractEvent event)  {
      /* ---------------> Look for other relevant event types!!! and fix for nullness--- */
      if (event instanceof FrameEvent) {
        handleFrameEvent((FrameEvent) event);
      } else if (event instanceof ClsEvent) {
        handleClsEvent((ClsEvent) event);
      } if (event instanceof KnowledgeBaseEvent) {
        handleKnowledgeBaseEvent((KnowledgeBaseEvent) event);
      }
    }
  

    /*
     * Warning... calling getSlot for the wrong event type can cause a 
     *            ClassCastException.
     */
  private void handleFrameEvent(FrameEvent frameEvent) {
    Frame frame = frameEvent.getFrame();
    int type = frameEvent.getEventType();
    if (type == FrameEvent.OWN_SLOT_ADDED) {
      Slot slot = frameEvent.getSlot();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, (Facet) null, false));
    } else if (type == FrameEvent.OWN_SLOT_REMOVED) {
      Slot slot = frameEvent.getSlot();
      if (existsTransaction()) {
        _updateWriter.write(new InvalidateCacheUpdate(frame, slot, (Facet) null, false));
      } else {
        _updateWriter.write(new RemoveCacheUpdate(frame, slot, (Facet) null, false));
      }
    } else if (type == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
      Slot slot = frameEvent.getSlot();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, (Facet) null, false));
    } else if (type == FrameEvent.DELETED) {
      _updateWriter.write(new InvalidateCacheUpdate(frame, nameSlot, (Facet) null, false));
      _updateWriter.write(new RemoveFrameCache(frame));
    }
  }

  /*
   * Warning... calling getSlot/getFacet for the wrong event type can cause a 
   *            ClassCastException.
   */
  private void handleClsEvent(ClsEvent clsEvent) {
    Frame frame = clsEvent.getCls();
    int type = clsEvent.getEventType();
    if (type == ClsEvent.TEMPLATE_SLOT_ADDED) {
      Slot slot = clsEvent.getSlot();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, (Facet) null, true));
    } else if (type == ClsEvent.TEMPLATE_SLOT_REMOVED) {
      Slot slot = clsEvent.getSlot();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, (Facet) null, true));
    } else if (type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, valuesFacet, true));
    } else if (type == ClsEvent.TEMPLATE_FACET_ADDED) {
      Slot slot = clsEvent.getSlot();
      Facet facet = clsEvent.getFacet();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, facet, true));
    } else if (type == ClsEvent.TEMPLATE_FACET_REMOVED) {
      Slot slot = clsEvent.getSlot();
      Facet facet = clsEvent.getFacet();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, facet, true));
    } else if (type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      Facet facet  = clsEvent.getFacet();
      _updateWriter.write(new InvalidateCacheUpdate(frame, slot, facet, true));
    }
  }
  
  private void handleKnowledgeBaseEvent(KnowledgeBaseEvent event) {
    int type = event.getEventType();
    if (type == KnowledgeBaseEvent.CLS_DELETED || type == KnowledgeBaseEvent.SLOT_DELETED
        || type == KnowledgeBaseEvent.FACET_DELETED || type == KnowledgeBaseEvent.INSTANCE_DELETED) {
      Frame deletedFrame = event.getFrame();
      _updateWriter.write(new InvalidateCacheUpdate(deletedFrame, nameSlot, (Facet) null, false));
      _updateWriter.write(new RemoveFrameCache(deletedFrame));
    }
  }

    
    public boolean beginTransaction(String name, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        boolean success = getDelegate().beginTransaction(name);
        updateEvents();
        transactionMonitor.beginTransaction(name);
        return success;
      }
    }

    public boolean commitTransaction(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        boolean success = getDelegate().commitTransaction();
        updateEvents();
        transactionMonitor.commitTransaction();
        return success;
      }
    }

    public boolean rollbackTransaction(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        boolean success = getDelegate().rollbackTransaction();
        updateEvents();
        List<AbstractEvent> newEvents = new ArrayList<AbstractEvent>();
        for (AbstractEvent eo : transactionEvents) {
          if (!eo.getSession().equals(session)) {
            newEvents.add(eo);
          }
        }
        transactionMonitor.rollbackTransaction();
        transactionEvents = newEvents;

        return success;
      }
    }
    
    public boolean inTransaction() {
      return transactionMonitor.inTransaction();
    }
    
    public  boolean existsTransaction()  {
      return transactionMonitor.existsTransaction();
    }
    
    public void waitForTransactionsToComplete() {
      transactionMonitor.waitForTransactionsToComplete();
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

    public OntologyUpdate moveDirectSubslot(Slot slot, Slot subslot, int index, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
        return new OntologyUpdate(getValueUpdates(session));
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
      return new RemoteResponse<List>(null, getValueUpdates(session));
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
}
