package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

public class ChangeMonitorFrameStore extends ModificationFrameStore {
    private boolean changed;

    private void markChanged() {
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean b) {
        changed = b;
    }

    public void reinitialize() {
        changed = false;
    }

    public Cls createCls(FrameID id, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues) {
        markChanged();
        return getDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues);
    }

    public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues) {
        markChanged();
        return getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaultValues);
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        markChanged();
        return getDelegate().createFacet(id, directTypes, loadDefaultValues);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes,
            boolean loadDefaultValues) {
        markChanged();
        return getDelegate().createSimpleInstance(id, directTypes, loadDefaultValues);
    }

    public void deleteCls(Cls cls) {
        markChanged();
        getDelegate().deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        markChanged();
        getDelegate().deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        markChanged();
        getDelegate().deleteFacet(facet);
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        markChanged();
        getDelegate().deleteSimpleInstance(simpleInstance);
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom, int indexTo) {
        markChanged();
        getDelegate().moveDirectOwnSlotValue(frame, slot, indexFrom, indexTo);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        markChanged();
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        markChanged();
        getDelegate().addDirectTemplateSlot(cls, slot);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        markChanged();
        getDelegate().removeDirectTemplateSlot(cls, slot);
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        markChanged();
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        markChanged();
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        markChanged();
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        markChanged();
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        markChanged();
        getDelegate().addDirectSuperclass(cls, superclass);
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        markChanged();
        getDelegate().removeDirectSuperclass(cls, superclass);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        markChanged();
        getDelegate().moveDirectSubclass(cls, subclass, index);
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        markChanged();
        getDelegate().addDirectSuperslot(slot, superslot);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        markChanged();
        getDelegate().removeDirectSuperslot(slot, superslot);
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        markChanged();
        getDelegate().moveDirectSubslot(slot, subslot, index);
    }

    public void addDirectType(Instance instance, Cls type) {
        markChanged();
        getDelegate().addDirectType(instance, type);
    }

    public void removeDirectType(Instance instance, Cls type) {
        markChanged();
        getDelegate().removeDirectType(instance, type);
    }

    public void moveDirectType(Instance instance, Cls type, int index) {
        markChanged();
        getDelegate().moveDirectType(instance, type, index);
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

    public void replaceFrame(Frame original, Frame replacement) {
      markChanged();
      getDelegate().replaceFrame(original, replacement);
    }

}
