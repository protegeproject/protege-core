package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;

public class FrameStoreAdapter extends AbstractFrameStore {

    public void reinitialize() {
        // do nothing
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
    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
    }
    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
    }
    public void addDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().addDirectSuperclass(cls, superclass);
    }
    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().removeDirectSuperslot(slot, superslot);
    }
    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().removeDirectSuperclass(cls, superclass);
    }
    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
    }
    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
    }
    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        return getDelegate().getDirectTemplateSlotValues(cls, slot);
    }
    public Set getSuperslots(Slot slot) {
        return getDelegate().getSuperslots(slot);
    }
    public Set getOwnSlots(Frame frame) {
        return getDelegate().getOwnSlots(frame);
    }
    public Set getInstances(Cls cls) {
        return getDelegate().getInstances(cls);
    }
    public Set getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
    }
    public Set getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
    }
    public Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
    }
    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        return getDelegate().getClsesWithDirectTemplateFacetValue(slot, facet, value);
    }
    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
    }
    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
    }
    public Set getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
    }
    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
    }
    public List getDirectSuperclasses(Cls cls) {
        return getDelegate().getDirectSuperclasses(cls);
    }
    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getDelegate().getTemplateSlotValues(cls, slot);
    }
    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
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

    public Set getClses() {
        return getDelegate().getClses();
    }
    public Set getTemplateFacets(Cls cls, Slot slot) {
        return getDelegate().getTemplateFacets(cls, slot);
    }
    public Frame getFrame(String name) {
        return getDelegate().getFrame(name);
    }

    public Frame getFrame(FrameID id) {
        return getDelegate().getFrame(id);
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
    public List getDirectInstances(Cls cls) {
        return getDelegate().getDirectInstances(cls);
    }
    public Set getSubclasses(Cls cls) {
        return getDelegate().getSubclasses(cls);
    }
    public Set getSlots() {
        return getDelegate().getSlots();
    }
    public Set getSuperclasses(Cls cls) {
        return getDelegate().getSuperclasses(cls);
    }
    public Set getSubslots(Slot slot) {
        return getDelegate().getSubslots(slot);
    }
    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
    }
    public Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaults) {
        return getDelegate().createFacet(id, name, directTypes, loadDefaults);
    }
    public List getDirectSubclasses(Cls cls) {
        return getDelegate().getDirectSubclasses(cls);
    }
    public Set getFrames() {
        return getDelegate().getFrames();
    }
    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }
    public Set getTypes(Instance instance) {
        return getDelegate().getTypes(instance);
    }
    public Set getTemplateSlots(Cls cls) {
        return getDelegate().getTemplateSlots(cls);
    }
    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getDelegate().getTemplateFacetValues(cls, slot, facet);
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
    public Slot createSlot(
        FrameID id,
        String name,
        Collection directTypes,
        Collection directSuperslots,
        boolean loadDefaults) {
        return getDelegate().createSlot(id, name, directTypes, directSuperslots, loadDefaults);
    }
    public List getDirectTypes(Instance instance) {
        return getDelegate().getDirectTypes(instance);
    }
    public List getDirectSubslots(Slot slot) {
        return getDelegate().getDirectSubslots(slot);
    }
    public void addDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().addDirectSuperslot(slot, superslot);
    }
    public List getEvents() {
        return getDelegate().getEvents();
    }
    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().addDirectTemplateSlot(cls, slot);
    }
    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }
    public Cls createCls(
        FrameID id,
        String name,
        Collection directTypes,
        Collection directSuperclasses,
        boolean loadDefaults) {
        return getDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaults);
    }
    public Set getFacets() {
        return getDelegate().getFacets();
    }
    public Set executeQuery(Query query) {
        return getDelegate().executeQuery(query);
    }
    public void removeDirectType(Instance instance, Cls directType) {
        getDelegate().removeDirectType(instance, directType);
    }
    public Set getReferences(Object value) {
        return getDelegate().getReferences(value);
    }
    public Set getMatchingReferences(String value, int maxMatches) {
        return getDelegate().getMatchingReferences(value, maxMatches);
    }
    public SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes, boolean loadDefaults) {
        return getDelegate().createSimpleInstance(id, name, directTypes, loadDefaults);
    }
    public void addDirectType(Instance instance, Cls type) {
        getDelegate().addDirectType(instance, type);
    }
    public List getDirectSuperslots(Slot slot) {
        return getDelegate().getDirectSuperslots(slot);
    }
    public String getFrameName(Frame frame) {
        return getDelegate().getFrameName(frame);
    }
    public void setFrameName(Frame frame, String name) {
        getDelegate().setFrameName(frame, name);
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
    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
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
}