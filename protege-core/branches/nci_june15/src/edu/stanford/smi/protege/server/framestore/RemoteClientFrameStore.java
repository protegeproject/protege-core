package edu.stanford.smi.protege.server.framestore;

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

import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;
import edu.stanford.smi.protege.server.update.FrameEvaluationCompleted;
import edu.stanford.smi.protege.server.update.FrameEvaluationPartial;
import edu.stanford.smi.protege.server.update.FrameEvaluationStarted;
import edu.stanford.smi.protege.server.update.FrameRead;
import edu.stanford.smi.protege.server.update.FrameWrite;
import edu.stanford.smi.protege.server.update.InvalidateCacheUpdate;
import edu.stanford.smi.protege.server.update.OntologyUpdate;
import edu.stanford.smi.protege.server.update.RemoteResponse;
import edu.stanford.smi.protege.server.update.RemoveCacheUpdate;
import edu.stanford.smi.protege.server.update.RemoveFrameCache;
import edu.stanford.smi.protege.server.update.SftUpdate;
import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/*
 * Transactions:
 * 
 * This class gets updates to its caches from the ServerFrameStore.  
 */

public class RemoteClientFrameStore implements FrameStore {
    private static transient Logger log = Log.getLogger(RemoteClientFrameStore.class);
    private static transient Logger cacheLog = ServerFrameStore.cacheLog;
    
    private static Method getEventsMethod;
    static {
      try {
        getEventsMethod = RemoteServerFrameStore.class.getDeclaredMethod("getEvents", 
                                                                         new Class[] { RemoteSession.class });
      } catch (NoSuchMethodException nsme) {
        Log.getLogger().log(Level.SEVERE, "No such method ", nsme);
      }
    }

    
    private KnowledgeBase kb;
    private ClassLoader kbClassLoader;
    private SystemFrames systemFrames;
    private Object lock;
    private RemoteSession session;
    private RemoteServer server;
    private RemoteServerFrameStore proxiedDelegate;
    private RemoteServerFrameStore remoteDelegate;
    

    private enum CacheStatus {
      STARTED_CACHING, COMPLETED_CACHING
    }

    private TransactionIsolationLevel transactionLevel;
    private int transactionNesting = 0;
    

  /*
   * These three variables (involving caching are synchronized using the cache object.
   * The purpose of this synchronization is to protect updates from the getEvents thread.
   */
    private Map<Frame, CacheStatus> cacheStatus = new HashMap<Frame, CacheStatus>();
    private Map<Frame, Map<Sft, List>> cache = new HashMap<Frame, Map<Sft, List>>();
    private Map<Frame, Map<Sft, List>> sessionCache = new HashMap<Frame, Map<Sft, List>>();
    private Map<String, Frame> frameNameToFrameMap = new HashMap<String, Frame>();
    
    private RemoteClientStatsImpl stats = new RemoteClientStatsImpl();

 
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

    public RemoteClientFrameStore(RemoteServerFrameStore delegate, 
                                  RemoteSession session,
                                  KnowledgeBase kb,
                                  boolean preloadAll) {
        try {
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
    
    private void startHeartbeatThread() {
      if (ServerProperties.heartbeatDisabled()) {
        return;
      }
      new Thread("Heartbeat thread [" + kb + "]") {
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
              fixLoader();
              if (!method.equals(getEventsMethod)) {
                ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
                if (view != null) {
                  // make the server activity button red, it reverts to white after 300 milliseconds of inactivity
                  view.startBusyFlagThread();	 
                }
              }
              try {
                return method.invoke(remoteDelegate, args);
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
            Log.getLogger().finest("Changing loader from " + currentLoader + " to " + correctLoader);
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
        // do nothing
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

    private void localize(Object o) {
        LocalizeUtils.localize(o, kb);
    }

    public Set<Cls> getClses() {
        try {
            Set clses = getRemoteDelegate().getClses(session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        } 
    }

    public Set<Slot> getSlots() {
        Cls rootSlotClass = getSystemFrames().getRootSlotMetaCls();
        return getInstances(rootSlotClass);
    }

    public Set<Facet> getFacets() {
        try {
            Set<Facet> facets = getRemoteDelegate().getFacets(session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        } 
    }

    public Set<Frame> getFrames() {
        try {
            Set frames = getRemoteDelegate().getFrames(session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        } 
    }

    public Frame getFrame(FrameID id) {
        try {
            Frame frame = getRemoteDelegate().getFrame(id, session);
            localize(frame);
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
        if (frame == null) {
          if (!containsFrame) {
            if (log.isLoggable(Level.FINE)) {
              log.fine("Cache miss for frame named " + name);
            }
            RemoteResponse<Frame> response = getRemoteDelegate().getFrame(name, session);
            localize(response);
            processValueUpdate(response);
            frame = response.getResponse();
          }
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

    public synchronized void setFrameName(Frame frame, String name) {
        try {
            OntologyUpdate vu = getRemoteDelegate().setFrameName(frame, name, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Cls createCls(FrameID id, 
                         String name, 
                         Collection directTypes, 
                         Collection directSuperclasses,
                         boolean loadDefaultValues) {
        try {
            RemoteResponse<Cls> wrappedCls = getRemoteDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaultValues,
                    session);
            localize(wrappedCls);
            processValueUpdate(wrappedCls);
            return wrappedCls.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Slot createSlot(FrameID id, 
                           String name, 
                           Collection directTypes, 
                           Collection directSuperslots,
                           boolean loadDefaultValues) {
        try {
            RemoteResponse<Slot> wrappedSlot 
              = getRemoteDelegate().createSlot(id, name, 
                                               directTypes, directSuperslots, 
                                               loadDefaultValues,
                                               session);
            localize(wrappedSlot);
            processValueUpdate(wrappedSlot);
            return wrappedSlot.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaultValues) {
        try {
            RemoteResponse<Facet> wrappedFacet 
              = getRemoteDelegate().createFacet(id, name, 
                                                directTypes, 
                                                loadDefaultValues, session);
            localize(wrappedFacet);
            processValueUpdate(wrappedFacet);
            return wrappedFacet.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized SimpleInstance createSimpleInstance(FrameID id, 
                                               String name, 
                                               Collection directTypes,
                                               boolean loadDefaultValues) {
        try {
            RemoteResponse<SimpleInstance> wrappedSimpleInstance 
              = getRemoteDelegate().createSimpleInstance(id, name, directTypes,
                                                         loadDefaultValues, 
                                                         session);
            localize(wrappedSimpleInstance);
            processValueUpdate(wrappedSimpleInstance);
            return wrappedSimpleInstance.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteCls(Cls cls) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteCls(cls, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteSlot(Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteSlot(slot, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteFacet(Facet facet) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteFacet(facet, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void deleteSimpleInstance(SimpleInstance simpleInstance) {
        try {
            OntologyUpdate vu = getRemoteDelegate().deleteSimpleInstance(simpleInstance, session);
            localize(vu);
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
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        try {
            OntologyUpdate vu = getRemoteDelegate().setDirectOwnSlotValues(frame, slot, values, session);
            localize(vu);
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
            localize(slots);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        try {
            Set slots = getRemoteDelegate().getDirectlyOverriddenTemplateSlots(cls, session);
            localize(slots);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void addDirectTemplateSlot(Cls cls, Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().addDirectTemplateSlot(cls, slot, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectTemplateSlot(Cls cls, Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectTemplateSlot(cls, slot, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectTemplateSlot(cls, slot, index, session);
            localize(vu);
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
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        try {
          Set facets = getRemoteDelegate().getTemplateFacets(cls, slot, session);
          localize(facets);
          return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = getRemoteDelegate().getOverriddenTemplateFacets(cls, slot, session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = getRemoteDelegate().getDirectlyOverriddenTemplateFacets(cls, slot, session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectTemplateFacetOverrides(cls, slot, session);
            localize(vu);
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
            localize(vu);
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
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectSuperclass(Cls cls, Cls superclass) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectSuperclass(cls, superclass, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectSubclass(cls, subclass, index, session);
            localize(vu);
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
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void removeDirectSuperslot(Slot slot, Slot superslot) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectSuperslot(slot, superslot, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectSubslot(slot, subslot, index, session);
            localize(vu);
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

    public synchronized List getDirectInstances(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectInstancesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getInstances(Cls cls) {
      Set<Cls> subClasses = new HashSet<Cls>();
      subClasses.addAll(getSubclasses(cls));
      subClasses.add(cls);
      Set values = new HashSet();
      Set<Frame> missingClasses = new HashSet<Frame>();
      for (Cls subClass : subClasses) {
        if (isCached(subClass, getSystemFrames().getDirectInstancesSlot(), (Facet) null, false)) {
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
            RemoteResponse<Set> instances = getRemoteDelegate().getInstances(cls, session);
            localize(instances);
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
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

  public synchronized void removeDirectType(Instance instance, Cls type) {
        try {
            OntologyUpdate vu = getRemoteDelegate().removeDirectType(instance, type, session);
            localize(vu);
            processValueUpdate(vu);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized void moveDirectType(Instance instance, Cls type, int index) {
        try {
            OntologyUpdate vu = getRemoteDelegate().moveDirectType(instance, type, index, session);
            localize(vu);
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
          localize(response);
          processValueUpdate(response);
          receivedEvents = response.getResponse();
          return receivedEvents;
        } catch (RemoteException e) {
          Log.getLogger().log(Level.SEVERE, 
                              "Exception caught - local cache may be out of date", e);
          throw new RuntimeException(e);
        }
    }
 
    public Set executeQuery(Query query) {
        try {
            Set frames = getRemoteDelegate().executeQuery(query, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public synchronized Set getReferences(Object object) {
        try {
            Set references = getRemoteDelegate().getReferences(object, session);
            localize(references);
            return references;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingBrowserText(String text, Collection superclasses, int maxMatches) {
        try {
            Set clses = getRemoteDelegate().getClsesWithMatchingBrowserText(text, superclasses, maxMatches, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }

    }

    public Set getMatchingReferences(String string, int maxMatches) {
        try {
            Set references = getRemoteDelegate().getMatchingReferences(string, maxMatches, session);
            localize(references);
            return references;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        try {
            Set frames = getRemoteDelegate().getFramesWithDirectOwnSlotValue(slot, value, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        try {
            Set frames = getRemoteDelegate().getFramesWithAnyDirectOwnSlotValue(slot, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        try {
            Set frames = getRemoteDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        try {
            Set clses = getRemoteDelegate().getClsesWithDirectTemplateSlotValue(slot, value, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        try {
            Set clses = getRemoteDelegate().getClsesWithAnyDirectTemplateSlotValue(slot, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        try {
            Set clses = getRemoteDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches,
                    session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        try {
            Set clses = getRemoteDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        try {
            Set clses = getRemoteDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value,
                    maxMatches, session);
            localize(clses);
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
            transactionNesting++;
            RemoteResponse<Boolean> ret = getRemoteDelegate().beginTransaction(name, session);
            localize(ret);
            processValueUpdate(ret);
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
            transactionNesting--;
            if (transactionNesting == 0) {
              sessionCache = new HashMap<Frame, Map<Sft, List>>();
            }
            RemoteResponse<Boolean> ret =  getRemoteDelegate().commitTransaction(session);
            localize(ret);
            processValueUpdate(ret);
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
            transactionNesting--;
            if (transactionNesting == 0) {
              sessionCache = new HashMap<Frame, Map<Sft, List>>();
            }
            RemoteResponse<Boolean> ret = getRemoteDelegate().rollbackTransaction(session);
            localize(ret);
            processValueUpdate(ret);
            return ret.getResponse();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public TransactionMonitor getTransactionStatusMonitor()  {
      throw new UnsupportedOperationException("Shouldn't be doing this on the client side");
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
      boolean skip = Boolean.getBoolean(ServerProperties.SKIP_PRELOAD);
      if (skip) {
        return;
      }
      Log.getLogger().config("Preloading frame values");
      Set<String> frames = ServerProperties.preloadUserFrames();
      OntologyUpdate vu = getRemoteDelegate().preload(frames, preloadAll, session);
      localize(vu);
      processValueUpdate(vu);
    }
      
    private Set getCacheOwnSlotValueClosure(Frame frame, Slot slot) throws RemoteException {
        return getCacheClosure(frame, slot);
    }

    private Set getCacheOwnSlotValueClosure(Collection<Frame> frames, Slot slot) throws RemoteException {
        return getCacheClosure(frames, slot);
    }

    private Set getCacheClosure(Frame frame, Slot slot) throws RemoteException {
      Set closure = new HashSet();
      Set<Frame> missing = new HashSet<Frame>();
      calculateClosureFromCacheOnly(frame, slot, closure, missing);
      if (!missing.isEmpty()) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("not in closure cache: " + frame.getFrameID() + ", " + slot.getFrameID());
        }
        stats.closureMiss++;
        RemoteResponse<Set> wrappedClosure = 
          getRemoteDelegate().getDirectOwnSlotValuesClosure(frame, slot, missing, session);
        localize(wrappedClosure);
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
      if (isCached(frame, slot, (Facet) null, false)) {
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
        Set closure = new HashSet(frames);
        Set<Frame> missing = new HashSet<Frame>();
        for (Frame frame : frames) {
          calculateClosureFromCacheOnly(frame, slot, closure, missing);
        }
        if (!missing.isEmpty()) {
          stats.closureMiss++;
          RemoteResponse<Set> wrappedClosure = 
            getRemoteDelegate().getDirectOwnSlotValuesClosure(frames, slot, missing, session);
          localize(wrappedClosure);
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


    
    // -----------------------------------------------------------

    // This code is copied from SimpleFrameStore
    private Collection getCacheOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        Collection values = new ArrayList();
        Iterator i = getDirectTypes((Instance) frame).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            values.addAll(getTemplateFacetValues(cls, slot, facet));
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

    private Set getCacheOwnSlots(Frame frame) {
        Collection types = getTypes((Instance) frame);
        Set ownSlots = collectOwnSlotValues(types, getSystemFrames().getDirectTemplateSlotsSlot());
        ownSlots.add(getSystemFrames().getNameSlot());
        ownSlots.add(getSystemFrames().getDirectTypesSlot());
        return ownSlots;
    }

    private Set collectOwnSlotValues(Collection frames, Slot slot) {
        Set values = new LinkedHashSet();
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
      if (isCached(frame, slot, facet, isTemplate)) {
        values = readCache(frame, slot, facet, isTemplate);
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
        localize(vu);
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
    private boolean isCached(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
      /*
       * if the transaction isolation level is repeatable read, then we need to let the database know
       * about all read operations.  A more complicated and optimized solution is possible if I start
       * new cache's when the transaction starts.  Then the database will know about the first read but
       * later reads will come from the cache.
       */
      Sft lookup = new Sft(slot, facet, isTemplate);
      if (transactionNesting > 0) {
        TransactionIsolationLevel level;
        try {
          level = getTransactionIsolationLevel();
          if (level == null) {
            stats.miss++;
            return false;
          }
        } catch (TransactionException e) {
          Log.getLogger().log(Level.WARNING, "Could not get transaction isolation level - caching disabled", e);
          stats.miss++;
          return false;
        }
        Map<Sft, List> sessionCachedValues = sessionCache.get(frame);
        if (level.compareTo(TransactionIsolationLevel.REPEATABLE_READ) >= 0 &&
            (sessionCachedValues == null || sessionCachedValues.get(lookup) == null)) {
          stats.miss++;
          return false;
        }
        if (level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0 &&
            sessionCachedValues != null && sessionCachedValues.containsKey(lookup)) {
          if (sessionCachedValues.get(lookup) == null) {
            stats.miss++;
            return false;
          } else {
            stats.hit++;
            return true;
          }
        }
      }
      Map<Sft, List> m = cache.get(frame);
      if (m == null) {
        stats.miss++;
        return false;
      }
      List values = m.get(lookup);
      if (values != null) {
        stats.hit++;
        return  true;
      } else {
        boolean ret =  cacheStatus.get(frame) == CacheStatus.COMPLETED_CACHING 
          && !m.containsKey(lookup);
        if  (ret) {
          stats.hit++;
        } else {
          stats.miss++;
        }
        return ret;
      }
    }

    private List readCache(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
      List result = null;
      Sft lookup = new Sft(slot, facet, isTemplate);
      if (transactionNesting > 0 && sessionCache.get(frame) != null) {
        TransactionIsolationLevel level;
        try {
          level = getTransactionIsolationLevel();
        } catch (TransactionException e) {
          Log.getLogger().log(Level.WARNING, "Could not get transaction isolation level - caching disabled", e);
          level = null;
        }
        if (level != null && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
          result = sessionCache.get(frame).get(lookup);
        }
      }
      if (result == null) {
        result = cache.get(frame).get(lookup);
      }
      return result;
    }


    private void addCachedEntry(boolean isTransactionScope,
                                Frame frame, 
                                Slot slot,
                                Facet facet,
                                boolean isTemplate,
                                List values) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Client Received value for frame " + frame.getFrameID() + 
                      " slot " + slot.getFrameID() + " facet " + 
                      (facet == null ? "null" : "" + facet.getFrameID()) + 
                      " is template " + isTemplate);
        cacheLog.fine("Transaction Scope = " + isTransactionScope);
      }
      Map<Frame, Map<Sft,List>> workingCache = isTransactionScope ? sessionCache : cache;
      Map<Sft, List> slotValueMap = workingCache.get(frame);
      if (slotValueMap == null) {
        slotValueMap = new HashMap<Sft, List>();
        workingCache.put(frame, slotValueMap);
      }
      Sft lookupSft = new Sft(slot, facet, isTemplate);
      slotValueMap.put(lookupSft, values);
      if (facet == null &&
          !isTemplate &&
          slot.equals(getSystemFrames().getNameSlot()) &&
          !isTransactionScope) {
        if (values != null && !values.isEmpty()) {
          if (cacheLog.isLoggable(Level.FINE)) {
            cacheLog.fine("frame " + frame.getFrameID() + " has name " + values.get(0));
          }
          frameNameToFrameMap.put((String) values.get(0), frame);
        }
      }
    }


    private void invalidateCachedEntry(boolean isTransactionScope,
                                       Frame frame,
                                       Slot slot,
                                       Facet facet,
                                       boolean isTemplate,
                                       boolean remove) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("making invalid cache entry for frame " +
                      frame.getFrameID() + " slot " + slot.getFrameID() +
                      " facet " + (facet == null ? "null" : facet.toString()) +
                      " isTemplate " + isTemplate + " remove flag " + remove);
        cacheLog.fine("Transaction Scope = " + isTransactionScope);
      }
      Map<Frame, Map<Sft, List>> workingCache = isTransactionScope ? sessionCache : cache;
      Map<Sft, List> slotValueMap = workingCache.get(frame);
      CacheStatus status = cacheStatus.get(frame);
      if (isTransactionScope && !remove && slotValueMap == null) {
        slotValueMap = new HashMap<Sft, List>();
        workingCache.put(frame, slotValueMap);
      }
      if (slotValueMap != null) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        
        if (facet == null && 
            !isTemplate && 
            slot.equals(getSystemFrames().getNameSlot())) {
          List values = slotValueMap.get(lookupSft);
          if (values != null && !values.isEmpty()) {
            String name = (String) values.get(0);
            frameNameToFrameMap.remove(name);
            if (cacheLog.isLoggable(Level.FINE)) {
              cacheLog.fine("Flushing name also " + name);
            }
          }
        }
        if (cacheLog.isLoggable(Level.FINE)) {
          cacheLog.fine("Cache Completion Status = " + status);
        }
        if (!remove && (isTransactionScope || status != null)) {
          slotValueMap.put(lookupSft, null);
        } else {
          slotValueMap.remove(lookupSft);
        }
      }
    }

  private void removeCachedUpdate(boolean isTransactionScope, Frame frame) {
    if (cacheLog.isLoggable(Level.FINE)) {
      cacheLog.fine("Removing cache entry for " + frame.getFrameID());
      cacheLog.fine("Transaction Scope = " + isTransactionScope);
    }
    Sft lookup = new Sft(getSystemFrames().getNameSlot(), null, false);
    if (cache.get(frame) != null && cache.get(frame).get(lookup) != null) {
      String name = (String) cache.get(frame).get(lookup).get(0);
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("\tName too (" + name + ")");
      }
      frameNameToFrameMap.remove(name);
    }
    Map<Frame, Map<Sft, List>> workingCache = isTransactionScope ? sessionCache : cache;
    workingCache.remove(frame);
    cacheStatus.remove(frame);
  }

  /*
   * This call assumes that the eventLock is held by the
   * caller.
   */
  private void processValueUpdate(OntologyUpdate vu) {
    processValueUpdate(vu.getValueUpdates());
  }
  
  private void processValueUpdate(List<ValueUpdate> updates) {
    if (cacheLog.isLoggable(Level.FINE)) {
      cacheLog.fine("received " + updates.size() + " value updates");
    }
    for (ValueUpdate vu : updates) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Client Processing event " + vu);
      }
      boolean isTransactionScope = vu.isTransactionScope();
      Map<Frame, Map<Sft, List>> workingCache = isTransactionScope ? sessionCache : cache;
      Frame frame = vu.getFrame();
      if (vu instanceof FrameEvaluationStarted) {
        if (cacheLog.isLoggable(Level.FINE)) {
          cacheLog.fine("Started caching for frame" + frame.getFrameID());  
        }
        cacheStatus.put(frame, CacheStatus.STARTED_CACHING);
      } else if (vu instanceof FrameEvaluationCompleted) {
        CacheStatus status = cacheStatus.get(frame);
        if (status != null && status == CacheStatus.STARTED_CACHING) {
          if (cacheLog.isLoggable(Level.FINE)) {
            cacheLog.fine("Completed caching for " + frame.getFrameID());
          }
          cacheStatus.put(frame, CacheStatus.COMPLETED_CACHING);
        }
      } else if (vu instanceof FrameEvaluationPartial) {
        if (cacheLog.isLoggable(Level.FINE)) {
          cacheLog.fine("Aborted full cache for " + frame.getFrameID());
        }
        cacheStatus.remove(frame);
      } else if (vu instanceof RemoveFrameCache) {
        removeCachedUpdate(isTransactionScope, frame);
      } else if (vu instanceof SftUpdate) {
        SftUpdate sftu = (SftUpdate) vu;
        Slot slot = sftu.getSlot();
        Facet facet = sftu.getFacet();
        boolean isTemplate = sftu.isTemplate();
        TransactionIsolationLevel level;
        try {
          level = getTransactionIsolationLevel();
        } catch (TransactionException te) {
          Log.getLogger().log(Level.WARNING, "Error handling cache update", te);
          level = null;
        }
        if (cacheLog.isLoggable(Level.FINE)) {
          cacheLog.fine("Transaction Isolation Level = " + level);
        }
        if (level == null && vu instanceof FrameRead) {
          invalidateCachedEntry(false, frame, slot, facet, isTemplate, false);
          invalidateCachedEntry(true, frame, slot, facet, isTemplate, false);
        } else if (vu instanceof FrameRead && level == TransactionIsolationLevel.READ_COMMITTED) {
          if (sessionCache.get(frame) == null || sessionCache.get(frame).get(new Sft(slot, facet, isTemplate)) == null) {
            addCachedEntry(false, frame, slot, facet, isTemplate, ((FrameRead) vu).getValues());
          }
        } else if (vu instanceof FrameRead) {
          addCachedEntry(isTransactionScope, frame, slot, facet, isTemplate, ((FrameRead) vu).getValues());
        } else if (vu instanceof FrameWrite) {
          addCachedEntry(isTransactionScope, frame, slot, facet, isTemplate, ((FrameWrite) vu).getValues());
        } else if (vu instanceof RemoveCacheUpdate) {
          invalidateCachedEntry(isTransactionScope, frame, slot, facet, isTemplate, true);
        } else if (vu instanceof InvalidateCacheUpdate) {
          invalidateCachedEntry(isTransactionScope, frame, slot, facet, isTemplate, false);
        }
      }
    }
  }
  
  public synchronized void flushCache() {
    if (cacheLog.isLoggable(Level.FINE)) {
      cacheLog.fine("Flushing client cache");
    }
    cacheStatus.clear();
    cache.clear();
    sessionCache.clear();
    frameNameToFrameMap.clear();
    stats = new RemoteClientStatsImpl();
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
}
