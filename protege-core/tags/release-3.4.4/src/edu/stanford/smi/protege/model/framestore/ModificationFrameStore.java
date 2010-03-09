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
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public abstract class ModificationFrameStore extends AbstractFrameStore {

    public Frame getFrame(FrameID id) {
        return getDelegate().getFrame(id);
    }

    public Frame getFrame(String name) {
        return getDelegate().getFrame(name);
    }

    public String getFrameName(Frame frame) {
        return getDelegate().getFrameName(frame);
    }

    public int getFrameCount() {
        return getDelegate().getFrameCount();
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

    public Set<Frame> getFrames() {
        return getDelegate().getFrames();
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

    public List getDirectTypes(Instance instance) {
        return getDelegate().getDirectTypes(instance);
    }

    public List<Instance> getDirectInstances(Cls cls) {
        return getDelegate().getDirectInstances(cls);
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

    public Set<Instance> getInstances(Cls cls) {
        return getDelegate().getInstances(cls);
    }

    public Set getTypes(Instance instance) {
        return getDelegate().getTypes(instance);
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot) {
        return getDelegate().getOwnSlotValues(frame, slot);
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
    }

    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        return getDelegate().getDirectOwnSlotValues(frame, slot);
    }

    public Set<Slot> getOwnSlots(Frame frame) {
        return getDelegate().getOwnSlots(frame);
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getDelegate().getTemplateSlotValues(cls, slot);
    }

    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        return getDelegate().getDirectTemplateSlotValues(cls, slot);
    }

    public Set getTemplateSlots(Cls cls) {
        return getDelegate().getTemplateSlots(cls);
    }

    public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getTemplateFacets(cls, slot);
    }

    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
    }

    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
    }

    public Set<Reference> getReferences(Object value) {
        return getDelegate().getReferences(value);
    }

    public Set<Cls> getClsesWithMatchingBrowserText(String value, Collection superclasses, int maxMatches) {
        return getDelegate().getClsesWithMatchingBrowserText(value, superclasses, maxMatches);
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        return getDelegate().getMatchingReferences(value, maxMatches);
    }

    public List<AbstractEvent> getEvents() {
        return getDelegate().getEvents();
    }

    public void executeQuery(Query query, QueryCallback callback) {
        getDelegate().executeQuery(query, callback);
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

    public Set getOwnFacets(Frame frame, Slot slot) {
        return getDelegate().getOwnFacets(frame, slot);
    }

    public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        return getDelegate().getOwnFacetValues(frame, slot, facet);
    }

    public Set getOverriddenTemplateSlots(Cls cls) {
        return getDelegate().getOverriddenTemplateSlots(cls);
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getOverriddenTemplateFacets(cls, slot);
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getDirectlyOverriddenTemplateFacets(cls, slot);
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        return getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
    }

    public TransactionMonitor getTransactionStatusMonitor()  {
      return getDelegate().getTransactionStatusMonitor();
    }
}
