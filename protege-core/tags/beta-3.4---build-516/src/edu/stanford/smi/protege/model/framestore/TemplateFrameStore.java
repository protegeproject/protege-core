package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
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

public final class TemplateFrameStore extends AbstractFrameStore {

    public Frame getFrame(FrameID id) {
        return getDelegate().getFrame(id);
    }

    public Frame getFrame(String name) {
        return getDelegate().getFrame(name);
    }

    public String getFrameName(Frame frame) {
        return getDelegate().getFrameName(frame);
    }

    public Cls createCls(FrameID id, Collection directTypes, Collection directSuperclasses, boolean loadDefaultValues) {
        return getDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues);
    }

    public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues) {
        return getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaultValues);
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        return getDelegate().createFacet(id, directTypes, loadDefaultValues);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        return getDelegate().createSimpleInstance(id, directTypes, loadDefaultValues);
    }

    public void deleteCls(Cls cls) {
        getDelegate().deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        getDelegate().deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        getDelegate().deleteFacet(facet);

    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        getDelegate().deleteSimpleInstance(simpleInstance);
    }

    public Set<Slot> getOwnSlots(Frame frame) {
        return getDelegate().getOwnSlots(frame);
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot) {
        return getDelegate().getOwnSlotValues(frame, slot);
    }

    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        return getDelegate().getDirectOwnSlotValues(frame, slot);
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom, int indexTo) {
        getDelegate().moveDirectOwnSlotValue(frame, slot, indexFrom, indexTo);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }

    public Set getOwnFacets(Frame frame, Slot slot) {
        return getDelegate().getOwnFacets(frame, slot);
    }

    public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        return getDelegate().getOwnFacetValues(frame, slot, facet);
    }

    public Set getTemplateSlots(Cls cls) {
        return getDelegate().getTemplateSlots(cls);
    }

    public List getDirectTemplateSlots(Cls cls) {
        return getDelegate().getDirectTemplateSlots(cls);
    }

    public List getDirectDomain(Slot slot) {
        return getDelegate().getDirectDomain(slot);
    }

    public Set getDomain(Slot slot) {
        return getDelegate().getDomain(slot);
    }

    public Set getOverriddenTemplateSlots(Cls cls) {
        return getDelegate().getOverriddenTemplateSlots(cls);
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().addDirectTemplateSlot(cls, slot);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getDelegate().getTemplateSlotValues(cls, slot);
    }

    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        return getDelegate().getDirectTemplateSlotValues(cls, slot);
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getTemplateFacets(cls, slot);
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getDirectlyOverriddenTemplateFacets(cls, slot);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
    }

    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
    }

    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    public List<Cls> getDirectSuperclasses(Cls cls) {
        return getDelegate().getDirectSuperclasses(cls);
    }

    public Set getSuperclasses(Cls cls) {
        return getDelegate().getSuperclasses(cls);
    }

    public List<Cls> getDirectSubclasses(Cls cls) {
        return getDelegate().getDirectSubclasses(cls);
    }

    public Set<Cls> getSubclasses(Cls cls) {
        return getDelegate().getSubclasses(cls);
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().addDirectSuperclass(cls, superclass);
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().removeDirectSuperclass(cls, superclass);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
    }

    public List getDirectSuperslots(Slot slot) {
        return getDelegate().getDirectSuperslots(slot);
    }

    public Set getSuperslots(Slot slot) {
        return getDelegate().getSuperslots(slot);
    }

    public List getDirectSubslots(Slot slot) {
        return getDelegate().getDirectSubslots(slot);
    }

    public Set getSubslots(Slot slot) {
        return getDelegate().getSubslots(slot);
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().addDirectSuperslot(slot, superslot);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().removeDirectSuperslot(slot, superslot);
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
    }

    public List getDirectTypes(Instance instance) {
        return getDelegate().getDirectTypes(instance);
    }

    public Set getTypes(Instance instance) {
        return getDelegate().getTypes(instance);
    }

    public List<Instance> getDirectInstances(Cls cls) {
        return getDelegate().getDirectInstances(cls);
    }

    public Set<Instance> getInstances(Cls cls) {
        return getDelegate().getInstances(cls);
    }

    public void addDirectType(Instance instance, Cls type) {
        getDelegate().addDirectType(instance, type);
    }

    public void removeDirectType(Instance instance, Cls type) {
        getDelegate().removeDirectType(instance, type);
    }

    public void moveDirectType(Instance instance, Cls type, int index) {
        getDelegate().moveDirectType(instance, type, index);
    }

    public void executeQuery(Query query, QueryCallback callback) {
      getDelegate().executeQuery(query, callback);
    }

    public Set<Reference> getReferences(Object object) {
        return getDelegate().getReferences(object);
    }

    public Set<Cls> getClsesWithMatchingBrowserText(String text, Collection superclasses, int maxMatches) {
        return getDelegate().getClsesWithMatchingBrowserText(text, superclasses, maxMatches);
    }

    public Set<Reference> getMatchingReferences(String string, int maxMatches) {
        return getDelegate().getMatchingReferences(string, maxMatches);
    }

    public Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
    }

    public Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
    }

    public Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
    }

    public Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        return getDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value);
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        return getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
    }

    public boolean beginTransaction(String name) {
        return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        return getDelegate().commitTransaction();
    }

    public boolean rollbackTransaction() {
        return getDelegate().rollbackTransaction();
    }

    public TransactionMonitor getTransactionStatusMonitor()  {
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
      getDelegate().replaceFrame(original, replacement);
    }
}
