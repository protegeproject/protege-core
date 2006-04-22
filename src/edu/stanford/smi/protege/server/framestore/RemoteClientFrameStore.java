package edu.stanford.smi.protege.server.framestore;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Cls;
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
import edu.stanford.smi.protege.server.framestore.background.FrameEvaluationCompleted;
import edu.stanford.smi.protege.server.framestore.background.FrameEvaluationEvent;
import edu.stanford.smi.protege.server.framestore.background.FrameEvaluationStarted;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class RemoteClientFrameStore implements FrameStore {
    private static Logger log = Log.getLogger(RemoteClientFrameStore.class);

    private KnowledgeBase kb;
    private RemoteSession session;
    private RemoteServer server;
    private RemoteServerFrameStore remoteDelegate;
    
    private Slot nameSlot;

    /*
     * Deadlock Warning... You can't do knowledge base like calls inside an
     *                     event lock.  The knolwedge base is synchronized and
     *                     knowledge base calls will lock the eventLock when 
     *                     interpreting event data from the remote server to update 
     *                     the cache.  In particular this is why the nameSlot is 
     *                     handled specially. 
     */
    private Object eventLock = new Object();
    private List<EventObject> events = new ArrayList<EventObject>();
    
    private Map<String, Frame> frameNameToFrameMap = new HashMap<String, Frame>();
    /*
     * A Cache of the direct own slot values.
     * 
     * There are two threads that I know of that compete for the cache
     * variable.  The EventDispatcher thread which is run periodically
     * or when notified by this code and the main knowledge base thread.
     *
     */
    private Map<Frame, Map<Sft, List>> cache = new HashMap<Frame, Map<Sft, List>>();

    
    private enum CacheStatus {
      STARTED_CACHING, COMPLETED_CACHING
    }
    private Map<Frame, CacheStatus> cacheStatus = new HashMap<Frame, CacheStatus>();
    
    public String getName() {
        return getClass().getName();
    }

    public RemoteServerFrameStore getRemoteDelegate() {
        fixLoader();
        return remoteDelegate;
    }

    private void fixLoader() {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader correctLoader = kb.getClass().getClassLoader();
        if (currentLoader != correctLoader) {
            if (log.isLoggable(Level.FINEST)) {
              Log.getLogger().finest("Changing loader from " + currentLoader + " to " + correctLoader);
            }
            Thread.currentThread().setContextClassLoader(correctLoader);
        }
    }

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
            nameSlot = getSystemFrames().getNameSlot();
            preload(preloadAll);
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
            nameSlot = getSystemFrames().getNameSlot();
            preload(preloadAll);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            log.log(Level.FINE, "Exception caught", e);
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
        try {
            Set slots = getRemoteDelegate().getSlots(session);
            localize(slots);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Facet> getFacets() {
        try {
            Set facets = getRemoteDelegate().getFacets(session);
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

    public Frame getFrame(String name) {
      try {
        Frame frame = frameNameToFrameMap.get(name);
        if (frame == null) {
          if (!frameNameToFrameMap.containsKey(name)) {
            synchronized(eventLock) {
              RemoteResponse<Frame> response = getRemoteDelegate().getFrame(name, session);
              localize(response);
              processValueUpdate(response);
              frame = response.getResponse();
            }
          }
        }
        return frame;
      } catch (RemoteException e) {
        throw convertException(e);
      }
    }

    public String getFrameName(Frame frame) {
        try {
            Collection values = getCacheDirectOwnSlotValues(frame, getSystemFrames().getNameSlot());
            return (String) CollectionUtilities.getFirstItem(values);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void setFrameName(Frame frame, String name) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().setFrameName(frame, name, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Cls createCls(FrameID id, 
                         String name, 
                         Collection directTypes, 
                         Collection directSuperclasses,
                         boolean loadDefaultValues) {
        try {
          synchronized(eventLock) {
            RemoteResponse<Cls> wrappedCls = getRemoteDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaultValues,
                    session);
            localize(wrappedCls);
            processValueUpdate(wrappedCls);
            return wrappedCls.getResponse();
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Slot createSlot(FrameID id, 
                           String name, 
                           Collection directTypes, 
                           Collection directSuperslots,
                           boolean loadDefaultValues) {
        try {
          synchronized(eventLock) {
            RemoteResponse<Slot> wrappedSlot 
              = getRemoteDelegate().createSlot(id, name, 
                                               directTypes, directSuperslots, 
                                               loadDefaultValues,
                                               session);
            localize(wrappedSlot);
            processValueUpdate(wrappedSlot);
            return wrappedSlot.getResponse();
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaultValues) {
        try {
          synchronized(eventLock) {
            RemoteResponse<Facet> wrappedFacet 
              = getRemoteDelegate().createFacet(id, name, 
                                                directTypes, 
                                                loadDefaultValues, session);
            localize(wrappedFacet);
            processValueUpdate(wrappedFacet);
            return wrappedFacet.getResponse();
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public SimpleInstance createSimpleInstance(FrameID id, 
                                               String name, 
                                               Collection directTypes,
                                               boolean loadDefaultValues) {
        try {
          synchronized (eventLock) {
            RemoteResponse<SimpleInstance> wrappedSimpleInstance 
              = getRemoteDelegate().createSimpleInstance(id, name, directTypes,
                                                         loadDefaultValues, 
                                                         session);
            localize(wrappedSimpleInstance);
            processValueUpdate(wrappedSimpleInstance);
            return wrappedSimpleInstance.getResponse();
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteCls(Cls cls) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().deleteCls(cls, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteSlot(Slot slot) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().deleteSlot(slot, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteFacet(Facet facet) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().deleteFacet(facet, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().deleteSimpleInstance(simpleInstance, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Slot> getOwnSlots(Frame frame) {
        return getCacheOwnSlots(frame);
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot) {
        return getCacheOwnSlotValues(frame, slot);
    }

    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(frame, slot);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(frame, slot).size();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().moveDirectOwnSlotValue(frame, slot, from, to, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().setDirectOwnSlotValues(frame, slot, values, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getOwnFacets(Frame frame, Slot slot) {
        return getCacheOwnFacets(frame, slot);
    }

    public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        return getCacheOwnFacetValues(frame, slot, facet);
    }

    public Set getTemplateSlots(Cls cls) {
        return getCacheTemplateSlots(cls);
    }

    public List getDirectTemplateSlots(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectTemplateSlotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getDirectDomain(Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(slot, getSystemFrames().getDirectDomainSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDomain(Slot slot) {
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

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().addDirectTemplateSlot(cls, slot, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().removeDirectTemplateSlot(cls, slot, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().moveDirectTemplateSlot(cls, slot, index, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getCacheTemplateSlotValues(cls, slot);
    }

    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        try {
            return getCacheValues(cls, slot, getSystemFrames().getValuesFacet(), true);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().setDirectTemplateSlotValues(cls, slot, values, session);
            localize(vu);
            processValueUpdate(vu);
          }
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

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().removeDirectTemplateFacetOverrides(cls, slot, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getCacheTemplateFacetValues(cls, slot, facet);
    }

    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        try {
            return getCacheValues(cls, slot, facet, true);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().setDirectTemplateFacetValues(cls, slot, facet, values, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getDirectSuperclasses(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectSuperclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getSuperclasses(Cls cls) {
        try {
            return getCacheOwnSlotValueClosure(cls, getSystemFrames().getDirectSuperclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    private SystemFrames getSystemFrames() {
        return kb.getSystemFrames();
    }

    public List<Cls> getDirectSubclasses(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectSubclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set<Cls> getSubclasses(Cls cls) {
        try {
            return getCacheOwnSlotValueClosure(cls, getSystemFrames().getDirectSubclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().addDirectSuperclass(cls, superclass, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().removeDirectSuperclass(cls, superclass, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().moveDirectSubclass(cls, subclass, index, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getDirectSuperslots(Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(slot, getSystemFrames().getDirectSuperslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getSuperslots(Slot slot) {
        try {
            return getCacheOwnSlotValueClosure(slot, getSystemFrames().getDirectSuperslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getDirectSubslots(Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(slot, getSystemFrames().getDirectSubslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getSubslots(Slot slot) {
        try {
            return getCacheOwnSlotValueClosure(slot, getSystemFrames().getDirectSubslotsSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().addDirectSuperslot(slot, superslot, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().removeDirectSuperslot(slot, superslot, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().moveDirectSubslot(slot, subslot, index, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getDirectTypes(Instance instance) {
        try {
            return getCacheDirectOwnSlotValues(instance, getSystemFrames().getDirectTypesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getTypes(Instance instance) {
        try {
            return getCacheOwnSlotValueClosure(getDirectTypes(instance), getSystemFrames().getDirectSuperclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getDirectInstances(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectInstancesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getInstances(Cls cls) {
        try {
            Set instances = getRemoteDelegate().getInstances(cls, session);
            localize(instances);
            return instances;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void addDirectType(Instance instance, Cls type) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().addDirectType(instance, type, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectType(Instance instance, Cls type) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().removeDirectType(instance, type, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectType(Instance instance, Cls type, int index) {
        try {
          synchronized(eventLock) {
            ValueUpdate vu = getRemoteDelegate().moveDirectType(instance, type, index, session);
            localize(vu);
            processValueUpdate(vu);
          }
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }
    
    public List<EventObject> getEvents() {
      synchronized (eventLock) {
        List<EventObject> ret = null;
        List<EventObject> receivedEvents = null;
        try {
          receivedEvents = getRemoteDelegate().getEvents(session);
          localize(receivedEvents);
          receivedEvents = processEvents(receivedEvents);
        } catch (RemoteException e) {
          Log.getLogger().log(Level.SEVERE, 
                              "Exception caught - local cache may be out of date", e);
        }
        events.addAll(receivedEvents);
        ret = events;
        events = new ArrayList<EventObject>();
        return ret;
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

    public Set getReferences(Object object) {
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

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        try {
            Set values = getRemoteDelegate().getDirectOwnSlotValuesClosure(frame, slot, session);
            localize(values);
            return values;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public boolean beginTransaction(String name) {
        try {
            return getRemoteDelegate().beginTransaction(name, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public boolean commitTransaction() {
        try {
            return getRemoteDelegate().commitTransaction(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public boolean rollbackTransaction() {
        try {
            return getRemoteDelegate().rollbackTransaction(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void close() {
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
    public void preload(boolean preloadAll) throws RemoteException {
      Log.getLogger().config("Preloading frame values");
      synchronized (eventLock) {
        ValueUpdate vu = getRemoteDelegate().preload(preloadAll, session);
        localize(vu);
        processValueUpdate(vu);
      }
    }

    private Set getCacheOwnSlotValueClosure(Frame frame, Slot slot) throws RemoteException {
        return getCacheClosure(frame, slot, null, false);
    }

    private Set getCacheOwnSlotValueClosure(Collection frames, Slot slot) throws RemoteException {
        return getCacheClosure(frames, slot, null, false);
    }

    private Set getCacheClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
        Set closure = new HashSet();
        boolean succeeded = calculateClosureFromCacheOnly(frame, slot, facet, isTemplate, closure);
        if (!succeeded) {
            if (log.isLoggable(Level.FINE)) {
              log.fine("not in cache: " + frame);
            }
            closure = getRemoteDelegate().getDirectOwnSlotValuesClosure(frame, slot, session);
            localize(closure);
        }
        return closure;
    }

    private Set getCacheClosure(Collection frames, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
        Set closure = new HashSet(frames);
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            closure.addAll(getCacheClosure(frame, slot, facet, isTemplate));
        }
        return closure;
    }

    private boolean isCached(Frame frame) {
      synchronized (cache) {
        return cache.containsKey(frame);
      }
    }

    private boolean calculateClosureFromCacheOnly(Frame frame, Slot slot, Facet facet, boolean isTemplate, Set closure)
            throws RemoteException {
        boolean inCache = isCached(frame) && (facet == null || isTemplate);
        if (inCache) {
            Collection values = getCacheValues(frame, slot, facet, isTemplate);
            Iterator i = values.iterator();
            while (i.hasNext() && inCache) {
                Object value = i.next();
                boolean changed = closure.add(value);
                if (changed && value instanceof Frame) {
                    inCache = calculateClosureFromCacheOnly((Frame) value, slot, facet, isTemplate, closure);
                }
            }
        }
        return inCache;
    }

    private List getCacheDirectOwnSlotValues(Frame frame, Slot slot) throws RemoteException {
        return getCacheValues(frame, slot, null, false);
    }

    private Object getCacheDirectOwnSlotValue(Frame frame, Slot slot) throws RemoteException {
        return CollectionUtilities.getFirstItem(getCacheValues(frame, slot, null, false));
    }   
    /*
     * This is the main routine for checking the cached data before going to the
     * server.
     */
    private List getCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
      if (log.isLoggable(Level.FINER)) {
        log.finer("client requesting cached value for frame " + frame.getFrameID() + " slot " +
            slot.getFrameID() + (facet == null ? "null" : "" + facet.getFrameID()) +
            " template " + isTemplate);
      }
      Map<Sft, List> sftValues;
      synchronized (cache) {
        sftValues = cache.get(frame);
      }
      List values;
      Sft lookupSft = new Sft(slot, facet, isTemplate);
      if (sftValues != null) {
        values = sftValues.get(lookupSft);
      } else {
        values = null;
      }
      if (values == null) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("getting missing values for:  " + frame.getFrameID());
        }
        CacheStatus cstatus = cacheStatus.get(frame);
        if (sftValues == null ||
            cstatus != CacheStatus.COMPLETED_CACHING  || 
            sftValues.containsKey(lookupSft)) {
          synchronized (eventLock) {
            RemoteResponse<List> vu = null;
            if (facet != null) {
              if (isTemplate) {
                vu = getRemoteDelegate().getDirectTemplateFacetValues((Cls) frame, slot, facet, session);
              } else {
                throw new UnsupportedOperationException("We don't cache this information...");
              }
            } else {
              if (isTemplate) {
                vu = getRemoteDelegate().getDirectTemplateSlotValues((Cls) frame, slot, session);
              } else {
                vu = getRemoteDelegate().getDirectOwnSlotValues(frame, slot, session);
              }
            }
            localize(vu);
            processValueUpdate(vu);
            values = vu.getResponse();
          }
        }
        if (values == null) {
          values = new ArrayList();
        }
      }
      return values;
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


    private List<EventObject> processEvents(List<EventObject> events)  {
      List reducedEvents = new ArrayList<EventObject>();
      synchronized (cache) {
        for (EventObject event : events) {
            boolean included = true;
            if (log.isLoggable(Level.FINER)) {
              log.finer("Client received event " + event);
            }
            /* ---------------> Look for other relevant event types!!! and fix for nullness--- */
            if (event instanceof FrameEvent) {
              handleFrameEvent((FrameEvent) event);
            } else if (event instanceof ClsEvent) {
              handleClsEvent((ClsEvent) event);
            } else if (event instanceof FrameEvaluationStarted) {
              included = false;
              handleCacheStarted((FrameEvaluationStarted) event);
            } else if (event instanceof FrameEvaluationCompleted) {
              included = false;
              handleCacheCompleted((FrameEvaluationCompleted) event);
            } else if (event instanceof FrameEvaluationEvent) {
              included = false;
              handleFrameValue((FrameEvaluationEvent) event);
            }
            if (included) {
              reducedEvents.add(event);
            }
        }
        return reducedEvents;
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
      invalidateCachedEntry(frame, slot, (Facet) null, false, false);
    } else if (type == FrameEvent.OWN_SLOT_REMOVED) {
      Slot slot = frameEvent.getSlot();
      invalidateCachedEntry(frame, slot, (Facet) null, false, true);
    } else if (type == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
      Slot slot = frameEvent.getSlot();
      invalidateCachedEntry(frame, slot, (Facet) null, false, false);
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
      invalidateCachedEntry(frame, slot, (Facet) null, true, false);
    } else if (type == ClsEvent.TEMPLATE_SLOT_REMOVED) {
      Slot slot = clsEvent.getSlot();
      invalidateCachedEntry(frame, slot, (Facet) null, true, true);
    } else if (type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      invalidateCachedEntry(frame, slot, (Facet) null, true, false);
    } else if (type == ClsEvent.TEMPLATE_FACET_ADDED) {
      Slot slot = clsEvent.getSlot();
      Facet facet = clsEvent.getFacet();
      invalidateCachedEntry(frame, slot, facet, true, false); 
    } else if (type == ClsEvent.TEMPLATE_FACET_REMOVED) {
      Slot slot = clsEvent.getSlot();
      Facet facet = clsEvent.getFacet();
      invalidateCachedEntry(frame, slot, facet, true, true);
    } else if (type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED) {
      Slot slot = clsEvent.getSlot();
      Facet facet  = clsEvent.getFacet();
      invalidateCachedEntry(frame, slot, facet, true, false);
    }
  }

  private void handleCacheStarted(FrameEvaluationStarted event) {
    if (log.isLoggable(Level.FINE)) {
      log.fine("started caching for frame " + event.getFrame().getFrameID());
    }
    cacheStatus.put(event.getFrame(), CacheStatus.STARTED_CACHING);
  }

  private void handleCacheCompleted(FrameEvaluationCompleted event) {
    CacheStatus status = cacheStatus.get(event.getFrame());
    if (status != null && status == CacheStatus.STARTED_CACHING) {
      if (log.isLoggable(Level.FINE)) {
        log.fine("finished caching for frame " + event.getFrame().getFrameID());
      }
      cacheStatus.put(event.getFrame(), CacheStatus.COMPLETED_CACHING);
    }
  }

  private void handleFrameValue(FrameEvaluationEvent event) {
    Frame frame = event.getFrame();
    Slot slot = event.getSlot();
    Facet facet = event.getFacet();
    List  values = new ArrayList(event.getValues());
    boolean isTemplate = event.isTemplate();
    addCachedEntry(frame, slot, facet, isTemplate, values);
  }



    private void addCachedEntry(Frame frame, 
                                Slot slot,
                                Facet facet,
                                boolean isTemplate,
                                List values) {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Client Received value for frame " + frame.getFrameID() + 
                  " slot " + slot.getFrameID() + " facet " + 
                  (facet == null ? "null" : "" + facet.getFrameID()) + 
                  " is template " + isTemplate);
      }
      Map<Sft, List> slotValueMap = cache.get(frame);
      if (slotValueMap == null) {
        slotValueMap = new HashMap<Sft, List>();
        cache.put(frame, slotValueMap);
      }
      Sft lookupSft = new Sft(slot, facet, isTemplate);
      slotValueMap.put(lookupSft, values);
      if (facet == null &&
          !isTemplate &&
          slot.equals(nameSlot)) {
        if (values != null && !values.isEmpty()) {
          if (log.isLoggable(Level.FINE)) {
            log.fine("frame " + frame.getFrameID() + " has name " + values.get(0));
          }
          frameNameToFrameMap.put((String) values.get(0), frame);
        }
      }
    }


    private void invalidateCachedEntry(Frame frame,
                                       Slot slot,
                                       Facet facet,
                                       boolean isTemplate,
                                       boolean remove) {
      if (log.isLoggable(Level.FINE)) {
        log.fine("making invalid cache entry for frame " +
                 frame.getFrameID() + " slot " + slot.getFrameID() +
                 " facet " + (facet == null ? "null" : facet.toString()) +
                 " isTemplate " + isTemplate + " remove flag " + remove);
      }
      synchronized (cache) {
        Map<Sft, List> slotValueMap = cache.get(frame);
        CacheStatus status = cacheStatus.get(frame);
        if (slotValueMap != null) {
          Sft lookupSft = new Sft(slot, facet, isTemplate);

          if (facet == null && 
              !isTemplate && 
              slot.equals(getSystemFrames().getNameSlot())) {
            List values = slotValueMap.get(lookupSft);
            if (values != null && !values.isEmpty()) {
              String name = (String) values.get(0);
              frameNameToFrameMap.remove(name);
            }
          }
          if (log.isLoggable(Level.FINE)) {
            log.fine("Status = " + status);
          }
          if (!remove && status != null) {
            slotValueMap.put(lookupSft, null);
          } else {
            slotValueMap.remove(lookupSft);
          }
        }
      }
    }

  /*
   * This call assumes that the eventLock is held by the
   * caller.
   */
  private void processValueUpdate(ValueUpdate vu) {
    List received = processEvents(vu.getEvents());
    events.addAll(received);
  }
}
