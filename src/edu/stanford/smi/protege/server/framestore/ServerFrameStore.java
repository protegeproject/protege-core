package edu.stanford.smi.protege.server.framestore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.Registration;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class ServerFrameStore extends UnicastRemoteObject implements RemoteServerFrameStore {
    private static transient Logger log = Log.getLogger(ServerFrameStore.class);
  
    private FrameStore _delegate;
    private FrameStore _wrappedDelegate;
    private KnowledgeBase _kb;
    private List _events = new ArrayList();
    private Map<RemoteSession, Registration> _sessionToRegistrationMap 
      = new HashMap<RemoteSession, Registration>();
    private boolean _isDirty;
    private static final int DELAY_MSEC = Integer.getInteger("server.delay", 0).intValue();
    private static final int MAX_VALUES = 20;
    private static final int MIN_PRELOAD_FRAMES = Integer.getInteger("preload.frame.limit", 5000).intValue();
    /*
     * A performance hack Indentical copies of the same sft are reduced to the same object so that only a single copy
     * needs to be sent over the wire.
     */
    private Map<Sft,Sft> sftMap = new HashMap<Sft,Sft>();

    
    //ESCA-JAVA0160 
    public ServerFrameStore(FrameStore delegate, KnowledgeBase kb) throws RemoteException {
        _delegate = delegate;
        _kb = kb;
        kb.setDispatchEventsEnabled(false);
        // kb.setJournalingEnabled(true);
        if (DELAY_MSEC != 0) {
            //used for simulating slow network response time
            Log.getLogger().config("Simulated delay of " + DELAY_MSEC + " msec/call");
        }
    }

    private FrameStore getDelegate() {
        return _delegate;
    }

    private int nDelayedCalls = 0;

    private void delay() {
        if (DELAY_MSEC != 0) {
            SystemUtilities.sleepMsec(DELAY_MSEC);
            if (++nDelayedCalls % 10 == 0) {
                Log.getLogger().info(nDelayedCalls + " delayed calls");
            }
        }
    }

    private static RemoteSession currentSession;

    private void recordCall(RemoteSession session) {
        delay();
        currentSession = session;
    }

    public static RemoteSession getCurrentSession() {
        return currentSession;
    }

    public synchronized int getClsCount(RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsCount();
    }

    public synchronized int getSlotCount(RemoteSession session) {
        recordCall(session);
        return getDelegate().getSlotCount();
    }

    public synchronized int getFacetCount(RemoteSession session) {
        recordCall(session);
        return getDelegate().getFacetCount();
    }

    public synchronized int getSimpleInstanceCount(RemoteSession session) {
        recordCall(session);
        return getDelegate().getSimpleInstanceCount();
    }

    public synchronized int getFrameCount(RemoteSession session) {
        recordCall(session);
        return getDelegate().getFrameCount();
    }

    public synchronized List getDirectTemplateSlots(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectTemplateSlots(cls);
    }

    public synchronized void removeDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        getDelegate().removeDirectTemplateSlot(cls, slot);
        markDirty();
    }

    public synchronized void moveDirectTemplateSlot(Cls cls, Slot slot, int index, RemoteSession session) {
        recordCall(session);
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        markDirty();
    }

    public synchronized void addDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) {
        recordCall(session);
        getDelegate().addDirectSuperclass(cls, superclass);
        markDirty();
    }

    public synchronized void removeDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) {
        recordCall(session);
        getDelegate().removeDirectSuperslot(slot, superslot);
        markDirty();
    }

    public synchronized void removeDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) {
        recordCall(session);
        getDelegate().removeDirectSuperclass(cls, superclass);
        markDirty();
    }

    public synchronized void moveDirectSubclass(Cls cls, Cls subclass, int index, RemoteSession session) {
        recordCall(session);
        getDelegate().moveDirectSubclass(cls, subclass, index);
        markDirty();
    }

    public synchronized List getDirectTemplateSlotValues(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectTemplateSlotValues(cls, slot);
    }

    public synchronized Set getSuperslots(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getSuperslots(slot);
    }

    public synchronized Set getOwnSlots(Frame frame, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOwnSlots(frame);
    }

    public synchronized Set getInstances(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getInstances(cls);
    }

    public synchronized Set getFramesWithDirectOwnSlotValue(Slot slot, Object value, RemoteSession session) {
        recordCall(session);
        return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
    }

    public synchronized Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value, RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
    }

    public synchronized Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value,
            RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value);
    }

    public synchronized Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches,
            RemoteSession session) {
        recordCall(session);
        return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
    }

    public synchronized Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value,
            int maxMatches, RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
    }

    public synchronized Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches,
            RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
    }

    public synchronized List getDirectSuperclasses(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectSuperclasses(cls);
    }

    public synchronized Collection getTemplateSlotValues(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getTemplateSlotValues(cls, slot);
    }

    public synchronized List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
    }

    public synchronized Set getClses(RemoteSession session) {
        recordCall(session);
        return getDelegate().getClses();
    }

    public synchronized Set getTemplateFacets(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getTemplateFacets(cls, slot);
    }

    public synchronized Frame getFrame(String name, RemoteSession session) {
        recordCall(session);
        return getDelegate().getFrame(name);
    }

    public synchronized Frame getFrame(FrameID id, RemoteSession session) {
        recordCall(session);
        return getDelegate().getFrame(id);
    }

    public synchronized Collection getOwnSlotValues(Frame frame, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOwnSlotValues(frame, slot);
    }

    public synchronized int getDirectOwnSlotValuesCount(Frame frame, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
    }

    public synchronized List getDirectOwnSlotValues(Frame frame, Slot slot, RemoteSession session) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("getDirectOwnSlotValues for frame " + frame.getFrameID() + " slot " + slot.getFrameID());
        }
        recordCall(session);
        return getDelegate().getDirectOwnSlotValues(frame, slot);
    }

    public synchronized List getDirectInstances(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectInstances(cls);
    }

    public synchronized Set getSubclasses(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getSubclasses(cls);
    }

    public synchronized Set getSlots(RemoteSession session) {
        recordCall(session);
        return getDelegate().getSlots();
    }

    public synchronized Set getSuperclasses(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getSuperclasses(cls);
    }

    public synchronized Set getSubslots(Slot slot, RemoteSession session) {
        return getDelegate().getSubslots(slot);
    }

    public synchronized void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values,
            RemoteSession session) {
        recordCall(session);
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        markDirty();
    }

    public synchronized Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaults,
            RemoteSession session) {
        recordCall(session);
        markDirty();
        return getDelegate().createFacet(id, name, directTypes, loadDefaults);
    }

    public synchronized List getDirectSubclasses(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectSubclasses(cls);
    }

    public synchronized Set getFrames(RemoteSession session) {
        recordCall(session);
        return getDelegate().getFrames();
    }

    public synchronized void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, RemoteSession session) {
        recordCall(session);
        markDirty();
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }

    public synchronized Set getTypes(Instance instance, RemoteSession session) {
        recordCall(session);
        return getDelegate().getTypes(instance);
    }

    public synchronized Set getTemplateSlots(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getTemplateSlots(cls);
    }

    public synchronized Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) {
        recordCall(session);
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
    }

    public synchronized void deleteCls(Cls cls, RemoteSession session) {
        recordCall(session);
        getDelegate().deleteCls(cls);
        markDirty();
    }

    public synchronized void deleteSlot(Slot slot, RemoteSession session) {
        recordCall(session);
        getDelegate().deleteSlot(slot);
        markDirty();
    }

    public synchronized void deleteFacet(Facet facet, RemoteSession session) {
        recordCall(session);
        getDelegate().deleteFacet(facet);
        markDirty();
    }

    public synchronized void deleteSimpleInstance(SimpleInstance simpleInstance, RemoteSession session) {
        recordCall(session);
        getDelegate().deleteSimpleInstance(simpleInstance);
        markDirty();
    }

    public synchronized Slot createSlot(FrameID id, String name, Collection directTypes, Collection directSuperslots,
            boolean loadDefaults, RemoteSession session) {
        recordCall(session);
        markDirty();
        return getDelegate().createSlot(id, name, directTypes, directSuperslots, loadDefaults);
    }

    public synchronized List getDirectTypes(Instance instance, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectTypes(instance);
    }

    public synchronized List getDirectSubslots(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectSubslots(slot);
    }

    public synchronized void addDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) {
        recordCall(session);
        getDelegate().addDirectSuperslot(slot, superslot);
        markDirty();
    }

    public synchronized void addDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        getDelegate().addDirectTemplateSlot(cls, slot);
        markDirty();
    }

    public synchronized void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to, RemoteSession session) {
        recordCall(session);
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        markDirty();
    }

    public synchronized void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, RemoteSession session) {
        recordCall(session);
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        markDirty();
    }

    public synchronized Cls createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaults, RemoteSession session) {
        recordCall(session);
        markDirty();
        return getDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaults);
    }

    public synchronized Set getFacets(RemoteSession session) {
        recordCall(session);
        return getDelegate().getFacets();
    }

    public synchronized Set executeQuery(Query query, RemoteSession session) {
        recordCall(session);
        return getDelegate().executeQuery(query);
    }

    public synchronized void removeDirectType(Instance instance, Cls directType, RemoteSession session) {
        recordCall(session);
        getDelegate().removeDirectType(instance, directType);
        markDirty();
    }

    public synchronized Set getReferences(Object value, RemoteSession session) {
        recordCall(session);
        return getDelegate().getReferences(value);
    }

    public synchronized Set getMatchingReferences(String value, int maxMatches, RemoteSession session) {
        recordCall(session);
        return getDelegate().getMatchingReferences(value, maxMatches);
    }

    public synchronized Set getClsesWithMatchingBrowserText(String value, Collection superclasses, int maxMatches,
            RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsesWithMatchingBrowserText(value, superclasses, maxMatches);
    }

    public synchronized SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes,
            boolean loadDefaults, RemoteSession session) {
        recordCall(session);
        markDirty();
        return getDelegate().createSimpleInstance(id, name, directTypes, loadDefaults);
    }

    public synchronized void addDirectType(Instance instance, Cls type, RemoteSession session) {
        recordCall(session);
        getDelegate().addDirectType(instance, type);
        markDirty();
    }

    public synchronized void moveDirectType(Instance instance, Cls cls, int index, RemoteSession session) {
        recordCall(session);
        getDelegate().moveDirectType(instance, cls, index);
        markDirty();
    }

    public synchronized List getDirectSuperslots(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectSuperslots(slot);
    }

    public synchronized String getFrameName(Frame frame, RemoteSession session) {
        recordCall(session);
        return getDelegate().getFrameName(frame);
    }

    public synchronized void setFrameName(Frame frame, String name, RemoteSession session) {
        recordCall(session);
        getDelegate().setFrameName(frame, name);
        markDirty();
    }

    public synchronized Set getOwnFacets(Frame frame, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOwnFacets(frame, slot);
    }

    public synchronized Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOwnFacetValues(frame, slot, facet);
    }

    public synchronized Set getOverriddenTemplateSlots(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOverriddenTemplateSlots(cls);
    }

    public synchronized Set getDirectlyOverriddenTemplateSlots(Cls cls, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
    }

    public synchronized Set getOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
    }

    public synchronized Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
    }

    public synchronized void removeDirectTemplateFacetOverrides(Cls cls, Slot slot, RemoteSession session) {
        recordCall(session);
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        markDirty();
    }

    public synchronized void close(RemoteSession session) {
        recordCall(session);
        // do nothing
    }

    public synchronized void register(RemoteSession session) {
        Registration registration = new Registration(_events.size());
        _sessionToRegistrationMap.put(session, registration);
    }

    public String toString() {
        return "ServerFrameStoreImpl";
    }

    public synchronized List<EventObject> getEvents(RemoteSession session) {
        recordCall(session);
        List events;
        List newEvents = getDelegate().getEvents();
        if (session == null) {
            events = newEvents;
        } else {
            _events.addAll(newEvents);
            Registration reg = _sessionToRegistrationMap.get(session);
            if (reg == null) {
                throw new IllegalStateException("Not registered");
            }
            int lastEvent = reg.getLastEvent();
            int size = _events.size();
            reg.setLastEvent(size);
            if (lastEvent == size) {
                events = Collections.EMPTY_LIST;
            } else {
                events = new ArrayList(_events.subList(lastEvent, size));
                if (log.isLoggable(Level.FINE)) {
                  log.fine("" + session + " events=" + events);
                }
            }
        }
        // edu.stanford.smi.protege.util.Log.enter(this, "getEvents", session,
        // events);
        return events;
    }
    
    public synchronized boolean beginTransaction(String name, RemoteSession session) {
        recordCall(session);
        return getDelegate().beginTransaction(name);
    }

    public synchronized boolean commitTransaction(RemoteSession session) {
        recordCall(session);
        return getDelegate().commitTransaction();
    }

    public synchronized boolean rollbackTransaction(RemoteSession session) {
        recordCall(session);
        return getDelegate().rollbackTransaction();
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

    public synchronized List getDirectDomain(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectDomain(slot);
    }

    public synchronized Set getDomain(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDomain(slot);
    }

    public synchronized void moveDirectSubslot(Slot slot, Slot subslot, int index, RemoteSession session) {
        recordCall(session);
        getDelegate().moveDirectSubslot(slot, subslot, index);
    }

    public synchronized Set getFramesWithAnyDirectOwnSlotValue(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
    }

    public synchronized Set getClsesWithAnyDirectTemplateSlotValue(Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
    }

    public synchronized Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot, RemoteSession session) {
        recordCall(session);
        return getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
    }

    private static final int LIMIT = 100;

    // add siblings
    private Collection supplementFrames(Collection frames) {
        Collection allFrames = frames;
        if (frames.size() == 1) {
            Object frame = CollectionUtilities.getFirstItem(frames);
            if (frame instanceof Cls) {
                Cls cls = (Cls) frame;
                Cls superclass = (Cls) CollectionUtilities.getFirstItem(getDelegate().getDirectSuperclasses(cls));
                if (superclass != null) {
                    Slot directSubclassesSlot = (Slot) getDelegate().getFrame(Model.Slot.DIRECT_SUBCLASSES);
                    allFrames = new LinkedHashSet(frames);
                    if (getDelegate().getDirectOwnSlotValuesCount(superclass, directSubclassesSlot) < LIMIT) {
                        Collection subclasses = getDelegate().getDirectSubclasses(superclass);
                        allFrames.addAll(subclasses);
                    }
                }
            }
            if (allFrames.size() != 1) {
                // Log.trace("Supplemented " + frame + " with " + allFrames, this, "allFrames");
            }
        }
        return allFrames;
    }

    public synchronized Map getFrameValues(Collection frames, RemoteSession session) {
        long start = 0;
        if (log.isLoggable(Level.FINE)) {
          log.fine("calling getFrameValues");
          start = System.currentTimeMillis();
        }
        
        recordCall(session);
        Map result =  getFrameValues(frames, false);
        
        if (log.isLoggable(Level.FINE)) {
          log.fine("getFrameValues took " + (System.currentTimeMillis() - start) + " milliseconds");
        }
        
        return result;
    }

    private void localize(Collection frames) {
        LocalizeUtils.localize(frames, _kb);
    }

    private Map getFrameValues(Collection frames, boolean includeAllSlots) {
        localize(frames);
        frames = supplementFrames(frames);
        if (log.isLoggable(Level.FINER)) {
          log.fine(_kb.getName() + " getFrameValues: " + CollectionUtilities.toString(frames));
        }
        HashMap map = new LinkedHashMap();
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            long startTime = 0;
            if (log.isLoggable(Level.FINE)) {
              startTime = System.currentTimeMillis();
              log.fine("Started getting values for frame (" + frame.getFrameID() + ")");
            }
            Map sftValues = (Map) map.get(frame);
            if (sftValues == null) {
                sftValues = new LinkedHashMap();
                map.put(frame, sftValues);
            }
            insertOwnSlots(sftValues, frame, includeAllSlots);
            if (frame instanceof Cls) {
                insertTemplateValues(sftValues, (Cls) frame);
            }
            if (log.isLoggable(Level.FINE)) {
              log.fine("Got frame value in " + (System.currentTimeMillis() - startTime) + " milliseconds");
            }
        }
        return map;
    }

    private void insertOwnSlots(Map sftValues, Frame frame, boolean includeAllSlots) {
        Iterator i = getDelegate().getOwnSlots(frame).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            int count = getDelegate().getDirectOwnSlotValuesCount(frame, slot);
            if (includeAllSlots || count < MAX_VALUES) {
                Collection values = getDelegate().getDirectOwnSlotValues(frame, slot);
                insertValues(sftValues, slot, null, false, values);
            } else {
                insertNullValues(sftValues, slot, null, false);
                // Log.trace("excluded values for " + slot, this,
                // "insertOwnSlots", frame);
            }
        }
    }

    private void insertTemplateValues(Map sftValues, Cls cls) {
        Iterator i = getDelegate().getTemplateSlots(cls).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            Collection values = getDelegate().getDirectTemplateSlotValues(cls, slot);
            insertValues(sftValues, slot, null, true, values);
            insertFacets(sftValues, cls, slot);
        }
    }

    private void insertFacets(Map sftValues, Cls cls, Slot slot) {
        Iterator i = getDelegate().getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            Collection values = getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
            insertValues(sftValues, slot, facet, true, values);
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

    public synchronized Map preload(boolean all, RemoteSession session) {
        recordCall(session);
        Collection frames;
        if (all || getDelegate().getFrameCount() < MIN_PRELOAD_FRAMES) {
            frames = getDelegate().getFrames();
        } else {
            frames = new LinkedHashSet();
            frames.addAll(getDelegate().getSlots());
            frames.addAll(getDelegate().getFacets());
            addHierarchy(frames, Model.Cls.ROOT_META_CLASS);
            addHierarchy(frames, Model.Cls.CONSTRAINT);
            addHierarchy(frames, Model.Cls.ANNOTATION);
            addHierarchy(frames, Model.Cls.RELATION);
            addFrame(frames, Model.Cls.SYSTEM_CLASS);
            addRootHierarchy(frames, _kb.getRootCls());

            Project p = _kb.getProject();
            if (p != null) {
                frames.addAll(p.getClsesWithCustomizedForms());
                frames.addAll(p.getClsesWithDirectBrowserSlots());
                frames.addAll(p.getHiddenFrames());
            }
        }
        return getFrameValues(frames, all);
    }

    private void addFrame(Collection frames, String className) {
        Cls cls = (Cls) getDelegate().getFrame(className);
        frames.add(cls);
    }

    private void addHierarchy(Collection frames, String className) {
        Cls cls = (Cls) getDelegate().getFrame(className);
        frames.add(cls);
        frames.addAll(getDelegate().getSubclasses(cls));
    }

    private void addRootHierarchy(Collection frames, Cls rootCls) {
        frames.add(rootCls);
        Iterator i = getDelegate().getDirectSubclasses(rootCls).iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            if (!subclass.isSystem()) {
                frames.add(subclass);
                frames.addAll(getDelegate().getDirectSubclasses(subclass));
            }
        }
    }
}