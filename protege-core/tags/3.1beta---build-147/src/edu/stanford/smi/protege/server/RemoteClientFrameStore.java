package edu.stanford.smi.protege.server;

import java.rmi.*;
import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

public class RemoteClientFrameStore implements FrameStore {
    private KnowledgeBase kb;
    private RemoteSession session;
    private RemoteServerFrameStore delegate;

    public String getName() {
        return getClass().getName();
    }

    public RemoteClientFrameStore(String host, String user, String password, String projectName, KnowledgeBase kb,
            boolean preloadAll) {
        try {
            RemoteServer server = (RemoteServer) Naming.lookup("//" + host + "/" + Server.getBoundName());
            String machine = SystemUtilities.getMachineName();
            session = server.openSession(user, machine, password);
            RemoteServerProject project = server.openProject(projectName, session);
            delegate = project.getDomainKbFrameStore(session);
            this.kb = kb;
            preload(preloadAll);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }

    public RemoteClientFrameStore(RemoteServerFrameStore delegate, RemoteSession session, KnowledgeBase kb,
            boolean preloadAll) {
        try {
            this.session = session;
            this.kb = kb;
            this.delegate = delegate;
            preload(preloadAll);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
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

    private RuntimeException convertException(Exception e) {
        return new RuntimeException(e);
    }

    public int getClsCount() {
        try {
            return delegate.getClsCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getSlotCount() {
        try {
            return delegate.getSlotCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getFacetCount() {
        try {
            return delegate.getFacetCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getSimpleInstanceCount() {
        try {
            return delegate.getSimpleInstanceCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public int getFrameCount() {
        try {
            return delegate.getFrameCount(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    private void localize(Object o) {
        LocalizeUtils.localize(o, kb);
    }

    public Set getClses() {
        try {
            Set clses = delegate.getClses(session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getSlots() {
        try {
            Set slots = delegate.getSlots(session);
            localize(slots);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFacets() {
        try {
            Set facets = delegate.getFacets(session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFrames() {
        try {
            Set frames = delegate.getFrames(session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Frame getFrame(FrameID id) {
        try {
            Frame frame = delegate.getFrame(id, session);
            localize(frame);
            return frame;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Frame getFrame(String name) {
        try {
            return getCacheFrame(name);
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
            setCachedFrameName(frame, name);
            delegate.setFrameName(frame, name, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Cls createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues) {
        try {
            Cls cls = delegate.createCls(id, name, directTypes, directSuperclasses, loadDefaultValues, session);
            localize(cls);
            addCacheOwnSlotValue(directTypes, getSystemFrames().getDirectInstancesSlot(), cls);
            addCacheOwnSlotValue(directSuperclasses, getSystemFrames().getDirectSubclassesSlot(), cls);
            return cls;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Slot createSlot(FrameID id, String name, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues) {
        try {
            Slot slot = delegate.createSlot(id, name, directTypes, directSuperslots, loadDefaultValues, session);
            localize(slot);
            addCacheOwnSlotValue(directTypes, getSystemFrames().getDirectInstancesSlot(), slot);
            addCacheOwnSlotValue(directSuperslots, getSystemFrames().getDirectSubslotsSlot(), slot);
            return slot;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaultValues) {
        try {
            Facet facet = delegate.createFacet(id, name, directTypes, loadDefaultValues, session);
            localize(facet);
            addCacheOwnSlotValue(directTypes, getSystemFrames().getDirectInstancesSlot(), facet);
            return facet;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes,
            boolean loadDefaultValues) {
        try {
            SimpleInstance simpleInstance = delegate.createSimpleInstance(id, name, directTypes, loadDefaultValues,
                    session);
            localize(simpleInstance);
            addCacheOwnSlotValue(directTypes, getSystemFrames().getDirectInstancesSlot(), simpleInstance);
            return simpleInstance;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteCls(Cls cls) {
        try {
            removeCacheFrame(cls);
            delegate.deleteCls(cls, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteSlot(Slot slot) {
        try {
            removeCacheFrame(slot);
            delegate.deleteSlot(slot, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteFacet(Facet facet) {
        try {
            removeCacheFrame(facet);
            delegate.deleteFacet(facet, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        try {
            removeCacheFrame(simpleInstance);
            delegate.deleteSimpleInstance(simpleInstance, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getOwnSlots(Frame frame) {
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

    //  public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
    //  int count;
    //  try {
    //      if (isCached(frame)) {
    //          count = getCacheDirectOwnSlotValues(frame, slot).size();
    //      } else {
    //          count = delegate.getDirectOwnSlotValuesCount(frame, slot, session);
    //      }
    //  } catch (RemoteException e) {
    //      throw convertException(e);
    //  }
    //  return count;
    //}

    //    private boolean isCached(Frame frame) {
    //        return cache.get(frame) != null;
    //    }
    //

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        try {
            return getCacheDirectOwnSlotValues(frame, slot).size();
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        try {
            moveCacheValue(frame, slot, null, false, from, to);
            delegate.moveDirectOwnSlotValue(frame, slot, from, to, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        try {
            updateCacheInverseSlotValues(frame, slot, values);
            setCacheOwnSlotValues(frame, slot, values);
            delegate.setDirectOwnSlotValues(frame, slot, values, session);

        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    private void updateCacheInverseSlotValues(Frame source, Slot sourceSlot, Collection newValues)
            throws RemoteException {
        Slot targetSlot = sourceSlot.getInverseSlot();
        if (targetSlot != null) {
            Iterator i = new ArrayList(getDirectOwnSlotValues(source, sourceSlot)).iterator();
            while (i.hasNext()) {
                Object value = i.next();
                if (value instanceof Frame) {
                    Frame target = (Frame) value;
                    removeCacheOwnSlotValue(target, targetSlot, source);
                }
            }

            boolean targetSlotIsSingle = !targetSlot.getAllowsMultipleValues();
            Iterator j = newValues.iterator();
            while (j.hasNext()) {
                Object value = j.next();
                if (value instanceof Frame) {
                    Frame target = (Frame) value;
                    if (targetSlotIsSingle) {
                        Object formerValue = getCacheDirectOwnSlotValue(target, targetSlot);
                        if (formerValue instanceof Frame) {
                            Frame formerSource = (Frame) value;
                            removeCacheOwnSlotValue(formerSource, sourceSlot, target);
                        }
                    }
                    addCacheOwnSlotValue(target, targetSlot, source);
                }
            }
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
            Set slots = delegate.getOverriddenTemplateSlots(cls, session);
            localize(slots);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        try {
            Set slots = delegate.getDirectlyOverriddenTemplateSlots(cls, session);
            localize(slots);
            return slots;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        try {
            addCacheOwnSlotValue(cls, getSystemFrames().getDirectTemplateSlotsSlot(), slot);
            addCacheOwnSlotValue(slot, getSystemFrames().getDirectDomainSlot(), cls);
            delegate.addDirectTemplateSlot(cls, slot, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        try {
            removeCacheOwnSlotValue(cls, getSystemFrames().getDirectTemplateSlotsSlot(), slot);
            removeCacheOwnSlotValue(slot, getSystemFrames().getDirectDomainSlot(), cls);
            delegate.removeDirectTemplateSlot(cls, slot, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        try {
            moveCacheOwnSlotValue(cls, getSystemFrames().getDirectTemplateSlotsSlot(), slot, index);
            delegate.moveDirectTemplateSlot(cls, slot, index, session);
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
            setCacheValues(cls, slot, getSystemFrames().getValuesFacet(), true, values);
            delegate.setDirectTemplateSlotValues(cls, slot, values, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = delegate.getTemplateFacets(cls, slot, session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = delegate.getOverriddenTemplateFacets(cls, slot, session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        try {
            Set facets = delegate.getDirectlyOverriddenTemplateFacets(cls, slot, session);
            localize(facets);
            return facets;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        try {
            delegate.removeDirectTemplateFacetOverrides(cls, slot, session);
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
            setCacheValues(cls, slot, facet, true, values);
            delegate.setDirectTemplateFacetValues(cls, slot, facet, values, session);
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

    public List getDirectSubclasses(Cls cls) {
        try {
            return getCacheDirectOwnSlotValues(cls, getSystemFrames().getDirectSubclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getSubclasses(Cls cls) {
        try {
            return getCacheOwnSlotValueClosure(cls, getSystemFrames().getDirectSubclassesSlot());
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        try {
            addCacheOwnSlotValue(superclass, getSystemFrames().getDirectSubclassesSlot(), cls);
            addCacheOwnSlotValue(cls, getSystemFrames().getDirectSuperclassesSlot(), superclass);
            delegate.addDirectSuperclass(cls, superclass, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        try {
            removeCacheOwnSlotValue(superclass, getSystemFrames().getDirectSubclassesSlot(), cls);
            removeCacheOwnSlotValue(cls, getSystemFrames().getDirectSuperclassesSlot(), superclass);
            delegate.removeDirectSuperclass(cls, superclass, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        try {
            moveCacheOwnSlotValue(cls, getSystemFrames().getDirectSubclassesSlot(), subclass, index);
            delegate.moveDirectSubclass(cls, subclass, index, session);
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
            addCacheOwnSlotValue(superslot, getSystemFrames().getDirectSubslotsSlot(), slot);
            addCacheOwnSlotValue(slot, getSystemFrames().getDirectSuperslotsSlot(), superslot);
            delegate.addDirectSuperslot(slot, superslot, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        try {
            removeCacheOwnSlotValue(superslot, getSystemFrames().getDirectSubslotsSlot(), slot);
            removeCacheOwnSlotValue(slot, getSystemFrames().getDirectSuperslotsSlot(), superslot);
            delegate.removeDirectSuperslot(slot, superslot, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        try {
            moveCacheOwnSlotValue(slot, getSystemFrames().getDirectSubslotsSlot(), subslot, index);
            delegate.moveDirectSubslot(slot, subslot, index, session);
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
            Set instances = delegate.getInstances(cls, session);
            localize(instances);
            return instances;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void addDirectType(Instance instance, Cls type) {
        try {
            addCacheOwnSlotValue(instance, getSystemFrames().getDirectTypesSlot(), type);
            addCacheOwnSlotValue(type, getSystemFrames().getDirectInstancesSlot(), instance);
            delegate.addDirectType(instance, type, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void removeDirectType(Instance instance, Cls type) {
        try {
            removeCacheOwnSlotValue(instance, getSystemFrames().getDirectTypesSlot(), type);
            removeCacheOwnSlotValue(type, getSystemFrames().getDirectInstancesSlot(), instance);
            delegate.removeDirectType(instance, type, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public List getEvents() {
        try {
            List events = delegate.getEvents(session);
            localize(events);
            processEvents(events);
            return events;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set executeQuery(Query query) {
        try {
            Set frames = delegate.executeQuery(query, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getReferences(Object object) {
        try {
            Set references = delegate.getReferences(object, session);
            localize(references);
            return references;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getMatchingReferences(String string, int maxMatches) {
        try {
            Set references = delegate.getMatchingReferences(string, maxMatches, session);
            localize(references);
            return references;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        try {
            Set frames = delegate.getFramesWithDirectOwnSlotValue(slot, value, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        try {
            Set frames = delegate.getFramesWithAnyDirectOwnSlotValue(slot, session);
            localize(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        try {
            Set frames = delegate.getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches, session);
            localize(frames);
            loadReturnedValues(frames);
            return frames;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        try {
            Set clses = delegate.getClsesWithDirectTemplateSlotValue(slot, value, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        try {
            Set clses = delegate.getClsesWithAnyDirectTemplateSlotValue(slot, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        try {
            Set clses = delegate.getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        try {
            Set clses = delegate.getClsesWithDirectTemplateFacetValue(slot, facet, value, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        try {
            Set clses = delegate.getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches, session);
            localize(clses);
            return clses;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        try {
            Set values = delegate.getDirectOwnSlotValuesClosure(frame, slot, session);
            localize(values);
            return values;
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public boolean beginTransaction(String name) {
        try {
            return delegate.beginTransaction(name, session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public boolean commitTransaction() {
        try {
            return delegate.commitTransaction(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public boolean rollbackTransaction() {
        try {
            return delegate.rollbackTransaction(session);
        } catch (RemoteException e) {
            throw convertException(e);
        }
    }

    public void close() {
        delegate = null;
    }

    //------------------------------
    public void preload(boolean preloadAll) throws RemoteException {
        Map fsftValues = delegate.preload(preloadAll, session);
        Log.getLogger().info("Preloaded with " + fsftValues.size() + " frames");
        insertValues(fsftValues);
    }

    private Map frameNameToFrameMap = new HashMap();
    private Map cache = new HashMap();
    private Sft lookupSft = new Sft();

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
            // Log.trace("not in cache: " + frame, this, "getCacheClosure");
            closure = delegate.getDirectOwnSlotValuesClosure(frame, slot, session);
            localize(closure);
            loadReturnedValues(closure);
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
        return cache.containsKey(frame);
    }

    private boolean calculateClosureFromCacheOnly(Frame frame, Slot slot, Facet facet, boolean isTemplate, Set closure)
            throws RemoteException {
        boolean inCache = isCached(frame);
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

    private List getCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
        Map sftValues = (Map) cache.get(frame);
        if (sftValues == null) {
            sftValues = loadValues(frame);
        }
        lookupSft.set(slot, facet, isTemplate);
        List values = (List) sftValues.get(lookupSft);
        if (values == null && sftValues.containsKey(lookupSft)) {
            // Log.trace("getting missing values for: " + frame, this,
            // "getCacheValues");
            values = delegate.getDirectOwnSlotValues(frame, slot, session);
            localize(values);
            sftValues.put(new Sft(slot, facet, isTemplate), new ArrayList(values));
        }
        if (values == null) {
            values = Collections.EMPTY_LIST;
        }
        return values;
    }

    private Map loadValues(Frame frame) throws RemoteException {
        loadValues(Collections.singleton(frame));
        return (Map) cache.get(frame);
    }

    private void loadValues(Collection frames) throws RemoteException {
        Map fsftValues = delegate.getFrameValues(frames, session);
        insertValues(fsftValues);
    }

    private void insertValues(Map fsftValues) {
        localize(fsftValues);
        Iterator i = fsftValues.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Frame frame = (Frame) entry.getKey();
            Map sftValues = (Map) entry.getValue();
            cache.put(frame, sftValues);
            String name = getFrameName(frame);
            frameNameToFrameMap.put(name, frame);
            // System.out.println(fsftValues.size() + " " + name + " -
            // insertValues size: " + sftValues.size());
        }
    }

    private void addCacheOwnSlotValue(Frame frame, Slot slot, Object value) {
        addCacheValue(frame, slot, null, false, value);
    }

    private void addCacheOwnSlotValue(Collection frames, Slot slot, Object value) {
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            addCacheOwnSlotValue(frame, slot, value);
        }
    }

    private void removeCacheOwnSlotValue(Frame frame, Slot slot, Object value) {
        removeCacheValue(frame, slot, null, false, value);
    }

    private void setCacheOwnSlotValues(Frame frame, Slot slot, Collection values) {
        setCacheValues(frame, slot, null, false, values);
    }

    private void addCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Map sftValues = (Map) cache.get(frame);
        if (sftValues != null) {
            lookupSft.set(slot, facet, isTemplate);
            boolean containsKey = sftValues.containsKey(lookupSft);
            List values = (List) sftValues.get(lookupSft);
            if (!(containsKey && values == null)) {
                if (values == null) {
                    values = new ArrayList();
                    sftValues.put(new Sft(slot, facet, isTemplate), values);
                }
                values.add(value);
            }
        }
    }

    private void moveCacheOwnSlotValue(Frame frame, Slot slot, Object o, int index) {
        moveCacheValue(frame, slot, null, false, o, index);
    }

    private void moveCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value, int index) {
        Map sftValues = (Map) cache.get(frame);
        if (sftValues != null) {
            lookupSft.set(slot, facet, isTemplate);
            List values = (List) sftValues.get(lookupSft);
            if (values != null) {
                values.remove(value);
                values.add(index, value);
            }
        }
    }

    private void moveCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        Map sftValues = (Map) cache.get(frame);
        if (sftValues != null) {
            lookupSft.set(slot, facet, isTemplate);
            List values = (List) sftValues.get(lookupSft);
            if (values != null) {
                Object value = values.remove(from);
                values.add(to, value);
            }
        }
    }

    private void setCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        setCacheValues(frame, slot, facet, isTemplate, Collections.singleton(value));
    }

    private void setCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Map sftValues = (Map) cache.get(frame);
        if (sftValues != null) {
            sftValues.put(new Sft(slot, facet, isTemplate), new ArrayList(values));
        }
    }

    private void removeCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Map sftValues = (Map) cache.get(frame);
        if (sftValues != null) {
            lookupSft.set(slot, facet, isTemplate);
            List values = (List) sftValues.get(lookupSft);
            if (values != null) {
                values.remove(value);
            }
        }
    }

    // we handle the case where the user looks up names that don't correspond to
    // a frame
    private Frame getCacheFrame(String name) throws RemoteException {
        Frame frame = (Frame) frameNameToFrameMap.get(name);
        if (frame == null) {
            if (!frameNameToFrameMap.containsKey(name)) {
                frame = delegate.getFrame(name, session);
                if (frame != null) {
                    localize(frame);
                }
            }
            frameNameToFrameMap.put(name, frame);
        }
        return frame;
    }

    private void removeCacheFrame(Frame frame) {
        String name = getFrameName(frame);
        cache.remove(frame);
        frameNameToFrameMap.remove(name);
    }

    private void setCachedFrameName(Frame frame, String newName) {
        String oldName = getFrameName(frame);
        frameNameToFrameMap.remove(oldName);
        frameNameToFrameMap.put(newName, frame);
        setCacheValue(frame, getSystemFrames().getNameSlot(), null, false, newName);
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

    private Collection resolveValues(Collection values, Collection newValues, Facet facet) {
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

    private void processEvents(Collection events) throws RemoteException {
        Iterator i = events.iterator();
        while (i.hasNext()) {
            Object event = i.next();
            if (event instanceof FrameEvent) {
                FrameEvent frameEvent = (FrameEvent) event;
                if (frameEvent.getEventType() == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
                    Frame frame = frameEvent.getFrame();
                    Slot slot = frameEvent.getSlot();
                    if (isOwnSlotValueCached(frame, slot)) {
                        List newValues = delegate.getDirectOwnSlotValues(frame, slot, session);
                        localize(newValues);
                        setCacheOwnSlotValues(frame, slot, newValues);
                    }
                }
            }
        }
    }

    private boolean isOwnSlotValueCached(Frame frame, Slot slot) {
        boolean isCached = false;
        Map map = (Map) cache.get(frame);
        if (map != null) {
            lookupSft.set(slot, null, false);
            if (map.containsKey(lookupSft)) {
                isCached = map.get(lookupSft) != null;
            }
        }
        return isCached;
    }

    private void loadReturnedValues(Collection values) throws RemoteException {
        List frames = new ArrayList();
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Frame) {
                Frame frame = (Frame) o;
                if (cache.get(frame) == null) {
                    frames.add(frame);
                }
            }
        }
        if (!frames.isEmpty()) {
            loadValues(frames);
            checkFrames(frames);
        }
    }

    private void checkFrames(Collection frames) {
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (cache.get(frame) == null) {
                Log.getLogger().warning("Missing frame: " + frame.getFrameID());
                cache.put(frame, new HashMap());
            }
        }
    }
}