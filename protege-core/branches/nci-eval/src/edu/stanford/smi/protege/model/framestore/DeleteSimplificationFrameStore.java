package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DeleteSimplificationFrameStore extends FrameStoreAdapter {
    private final Set fixedIds = new HashSet();

    {
        fixedIds.add(Model.SlotID.NAME);
        fixedIds.add(Model.SlotID.DIRECT_SUPERCLASSES);
        fixedIds.add(Model.SlotID.DIRECT_SUBCLASSES);
        fixedIds.add(Model.SlotID.DIRECT_SUPERSLOTS);
        fixedIds.add(Model.SlotID.DIRECT_SUBSLOTS);
        fixedIds.add(Model.SlotID.DIRECT_TYPES);
        fixedIds.add(Model.SlotID.DIRECT_INSTANCES);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        // beginTransaction("Remove template slot " + slot.getBrowserText() + " from " + cls.getBrowserText());
        beginTransaction("Remove template slot");
        internalRemoveDirectTemplateSlot(cls, slot);
        commitTransaction();
    }

    private void internalRemoveDirectTemplateSlot(Cls cls, Slot slot) {
        Collection instances = getInstancesOfClsWithSlotValue(cls, slot);
        getDelegate().removeDirectTemplateSlot(cls, slot);
        removeSlotValues(slot, instances);
    }

    private Collection getInstancesOfClsWithSlotValue(Cls cls, Slot slot) {
        Collection instances = new ArrayList(getFramesWithAnyDirectOwnSlotValue(slot));
        Iterator i = instances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!hasType(instance, cls)) {
                i.remove();
            }
        }
        return instances;
    }

    private void removeSlotValues(Slot slot, Collection instances) {
        Iterator i = instances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            Collection values = instance.getDirectOwnSlotValues(slot);
            if (!values.isEmpty() && !instance.hasOwnSlot(slot)) {
                setDirectOwnSlotValues(instance, slot, Collections.EMPTY_LIST);
            }
        }
    }

    private boolean hasType(Instance instance, Cls cls) {
        return getTypes(instance).contains(cls);
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        // beginTransaction("Remove superclass " + superclass.getBrowserText() + " from " + cls.getBrowserText());
        beginTransaction("Remove superclass ");
        internalRemoveDirectSuperclass(cls, superclass);
        commitTransaction();
    }

    private Collection getSlotsToRemoveOnSuperclassRemove(Cls cls, Cls superclass) {
        Collection slotsToRemove = new HashSet(cls.getTemplateSlots());
        slotsToRemove.removeAll(cls.getDirectTemplateSlots());
        Iterator i = cls.getDirectSuperclasses().iterator();
        while (i.hasNext()) {
            Cls directSuperclass = (Cls) i.next();
            if (!directSuperclass.equals(superclass)) {
                slotsToRemove.removeAll(directSuperclass.getTemplateSlots());
            }
        }
        return slotsToRemove;
    }

    private void internalRemoveDirectSuperclass(Cls cls, Cls superclass) {
        Collection slotToRemove = getSlotsToRemoveOnSuperclassRemove(cls, superclass);
        Iterator i = slotToRemove.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            removeDirectTemplateSlot(cls, slot);
        }
        getDelegate().removeDirectSuperclass(cls, superclass);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        // beginTransaction("Remove superslot " + superslot.getBrowserText() + " from " + slot.getBrowserText());
        beginTransaction("Remove superslot ");
        super.removeDirectSuperslot(slot, superslot);
        commitTransaction();
    }

    public void deleteCls(Cls cls) {
        // beginTransaction("Delete class " + cls.getBrowserText());
        beginTransaction("Delete class ");
        cls.markDeleting();
        internalDeleteCls(cls);
        commitTransaction();
    }

    public void deleteSlot(Slot slot) {
        // beginTransaction("Delete slot " + slot.getBrowserText());
        beginTransaction("Delete slot ");
        slot.markDeleting();
        internalDeleteSlot(slot);
        commitTransaction();
    }

    public void deleteFacet(Facet facet) {
        // beginTransaction("Delete facet " + facet.getBrowserText());
        beginTransaction("Delete facet ");
        facet.markDeleting();
        internalDeleteFacet(facet);
        commitTransaction();
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        // beginTransaction("Delete simple instance  " + simpleInstance.getBrowserText());
        beginTransaction("Delete simple instance  ");
        simpleInstance.markDeleting();
        internalDeleteSimpleInstance(simpleInstance);
        commitTransaction();
    }

    public Collection getClsesToDelete(Cls cls) {
        Collection subclasses = getSubclasses(cls);
        Collection clsesToBeDeleted = new HashSet(subclasses);
        clsesToBeDeleted.add(cls);

        Iterator i = subclasses.iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            // take care with recursive inheritance situations!
            if (!subclass.equals(cls) && reachableByAnotherRoute(subclass, clsesToBeDeleted)) {
                clsesToBeDeleted.remove(subclass);
                Collection subsubclasses = new HashSet(getSubclasses(subclass));
                subsubclasses.remove(cls);
                clsesToBeDeleted.removeAll(subsubclasses);
            }
        }
        return clsesToBeDeleted;
    }

    private boolean reachableByAnotherRoute(Cls subclass, Collection classesToBeDeleted) {
        boolean reachable = false;
        Collection superclasses = getDirectSuperclasses(subclass);
        if (superclasses.size() > 1) {
            Iterator j = superclasses.iterator();
            while (j.hasNext()) {
                Cls superclass = (Cls) j.next();
                if (!classesToBeDeleted.contains(superclass)) {
                    reachable = true;
                    break;
                }
            }
        }
        return reachable;
    }

    public Collection getSlotsToDelete(Slot slot) {
        // TODO Narrow this for multiple inheritance
        Collection slots = new HashSet(getSubslots(slot));
        slots.add(slot);
        return slots;

    }

    private void internalDeleteCls(Cls cls) {
        Iterator i = getClsesToDelete(cls).iterator();
        while (i.hasNext()) {
            Cls clsToDelete = (Cls) i.next();
            deleteValuesOfTemplateSlots(cls);
            internalDeleteInstance(clsToDelete);
        }
    }

    private void internalDeleteSlot(Slot slot) {
        Iterator i = getSlotsToDelete(slot).iterator();
        while (i.hasNext()) {
            Slot slotToDelete = (Slot) i.next();
            removeSlotFromClses(slotToDelete);
            internalDeleteInstance(slotToDelete);
        }
    }

    private void internalDeleteSimpleInstance(Instance instance) {
        internalDeleteInstance(instance);
    }

    private void internalDeleteFacet(Facet facet) {
        deleteFacetValues(facet);
        internalDeleteInstance(facet);
    }

    private void internalDeleteInstance(Instance instance) {
        internalDeleteFrame(instance);
    }

    private void internalDeleteFrame(Frame frame) {
        deleteReferences(frame);
        deleteValuesOfOwnSlots(frame);
        deleteFrame(frame);
    }

    private void deleteFrame(Frame frame) {
        if (frame instanceof Cls) {
            getDelegate().deleteCls((Cls) frame);
        } else if (frame instanceof Slot) {
            getDelegate().deleteSlot((Slot) frame);
        } else if (frame instanceof Facet) {
            getDelegate().deleteFacet((Facet) frame);
        } else if (frame instanceof SimpleInstance) {
            getDelegate().deleteSimpleInstance((SimpleInstance) frame);
        }
    }

    private void deleteReferences(Frame frame) {
        Iterator i = new ArrayList(getReferences(frame)).iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            if (ref.getFacet() == null) {
                if (ref.isTemplate()) {
                    removeTemplateSlotValue((Cls) ref.getFrame(), ref.getSlot(), frame);
                } else {
                    removeOwnSlotValue(ref.getFrame(), ref.getSlot(), frame);
                }
            } else {
                removeTemplateFacetValue((Cls) ref.getFrame(), ref.getSlot(), ref.getFacet(), frame);
            }
        }
    }

    private void deleteValuesOfOwnSlots(Frame frame) {
        Iterator i = getOwnSlots(frame).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (isDeletableOwnSlot(slot)) {
                Collection values = getDirectOwnSlotValues(frame, slot);
                if (!values.isEmpty()) {
                    setDirectOwnSlotValues(frame, slot, Collections.EMPTY_LIST);
                }
            }
        }
    }

    private boolean isDeletableOwnSlot(Slot slot) {
        boolean isDeletable = true;
        if (fixedIds.contains(slot.getFrameID())) {
            isDeletable = false;
        }
        return isDeletable;
    }

    private void deleteValuesOfTemplateSlots(Cls cls) {
        Iterator i = getTemplateSlots(cls).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            Collection values = getDirectTemplateSlotValues(cls, slot);
            if (!values.isEmpty()) {
                setDirectTemplateSlotValues(cls, slot, Collections.EMPTY_LIST);
            }
            deleteValuesOfFacets(cls, slot);
        }
    }

    private void deleteValuesOfFacets(Cls cls, Slot slot) {
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            Collection values = getDirectTemplateFacetValues(cls, slot, facet);
            if (!values.isEmpty()) {
                setDirectTemplateFacetValues(cls, slot, facet, Collections.EMPTY_LIST);
            }
        }
    }

    private void removeSlotFromClses(Slot slot) {
        Iterator i = new ArrayList(getDirectDomain(slot)).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            removeDirectTemplateSlot(cls, slot);
        }
    }

    private void deleteFacetValues(Facet facet) {
    }

    private void removeTemplateSlotValue(Cls cls, Slot slot, Frame value) {
        Collection values = new ArrayList(getDirectTemplateSlotValues(cls, slot));
        values.remove(value);
        setDirectTemplateSlotValues(cls, slot, values);
    }

    private void removeOwnSlotValue(Frame frame, Slot slot, Frame value) {
        if (isDeletableOwnSlot(slot)) {
            Collection values = new ArrayList(getDirectOwnSlotValues(frame, slot));
            values.remove(value);
            setDirectOwnSlotValues(frame, slot, values);
        }
    }

    private void removeTemplateFacetValue(Cls cls, Slot slot, Facet facet, Frame value) {
        Collection values = new ArrayList(getDirectTemplateFacetValues(cls, slot, facet));
        values.remove(value);
        setDirectTemplateFacetValues(cls, slot, facet, values);
    }
}