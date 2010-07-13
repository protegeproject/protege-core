package edu.stanford.smi.protege.server.framestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.exception.ProtegeIOException;
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
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.socket.SimulateDelayAspect;
import edu.stanford.smi.protege.server.update.DeferredOperationCache;
import edu.stanford.smi.protege.server.update.OntologyUpdate;
import edu.stanford.smi.protege.server.update.RemoteResponse;
import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheFactory;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheBeginTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheCommitTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheDelete;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheModify;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRollbackTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

/*
 * Transactions:
 *
 * This class gets updates to its caches from the ServerFrameStore.
 */

public class RemoteClientFrameStore implements FrameStore {
    private static transient Logger log = Log.getLogger(RemoteClientFrameStore.class);
    private static transient Logger cacheLog = ServerFrameStore.cacheLog;

    private static Method executeProtegeJobMethod;
    static {
      try {
        executeProtegeJobMethod = RemoteServerFrameStore.class.getDeclaredMethod("executeProtegeJob",
                                                                                 new Class[] {ProtegeJob.class, RemoteSession.class});
      } catch (NoSuchMethodException nsme) {
        Log.getLogger().log(Level.SEVERE, "No such method ", nsme);
      }
    }


    private KnowledgeBase kb;
    private ClassLoader kbClassLoader;
    private SystemFrames systemFrames;
    private RemoteSession session;
    private RemoteServer server;
    private RemoteServerFrameStore proxiedDelegate;
    private RemoteServerFrameStore remoteDelegate;
    
    private final FifoWriter<SerializedCacheUpdate<RemoteSession, Sft,  List>> deferredTransactionsWriter 
        = new FifoWriter<SerializedCacheUpdate<RemoteSession, Sft,  List>>();
    /*
     * It is true that this cacheMap is a memory leak on the client but a weak hash map does not do the right
     * thing. Frames with equal frame id are constantly being seen and garbage collected, so with a weak hash map,
     * the cacheMap is far too empty.
     */
    private final Map<Frame, DeferredOperationCache> cacheMap = new HashMap<Frame, DeferredOperationCache>();

    private TransactionIsolationLevel transactionLevel;
    private int transactionNesting = 0;

    //FIXME: frameNameToFrameMap may not handle transactions or type updates correctly
    // Any positive result from this map is checked against the cache (more reliable)
    // The negative cache is aggressively cleared and hopefully is accurate.
    private final Map<String, Frame> frameNameToFrameMap = new HashMap<String, Frame>();

    private final RemoteClientStatsImpl stats = new RemoteClientStatsImpl();
    private Set<Operation> allowedOps;
    private Set<Operation> knownOps;


    public RemoteClientFrameStore(String host,
                                  String user,
                                  String password,
                                  String projectName,
                                  KnowledgeBase kb,
                                  boolean preloadAll) {
        try {
            server = (RemoteServer) Naming.lookup("//" + host + "/" + Server.getBoundName());
            String ipAddress = SystemUtilities.getMachineIpAddress();
            session = server.openSession(user, ipAddress, password);
            RemoteServerProject project = server.openProject(projectName, session);
            remoteDelegate = project.getDomainKbFrameStore(session);
            this.kb = kb;
            initialize(preloadAll);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            log.log(Level.FINE, "Exception caught", e);
        }
    }

    public RemoteClientFrameStore(RemoteServer server,
                                  RemoteServerFrameStore delegate,
                                  RemoteSession session,
                                  KnowledgeBase kb,
                                  boolean preloadAll) {
        try {
            this.server = server;
            this.session = session;
            this.kb = kb;
            this.remoteDelegate = delegate;
            initialize(preloadAll);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            log.log(Level.FINE, "Exception caught", e);
        }
    }

    public void initialize(boolean preloadAll) throws RemoteException {
      // precalculate these to avoid deadlock with the getEvents thread
      systemFrames = kb.getSystemFrames();
      kbClassLoader = kb.getClass().getClassLoader();
      // disabled for now - if we need we will try it.
      startHeartbeatThread();
      preload(preloadAll);
    }
    
    private static RemoteClientFrameStore getMeFromKb(KnowledgeBase kb) {
        if (!(kb instanceof DefaultKnowledgeBase)) {
            return null;
        }
        DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
        for (FrameStore fs = dkb.getHeadFrameStore(); fs != null;  fs = fs.getDelegate()) {
            if (fs instanceof RemoteClientFrameStore) {
                return (RemoteClientFrameStore) fs;
            }
        }
        return null;
    }
    
    
    /**
     * This method returns true if the client has a cached value for the direct own/template slot 
     * value for the frame.
     * 
     * This call can be used regardless of whether the caller is a multi-user client.
     */
    public static boolean isCached(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        RemoteClientFrameStore rcfs = getMeFromKb(frame.getKnowledgeBase());
        if (rcfs == null) {
            return true;
        }
        return rcfs.isCachedInternal(frame, slot, facet, isTemplate);
    }
    
    /**
     * In practice, if this method returns true, then all direct own slot values for the 
     * frame will be cached except possibly for DIRECT-INSTANCES-SLOT.  
     * 
     * 
     * This call can be used regardless of whether the caller is a multi-user client. The formal definition 
     * is that if the internal cache does not find a value for a direct frame, slot value then
     * the value really is null.  The cache has a special value that it uses to indicate that 
     * even though the cache is "complete" it does not have a value for a particular frame.
     * 
     * @param frame
     * @return
     */
    public static boolean isCacheComplete(Frame frame) {
        RemoteClientFrameStore rcfs = getMeFromKb(frame.getKnowledgeBase());
        if (rcfs == null) {
            return true;
        }
        Cache<RemoteSession, Sft, List> cache = rcfs.cacheMap.get(frame);
        if (cache == null) {
        	return false;
        }
        return cache.isCacheComplete();
    }

    
    public static RemoteSession getCurrentSession(KnowledgeBase kb) {    
		FrameStoreManager framestore_manager = ((DefaultKnowledgeBase) kb).getFrameStoreManager();
		RemoteClientFrameStore remote_frame_store = framestore_manager.getFrameStoreFromClass(RemoteClientFrameStore.class);
		if (remote_frame_store == null) { return null; }
        return remote_frame_store.getSession();
    }
    

    private void startHeartbeatThread() {
      if (ServerProperties.heartbeatDisabled()) {
        return;
      }
      new Thread("Heartbeat thread [" + kb + "]") {
        @Override
		public void run() {
          try {
            while (true) {
              RemoteServerFrameStore remote = getRemoteDelegate();
              if (remote != null) {
                remote.heartBeat(session);
              }
              Thread.sleep(RemoteServerFrameStore.HEARTBEAT_POLL_INTERVAL);
            }
          } catch (ServerSessionLost ssl) {
            Log.emptyCatchBlock(ssl);
          } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE,
                                "Heartbeat thread died - can't survive the heart for long...",
                                e);
          }
        }
      }.start();
    }


    public String getName() {
      return getClass().getName();
    }

  public synchronized RemoteServerFrameStore getRemoteDelegate() {
      if (proxiedDelegate == null) {
        fixLoader();
        InvocationHandler invoker = new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            synchronized (RemoteClientFrameStore.this) {
              if (!method.equals(executeProtegeJobMethod)) {
                fixLoader();
              } else {
                ((ProtegeJob) args[0]).fixLoader();
              }
              try {
            	  if (log.isLoggable(Level.FINE)) {
            		  log.fine("Remote invoke: " + method.getName() + " Args:");
            		  if (args != null) {
            			  for (Object obj : args) {
            				  log.fine("\t" + (obj instanceof Frame ? ((Frame)obj).getFrameID() : obj));
            			  }
            		  }
            	  }
            	  long start = System.currentTimeMillis();
            	  SimulateDelayAspect.delayForLatency();
            	  Object val =  method.invoke(remoteDelegate, args);
            	  LocalizeUtils.localize(val, kb);
            	  if (log.isLoggable(Level.FINE)) {
            		  log.fine("Invocation took " + (System.currentTimeMillis() - start) + " ms");
            		  logSize(log, Level.FINEST, val);
            	  }
            	  return val;
              } catch (InvocationTargetException ite) {
                throw ite.getCause();
              }
            }
          }

        };
        proxiedDelegate = (RemoteServerFrameStore) Proxy.newProxyInstance(kbClassLoader,
                                                                          new Class[] {RemoteServerFrameStore.class},
                                                                          invoker);
      }
      return proxiedDelegate;
  }

  private void logSize(Logger log, Level level, Object o) {
	  if (o instanceof OntologyUpdate) {
		  OntologyUpdate response = (OntologyUpdate) o;
		  int valueUpdateSize = 0;
		  int valueUpdateCount = 0;
		  for (ValueUpdate vu : response.getValueUpdates()) {
			  valueUpdateSize += getSerializedSize(vu);
			  valueUpdateCount++;
		  }
		  log.log(level, "" + valueUpdateSize + " bytes of " + valueUpdateCount + " value updates");
		  if (response instanceof RemoteResponse) {
			  log.log(level, "" + getSerializedSize(((RemoteResponse) response).getResponse()) + " bytes of included object");
		  }
	  }
	  else {
		  log.log(level, "" + getSerializedSize(o) + " bytes for object of type " + o.getClass());
	  }
  }

  public static int getSerializedSize(Object o) {
	  try {
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  ObjectOutputStream oos = new ObjectOutputStream(baos);
		  oos.writeObject(o);
		  oos.close();
		  return baos.size();
	  }
	  catch (IOException ioe) {
		  if (log.isLoggable(Level.FINE)) {
			  log.fine("trouble calculating size");
		  }
		  return 0;
	  }
  }

  public Map<RemoteSession, Boolean> getUserInfo() {
    try {
      return getRemoteDelegate().getUserInfo();
    } catch (RemoteException re) {
      Log.getLogger().log(Level.WARNING, "Exception caught retrieving user data from remote server", re);
      return new HashMap<RemoteSession, Boolean>();
    }
  }

  public FrameCalculatorStats getServerStats() {
    try {
      return getRemoteDelegate().getStats();
    } catch (RemoteException re) {
      Log.getLogger().log(Level.INFO, "Remote exception getting server stats", re);
      return null;
    }
  }

  public synchronized RemoteClientStats getClientStats() {
    return stats;
  }

  private void fixLoader() {
      ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader correctLoader = kbClassLoader;
      if (currentLoader != correctLoader) {
          if (log.isLoggable(Level.FINEST)) {
            log.finest("Changing loader from " + currentLoader + " to " + correctLoader);
          }
          Thread.currentThread().setContextClassLoader(correctLoader);
      }
  }


    public void setDelegate(FrameStore delegate) {
        throw new UnsupportedOperationException();
    }

    public FrameStore getDelegate() {
        return null;
    }

    public void reinitialize() {
        flushCache();
    }

    private static RuntimeException convertException(Exception e) {
        return new RuntimeException(e);
    }

    public int getClsCount() {
        try {
            return getRemoteDelegate().getClsCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getSlotCount() {
        try {
            return getRemoteDelegate().getSlotCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getFacetCount() {
        try {
            return getRemoteDelegate().getFacetCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getSimpleInstanceCount() {
        try {
            return getRemoteDelegate().getSimpleInstanceCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getFrameCount() {
        try {
            return getRemoteDelegate().getFrameCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Cls> getClses() {
        try {
            Set clses = getRemoteDelegate().getClses(session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<Slot> getSlots() {
        Cls rootSlotClass = getSystemFrames().getRootSlotMetaCls();
        return (Set) getInstances(rootSlotClass);
    }

    public Set<Facet> getFacets() {
        try {
            Set<Facet> facets = getRemoteDelegate().getFacets(session);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Frame> getFrames() {
        try {
            Set frames = getRemoteDelegate().getFrames(session);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Frame getFrame(FrameID id) {
        try {
            Frame frame = getRemoteDelegate().getFrame(id, session);
            return frame;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Frame getFrame(String name) {
      try {
        Frame frame;
        boolean containsFrame = true;
        frame = frameNameToFrameMap.get(name);
        if (frame == null) {
          containsFrame = frameNameToFrameMap.containsKey(name);
        }
        else if (!isCachedInternal(frame, systemFrames.getNameSlot(), null, false)) {
            frame = null;
            frameNameToFrameMap.remove(name);
            containsFrame = false;
        }
        if (frame == null) {
          if (!containsFrame) {
            if (log.isLoggable(Level.FINE)) {
              log.fine("Cache miss for frame named " + name);
            }
            RemoteResponse<Frame> response = getRemoteDelegate().getFrame(name, session);
            processValueUpdate(response);
            frame = response.getResponse();
          }
        }
        if (transactionNesting == 0) {
        	frameNameToFrameMap.put(name, frame);
        }
        return frame;
      } catch (RemoteException e) {
        throw convertException(e);
      }
    }

    public synchronized String getFrameName(Frame frame) {
        try {
            Collection values = getCacheDirectOwnSlotValues(frame, getSystemFrames().getNameSlot());
            return (String) CollectionUtilities.getFirstItem(values);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Cls createCls(FrameID id,
                         Collection directTypes,
                         Collection directSuperclasses,
                         boolean loadDefaultValues) {
        try {
            RemoteResponse<Cls> wrappedCls = getRemoteDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues,
                    session);
            processValueUpdate(wrappedCls);
            return wrappedCls.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Slot createSlot(FrameID id,
                           Collection directTypes,
                           Collection directSuperslots,
                           boolean loadDefaultValues) {
        try {
            RemoteResponse<Slot> wrappedSlot
              = getRemoteDelegate().createSlot(id,
                                               directTypes, directSuperslots,
                                               loadDefaultValues,
                                               session);
            processValueUpdate(wrappedSlot);
            return wrappedSlot.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }


    public synchronized Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        try {
            RemoteResponse<Facet> wrappedFacet
              = getRemoteDelegate().createFacet(id,
                                                directTypes,
                                                loadDefaultValues, session);
            processValueUpdate(wrappedFacet);
            return wrappedFacet.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized SimpleInstance createSimpleInstance(FrameID id,
                                               Collection directTypes,
                                               boolean loadDefaultValues) {
        try {
            RemoteResponse<SimpleInstance> wrappedSimpleInstance
              = getRemoteDelegate().createSimpleInstance(id, directTypes,
                                                         loadDefaultValues,
                                                         session);
            processValueUpdate(wrappedSimpleInstance);
            return wrappedSimpleInstance.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteCls(Cls cls) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteCls(cls, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteSlot(Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteSlot(slot, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteFacet(Facet facet) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteFacet(facet, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteSimpleInstance(SimpleInstance simpleInstance) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteSimpleInstance(simpleInstance, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set<Slot> getOwnSlots(Frame frame) {
        return getCacheOwnSlots(frame);
    }

    public synchronized Collection getOwnSlotValues(Frame frame, Slot slot) {
        return getCacheOwnSlotValues(frame, slot);
    }

    public synchronized List getDirectOwnSlotValues(Frame frame, Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(frame, slot);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(frame, slot).size();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectOwnSlotValue(frame, slot, from, to, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        try {
            OntologyUpdate vu = getRemoteDelegate().setDirectOwnSlotValues(frame, slot, values, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getOwnFacets(Frame frame, Slot slot) {
        return getCacheOwnFacets(frame, slot);
    }

    public synchronized Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        return getCacheOwnFacetValues(frame, slot, facet);
    }

    public synchronized Set getTemplateSlots(Cls cls) {
        return getCacheTemplateSlots(cls);
    }

    public synchronized List getDirectTemplateSlots(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectTemplateSlotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List getDirectDomain(Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(slot, getSystemFrames().getDirectDomainSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getDomain(Slot slot) {
        try {
            return getCacheDomain(slot);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getOverriddenTemplateSlots(Cls cls) {
        try {
            Set slots = getRemoteDelegate().getOverriddenTemplateSlots(cls, session);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        try {
            Set slots = getRemoteDelegate().getDirectlyOverriddenTemplateSlots(cls, session);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void addDirectTemplateSlot(Cls cls, Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().addDirectTemplateSlot(cls, slot, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectTemplateSlot(Cls cls, Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectTemplateSlot(cls, slot, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectTemplateSlot(cls, slot, index, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getCacheTemplateSlotValues(cls, slot);
    }

    public synchronized List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        try {
            return getCacheValues(cls, slot, getSystemFrames().getValuesFacet(), true);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        try {
            OntologyUpdate vu = getRemoteDelegate().setDirectTemplateSlotValues(cls, slot, values, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        try {
          Set facets = getRemoteDelegate().getTemplateFacets(cls, slot, session);
          return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = getRemoteDelegate().getOverriddenTemplateFacets(cls, slot, session);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = getRemoteDelegate().getDirectlyOverriddenTemplateFacets(cls, slot, session);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectTemplateFacetOverrides(cls, slot, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getCacheTemplateFacetValues(cls, slot, facet);
    }

    public synchronized List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        try {
            return getCacheValues(cls, slot, facet, true);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        try {
            OntologyUpdate vu = getRemoteDelegate().setDirectTemplateFacetValues(cls, slot, facet, values, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List<Cls> getDirectSuperclasses(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectSuperclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getSuperclasses(Cls cls) {
        try {
            return getCacheOwnSlotValueClosure(cls, getSystemFrames().getDirectSuperclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    private SystemFrames getSystemFrames() {
      return systemFrames;
    }

    public synchronized List<Cls> getDirectSubclasses(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectSubclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set<Cls> getSubclasses(Cls cls) {
        try {
            return getCacheOwnSlotValueClosure(cls, getSystemFrames().getDirectSubclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void addDirectSuperclass(Cls cls, Cls superclass) {
        try {
            OntologyUpdate vu = getRemoteDelegate().addDirectSuperclass(cls, superclass, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectSuperclass(Cls cls, Cls superclass) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectSuperclass(cls, superclass, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectSubclass(cls, subclass, index, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List getDirectSuperslots(Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(slot, getSystemFrames().getDirectSuperslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getSuperslots(Slot slot) {
        try {
            return getCacheOwnSlotValueClosure(slot, getSystemFrames().getDirectSuperslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List getDirectSubslots(Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(slot, getSystemFrames().getDirectSubslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getSubslots(Slot slot) {
        try {
            return getCacheOwnSlotValueClosure(slot, getSystemFrames().getDirectSubslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void addDirectSuperslot(Slot slot, Slot superslot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().addDirectSuperslot(slot, superslot, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectSuperslot(Slot slot, Slot superslot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectSuperslot(slot, superslot, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectSubslot(slot, subslot, index, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List getDirectTypes(Instance instance) {
        try {
            return getCacheDirectOwnSlotValues(instance, getSystemFrames().getDirectTypesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

  public synchronized Set getTypes(Instance instance) {
        try {
            return getCacheOwnSlotValueClosure(getDirectTypes(instance), getSystemFrames().getDirectSuperclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List<Instance> getDirectInstances(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectInstancesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set<Instance> getInstances(Cls cls) {
      Set<Cls> subClasses = new HashSet<Cls>();
      subClasses.addAll(getSubclasses(cls));
      subClasses.add(cls);
      Set<Instance> values = new HashSet<Instance>();
      Set<Frame> missingClasses = new HashSet<Frame>();
      for (Cls subClass : subClasses) {
        if (isCachedInternal(subClass, getSystemFrames().getDirectInstancesSlot(), (Facet) null, false)) {
          values.addAll(getDirectInstances(subClass));
        } else {
          missingClasses.add(subClass);
        }
      }
      if (missingClasses.isEmpty()) {
        return values;
      } else if (missingClasses.size() == 1) {
        Cls subClass = (Cls) missingClasses.iterator().next();
        values.addAll(getDirectInstances(subClass));
        return values;
      } else {
        try {
            RemoteResponse<Set<Instance>> instances = getRemoteDelegate().getInstances(cls, session);
            processValueUpdate(instances);
            return instances.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
      }
    }

    public synchronized void addDirectType(Instance instance, Cls type) {
        try {
            OntologyUpdate vu = getRemoteDelegate().addDirectType(instance, type, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

  public synchronized void removeDirectType(Instance instance, Cls type) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectType(instance, type, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectType(Instance instance, Cls type, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectType(instance, type, index, session);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized List<AbstractEvent> getEvents() {
        if (transactionNesting > 0) {
          return new ArrayList<AbstractEvent>();
        }
        List<AbstractEvent> receivedEvents = null;
        try {
          RemoteResponse<List<AbstractEvent>> response = getRemoteDelegate().getEvents(session);
          processValueUpdate(response);
          receivedEvents = response.getResponse();
          return receivedEvents;
        } catch (RemoteException e) {
          Log.getLogger().log(Level.SEVERE,
                              "Exception caught - local cache may be out of date", e);
          throw new RuntimeException(e);
        }
    }

    public void executeQuery(final Query query, final QueryCallback callback) {
      new Thread("Remote Client Callback thread") {
        @Override
		public void run() {
          try {
            RemoteResponse<Collection<Frame>> response = getRemoteDelegate().executeQuery(query, session);
            processValueUpdate(response);
            callback.provideQueryResults(response.getResponse());
          } catch (OntologyException oe) {
            callback.handleError(oe);
          } catch (ProtegeIOException ioe) {
            callback.handleError(ioe);
          } catch (RemoteException re) {
            Log.getLogger().log(Level.WARNING, "Exception accessing remote host", re);
            callback.handleError(new ProtegeIOException(re));
          } catch (ProtegeError pe) {
            callback.handleError(pe);
          } catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Developer error", t);
            callback.handleError(new ProtegeError(t));
          }
        }
      }.start();
    }

    public synchronized Set<Reference> getReferences(Object object) {
        try {
            Set<Reference> references = getRemoteDelegate().getReferences(object, session);
            return references;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Cls> getClsesWithMatchingBrowserText(String text,
                                                    Collection superclasses,
                                                    int maxMatches) {
        try {
            Set clses = getRemoteDelegate().getClsesWithMatchingBrowserText(text, superclasses, maxMatches, session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }

    }

    public Set<Reference> getMatchingReferences(String string, int maxMatches) {
        try {
            Set references = getRemoteDelegate().getMatchingReferences(string, maxMatches, session);
            return references;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        try {
            Set frames = getRemoteDelegate().getFramesWithDirectOwnSlotValue(slot, value, session);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        try {
            Set frames = getRemoteDelegate().getFramesWithAnyDirectOwnSlotValue(slot, session);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        try {
            Set<Frame> frames = getRemoteDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches, session);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        try {
            Set clses = getRemoteDelegate().getClsesWithDirectTemplateSlotValue(slot, value, session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        try {
            Set clses = getRemoteDelegate().getClsesWithAnyDirectTemplateSlotValue(slot, session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        try {
            Set clses = getRemoteDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches,
                    session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        try {
            Set clses = getRemoteDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value, session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        try {
            Set clses = getRemoteDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value,
                    maxMatches, session);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        try {
            return getCacheClosure(frame, slot);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized boolean beginTransaction(String name) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Begin Transaction");
      }
        try {
            RemoteResponse<Boolean> ret = getRemoteDelegate().beginTransaction(name, session);
            processValueUpdate(ret);
            transactionNesting++;
            return ret.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized boolean commitTransaction() {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Commit Transaction");
      }
        try {
            RemoteResponse<Boolean> ret =  getRemoteDelegate().commitTransaction(session);
            processValueUpdate(ret);
            transactionNesting--;
            return ret.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized boolean rollbackTransaction() {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Rollback Transaction");
      }
        try {
            RemoteResponse<Boolean> ret = getRemoteDelegate().rollbackTransaction(session);
            processValueUpdate(ret);
            transactionNesting--;
            return ret.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public TransactionMonitor getTransactionStatusMonitor()  {
      throw new UnsupportedOperationException("Shouldn't be doing this on the client side");
    }

    public static TransactionIsolationLevel getTransactionIsolationLevel(KnowledgeBase kb)
    throws TransactionException {
      RemoteClientFrameStore frameStore = getMeFromKb(kb);
      if (frameStore == null) {
        return TransactionIsolationLevel.NONE;
      }
      return frameStore.getTransactionIsolationLevel();
    }

    public synchronized TransactionIsolationLevel getTransactionIsolationLevel() throws TransactionException {
      if (transactionLevel != null) {
        return transactionLevel;
      }
      try {
        return transactionLevel = getRemoteDelegate().getTransactionIsolationLevel();
      } catch (RemoteException re) {
        throw new TransactionException(re);
      }
    }

    public static boolean setTransactionIsolationLevel(KnowledgeBase kb, TransactionIsolationLevel level)
    throws TransactionException {
      RemoteClientFrameStore frameStore = getMeFromKb(kb);
      if (frameStore == null) {
        return false;
      }
      return frameStore.setTransactionIsolationLevel(level);
    }

    public synchronized boolean setTransactionIsolationLevel(TransactionIsolationLevel level)
      throws TransactionException {
      try {
        transactionLevel = null;
        return getRemoteDelegate().setTransactionIsolationLevel(level);
      } catch (RemoteException re) {
        throw new TransactionException(re);
      }
    }

    public synchronized void close() {
        try {
            if (server != null) {
                server.closeSession(session);
                server = null;
            }
            remoteDelegate = null;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    //------------------------------
    public synchronized void preload(boolean preloadAll) throws RemoteException {
      boolean skip = ServerProperties.skipPreload();
      if (skip) {
        return;
      }      
      Log.getLogger().config("Preloading frame values: " + kb);
      Set<String> frames = ServerProperties.preloadUserFrames();
      OntologyUpdate vu = getRemoteDelegate().preload(frames, preloadAll, session);
      processValueUpdate(vu);     
    }

    private Set getCacheOwnSlotValueClosure(Frame frame, Slot slot) throws RemoteException {
        return getCacheClosure(frame, slot);
    }

    private Set getCacheOwnSlotValueClosure(Collection<Frame> frames, Slot slot) throws RemoteException {
        return getCacheClosure(frames, slot);
    }

    private Set getCacheClosure(Frame frame, Slot slot) throws RemoteException {
      Set closure = new LinkedHashSet();
      Set<Frame> missing = new HashSet<Frame>();
      calculateClosureFromCacheOnly(frame, slot, closure, missing);
      if (!missing.isEmpty()) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("not in closure cache: " + frame.getFrameID() + ", " + slot.getFrameID());
        }
        stats.closureMiss++;
        RemoteResponse<Set> wrappedClosure =
          getRemoteDelegate().getDirectOwnSlotValuesClosure(frame, slot, missing, session);
        processValueUpdate(wrappedClosure);
        closure = wrappedClosure.getResponse();
        if (log.isLoggable(Level.FINE)) {
          for (Object o : closure) {
            if  (o instanceof Frame) {
              log.fine("\t closure frame = " + ((Frame) o).getFrameID());
            } else {
              log.fine("other closure " + o);
            }
          }
        }
      } else {
        stats.closureHit++;
      }
      return closure;
    }

    /*
     * There is a fair bit of inefficiency here but taking the hit on the client side is much better
     * than making extra calls to the server.  Also the server could be more aggressive about calculating
     * values that the client might need, but I am finding that the server  is spending a lot of time
     * recalculating the same values over and over.  This is the start of an experiment where the server
     * needs a little more reason to believe that values need to be calculated for the client.
     */
    private void calculateClosureFromCacheOnly(Frame frame, Slot slot, Set closure, Set<Frame> missing)
            throws RemoteException {
      if (isCachedInternal(frame, slot, (Facet) null, false)) {
        Collection values = getCacheValues(frame, slot, (Facet) null, false);
        for (Object value : values) {
          boolean changed = closure.add(value);
          if (changed && value instanceof Frame) {
            calculateClosureFromCacheOnly((Frame) value, slot, closure, missing);
          }
        }
      } else {
        missing.add(frame);
      }
    }

    private Set getCacheClosure(Collection<Frame> frames, Slot slot) throws RemoteException {
        Set closure = new LinkedHashSet(frames);
        Set<Frame> missing = new HashSet<Frame>();
        for (Frame frame : frames) {
          calculateClosureFromCacheOnly(frame, slot, closure, missing);
        }
        if (!missing.isEmpty()) {
          stats.closureMiss++;
          RemoteResponse<Set> wrappedClosure =
            getRemoteDelegate().getDirectOwnSlotValuesClosure(frames, slot, missing, session);
          processValueUpdate(wrappedClosure);
          closure = wrappedClosure.getResponse();
          closure.addAll(frames);
          return closure;
        } else {
          stats.closureHit++;
          return closure;
        }
    }

    private List getCacheDirectOwnSlotValues(Frame frame, Slot slot) throws RemoteException {
        return getCacheValues(frame, slot, null, false);
    }


    // TT 2007/01/09:  This code is copied from SimpleFrameStore
    public Collection getCacheOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        Collection values = new ArrayList();
        Iterator i = getDirectTypes((Instance) frame).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            Collection typeValues = getTemplateFacetValues(cls, slot, facet);
            values = resolveValues(values, typeValues, facet);
        }
        return values;
    }


    private Collection getCacheTemplateFacetValues(Cls localCls, Slot slot, Facet facet) {
        Collection values = new ArrayList(getDirectTemplateFacetValues(localCls, slot, facet));
        Iterator i = getSuperclasses(localCls).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            Collection superclassValues = getDirectTemplateFacetValues(cls, slot, facet);
            values = resolveValues(values, superclassValues, facet);
        }
        Slot associatedSlot = (Slot) getDirectOwnSlotValue(facet, getSystemFrames().getAssociatedSlotSlot());
        if (associatedSlot != null) {
            Collection topLevelValues = getDirectOwnSlotValues(slot, associatedSlot);
            values = resolveValues(values, topLevelValues, facet);
        }
        return values;
    }

    private Object getDirectOwnSlotValue(Frame frame, Slot slot) {
        Collection values = getDirectOwnSlotValues(frame, slot);
        return CollectionUtilities.getFirstItem(values);
    }

    private static Collection resolveValues(Collection values, Collection newValues, Facet facet) {
        if (!newValues.isEmpty()) {
            if (values.isEmpty()) {
                values.addAll(newValues);
            } else {
                values = facet.resolveValues(values, newValues);
                if (values == newValues) {
                    values = new ArrayList(values);
                }
            }
        }
        return values;
    }

  public Collection getCacheOwnSlotValues(Frame frame, Slot slot) {
        Collection values = new ArrayList();
        addOwnSlotValues(frame, slot, values);
        return values;
    }

    private void addOwnSlotValues(Frame frame, Slot slot, Collection values) {
        values.addAll(getDirectOwnSlotValues(frame, slot));
        addInheritedTemplateSlotValues(frame, slot, values);
        addSubslotValues(frame, slot, values);
        if (frame instanceof Slot && values.isEmpty() && isInheritedSuperslotSlot(slot)) {
            addInheritedSuperslotValues((Slot) frame, slot, values);
        }
    }

    private boolean isInheritedSuperslotSlot(Slot slot) {
        return slot.equals(getSystemFrames().getDirectDomainSlot())
                || slot.equals(getSystemFrames().getValueTypeSlot())
                || slot.equals(getSystemFrames().getMaximumCardinalitySlot())
                || slot.equals(getSystemFrames().getMinimumValueSlot())
                || slot.equals(getSystemFrames().getMaximumValueSlot());
    }

    private void addInheritedSuperslotValues(Slot slotFrame, Slot slot, Collection values) {
        Facet facet = (Facet) getDirectOwnSlotValue(slot, getSystemFrames().getAssociatedFacetSlot());
        Iterator i = getSuperslots(slotFrame).iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            Collection superslotValues = getDirectOwnSlotValues(superslot, slot);
            if (facet == null) {
                values.addAll(superslotValues);
            } else {
                Collection resolvedValues = facet.resolveValues(values, superslotValues);
                if (!resolvedValues.equals(values)) {
                    values.clear();
                    values.addAll(resolvedValues);
                }
            }
        }
    }

    private void addInheritedTemplateSlotValues(Frame frame, Slot slot, Collection values) {
        if (frame instanceof Instance) {
            Set templateSlotValues = new LinkedHashSet();
            Instance instance = (Instance) frame;
            Iterator i = getTypes(instance).iterator();
            while (i.hasNext()) {
                Cls type = (Cls) i.next();
                templateSlotValues.addAll(getDirectTemplateSlotValues(type, slot));
            }
            values.addAll(templateSlotValues);
        }
    }

    private void addSubslotValues(Frame frame, Slot slot, Collection values) {
        Iterator i = getSubslots(slot).iterator();
        while (i.hasNext()) {
            Slot subslot = (Slot) i.next();
            values.addAll(getDirectOwnSlotValues(frame, subslot));
        }
    }

    private Set getCacheOwnFacets(Frame frame, Slot slot) {
        Set facets = new HashSet();
        Iterator i = getOwnSlots(slot).iterator();
        while (i.hasNext()) {
            Slot ownSlot = (Slot) i.next();
            Facet facet = (Facet) getDirectOwnSlotValue(ownSlot, getSystemFrames().getAssociatedFacetSlot());
            if (facet != null) {
                facets.add(facet);
            }
        }
        return facets;
    }

    private Collection getCacheTemplateSlotValues(Cls cls, Slot slot) {
        return getTemplateFacetValues(cls, slot, getSystemFrames().getValuesFacet());
    }

    private Set<Slot> getCacheOwnSlots(Frame frame) {
        Collection types = getTypes((Instance) frame);
        Set<Slot> ownSlots = collectOwnSlotValues(types, getSystemFrames().getDirectTemplateSlotsSlot());
        ownSlots.add(getSystemFrames().getNameSlot());
        ownSlots.add(getSystemFrames().getDirectTypesSlot());
        return ownSlots;
    }

    private Set<Slot> collectOwnSlotValues(Collection frames, Slot slot) {
        Set<Slot> values = new LinkedHashSet<Slot>();
        Object[] frameArray = frames.toArray();
        for (int i = 0; i < frameArray.length; ++i) {
            Frame frame = (Frame) frameArray[i];
            values.addAll(getDirectOwnSlotValues(frame, slot));
        }
        return values;
    }

    private Set getCacheTemplateSlots(Cls cls) {
        Set clses = new LinkedHashSet(getSuperclasses(cls));
        clses.add(cls);
        Set values = collectOwnSlotValues(clses, getSystemFrames().getDirectTemplateSlotsSlot());
        return values;
    }

    private Set getCacheDomain(Slot slot) throws RemoteException {
        return getCacheOwnSlotValueClosure(getDirectDomain(slot), getSystemFrames().getDirectSubclassesSlot());
    }


    /*
     * This is the main routine for checking the cached data before going to the
     * server.
     */
    private List getCacheValues(Frame frame,
                                Slot slot,
                                Facet facet,
                                boolean isTemplate) throws RemoteException {
      List values = null;
      CacheResult<List> cachedResult = readCache(frame, slot, facet, isTemplate);
      if (cachedResult.isValid()) {
        values = cachedResult.getResult();
      } else {
        if (log.isLoggable(Level.FINE)) {
          log.fine("cache miss for frame " +
              frame.getFrameID() + " slot " + slot.getFrameID() +
              (facet == null ? "null" : "" + facet.getFrameID()) +
              " template " + isTemplate);
        }
        RemoteResponse<List> vu = null;
        if (facet != null) {
          if (isTemplate) {
            vu = getRemoteDelegate().getDirectTemplateFacetValues((Cls) frame,
                                                                  slot, facet,
                                                                  session);
          } else {
            throw new UnsupportedOperationException(
                                   "We don't cache this information...");
          }
        } else {
          if (isTemplate) {
            vu = getRemoteDelegate().getDirectTemplateSlotValues((Cls) frame,
                                                                 slot, session);
          } else {
            vu = getRemoteDelegate().getDirectOwnSlotValues(frame, slot, session);
          }
        }
        processValueUpdate(vu);
        values = vu.getResponse();
      }
      if (values == null) {
        values = new ArrayList();
      }
      return values;
    }

    /**
     * This routine assumes that the caller is holding the cache lock
     */
    private boolean isCachedInternal(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return readCache(frame, slot, facet, isTemplate).isValid();
    }

    private CacheResult<List> readCache(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        CacheResult<List> ret;    
        Cache<RemoteSession, Sft, List> cache = cacheMap.get(frame);
        if (cacheLog.isLoggable(Level.FINEST) && cache != null) {
            cacheLog.finest("Using cache " + cache.getCacheId() + " for " + frame.getFrameID().getName());
        }
        if (cache == null) {
            ret = CacheResult.getInvalid();
        }
        else if (cache.isInvalid()) {
            if (cacheLog.isLoggable(Level.FINEST)) {
                cacheLog.finest("Cache is deleted");
            }
            cacheMap.remove(frame);
            cache = null;
            ret = CacheResult.getInvalid();
        }
        else {
            ret = cache.readCache(session, new Sft(slot, facet, isTemplate));
        }
        if (cacheLog.isLoggable(Level.FINEST)) {
            cacheLog.finest("Cache Result for " + frame.getFrameID().getName()
                                   + ", " + slot.getFrameID().getName() 
                                   + ", " + (facet == null ? "null" : facet.getFrameID().getName()) +", " + isTemplate + " returns = " + ret);
        }
        if (ret.isValid()) {
            stats.hit++;
        }
        else {
            stats.miss++;
        }
        return ret;
    }



  private void processValueUpdate(OntologyUpdate updates) {
    if (cacheLog.isLoggable(Level.FINE) && updates.getValueUpdates().size() != 0) {
      cacheLog.fine("received " + updates.getValueUpdates().size() + " value updates for Knowledge base " + kb);
    }
    // this reader marks the state at the beginning of this call.
    FifoReader<SerializedCacheUpdate<RemoteSession, Sft,  List>> deferredTransactionsReader 
               = new FifoReader<SerializedCacheUpdate<RemoteSession, Sft,  List>>(deferredTransactionsWriter);
    for (ValueUpdate vu : updates.getValueUpdates()) {
    	if (cacheLog.isLoggable(Level.FINER)) {
    		cacheLog.finer("processing " + vu);
    	}
    	SerializedCacheUpdate<RemoteSession, Sft,  List> cacheUpdate = vu.getUpdate();
    	if (cacheUpdate instanceof CacheBeginTransaction ||
    			cacheUpdate instanceof CacheCommitTransaction ||
    			cacheUpdate instanceof CacheRollbackTransaction) {
    		deferredTransactionsWriter.write(cacheUpdate);
    		continue;
    	}
    	Frame frame = vu.getFrame();
    	if (frame == null && cacheUpdate instanceof CacheDelete) {
    	    deferredTransactionsWriter.write(cacheUpdate);
    	    continue;
    	}
    	if (frameNameToFrameMap.get(frame.getName()) == null // be aggressive about cleaning the negative cache
    	        || cacheUpdate instanceof CacheDelete        // deleted guys might get a new type
    	        || cacheUpdate instanceof CacheModify)       // could be modifying the type.
    	{
    	    frameNameToFrameMap.remove(frame.getName());
    	}
    	try {
    	    Cache<RemoteSession, Sft, List> cache = cacheMap.get(frame);
    	    if (cache != null && cache.isInvalid()) {
    	        cache = null;
    	        cacheMap.remove(frame);
    	    }
    	    if (cache == null) {
    	        cache = CacheFactory.createEmptyCache(getTransactionIsolationLevel());
    	        FifoReader<SerializedCacheUpdate<RemoteSession, Sft,  List>> reader 
    	           = new FifoReader<SerializedCacheUpdate<RemoteSession, Sft,  List>>(deferredTransactionsReader);
    	        cache = new DeferredOperationCache(cache, reader);
    	        cacheMap.put(frame, (DeferredOperationCache) cache);
    	        if (cacheLog.isLoggable(Level.FINEST)) {
    	            cacheLog.finest("Created cache " + cache.getCacheId() + " for frame " + frame.getFrameID().getName());
    	        }
    	    }
    	    cacheUpdate.performUpdate(cache);
    	}
    	catch (Throwable t) {
    	    cacheMap.remove(frame);
    	    log.log(Level.WARNING, "Exception caught processing cache for " + frame.getFrameID().getName(), t);
    	}
    }
  }

  public synchronized void flushCache() {
    if (cacheLog.isLoggable(Level.FINE)) {
      cacheLog.fine("Flushing client cache");
    }
    frameNameToFrameMap.clear();
    for (Cache c : cacheMap.values()) {
        c.flush();
    }
  }

  public Object executeProtegeJob(ProtegeJob job) throws ProtegeException {
    try {
      Object result = getRemoteDelegate().executeProtegeJob(job, session);
      LocalizeUtils.localize(result, kb);
      return result;
    } catch (RemoteException remote) {
      throw new ProtegeIOException(remote);
    }
  }

  public Set<Operation> getAllowedOperations() throws ProtegeIOException {
    if (allowedOps == null) {
      try {
        allowedOps = getRemoteDelegate().getAllowedOperations(session);
      } catch (RemoteException e) {
        throw new ProtegeIOException(e);
      }
    }
    return allowedOps;
  }

  public Set<Operation> getKnownOperations() throws ProtegeIOException {
    if (knownOps == null) {
      try {
        knownOps = getRemoteDelegate().getKnownOperations(session);
      } catch (RemoteException re)  {
        throw new ProtegeIOException(re);
      }
    }
    return knownOps;
  }

  public static boolean isOperationAllowed(KnowledgeBase kb, Operation op)
  throws ProtegeIOException {
    DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
    FrameStore terminalFS = dkb.getTerminalFrameStore();
    if (!(terminalFS instanceof RemoteClientFrameStore)) {
      return true;
    }
    RemoteClientFrameStore remoteFS = (RemoteClientFrameStore) terminalFS;
    return !remoteFS.getKnownOperations().contains(op) ||
              remoteFS.getAllowedOperations().contains(op);
  }

  public RemoteServer getRemoteServer() {
      return server;
  }

  public RemoteSession getSession() {
      return session;
  }

  public class RemoteClientStatsImpl implements RemoteClientStats {
    int miss = 0;
    int hit = 0;
    int closureMiss = 0;
    int closureHit = 0;

    public int getCacheHits() {
      return hit;
    }

    public int getCacheMisses() {
      return miss;
    }

    public int getClosureCacheHits() {
      return closureHit;
    }

    public int getClosureCacheMisses() {
      return closureMiss;
    }

  }

    public void replaceFrame(Frame original, Frame replacement) {
        try {
            OntologyUpdate vu = getRemoteDelegate().replaceFrame(original, replacement, session);
            processValueUpdate(vu);
            DeferredOperationCache replacementCache = cacheMap.get(replacement);
            if (replacementCache != null) {
                replacementCache.invalidate(session);  //  because the events that would update this state don't happen
            }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }
    


}
