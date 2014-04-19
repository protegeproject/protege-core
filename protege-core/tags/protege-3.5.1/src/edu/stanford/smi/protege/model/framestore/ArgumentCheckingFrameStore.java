package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class ArgumentCheckingFrameStore extends AbstractFrameStore {

    private static void assertNotNull(String text, Object o) {
        if (o == null) {
            throw new IllegalArgumentException("null " + text);
        }
    }

    private static void fail(String text) {
        throw new IllegalArgumentException(text);
    }

    private static void checkFrame(Frame frame) {
        assertNotNull("frame", frame);
    }

    private static void checkCls(Cls cls) {
        assertNotNull("class", cls);
    }

    private static void checkSlot(Slot slot) {
        assertNotNull("slot", slot);
    }

    private static void checkFacet(Facet facet) {
        assertNotNull("facet", facet);
    }

    private static void checkInstance(Instance instance) {
        assertNotNull("instance", instance);
    }

    private static void checkString(String string) {
        assertNotNull("string", string);
    }

    private static void checkSimpleInstance(SimpleInstance simpleInstance) {
        assertNotNull("simple instance", simpleInstance);
    }

    private static void checkClses(Collection c) {
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof Cls)) {
                fail("not a class: " + o);
            }
        }
    }

    private static void checkSlots(Collection c) {
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof Slot)) {
                fail("not a slot: " + o);
            }
        }
    }

    private static void checkValue(Object o) {
        assertNotNull("value", o);
        if (!isValidType(o)) {
            fail("invalid type: " + o);
        }
    }

    private static boolean isValidType(Object o) {
        return o instanceof String || o instanceof Frame || o instanceof Boolean || o instanceof Integer
                || o instanceof Float;
    }

    private static void checkValues(Collection c) {
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            checkValue(o);
        }
    }

    public Frame getFrame(FrameID id) {
        return getDelegate().getFrame(id);
    }

    public Frame getFrame(String name) {
        checkString(name);
        return getDelegate().getFrame(name);
    }

    public String getFrameName(Frame frame) {
        checkFrame(frame);
        return getDelegate().getFrameName(frame);
    }

    public Cls createCls(FrameID id, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues) {
        checkClses(directTypes);
        checkClses(directSuperclasses);
        return getDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues);
    }

    public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues) {
        checkClses(directTypes);
        checkSlots(directSuperslots);
        return getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaultValues);
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        checkClses(directTypes);
        return getDelegate().createFacet(id, directTypes, loadDefaultValues);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes,
            boolean loadDefaultValues) {
        checkClses(directTypes);
        return getDelegate().createSimpleInstance(id, directTypes, loadDefaultValues);
    }

    public void deleteCls(Cls cls) {
        checkCls(cls);
        getDelegate().deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        checkSlot(slot);
        getDelegate().deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        checkFacet(facet);
        getDelegate().deleteFacet(facet);

    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        checkSimpleInstance(simpleInstance);
        getDelegate().deleteSimpleInstance(simpleInstance);
    }

    public Set<Slot> getOwnSlots(Frame frame) {
        checkFrame(frame);
        return getDelegate().getOwnSlots(frame);
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot) {
        checkFrame(frame);
        checkSlot(slot);
        return getDelegate().getOwnSlotValues(frame, slot);
    }

    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        checkFrame(frame);
        checkSlot(slot);
        return getDelegate().getDirectOwnSlotValues(frame, slot);
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        checkFrame(frame);
        checkSlot(slot);
        return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom, int indexTo) {
        checkFrame(frame);
        checkSlot(slot);
        getDelegate().moveDirectOwnSlotValue(frame, slot, indexFrom, indexTo);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        checkFrame(frame);
        checkSlot(slot);
        checkValues(values);
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }

    public Set getOwnFacets(Frame frame, Slot slot) {
        checkFrame(frame);
        checkSlot(slot);
        return getDelegate().getOwnFacets(frame, slot);
    }

    public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        checkFrame(frame);
        checkSlot(slot);
        checkFacet(facet);
        return getDelegate().getOwnFacetValues(frame, slot, facet);
    }

    public Set getTemplateSlots(Cls cls) {
        checkCls(cls);
        return getDelegate().getTemplateSlots(cls);
    }

    public List getDirectTemplateSlots(Cls cls) {
        checkCls(cls);
        return getDelegate().getDirectTemplateSlots(cls);
    }

    public List getDirectDomain(Slot slot) {
        checkSlot(slot);
        return getDelegate().getDirectDomain(slot);
    }

    public Set getDomain(Slot slot) {
        checkSlot(slot);
        return getDelegate().getDomain(slot);
    }

    public Set getOverriddenTemplateSlots(Cls cls) {
        checkCls(cls);
        return getDelegate().getOverriddenTemplateSlots(cls);
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        checkCls(cls);
        return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        getDelegate().addDirectTemplateSlot(cls, slot);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        getDelegate().removeDirectTemplateSlot(cls, slot);
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        checkCls(cls);
        checkSlot(slot);
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        return getDelegate().getTemplateSlotValues(cls, slot);
    }

    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        return getDelegate().getDirectTemplateSlotValues(cls, slot);
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        checkCls(cls);
        checkSlot(slot);
        checkValues(values);
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        return getDelegate().getTemplateFacets(cls, slot);
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        return getDelegate().getDirectlyOverriddenTemplateFacets(cls, slot);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        checkCls(cls);
        checkSlot(slot);
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
    }

    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        checkCls(cls);
        checkSlot(slot);
        checkFacet(facet);
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
    }

    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        checkCls(cls);
        checkSlot(slot);
        checkFacet(facet);
        return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        checkCls(cls);
        checkSlot(slot);
        checkFacet(facet);
        checkValues(values);
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    public List<Cls> getDirectSuperclasses(Cls cls) {
        checkCls(cls);
        return getDelegate().getDirectSuperclasses(cls);
    }

    public Set getSuperclasses(Cls cls) {
        checkCls(cls);
        return getDelegate().getSuperclasses(cls);
    }

    public List<Cls> getDirectSubclasses(Cls cls) {
        checkCls(cls);
        return getDelegate().getDirectSubclasses(cls);
    }

    public Set<Cls> getSubclasses(Cls cls) {
        checkCls(cls);
        return getDelegate().getSubclasses(cls);
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        checkCls(cls);
        checkCls(superclass);
        getDelegate().addDirectSuperclass(cls, superclass);
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        checkCls(cls);
        checkCls(superclass);
        getDelegate().removeDirectSuperclass(cls, superclass);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        checkCls(cls);
        checkCls(subclass);
        getDelegate().moveDirectSubclass(cls, subclass, index);
    }

    public List getDirectSuperslots(Slot slot) {
        checkSlot(slot);
        return getDelegate().getDirectSuperslots(slot);
    }

    public Set getSuperslots(Slot slot) {
        checkSlot(slot);
        return getDelegate().getSuperslots(slot);
    }

    public List getDirectSubslots(Slot slot) {
        checkSlot(slot);
        return getDelegate().getDirectSubslots(slot);
    }

    public Set getSubslots(Slot slot) {
        checkSlot(slot);
        return getDelegate().getSubslots(slot);
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        checkSlot(slot);
        checkSlot(superslot);
        getDelegate().addDirectSuperslot(slot, superslot);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        checkSlot(slot);
        checkSlot(superslot);
        getDelegate().removeDirectSuperslot(slot, superslot);
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        checkSlot(slot);
        checkSlot(subslot);
        getDelegate().moveDirectSubslot(slot, subslot, index);
    }

    public List getDirectTypes(Instance instance) {
        checkInstance(instance);
        return getDelegate().getDirectTypes(instance);
    }

    public Set getTypes(Instance instance) {
        checkInstance(instance);
        return getDelegate().getTypes(instance);
    }

    public List<Instance> getDirectInstances(Cls cls) {
        checkCls(cls);
        return getDelegate().getDirectInstances(cls);
    }

    public Set<Instance> getInstances(Cls cls) {
        checkCls(cls);
        return getDelegate().getInstances(cls);
    }

    public void addDirectType(Instance instance, Cls type) {
        checkInstance(instance);
        checkCls(type);
        getDelegate().addDirectType(instance, type);
    }

    public void removeDirectType(Instance instance, Cls type) {
        checkInstance(instance);
        checkCls(type);
        getDelegate().removeDirectType(instance, type);
    }

    public void moveDirectType(Instance instance, Cls type, int index) {
        checkInstance(instance);
        checkCls(type);
        getDelegate().moveDirectType(instance, type, index);
    }

    public void executeQuery(Query query, QueryCallback callback) {
        getDelegate().executeQuery(query, callback);
    }

    public Set<Reference> getReferences(Object object) {
        checkValue(object);
        return getDelegate().getReferences(object);
    }

    public Set<Reference> getMatchingReferences(String string, int maxMatches) {
        checkString(string);
        return getDelegate().getMatchingReferences(string, maxMatches);
    }

    public Set<Cls> getClsesWithMatchingBrowserText(String text, Collection superclasses, int maxMatches) {
        checkString(text);
        checkClses(superclasses);
        return getDelegate().getClsesWithMatchingBrowserText(text, superclasses, maxMatches);
    }

    public Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        checkSlot(slot);
        checkValue(value);
        return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
    }

    public Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        checkSlot(slot);
        return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
    }

    public Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        checkSlot(slot);
        checkValue(value);
        return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        checkSlot(slot);
        checkValue(value);
        return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
    }

    public Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        checkSlot(slot);
        return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        checkSlot(slot);
        checkString(value);
        return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        checkSlot(slot);
        checkFacet(facet);
        checkValue(value);
        return getDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value);
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        checkSlot(slot);
        checkFacet(facet);
        checkString(value);
        return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        checkFrame(frame);
        checkSlot(slot);
        return getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
    }

    public boolean beginTransaction(String name) {
        checkString(name);
        return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        return getDelegate().commitTransaction();
    }

    public boolean rollbackTransaction() {
        return getDelegate().rollbackTransaction();
    }

    public TransactionMonitor getTransactionStatusMonitor() {
      return getDelegate().getTransactionStatusMonitor();
    }
   

    public void reinitialize() {
    }

    public int getClsCount() {
        return getDelegate().getClsCount();
    }

    public int getSlotCount() {
        return getDelegate().getSlotCount();
    }

    public int getFacetCount() {
        return getDelegate().getFacetCount();
    }

    public int getSimpleInstanceCount() {
        return getDelegate().getSimpleInstanceCount();
    }

    public int getFrameCount() {
        return getDelegate().getFrameCount();
    }

    public Set<Cls> getClses() {
        return getDelegate().getClses();
    }

    public Set<Slot> getSlots() {
        return getDelegate().getSlots();
    }

    public Set<Facet> getFacets() {
        return getDelegate().getFacets();
    }

    public Set<Frame> getFrames() {
        return getDelegate().getFrames();
    }

    public List<AbstractEvent> getEvents() {
        return getDelegate().getEvents();
    }

    public void replaceFrame(Frame original, Frame replacement) {
      checkFrame(original);
      checkFrame(replacement);
      getDelegate().replaceFrame(original, replacement);
    }
    
}
