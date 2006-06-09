package edu.stanford.smi.protege.server.framestore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.background.CacheRequestReason;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.server.framestore.background.WorkInfo;
import edu.stanford.smi.protege.server.update.FrameRead;
import edu.stanford.smi.protege.server.update.FrameWrite;
import edu.stanford.smi.protege.server.update.InvalidateCacheUpdate;
import edu.stanford.smi.protege.server.update.OntologyUpdate;
import edu.stanford.smi.protege.server.update.RemoteResponse;
import edu.stanford.smi.protege.server.update.RemoveCacheUpdate;
import edu.stanford.smi.protege.server.update.RemoveFrameCache;
import edu.stanford.smi.protege.server.update.SftUpdate;
import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.exceptions.TransactionException;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/*
 * Transactions:
 *
 *    One of the responsibilities of this class is to maintain the
 *    caches on the clients. Each client has two main caches.  The
 *    first cache (the main cache) holds values that will be seen by
 *    any client executing outside of a transaction.  The second cache
 *    (the transaction cache) contains values that are only seen by
 *    the one session for the duration of a transaction.  This cache
 *    breaks down as follows:
 *     - READ UNCOMMITTED or less: there is no transaction cache.
 *     - READ COMMITTED: the transaction cache contains data that has
 *       been written by the client during the transaction.  Other
 *       clients do not see these values unless this client commits.
 *     - REPEATABLE READ and higher: the transaction includes the
 *       values that have been read or written during a transaction.
 *       Other clients will not not see the values written until the
 *       client commits and will not necessarily see the same values
 *       as the the values read by the this client.
 *    The updates are handled with a pipeline of ValueUpdate objects
 *    which are transferred from the server to the client.  For the
 *    most part, these updates are sent by four main routines 
 * 
 *      cacheValuesReadFromStore
 *      updateCacheForWriteToStore
 *      invalidateCacheForWriteToStore
 *      removeFrameCache
 *
 *    In addition, these routines will store updates that need to be
 *    committed or rolled back.  These updates are stored in the
 *    registration for the session.  The rules for rollback and commit
 *    are:
 *      READ_UNCOMMITTED and below: Updates that have occurred during
 *      the tranaction need to be taken back during the rollback.
 *      READ_COMMITTED and above:  Updates that have occurred during
 *      the transaction need to be committed during the commit.
 */

public class ServerFrameStore extends UnicastRemoteObject implements RemoteServerFrameStore {
    private static transient Logger log = Log.getLogger(ServerFrameStore.class);
  
    private FrameStore _delegate;
    private KnowledgeBase _kb;
    
    private TransactionMonitor transactionMonitor;
    
    private FifoWriter<AbstractEvent> _eventWriter = new FifoWriter<AbstractEvent>();
    private FifoWriter<ValueUpdate> _updateWriter = new FifoWriter<ValueUpdate>();
    
    private Map<RemoteSession, Registration> _sessionToRegistrationMap 
      = new HashMap<RemoteSession, Registration>();
    private boolean _isDirty;
    private final Object _kbLock;

    private Slot nameSlot;
    private Facet valuesFacet;

    private static Map<Thread,RemoteSession> sessionMap = new HashMap<Thread, RemoteSession>();
    
    private FrameCalculator frameCalculator;

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
        transactionMonitor = delegate.getTransactionStatusMonitor();
        kb.setDispatchEventsEnabled(false);
        serverMode();
        nameSlot = _kb.getSystemFrames().getNameSlot();
        valuesFacet = _kb.getSystemFrames().getValuesFacet();
        frameCalculator = new FrameCalculator(_delegate, 
                                              _kbLock, 
                                              _updateWriter, 
                                              this,
                                              _sessionToRegistrationMap);
        // kb.setJournalingEnabled(true);
        if (ServerProperties.delayInMilliseconds() != 0) {
            //used for simulating slow network response time
            Log.getLogger().config("Simulated delay of " + ServerProperties.delayInMilliseconds() + " msec/call");
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
        if (ServerProperties.delayInMilliseconds() != 0) {
            SystemUtilities.sleepMsec(ServerProperties.delayInMilliseconds());
            if (++nDelayedCalls % 10 == 0) {
                Log.getLogger().info(nDelayedCalls + " delayed calls");
            }
        }
    }

    public static void recordCall(RemoteSession session) {
        delay();
        setCurrentSession(session);
    }
    
    public static void setCurrentSession(RemoteSession session) {
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
      frameCalculator.addRequest(cls, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
      synchronized(_kbLock) {
        List values = getDelegate().getDirectTemplateSlotValues(cls, slot);
        cacheValuesReadFromStore(session, cls, slot, (Facet) null, true, values);
        return new RemoteResponse<List>(values, getValueUpdates(session));
      }
    }

    public RemoteResponse<Set> getInstances(Cls cls, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        Set instances = getDelegate().getInstances(cls);
        return new  RemoteResponse<Set>(
                            instances,
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


    public RemoteResponse<List> getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) {
      recordCall(session);
      LocalizeUtils.localize(cls, _kb);
      frameCalculator.addRequest(cls, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
      synchronized(_kbLock) {
        List values = getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
        cacheValuesReadFromStore(session, cls, slot, facet, true, values);
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
          frameCalculator.addRequest(frame, session, CacheRequestReason.USER_NAME_REQUEST);
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
      LocalizeUtils.localize(frame, _kb);
      if (!slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES)) {
        frameCalculator.addRequest(frame, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
      }
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
        if (!slot.equals(Model.SlotID.DIRECT_INSTANCES)) {
          frameCalculator.addRequest(frame, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
        }
        if (slot.getFrameID().equals(Model.SlotID.DIRECT_SUBCLASSES)) {
          for (Object o : values) {
            if (o instanceof Frame) {
              frameCalculator.addRequest((Frame) o, session, CacheRequestReason.SUBCLASS);
            }
          }
        }              
        cacheValuesReadFromStore(session, frame, slot, (Facet) null, false, values);
        return new RemoteResponse<List>(values, getValueUpdates(session));
      }
    }

    public OntologyUpdate setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        if (!(values instanceof List)) {
          values = new ArrayList(values);
        }
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        markDirty();
        updateCacheForWriteToStore(session, cls, slot, facet, true, (List) values);
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public RemoteResponse<Facet> createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaults,
            RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Facet facet = getDelegate().createFacet(id, name, directTypes, loadDefaults);
        frameCalculator.addRequest(facet, session,  CacheRequestReason.NEW_FRAME);
        return new RemoteResponse<Facet>(facet, getValueUpdates(session));
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
      if (!(values instanceof  List)) {
        values = new ArrayList(values);
      }
      synchronized(_kbLock) {
        markDirty();
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        updateCacheForWriteToStore(session, cls, slot, null, true, (List) values);
        return new OntologyUpdate(getValueUpdates(session));
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
        frameCalculator.addRequest(slot,  session, CacheRequestReason.NEW_FRAME);
        return new RemoteResponse<Slot>(slot, getValueUpdates(session));
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
      if (!(values instanceof List)) {
        values = new ArrayList(values);
      }
      synchronized(_kbLock) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        markDirty();
        updateCacheForWriteToStore(session, frame, slot, null, false, (List) values);
        return new OntologyUpdate(getValueUpdates(session));
      }
    }

    public RemoteResponse<Cls> createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaults, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        markDirty();
        Cls cls = getDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaults);
        frameCalculator.addRequest(cls,  session, CacheRequestReason.NEW_FRAME);
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
        frameCalculator.addRequest(si,  session, CacheRequestReason.NEW_FRAME);
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

    public RemoteResponse<List<AbstractEvent>> getEvents(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        updateEvents(session);
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
        return new RemoteResponse<List<AbstractEvent>>(events, getValueUpdates(session));
      }
    }

    public void updateEvents(RemoteSession session) {
      Registration registration = _sessionToRegistrationMap.get(session);
      TransactionIsolationLevel level = getTransactionIsolationLevel();
      for (AbstractEvent eo : getDelegate().getEvents()) {
        addEvent(session, registration, level, eo);
      }
    }

    private void addEvent(RemoteSession session, 
                          Registration registration,
                          TransactionIsolationLevel level, 
                          AbstractEvent eo) {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Server Processing event " + eo);
      }
      processEvent(session, registration, level, eo);
      if (updatesSeenByUntransactedClients(level)) {
        _eventWriter.write(eo);
      } else {
        registration.addTransactionEvent(eo);
      }
    }
  
    private List<ValueUpdate> getValueUpdates(RemoteSession session) {
      updateEvents(session);
      FifoReader<ValueUpdate> valueUpdates = _sessionToRegistrationMap.get(session).getUpdates();
      ValueUpdate vu = null;
      List<ValueUpdate> ret = new ArrayList<ValueUpdate>();
      while ((vu = valueUpdates.read()) != null) {
        RemoteSession targettedClient = vu.getClient();
        if (targettedClient == null || targettedClient.equals(session)) {
          ret.add(vu);
        }
      }
      return ret;
    }
    
    private void processEvent(RemoteSession session, 
                              Registration registration, 
                              TransactionIsolationLevel level, 
                              AbstractEvent event)  {
      /* ---------------> Look for other relevant event types!!! and fix for nullness--- */
      if (event instanceof FrameEvent) {
        handleFrameEvent(session, registration, level, (FrameEvent) event);
      } else if (event instanceof ClsEvent) {
        handleClsEvent(session, registration, level, (ClsEvent) event);
      } if (event instanceof KnowledgeBaseEvent) {
        handleKnowledgeBaseEvent(session, registration, level, (KnowledgeBaseEvent) event);
      }
    }
  

    /*
     * Warning... calling getSlot for the wrong event type can cause a 
     *            ClassCastException.
     */
  private void handleFrameEvent(RemoteSession session,
                                Registration registration, 
                                TransactionIsolationLevel level,
                                FrameEvent frameEvent) {
    Frame frame = frameEvent.getFrame();
    int type = frameEvent.getEventType();

    if (type == FrameEvent.OWN_SLOT_ADDED) {
      Slot slot = frameEvent.getSlot();
      invalidateCacheForWriteToStore(frame, slot, null, false);
    } else if (type == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
      Slot slot = frameEvent.getSlot();
      invalidateCacheForWriteToStore(frame, slot, null, false);
    } else if (type == FrameEvent.OWN_SLOT_REMOVED) {
      Slot slot = frameEvent.getSlot();
      updateCacheForWriteToStore(session, frame, slot, (Facet) null, false, null);
    } else if (type == FrameEvent.DELETED) {
      removeFrameCache(frame);
    }
  }

  /*
   * Warning... calling getSlot/getFacet for the wrong event type can cause a 
   *            ClassCastException.
   */
  private void handleClsEvent(RemoteSession session, 
                              Registration registration,
                              TransactionIsolationLevel level,
                              ClsEvent clsEvent) {
    Frame frame = clsEvent.getCls();
    int type = clsEvent.getEventType();
    if (type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      invalidateCacheForWriteToStore(frame, slot, valuesFacet, true);
    } else if (type == ClsEvent.TEMPLATE_FACET_ADDED) {
      Slot slot = clsEvent.getSlot();
      Facet facet = clsEvent.getFacet();
      invalidateCacheForWriteToStore(frame, slot, facet, true);
    } else if (type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      Facet facet  = clsEvent.getFacet();
      invalidateCacheForWriteToStore(frame, slot, facet, true);
    }
  }
  
  private void handleKnowledgeBaseEvent(RemoteSession session, 
                                        Registration registration,
                                        TransactionIsolationLevel level,
                                        KnowledgeBaseEvent event) {
    int type = event.getEventType();
    if (type == KnowledgeBaseEvent.CLS_DELETED || type == KnowledgeBaseEvent.SLOT_DELETED
        || type == KnowledgeBaseEvent.FACET_DELETED || type == KnowledgeBaseEvent.INSTANCE_DELETED) {
      Frame deletedFrame = event.getFrame();
      removeFrameCache(deletedFrame);
    }
  }

    /*
     * These begin/rollback/commit transaction calls have to take care of four things:
     *    Updating Events
     *       -> I need to remember where I am in the transaction events queue so that I 
     *          can roll these events back later
     *       -> before I can do this I need to flush the events
     *    Calling the delegate
     *    Updating info about the transaction nesting (using the transaction monitor)
     *    Updating value updates
     *       -> Nothing to do
     */
    public RemoteResponse<Boolean> beginTransaction(String name, RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        updateEvents(session);
        boolean success = getDelegate().beginTransaction(name);
        return new RemoteResponse<Boolean>(success, getValueUpdates(session));
      }
    }
    
    /*
     * These begin/rollback/commit transaction calls have to take care of four things:
     *    Updating Events
     *       -> after the commit, flush the events and write them out if possible
     *    Calling the delegate
     *    Updating info about the transaction nesting (using the transaction monitor)
     *    Updating value updates
     *       -> Throw away the value update rollbacks.
     *       
     *  Flushing the event queue must happen before we update the transaction nesting so
     *  that the events end up on the transactio queue.  These events get moved to the clients
     *  later in the closeTransactionEvents() call.
     */
    public RemoteResponse<Boolean> commitTransaction(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        boolean success = getDelegate().commitTransaction();
        updateEvents(session);
        if (!inTransaction()) {
          Registration registration = _sessionToRegistrationMap.get(session);
          TransactionIsolationLevel level = getTransactionIsolationLevel();
          Collection<ValueUpdate> updates;
          if (success && level != null && 
              level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
            for (AbstractEvent eo : registration.getTransactionEvents()) {
              addEvent(session, registration, level, eo);
            }
            for (ValueUpdate vu : registration.getCommits()) {
              _updateWriter.write(vu);
            }
          }
          registration.endTransaction();
        }
        if (!existsTransaction()) {
          _kbLock.notifyAll();
        }
        return new RemoteResponse<Boolean>(success, getValueUpdates(session));
      }
    }

    /*
     * These begin/rollback/commit transaction calls have to take care of four things:
     *    Updating Events
     *       -> flush the events
     *       -> find the location in the transaction queue since the last transaction and delete the 
     *          events that belong to  this session.
     *       -> write the events out to the clients if possible
     *    Calling the delegate
     *    Updating info about the transaction nesting (using the transaction monitor)
     *    Updating value updates
     *       -> send the value update rollbacks (for this session) to the clients
     *       -> reset the value update rollbacks (for this session)
     *
     *  Flushing the event queue must happen before we update the transaction nesting so
     *  that the events end up on the transactio queue. These events get moved to the clients
     *  later in the closeTransactionEvents() call.
     */
    public RemoteResponse<Boolean> rollbackTransaction(RemoteSession session) {
      recordCall(session);
      synchronized(_kbLock) {
        updateEvents(session);
        boolean success = getDelegate().rollbackTransaction();
        if (!inTransaction()) {
          Registration registration = _sessionToRegistrationMap.get(session);
          TransactionIsolationLevel level = getTransactionIsolationLevel();
          if (success && (level != null ||
                          level.compareTo(TransactionIsolationLevel.READ_COMMITTED) < 0)) {
            for (AbstractEvent eo : registration.getTransactionEvents()) {
              addEvent(session, registration, level, eo);
            }
            for (ValueUpdate vu : registration.getCommits()) {
              ValueUpdate invalid = vu.getInvalidatingVariant();
              if (invalid != null) {
                _updateWriter.write(vu);
              }
            }
          }
          registration.endTransaction();
        }
        if (!existsTransaction()) {
          _kbLock.notifyAll();
        }
        return new RemoteResponse<Boolean>(success, getValueUpdates(session));
      }
    }

    public void cacheValuesReadFromStore(RemoteSession session, 
                                         Frame frame, 
                                         Slot slot, 
                                         Facet facet, 
                                         boolean isTemplate, 
                                         List values) {
      TransactionIsolationLevel level  = getTransactionIsolationLevel();
      if (level == null) {
        return;  // no caching can be done
      }
      SftUpdate vu = new FrameRead(frame, slot, facet, isTemplate, values);
      if (inTransaction() && level.compareTo(TransactionIsolationLevel.REPEATABLE_READ) >= 0) {
        vu.setClient(session);
        vu.setTransactionScope(true);
      }
      _updateWriter.write(vu);
    }

    private void updateCacheForWriteToStore(RemoteSession session,
                                            Frame frame,
                                            Slot slot, 
                                            Facet facet,
                                            boolean isTemplate,
                                            List values) {
      TransactionIsolationLevel level = getTransactionIsolationLevel();
      Registration registration = _sessionToRegistrationMap.get(session);
      if (level  == null) {
        InvalidateCacheUpdate invalid = new InvalidateCacheUpdate(frame, slot, facet, isTemplate);
        _updateWriter.write(invalid);
        registration.addCommittableUpdate(invalid);
        invalid = new InvalidateCacheUpdate(frame, slot, facet, isTemplate);
        invalid.setTransactionScope(true);
        _updateWriter.write(invalid);
        return;
      }
      SftUpdate vu = new FrameWrite(frame, slot, facet, isTemplate, values);
      if (!updatesSeenByUntransactedClients(level)) {
        vu.setClient(session);
        vu.setTransactionScope(true);
      }
      _updateWriter.write(vu);
      if (!updatesSeenByUntransactedClients(level)) {
        registration.addCommittableUpdate(new FrameWrite(frame, slot, facet, isTemplate, values));
      }
    }

    private void invalidateCacheForWriteToStore(Frame frame,
                                                Slot slot, 
                                                Facet facet,
                                                boolean isTemplate) {
      RemoteSession session = getCurrentSession();
      TransactionIsolationLevel level = getTransactionIsolationLevel();
      Registration registration = _sessionToRegistrationMap.get(session);
      if (level == null) {
        _updateWriter.write(new InvalidateCacheUpdate(frame, slot, facet, isTemplate));
        if (inTransaction()) {
          registration.addCommittableUpdate(new InvalidateCacheUpdate(frame, slot, facet, isTemplate));
        }
        InvalidateCacheUpdate invalid = new InvalidateCacheUpdate(frame, slot, facet, isTemplate);
        invalid.setTransactionScope(true);
        _updateWriter.write(invalid);
        return;
      }
      SftUpdate vu = new InvalidateCacheUpdate(frame, slot, facet, isTemplate);
      if (!updatesSeenByUntransactedClients(level)) {
        vu.setClient(session);
        vu.setTransactionScope(true);
      }
      _updateWriter.write(vu);
      if (!updatesSeenByUntransactedClients(level)) {
        registration.addCommittableUpdate(new InvalidateCacheUpdate(frame,slot, facet, isTemplate));
      }
    }

    public void removeFrameCache(Frame frame) {
      TransactionIsolationLevel level = getTransactionIsolationLevel();
      RemoteSession session = getCurrentSession();
      Registration registration = _sessionToRegistrationMap.get(session);
      if (level == null) {
        _updateWriter.write(new RemoveFrameCache(frame));
        if (inTransaction()) {
          registration.addCommittableUpdate(new RemoveFrameCache(frame));
        }
        RemoveFrameCache remove  = new RemoveFrameCache(frame);
        remove.setTransactionScope(true);
        _updateWriter.write(remove);
      }
      RemoveFrameCache remove = new RemoveFrameCache(frame);
      if (!updatesSeenByUntransactedClients(level)) {
        remove.setClient(session);
        remove.setTransactionScope(true);
      }
      _updateWriter.write(remove);
      if (!updatesSeenByUntransactedClients(level)) {
        registration.addCommittableUpdate(new RemoveFrameCache(frame));
      }
    }

    public boolean updatesSeenByUntransactedClients(TransactionIsolationLevel level) {
      return !inTransaction() || 
        (level != null && level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0);
    }
    
  /**
   * Calculates the transaction isolation level.  If it returns null
   * it indicates that an error has occured.
   */
    public TransactionIsolationLevel getTransactionIsolationLevel() {
      try {
        synchronized (_kbLock) {
          if (transactionMonitor == null) {
            return TransactionIsolationLevel.NONE;
          }
          return transactionMonitor.getTransationIsolationLevel();
        }
      } catch (TransactionException te) {
        Log.getLogger().log(Level.WARNING,  "Exception caught finding transaction isolation level", te);
        return null;
      }
    }
    
    public boolean setTransactionIsolationLevel(TransactionIsolationLevel level) throws TransactionException {
      synchronized (_kbLock) {
        if (transactionMonitor == null) {
          return false;
        }
        transactionMonitor.setTransactionIsolationLevel(level);
      }
      return true;
    } 
    
    public boolean inTransaction() {
      return transactionMonitor != null && transactionMonitor.inTransaction();
    }
    
    public  boolean existsTransaction()  {
      return transactionMonitor != null && transactionMonitor.existsTransaction();
    }
    
    public boolean exclusiveTransaction() {
      return transactionMonitor != null && transactionMonitor.exclusiveTransaction();
    }
    
    public TransactionMonitor getTransactionStatusMonitor() {
      return transactionMonitor;
    }
    
    public void waitForTransactionsToComplete() {
      synchronized (_kbLock) {
        while (existsTransaction()) {
          try {
            _kbLock.wait();
          } catch (InterruptedException e) {
            log.log(Level.WARNING, "Interrupted waiting for transactions to complete", e);
          }
        }
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

    public RemoteResponse<Set> getDirectOwnSlotValuesClosure(Frame frame, 
                                                             Slot slot, 
                                                             Set<Frame> missing, 
                                                             RemoteSession session) {
      recordCall(session);
      Set values = null;
      synchronized(_kbLock) {
        values = getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
      }
      LocalizeUtils.localize(missing, _kb);
      for (Frame value : missing) {
        frameCalculator.addRequest((Frame) value, session, CacheRequestReason.USER_CLOSURE_REQUEST);
      }
      return new RemoteResponse<Set>(values, getValueUpdates(session));
    }
    
    public RemoteResponse<Set> getDirectOwnSlotValuesClosure(Collection<Frame> frames, 
                                                             Slot slot, 
                                                             Set<Frame> missing, 
                                                             RemoteSession session) {
      recordCall(session);
      Set values = new HashSet();
      synchronized(_kbLock) {
        for (Frame frame : frames) {
          Set newValues = getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
          if (newValues != null)  {
            values.addAll(newValues);
          }
        }
      }
      LocalizeUtils.localize(missing,  _kb);
      for (Frame value : missing) {
        frameCalculator.addRequest((Frame) value, session, CacheRequestReason.USER_CLOSURE_REQUEST);
      }
      return new RemoteResponse<Set>(values, getValueUpdates(session));
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

    public OntologyUpdate preload(Set<String> userFrames, boolean all, RemoteSession session) {
      recordCall(session);
      Set<Frame> frames;
      int frameCount = 0;
      synchronized (_kbLock) {
        frameCount = getDelegate().getFrameCount();
      }
      if (all ||  frameCount < ServerProperties.minimumPreloadedFrames()) {
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
      }
      for (Frame frame : frames) {
        frameCalculator.addRequest(frame, session, CacheRequestReason.PRELOAD);
      }
      frames = new HashSet<Frame>();
      for (String frameName : userFrames) {
        Frame frame = null;
        synchronized(_kbLock) {
          frame = _delegate.getFrame(frameName);
        }
        if (frame == null) {
          continue;
        }
        frames.add(frame);
        if (frame instanceof Cls) {
          frames.addAll(_kb.getSuperclasses((Cls) frame));
          frames.addAll(_kb.getDirectSubclasses((Cls) frame));
        }
      }
      for (Frame frame : frames) {
        frameCalculator.addRequest(frame, session, CacheRequestReason.IMMEDIATE_PRELOAD);
      }
      return new OntologyUpdate(getValueUpdates(session));
    }
    
    private void addSystemClasses(Set<Frame> frames, Cls cls)  {
      if (!cls.getFrameID().isSystem() || frames.contains(cls)) {
        return;
      }
      List<Cls> subClasses = null;
      synchronized (_kbLock) {
        subClasses = _delegate.getDirectSubclasses(cls);
      }
      Set<Slot> slots = null;
      synchronized (_kbLock) {
        slots = _delegate.getOwnSlots(cls);
        slots.addAll(_delegate.getTemplateSlots(cls));
      }
      for (Slot slot : slots) {
        if (slot.isSystem()) {
          frames.add(slot);
        }
      }
      for (Cls subclass : subClasses) {
        addSystemClasses(frames, subclass);
      }
    }
    
    public void requestValueCache(Set<Frame> frames, boolean skipDirectInstances, RemoteSession session) {
      synchronized (frameCalculator.getRequestLock()) {
        for  (Frame frame : frames) {
          LocalizeUtils.localize(frame, _kb);
          WorkInfo wi = frameCalculator.addRequest(frame, session, CacheRequestReason.USER_SPECIFIC_FRAMES);
          if (wi != null) {
            wi.setSkipDirectInstances(skipDirectInstances);
          }
        }
      }
    }
}
