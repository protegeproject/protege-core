package edu.stanford.smi.protege.server.framestore;

import java.lang.reflect.Proxy;
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
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.EventDispatchFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.SynchronizeQueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.background.CacheRequestReason;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;
import edu.stanford.smi.protege.server.framestore.background.WorkInfo;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.server.socket.RmiSocketFactory;
import edu.stanford.smi.protege.server.socket.SSLFactory;
import edu.stanford.smi.protege.server.update.OntologyUpdate;
import edu.stanford.smi.protege.server.update.RemoteResponse;
import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.impl.CompleteableCache;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheBeginTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheCommitTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheDelete;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheModify;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRead;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRollbackTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

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
    private static final long serialVersionUID = 2965578539383364549L;

    private static transient Logger log = Log.getLogger(ServerFrameStore.class);

    /*
     * Arrange that this Logger is activated when a developer activates the other cache logs.
     */
    public static final transient Logger cacheLog = Logger.getLogger(CompleteableCache.class.getPackage().getName() + ".ClientServer");

    private final FrameStore _delegate;
    private final KnowledgeBase _kb;

    private final TransactionMonitor transactionMonitor;

    private ProjectInstance projectInstance;

    private final FifoWriter<AbstractEvent> _eventWriter = new FifoWriter<AbstractEvent>();
    {
      _eventWriter.setLogger(cacheLog, "New Event");
    }

    private final Map<RemoteSession, Registration> _sessionToRegistrationMap
      = new HashMap<RemoteSession, Registration>();
    private boolean _isDirty;
    private final Object _kbLock;

    private final Facet valuesFacet;

    private static Map<Thread,RemoteSession> sessionMap = new HashMap<Thread, RemoteSession>();

    private final FrameCalculator frameCalculator;

    private static Set<KnowledgeBase> requiresEventDispatch = new HashSet<KnowledgeBase>();
    
    private final Set<Thread> runningClientThreads = new HashSet<Thread>();


    //ESCA-JAVA0160
    public ServerFrameStore(KnowledgeBase kb) throws RemoteException {
        super(SSLFactory.getServerPort(SSLFactory.Context.ALWAYS),
              new RmiSocketFactory(SSLFactory.Context.ALWAYS),
              new RmiSocketFactory(SSLFactory.Context.ALWAYS));
        _kb = kb;
        _kbLock = kb;

        FrameStoreManager fsm = ((DefaultKnowledgeBase) kb).getFrameStoreManager();

        if (!requiresEventDispatch.contains(kb)) {
            kb.setDispatchEventsEnabled(false);
        }
        else {
            kb.getFrameStoreManager().setDispatchEventsPassThrough(true);
            requiresEventDispatch.remove(kb);
        }
        valuesFacet = _kb.getSystemFrames().getValuesFacet();
        frameCalculator = new FrameCalculator(fsm.getHeadFrameStore(),
                                              ((DefaultKnowledgeBase) _kb).getCacheMachine(),
                                              _kbLock,
                                              this,
                                              _sessionToRegistrationMap);
        
        fsm.insertFrameStore(new FrameCalculatorFrameStore(frameCalculator), 1); // after  the localization frame  store.
        
        _delegate = fsm.getHeadFrameStore();
        transactionMonitor = _delegate.getTransactionStatusMonitor();
        if (ServerProperties.delayInMilliseconds() != 0) {
            Log.getLogger().config("Simulated delay of " + ServerProperties.delayInMilliseconds() + " msec/call");
        }
        startHeartbeatThread(_kb.toString());
    }

    public static void requestEventDispatch(KnowledgeBase kb) {
        kb.setDispatchEventsEnabled(true);
        kb.getFrameStoreManager().setDispatchEventsPassThrough(true);
        requiresEventDispatch.add(kb);
    }

    // disabled for now.
    private void startHeartbeatThread(String name) {
      if (!ServerProperties.heartbeatDisabled()) {
        new Thread("Heartbeat checker [" + name + "]") {
          @Override
        public void run() {
            try {
              while (true) {
                Thread.sleep(RemoteServerFrameStore.HEARTBEAT_POLL_INTERVAL);
                long now = System.currentTimeMillis();
                synchronized (_kbLock) {
                  for (Map.Entry<RemoteSession, Registration> entry
                      : _sessionToRegistrationMap.entrySet()) {
                    Registration registration = entry.getValue();
                    long lastHeartbeat = registration.getLastHeartbeat();
                    if (lastHeartbeat != 0 &&  // don't kill the client before the first heartbeat
                        lastHeartbeat <= now - RemoteServerFrameStore.HEARTBEAT_CLIENT_DIED) {
                      RemoteSession session = entry.getKey();
                      Log.getLogger().info("Session disconnected because of timeout");
                      deregister(session);
                    }
                  }
                }
              }
            } catch (Exception e) {
              Log.getLogger().log(Level.WARNING, "Heartbeat thread died", e);
            }
          }
        }.start();
      }
    }

    public Map<RemoteSession, Boolean> getUserInfo() {
      Map<RemoteSession, Boolean> results = new HashMap<RemoteSession, Boolean>();
      for (RemoteSession session : _sessionToRegistrationMap.keySet()) {
        if (transactionMonitor != null) {
          results.put(session, transactionMonitor.getNesting(session) > 0);
        }
        else {
            results.put(session, Boolean.FALSE);
        }
      }
      return results;
    }

    public FrameCalculatorStats getStats() {
        return frameCalculator.getStats();
    }

    private FrameStore getDelegate() {
        return _delegate;
    }

    public void recordCall(RemoteSession session) throws ServerSessionLost {
      synchronized (sessionMap) {
        if (!_sessionToRegistrationMap.containsKey(session)) {
          throw new ServerSessionLost("Dropped connection due to timeout");
        }
      }
      recordCallNoCheck(session);
      synchronized (runningClientThreads) {
    	  runningClientThreads.add(Thread.currentThread());
      }
    }

    public static void recordCallNoCheck(RemoteSession session) {
        setCurrentSession(session);
    }
    
    public void unrecordCall() {
    	synchronized (runningClientThreads) {
    		runningClientThreads.remove(Thread.currentThread());
    		if (runningClientThreads.isEmpty()) {
    			runningClientThreads.notifyAll();
    		}
    	}
    }
    
    public void letOtherThreadsRun() {
    	synchronized (runningClientThreads) {
    		while (!runningClientThreads.isEmpty()) {
    			try {
    				if (log.isLoggable(Level.FINE)) {
    					log.fine("waitig on threads " + runningClientThreads);
    				}
					runningClientThreads.wait();
				} catch (InterruptedException e) {
					log.log(Level.WARNING, "Unexpected Interrupt - please don't press that red button again", e);
				}
    		}
    	}
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

    public int getClsCount(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsCount();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public int getSlotCount(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getSlotCount();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public int getFacetCount(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFacetCount();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public int getSimpleInstanceCount(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getSimpleInstanceCount();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public int getFrameCount(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFrameCount();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate removeDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().removeDirectTemplateSlot(cls, slot);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate moveDirectTemplateSlot(Cls cls, Slot slot, int index, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().moveDirectTemplateSlot(cls, slot, index);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate addDirectSuperclass(Cls cls, Cls superclass, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().addDirectSuperclass(cls, superclass);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate removeDirectSuperslot(Slot slot, Slot superslot, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().removeDirectSuperslot(slot, superslot);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate removeDirectSuperclass(Cls cls, Cls superclass, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().removeDirectSuperclass(cls, superclass);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate moveDirectSubclass(Cls cls, Cls subclass, int index, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().moveDirectSubclass(cls, subclass, index);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate replaceFrame(Frame original, Frame replacement, RemoteSession session)
    throws ServerSessionLost {
        recordCall(session);
        try {
            synchronized(_kbLock) {
                Collection<Reference> references = getDelegate().getReferences(original);
                getDelegate().replaceFrame(original, replacement);
                markDirty();
                wipeReferencingFrames(original, references, session);
                return new OntologyUpdate(getValueUpdates(session));
            }
        }
        finally {
            unrecordCall();
        }
    }

    @SuppressWarnings("unchecked")
	public RemoteResponse<List> getDirectTemplateSlotValues(Cls cls,
                                                            Slot slot,
                                                            RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          LocalizeUtils.localize(cls, _kb);
          synchronized(_kbLock) {
              List values = getDelegate().getDirectTemplateSlotValues(cls, slot);
              Sft sft = new Sft(slot, null, true);
              CacheResult<List> cacheValues = new CacheResult<List>(values, true);
              addReadUpdate(session, cls, new CacheRead<RemoteSession, Sft, List>(session, sft, cacheValues));
              return new RemoteResponse<List>(values, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Set<Instance>> getInstances(Cls cls, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              Set<Instance> instances = getDelegate().getInstances(cls);
              return new  RemoteResponse<Set<Instance>>(
                                                        instances,
                                                        getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Slot> getOwnSlots(Frame frame, RemoteSession session)
    throws ServerSessionLost {
        recordCall(session);
        try {
            synchronized(_kbLock) {
                return getDelegate().getOwnSlots(frame);
            }
        }
        finally {
            unrecordCall();
        }
    }



    public synchronized Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value, RemoteSession session)
    throws ServerSessionLost {
        recordCall(session);
        try {
            synchronized(_kbLock) {
                return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
            }
        }
        finally {
            unrecordCall();
        }
    }


    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot,
                                                    Facet facet,
                                                    Object value,
                                                    RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot,
                                                              String value,
                                                              int maxMatches,
                                                              RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot,
                                                            Facet facet,
                                                            String value,
                                                            int maxMatches,
                                                            RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot,
                                                           String value,
                                                           int maxMatches,
                                                           RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
          }
      }
      finally {
          unrecordCall();
      }
    }


    @SuppressWarnings("unchecked")
	public RemoteResponse<List> getDirectTemplateFacetValues(Cls cls,
                                                             Slot slot,
                                                             Facet facet,
                                                             RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          LocalizeUtils.localize(cls, _kb);
          synchronized(_kbLock) {
              List values = getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
              Sft sft = new Sft(slot, facet, true);
              CacheResult<List> cacheValues = new CacheResult<List>(values, true);
              addReadUpdate(session, cls, new CacheRead<RemoteSession, Sft, List>(session, sft, cacheValues));
              return new RemoteResponse<List>(values, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Cls> getClses(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClses();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getTemplateFacets(cls, slot);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Frame> getFrame(String name, RemoteSession session) throws  ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              Frame frame = getDelegate().getFrame(name);
              return new RemoteResponse<Frame>(frame, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Frame getFrame(FrameID id, RemoteSession session) throws  ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFrame(id);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot, RemoteSession session) throws ServerSessionLost  {
      recordCall(session);
      try {
          LocalizeUtils.localize(frame, _kb);
          synchronized(_kbLock) {
              return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
          }
      }
      finally {
          unrecordCall();
      }
    }

    @SuppressWarnings("unchecked")
	public RemoteResponse<List> getDirectOwnSlotValues(Frame frame, Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          if (log.isLoggable(Level.FINE)) {
              log.fine("getDirectOwnSlotValues for frame " + frame.getFrameID() + " slot " + slot.getFrameID());
          }
          LocalizeUtils.localize(frame, _kb);
          LocalizeUtils.localize(slot, _kb);
          synchronized(_kbLock) {
              List values = getDelegate().getDirectOwnSlotValues(frame, slot);
              Sft sft = new Sft(slot, null, false);
              CacheResult<List> cacheValues = new CacheResult<List>(values, true);
              addReadUpdate(session, frame, new CacheRead<RemoteSession, Sft, List>(session, sft, cacheValues));
              return new RemoteResponse<List>(values, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    @SuppressWarnings("unchecked")
	public OntologyUpdate setDirectTemplateFacetValues(Cls cls,
                                                       Slot slot,
                                                       Facet facet,
                                                       Collection values,
                                                       RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              if (!(values instanceof List)) {
                  values = new ArrayList(values);
              }
              getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
              markDirty();
              Sft sft = new Sft(slot, facet, true);
              CacheResult<List> cacheValues = new CacheResult<List>((List) values, true);
              addWriteUpdate(session, cls, new CacheModify<RemoteSession, Sft, List>(session, sft, cacheValues));
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Facet> createFacet(FrameID id,
                                             Collection directTypes,
                                             boolean loadDefaults,
                                             RemoteSession session) throws ServerSessionLost {
        recordCall(session);
        try {
            synchronized(_kbLock) {
                markDirty();
                Facet facet = getDelegate().createFacet(id, directTypes, loadDefaults);
                return new RemoteResponse<Facet>(facet, getValueUpdates(session));
            }
        }
        finally {
            unrecordCall();
        }
    }


    public Set<Frame> getFrames(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFrames();
          }
      }
      finally {
          unrecordCall();
      }
    }

    @SuppressWarnings("unchecked")
	public OntologyUpdate setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, RemoteSession session)
    throws ServerSessionLost {
      recordCall(session);
      try {
          if (!(values instanceof  List)) {
              values = new ArrayList(values);
          }
          synchronized(_kbLock) {
              markDirty();
              getDelegate().setDirectTemplateSlotValues(cls, slot, values);
              Sft sft = new Sft(slot, null, true);
              CacheResult<List> cacheValues = new CacheResult<List>((List) values, true);
              addWriteUpdate(session, cls, new CacheModify<RemoteSession, Sft, List>(session, sft, cacheValues));
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }


    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getTemplateFacetValues(cls, slot, facet);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate deleteCls(Cls cls, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().deleteCls(cls);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate deleteSlot(Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().deleteSlot(slot);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate deleteFacet(Facet facet, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().deleteFacet(facet);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate deleteSimpleInstance(SimpleInstance simpleInstance, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().deleteSimpleInstance(simpleInstance);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

  public RemoteResponse<Slot> createSlot(FrameID id, Collection directTypes,
                                 Collection directSuperslots,
                                 boolean loadDefaults, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              markDirty();
              Slot slot =  getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaults);
              return new RemoteResponse<Slot>(slot, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
  }


    public OntologyUpdate addDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().addDirectSuperslot(slot, superslot);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate addDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().addDirectTemplateSlot(cls, slot);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to, RemoteSession session)
      throws ServerSessionLost{
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    @SuppressWarnings("unchecked")
	public OntologyUpdate setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, RemoteSession session)
      throws ServerSessionLost {
      recordCall(session);
      try {
          if (!(values instanceof List)) {
              values = new ArrayList(values);
          }
          synchronized(_kbLock) {
              getDelegate().setDirectOwnSlotValues(frame, slot, values);
              markDirty();
              Sft sft = new Sft(slot, null, true);
              CacheResult<List> cacheValues = new CacheResult<List>((List) values, true);
              addReadUpdate(session, frame, new CacheModify<RemoteSession, Sft, List>(session, sft, cacheValues));
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Cls> createCls(FrameID id,
                                         Collection directTypes,
                                         Collection directSuperclasses,
                                         boolean loadDefaults,
                                         RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              markDirty();
              Cls cls = getDelegate().createCls(id,directTypes, directSuperclasses, loadDefaults);
              return new RemoteResponse(cls, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Facet> getFacets(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFacets();
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Collection<Frame>> executeQuery(Query query,
                                                   RemoteSession session)
                                                   throws ProtegeException, ServerSessionLost {
      recordCall(session);
      try {
          SynchronizeQueryCallback callback = new SynchronizeQueryCallback(_kbLock);
          synchronized(_kbLock) {
              getDelegate().executeQuery(query,callback);
          }
          return new RemoteResponse<Collection<Frame>>(callback.waitForResults(), getValueUpdates(session));
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate removeDirectType(Instance instance, Cls directType, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().removeDirectType(instance, directType);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Reference> getReferences(Object value, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getReferences(value);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getMatchingReferences(value, maxMatches);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Cls> getClsesWithMatchingBrowserText(String value,
                                                    Collection superclasses,
                                                    int maxMatches,
                                                    RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsesWithMatchingBrowserText(value, superclasses, maxMatches);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<SimpleInstance> createSimpleInstance(FrameID id,
                                                               Collection directTypes,
                                                               boolean loadDefaults,
                                                               RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              recordCall(session);
              markDirty();
              SimpleInstance si = getDelegate().createSimpleInstance(id, directTypes, loadDefaults);
              return new RemoteResponse<SimpleInstance>(si, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate addDirectType(Instance instance, Cls type, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().addDirectType(instance, type);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate moveDirectType(Instance instance, Cls cls, int index, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().moveDirectType(instance, cls, index);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public String getFrameName(Frame frame, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFrameName(frame);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getOverriddenTemplateSlots(Cls cls, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getOverriddenTemplateSlots(cls);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getOverriddenTemplateFacets(cls, slot);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getOverriddenTemplateFacets(cls, slot);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public OntologyUpdate removeDirectTemplateFacetOverrides(Cls cls, Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
              markDirty();
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public void close(RemoteSession session) throws ServerSessionLost {
      recordCallNoCheck(session);
    }

    public Object executeProtegeJob(ProtegeJob job,
                                                    RemoteSession session)
                                                    throws ProtegeException, ServerSessionLost {
      recordCall(session);
      try {
        synchronized(_kbLock) {
          job.localize(_kb);
        }
        return job.run();
      } catch (ProtegeException pe) {
        Log.getLogger().log(Level.WARNING, "Exception on remote execution", pe);
        throw pe;
      }
      finally {
          unrecordCall();
      }
    }

    public void register(RemoteSession session) throws ServerSessionLost {
      synchronized(_kbLock) {
        Registration registration = new Registration(_eventWriter);
        _sessionToRegistrationMap.put(session, registration);
      }
    }

    public void deregister(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              while (inTransaction()) {
                  rollbackTransaction(session);
              }
              _sessionToRegistrationMap.remove(session);
              frameCalculator.deregister(session);
          }
      }
      finally {
          unrecordCall();
      }
    }

    @Override
    public String toString() {
        return "ServerFrameStore[" + _kb + "]";
    }

    public RemoteResponse<List<AbstractEvent>> getEvents(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
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
      finally {
          unrecordCall();
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
      _eventWriter.write(eo);
    }

    private List<ValueUpdate> getValueUpdates(RemoteSession session) {
      updateEvents(session);
      List<ValueUpdate> ret = _sessionToRegistrationMap.get(session).getAndClearValueUpdates();
      int size = ret.size();
      if (size != 0) {
          _sessionToRegistrationMap.get(session).getBandWidthPolicy().addItemsSent(ret.size());
      }
      return ret;
    }

    private void processEvent(RemoteSession session,
                              Registration registration,
                              TransactionIsolationLevel level,
                              AbstractEvent event)  {
      /* ---------------> Look for other relevant event types!!! and fix for nullness--- */
      if (event.getEventType() != FrameEvent.REPLACE_FRAME && event.isReplacementEvent()) {
          return;
      }
      if (event instanceof FrameEvent) {
        handleFrameEvent(session, (FrameEvent) event);
      } else if (event instanceof ClsEvent) {
        handleClsEvent(session, (ClsEvent) event);
      } if (event instanceof KnowledgeBaseEvent) {
        handleKnowledgeBaseEvent(session, (KnowledgeBaseEvent) event);
      }
    }
    
    


    /*
     * Warning... calling getSlot for the wrong event type can cause a
     *            ClassCastException.
     */
  @SuppressWarnings("unchecked")
  private void handleFrameEvent(RemoteSession session,
                                FrameEvent frameEvent) {
    Frame frame = frameEvent.getFrame();
    int type = frameEvent.getEventType();
    
    Sft sft = null;

    if (type == FrameEvent.REPLACE_FRAME || type == FrameEvent.DELETED)  {
        if (frame instanceof Slot || frame instanceof Facet) {
            addWriteUpdate(session, null, new CacheDelete<RemoteSession, Sft, List>(session));
        }
        else { // I think that this doesn't work here but I am being cautious - the references are empty!
               // I have moved this logic to the actual call of the replaceFrame.
            wipeReferencingFrames(frame, getDelegate().getReferences(frame), session);
        }
        return;
    }
    if (type == FrameEvent.OWN_SLOT_ADDED) {
        Slot slot = frameEvent.getSlot();
        CacheResult<List> result = CacheResult.getInvalid();
        sft = new Sft(slot, null, false);
        addWriteUpdate(session, frame, 
 		               new CacheModify<RemoteSession, Sft, List>(session, sft, result));
      } else if (type == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
        Slot slot = frameEvent.getSlot();
        CacheResult<List> result = CacheResult.getInvalid();
        sft = new Sft(slot, null, false);
        addWriteUpdate(session, frame, 
 		               new CacheModify<RemoteSession, Sft, List>(session, sft, result));
      } else if (type == FrameEvent.OWN_SLOT_REMOVED) {
        Slot slot = frameEvent.getSlot();
        CacheResult<List> result = new CacheResult<List>(new ArrayList(), true);
        sft = new Sft(slot, null, false);        
        addWriteUpdate(session, frame, 
	                    new CacheModify<RemoteSession, Sft, List>(session, sft, result));
      }
  }
  
  private void wipeReferencingFrames(Frame frame, Collection<Reference> references, RemoteSession session) {
      Set<Frame> framesToWipe = new HashSet<Frame>();
      framesToWipe.add(frame);
      for (Reference ref : references) {
          framesToWipe.add(ref.getFrame());
      }
      for (Frame f : framesToWipe) {
          addWriteUpdate(session, f, new CacheDelete<RemoteSession, Sft, List>(session));
      }
  }
  
  /*
   * Warning... calling getSlot/getFacet for the wrong event type can cause a
   *            ClassCastException.
   */
  @SuppressWarnings("unchecked")
  private void handleClsEvent(RemoteSession session,
                              ClsEvent clsEvent) {
    Frame frame = clsEvent.getCls();
    int type = clsEvent.getEventType();
    Sft sft = null;
    if (type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED) {
        Slot slot = clsEvent.getSlot();
    	sft = new Sft(slot, valuesFacet, true);
    } else if (type == ClsEvent.TEMPLATE_FACET_ADDED) {
      Slot slot = clsEvent.getSlot();
      Facet facet = clsEvent.getFacet();
      sft = new Sft(slot, facet, false);
    } else if (type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      Facet facet  = clsEvent.getFacet();
      sft = new Sft(slot, facet, true);
    }
    if (sft != null) {
    	CacheResult<List> result = CacheResult.getInvalid();
    	addWriteUpdate(session, frame, 
    			new CacheModify<RemoteSession, Sft, List>(session, sft, result));
    }
  }

  private void addWriteUpdate(RemoteSession session, Frame frame, 
		                     SerializedCacheUpdate<RemoteSession, Sft, List> update) {
	  ValueUpdate vu = new ValueUpdate(frame, update);
	  TransactionIsolationLevel level = getTransactionIsolationLevel();
	  if (TransactionMonitor.updatesSeenByUntransactedClients(transactionMonitor, level)) {
		  for (Registration r : _sessionToRegistrationMap.values()) {
			  r.addUpdate(vu);
		  }
	  }
	  else {
		  Registration r = _sessionToRegistrationMap.get(getCurrentSession());
		  r.addUpdate(vu);
		  r.addCommittableUpdate(vu);
	  }
  }

  public void addReadUpdate(RemoteSession session, Frame frame, 
		                    SerializedCacheUpdate<RemoteSession, Sft, List> update) {
	  Registration r = _sessionToRegistrationMap.get(getCurrentSession());
	  ValueUpdate vu = new ValueUpdate(frame, update);
	  r.addUpdate(vu);
  }

  @SuppressWarnings("unchecked")
  private void handleKnowledgeBaseEvent(RemoteSession session,
                                      KnowledgeBaseEvent event) {
    int type = event.getEventType();
    if (type == KnowledgeBaseEvent.CLS_CREATED || type == KnowledgeBaseEvent.SLOT_CREATED ||
            type == KnowledgeBaseEvent.FACET_CREATED || type == KnowledgeBaseEvent.INSTANCE_CREATED ||
            type == KnowledgeBaseEvent.CLS_DELETED || type == KnowledgeBaseEvent.SLOT_DELETED ||
            type == KnowledgeBaseEvent.FACET_DELETED || type == KnowledgeBaseEvent.INSTANCE_DELETED) {
      Frame deletedFrame = event.getFrame();
	  addWriteUpdate(session, deletedFrame, 
		  	         new CacheDelete<RemoteSession, Sft, List>(session));
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
    @SuppressWarnings("unchecked")
	public RemoteResponse<Boolean> beginTransaction(String name, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          if (cacheLog.isLoggable(Level.FINE)) {
              cacheLog.fine("Begin Transaction for session " + session);
          }
          synchronized(_kbLock) {
              updateEvents(session);
              boolean success = getDelegate().beginTransaction(name);
              addWriteUpdate(session, null,
                             new CacheBeginTransaction<RemoteSession, Sft, List>(session));
              return new RemoteResponse<Boolean>(success, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
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
    public RemoteResponse<Boolean> commitTransaction(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          if (cacheLog.isLoggable(Level.FINE)) {
              cacheLog.fine("Commit Transaction for Session " + session);
          }
          synchronized(_kbLock) {
              addWriteUpdate(session, null, 
                             new CacheCommitTransaction<RemoteSession, Sft, List>(session));
              boolean success = getDelegate().commitTransaction();
              updateEvents(session);
              if (!inTransaction()) {
                  Registration registration = _sessionToRegistrationMap.get(session);
                  List<ValueUpdate> committedUpdates = registration.getCommits();
                  for (Registration otherRegistration : _sessionToRegistrationMap.values()) {
                      if (otherRegistration.equals(registration)) {
                          continue;
                      }
                      for (ValueUpdate committedUpdate : committedUpdates) {
                          otherRegistration.addUpdate(committedUpdate);
                      }
                  }
                  registration.endTransaction();
              }
              if (!existsTransaction()) { // who am i waking?
                  _kbLock.notifyAll();
              }
              return new RemoteResponse<Boolean>(success, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
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
    public RemoteResponse<Boolean> rollbackTransaction(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          if (cacheLog.isLoggable(Level.FINE)) {
              cacheLog.fine("Rollback Transaction for session " + session);
          }
          synchronized(_kbLock) {
              updateEvents(session);
              addWriteUpdate(session, null, 
                             new CacheRollbackTransaction<RemoteSession, Sft, List>(session));
              boolean success = getDelegate().rollbackTransaction();
              if (!inTransaction()) {
                  Registration registration = _sessionToRegistrationMap.get(session);
                  registration.endTransaction();
              }
              if (!existsTransaction()) {  // who am i waking?
                  _kbLock.notifyAll();
              }
              return new RemoteResponse<Boolean>(success, getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
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


    public OntologyUpdate moveDirectSubslot(Slot slot, Slot subslot, int index, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              getDelegate().moveDirectSubslot(slot, subslot, index);
              return new OntologyUpdate(getValueUpdates(session));
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot, RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized(_kbLock) {
              return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
          }
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Set> getDirectOwnSlotValuesClosure(Frame frame,
                                                             Slot slot,
                                                             Set<Frame> missing,
                                                             RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          Set values = null;
          synchronized(_kbLock) {
              values = getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
          }
          LocalizeUtils.localize(missing, _kb);
          if (!frameCalculator.isDisabled(session))  {
              for (Frame value : missing) {
                  frameCalculator.addRequest(value, session, CacheRequestReason.USER_CLOSURE_REQUEST);
              }
          }
          return new RemoteResponse<Set>(values, getValueUpdates(session));
      }
      finally {
          unrecordCall();
      }
    }

    public RemoteResponse<Set> getDirectOwnSlotValuesClosure(Collection<Frame> frames,
                                                             Slot slot,
                                                             Set<Frame> missing,
                                                             RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
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
          if (!frameCalculator.isDisabled(session)) {
              for (Frame value : missing) {
                  frameCalculator.addRequest(value, session, CacheRequestReason.USER_CLOSURE_REQUEST);
              }
          }
          return new RemoteResponse<Set>(values, getValueUpdates(session));
      }
      finally {
          unrecordCall();
      }
    }
    
    public OntologyUpdate preload(Set<String> userFrames, boolean all, RemoteSession session) throws ServerSessionLost {
        recordCall(session);
        try {
            if (all) {
                int preloadFrameLimit = ServerProperties.getPreloadFrameLimit();
                int preloaded = 0;
                synchronized (_kbLock) {
                    for (Frame frame : getDelegate().getFrames()) {
                        if (++preloaded > preloadFrameLimit) {
                            break;
                        }
                        frameCalculator.addRequest(frame, session, CacheRequestReason.PRELOAD);
                    }
                }
            } else {
                addUserFrames(session, userFrames);
                addSystemClasses(session);
            }
            return new OntologyUpdate(getValueUpdates(session));
        }
        finally {
            unrecordCall();
        }
      }

    private void addSystemClasses(RemoteSession session) {
      Set<Frame> frames = new LinkedHashSet<Frame>();
      frames.addAll(_kb.getSystemFrames().getFrames());

      /*
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
      */
      if (!frameCalculator.isDisabled(session)) {
          for (Frame frame : frames) {
        	  synchronized (frameCalculator.getRequestLock()) {
        		  WorkInfo wi = frameCalculator.addRequest(frame, session, CacheRequestReason.PRELOAD);
        		  if (wi != null) {
        			  wi.setSkipDirectInstances(true);
        		  }
        	  }
          }
      }
    }

    private void addSystemClasses(Set<Frame> frames, Cls cls)  {

      if (!cls.isSystem() || frames.contains(cls)) {
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

    private void addUserFrames(RemoteSession session, Set<String> userFrames) {
      Set<Frame> frames = new HashSet<Frame>();
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
          Cls cls = (Cls) frame;
          for (Object o : _kb.getSuperclasses(cls)) {
            Cls superCls = (Cls)  o;
            frames.add(superCls);
            frames.addAll(superCls.getDirectSubclasses());
          }
          synchronized (_kbLock) {
            frames.addAll(_kb.getSuperclasses((Cls) frame));
            frames.addAll(_kb.getDirectSubclasses((Cls) frame));
          }
        }
      }
      if (!frameCalculator.isDisabled(session)) {
          for (Frame frame : frames) {
              frameCalculator.addRequest(frame, session, CacheRequestReason.IMMEDIATE_PRELOAD);
          }
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

    public FrameCalculator getFrameCalculator() {
        return frameCalculator;
    }

    public void setFrameCalculatorDisabled(boolean disabled) {
      FrameCalculator.setDisabled(disabled);
    }

    public void heartBeat(RemoteSession session) throws ServerSessionLost {
      recordCall(session);
      try {
          synchronized (_kbLock) {
              Registration registration = _sessionToRegistrationMap.get(session);
              registration.setLastHeartbeat(System.currentTimeMillis());
          }
      }
      finally {
          unrecordCall();
      }
    }

    public void setMetaProjectInstance(ProjectInstance projectInstance) {
    	this.projectInstance = projectInstance;
    	if (projectInstance != null) {
    		FrameStoreManager fsm = _kb.getFrameStoreManager();
    		ReadAccessEnforcementFrameStore readAccessEnforcement = new ReadAccessEnforcementFrameStore(this);
    		if (readAccessEnforcement.isApplicable()) {
    			fsm.insertFrameStore((FrameStore) Proxy.newProxyInstance(getClass().getClassLoader(), 
    					new Class[] { FrameStore.class }, 
    					readAccessEnforcement), 1);
    		}
    		LastUsageInvocationHandler lastUsageFrameStore = new LastUsageInvocationHandler(projectInstance, frameCalculator);
    		fsm.insertFrameStore((FrameStore) Proxy.newProxyInstance(getClass().getClassLoader(), 
    		                                                         new Class<?>[] { FrameStore.class }, 
    		                                                         lastUsageFrameStore),
    		                      1); // past the localization frame store
    	}
    }

    public ProjectInstance getMetaProjectInstance() {
      return projectInstance;
    }

    public Set<Operation> getAllowedOperations(RemoteSession session) {
      Policy policy = Server.getPolicy();
      User user = policy.getUserByName(session.getUserName());
      return unbackOperations(policy.getAllowedOperations(user, projectInstance));
    }

    public Set<Operation> getKnownOperations(RemoteSession session) {
      return unbackOperations(Server.getPolicy().getKnownOperations());
    }

    private static Set<Operation> unbackOperations(Set<Operation> operations) {
        Set<Operation> unbacked = new HashSet<Operation>();
        for (Operation op : operations) {
            unbacked.add(new UnbackedOperationImpl(op));
        }
        return unbacked;
    }

}
