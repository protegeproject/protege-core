package edu.stanford.smi.protege.storage.jdbc;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.jdbc.framedb.*;
import edu.stanford.smi.protege.util.*;

/**
 * This class handles most of the management for a collection of frames.  It is a delegate of
 * the knowledge base class.  DefaultKnowledgeBase handles event notifications, journaling, and convenience methods.
 * This class handles most other stuff.  This class in turn simplifies and collapses the calls (reducting them only 
 * to slot/facet gets/sets and then delegates them to an implementation of {@link Storage}
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameManager implements Disposable {
    private OldJdbcDefaultKnowledgeBase _knowledgeBase;
    private Storage _storage = new FrameDBStorage();

    public FrameManager(OldJdbcDefaultKnowledgeBase kb) {
        _knowledgeBase = kb;
    }

    public void addDirectSuperclass(Cls child, Cls parent) {
        addOwnSlotValue(child, getDirectSuperclassesSlot(), parent);
        addOwnSlotValue(parent, getDirectSubclassesSlot(), child);
        removeDirectSlotsWhichAreNowInherited(child, parent);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        addOwnSlotValue(cls, getDirectTemplateSlotsSlot(), slot);
    }

    public void addInstance(Instance instance, String name, Cls type) {
        _storage.addFrame(instance);
        Slot nameSlot = getNameSlot();
        if (nameSlot != null) {
            addOwnSlotValue(instance, nameSlot, name);
        }
        Slot directTypeSlot = getDirectTypeSlot();
        Slot directInstancesSlot = getDirectInstancesSlot();
        if (directTypeSlot != null && directInstancesSlot != null) {
            addOwnSlotValue(instance, directTypeSlot, type);
            if (type != null) {
                addOwnSlotValue(type, directInstancesSlot, instance);
            }
        }

        updateCreationSlotValues(instance);
    }

    public void addOwnSlotValue(Frame frame, Slot slot, Object value) {
        _storage.addValue(frame, slot, null, false, value);
        updateModificationFacetValues(frame, slot);
    }

    public void addOwnSlotValue(Frame frame, Slot slot, Object value, int index) {
        _storage.addValue(frame, slot, null, false, value, index);
        updateModificationFacetValues(frame, slot);
    }

    public void addTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        _storage.addValue(cls, slot, facet, true, value);
    }

    public void addTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value, int index) {
        _storage.addValue(cls, slot, facet, true, value, index);
    }

    public void addTemplateSlotValue(Cls cls, Slot slot, Object value) {
        _storage.addValue(cls, slot, getValuesFacet(), true, value);
    }

    public boolean beginTransaction() {
        return _storage.beginTransaction();
    }

    public boolean containsFrame(String name) {
        return _storage.containsFrame(name);
    }

    public void deleteCls(Cls cls) {
        removeTemplateSlots(cls);
        deleteFrame(cls);
    }

    public void deleteFacet(Facet facet) {
        deleteFrame(facet);
    }

    private void deleteFrame(Frame frame) {
        // Log.enter(this, "deleteFrame", frame);
        _storage.removeFrame(frame);
    }

    public void deleteSimpleInstance(Instance instance) {
        deleteFrame(instance);
    }

    public void deleteSlot(Slot slot) {
        deleteFrame(slot);
    }

    private void directRemoveTemplateSlot(Cls cls, Slot slot) {
        removeTemplateFacetValueOverrides(cls, slot);
        // directRemoveInstanceOwnSlots(cls, slot);
    }

    public void dispose() {
        _storage.dispose();
        _storage = null;
    }

    public boolean endTransaction(boolean doCommit) {
        return _storage.endTransaction(doCommit);
    }

    public Collection getDefaultValues(Frame frame) {
        return safeCollection(_storage.getValues(frame, getDefaultValuesSlot(), null, false));
    }

    public Facet getDefaultValuesFacet() {
        return _knowledgeBase.getDefaultValuesFacet();
    }

    public Slot getDefaultValuesSlot() {
        return _knowledgeBase.getDefaultValuesSlot();
    }

    public int getDirectInstanceCount(Cls type) {
        return getOwnSlotValueCount(type, getDirectInstancesSlot());
    }

    public Collection getDirectInstances(Cls type) {
        return getOwnSlotValues(type, getDirectInstancesSlot());
    }

    public Slot getDirectInstancesSlot() {
        return _knowledgeBase.getDirectInstancesSlot();
    }

    private Collection getDirectOrInheritedTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        Collection result = getDirectTemplateFacetValues(cls, slot, facet);
        if (result.isEmpty()) {
            Iterator i = getDirectSuperclasses(cls).iterator();
            while (i.hasNext() && result.isEmpty()) {
                Cls superclass = (Cls) i.next();
                result = getDirectOrInheritedTemplateFacetValues(superclass, slot, facet);
            }
        }
        return result;
    }

    private Collection getDirectOrInheritedTemplateSlotDefaultValues(Cls cls, Slot slot) {
        Collection values = _storage.getValues(cls, slot, getDefaultValuesFacet(), true);
        if (values.isEmpty() && !hasDirectTemplateSlot(cls, slot)) {
            values = getInheritedTemplateSlotDefaultValues(cls, slot);
        }
        return values;
    }

    public int getDirectSubclassCount(Cls parent) {
        return getOwnSlotValueCount(parent, getDirectSubclassesSlot());
    }

    public Collection getDirectSubclasses(Cls parent) {
        return getOwnSlotValues(parent, getDirectSubclassesSlot());
    }

    public Slot getDirectSubclassesSlot() {
        return _knowledgeBase.getDirectSubclassesSlot();
    }

    public int getDirectSuperclassCount(Cls cls) {
        return getOwnSlotValueCount(cls, getDirectSuperclassesSlot());
    }

    public Collection getDirectSuperclasses(Cls cls) {
        Collection directSuperclasses = null;
        Slot directSuperclassesSlot = getDirectSuperclassesSlot();
        if (directSuperclassesSlot != null) {
            directSuperclasses = getOwnSlotValues(cls, directSuperclassesSlot);
        }
        return safeCollection(directSuperclasses);
    }

    public Slot getDirectSuperclassesSlot() {
        return _knowledgeBase.getDirectSuperclassesSlot();
    }

    public Slot getDirectSuperslotsSlot() {
        return _knowledgeBase.getDirectSuperslotsSlot();
    }

    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return _storage.getValues(cls, slot, facet, true);
    }

    public Collection getDirectTemplateSlots(Cls cls) {
        Collection slots = null;
        Slot directTemplateSlotsSlot = getDirectTemplateSlotsSlot();
        if (directTemplateSlotsSlot != null) {
            slots = getOwnSlotValues(cls, directTemplateSlotsSlot);
        }
        return safeCollection(slots);
    }

    /*
     * public Slot getOwnSlotsSlot() {
     * return knowledgeBase.getOwnSlotsSlot();
     * }
     */

    public Slot getDirectTemplateSlotsSlot() {
        return _knowledgeBase.getDirectTemplateSlotsSlot();
    }

    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        return _storage.getValues(cls, slot, null, true);
    }

    public Cls getDirectType(Instance instance) {
        Cls type = null;
        Slot directTypeSlot = getDirectTypeSlot();
        if (directTypeSlot != null) {
            type = (Cls) getOwnSlotValue(instance, directTypeSlot);
        }
        return type;
    }

    public Slot getDirectTypeSlot() {
        return _knowledgeBase.getDirectTypeSlot();
    }

    public Frame getFrame(FrameID id) {
        return _storage.getFrame(id);
    }

    public Frame getFrame(String name) {
        return _storage.getFrame(name);
    }

    public int getFrameCount() {
        return _storage.getFrameCount();
    }

    public int getFacetCount() {
        return _storage.getFacetCount();
    }

    public int getSlotCount() {
        return _storage.getSlotCount();
    }

    public int getClsCount() {
        return _storage.getClsCount();
    }

    public Collection getFrames() {
        return _storage.getFrames();
    }

    private Collection getInheritedTemplateSlotDefaultValues(Cls cls, Slot slot) {
        Collection values = Collections.EMPTY_LIST;
        Iterator i = getDirectSuperclasses(cls).iterator();
        while (i.hasNext() && values.isEmpty()) {
            Cls superclass = (Cls) i.next();
            // if (hasTemplateSlot(superclass, slot)) {
            values = getDirectOrInheritedTemplateSlotDefaultValues(superclass, slot);
            // }
        }
        return values;
    }

    public int getInstanceCount(Cls type) {
        return getInstances(type).size();
    }

    public Collection getInstances(Cls type) {
        Collection instances = new ArrayList(type.getDirectInstances());
        Iterator i = type.getSubclasses().iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            instances.addAll(cls.getDirectInstances());
        }
        return instances;
    }

    public Collection getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String s, int maxMatches) {
        return _storage.getMatchingFrames(slot, facet, isTemplate, s, maxMatches);
    }

    public Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object o) {
        return _storage.getFramesWithValue(slot, facet, isTemplate, o);
    }

    public String getName(Frame frame) {
        Slot nameSlot = getNameSlot();
        return (nameSlot == null) ? (String) null : (String) getOwnSlotValue(frame, nameSlot);
    }

    public Slot getNameSlot() {
        return _knowledgeBase.getNameSlot();
    }

    public Collection getOwnSlotAndSubslotValues(Frame frame, Slot slot) {
        Collection result = safeCollection(_storage.getValues(frame, slot, null, false));
        if (slot.getDirectSubslotCount() > 0) {
            Collection subslots = slot.getSubslots();
            Collection ownSlots = frame.getOwnSlots();
            subslots.retainAll(ownSlots);
            Iterator i = subslots.iterator();
            while (i.hasNext()) {
                Slot subslot = (Slot) i.next();
                result.addAll(_storage.getValues(frame, subslot, null, false));
            }
        }
        return result;
    }

    public Collection getOwnSlotFacetValues(Frame frame, Slot slot, Facet facet) {
        Collection c = _storage.getValues(frame, slot, facet, false);
        if (c.isEmpty()) {
            Cls type = ((Instance) frame).getDirectType();
            c = getTemplateFacetValues(type, slot, facet);
        }
        return c;
    }

    public Collection getOwnSlots(Frame frame) {
        Collection slots = null;
        if (frame instanceof Instance) {
            Instance instance = (Instance) frame;
            Cls type = getDirectType(instance);
            if (type != null) {
                slots = getTemplateSlots(type);
                slots.add(_knowledgeBase.getNameSlot());
                slots.add(_knowledgeBase.getDirectTypeSlot());
            }
        }
        return safeCollection(slots);
    }

    public Object getOwnSlotValue(Frame frame, Slot slot) {
        return _storage.getValue(frame, slot, null, false);
    }

    public int getOwnSlotValueCount(Frame frame, Slot slot) {
        return _storage.getValueCount(frame, slot, null, false);
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot) {
        return safeCollection(_storage.getValues(frame, slot, null, false));
    }

    public Collection getReferences(Object o, int maxReferences) {
        return safeCollection(_storage.getReferences(o, maxReferences));
    }

    public Storage getStorage() {
        return _storage;
    }

    public int getSubclassCount(Cls parent) {
        return getSubclasses(parent).size();
    }

    public Collection getSubclasses(Cls parent) {
        Collection subclasses = new HashSet();
        getSubclasses(parent, subclasses);
        return subclasses;
    }

    private void getSubclasses(Cls parent, Collection subclasses) {
        Iterator i = parent.getDirectSubclasses().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Cls) {
                Cls child = (Cls) o;
                boolean changed = subclasses.add(child);
                if (changed) {
                    getSubclasses(child, subclasses);
                }
            } else {
                Log.getLogger().warning("not a class: " + o);
            }
        }
    }

    public int getSuperclassCount(Cls child) {
        return getSuperclasses(child).size();
    }

    public Set getSuperclasses(Cls child) {
        Set superclasses = new HashSet();
        if (getDirectSuperclassesSlot() != null) {
            getSuperclasses(child, superclasses);
        }
        return superclasses;
    }

    private void getSuperclasses(Cls child, Collection superclasses) {
        ArrayList directSuperclasses = _storage.getValues(child, getDirectSuperclassesSlot(), null, false);
        for (int i = 0; i < directSuperclasses.size(); ++i) {
            Cls parent = (Cls) directSuperclasses.get(i);
            boolean changed = superclasses.add(parent);
            if (changed) {
                getSuperclasses(parent, superclasses);
            }
        }
    }

    public Collection getTemplateFacets(Cls cls, Slot slot) {
        Collection facets = new ArrayList();
        Iterator i = getOwnSlots(slot).iterator();
        while (i.hasNext()) {
            Slot ownSlot = (Slot) i.next();
            Facet facet = ownSlot.getAssociatedFacet();
            if (facet != null && equals(facet.getAssociatedSlot(), ownSlot)) {
                facets.add(facet);
            }
        }
        return facets;
    }

    public Object getTemplateFacetValue(Cls cls, Slot slot, Facet facet) {
        return CollectionUtilities.getFirstItem(getTemplateFacetValues(cls, slot, facet));
    }

    public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        Collection values = getDirectOrInheritedTemplateFacetValues(cls, slot, facet);
        if (values.isEmpty()) {
            values = _storage.getValues(slot, facet.getAssociatedSlot(), null, false);
        }
        return values;
    }

    public Collection getTemplateSlotDefaultValues(Cls cls, Slot slot) {
        Collection values = getDirectOrInheritedTemplateSlotDefaultValues(cls, slot);
        if (values.isEmpty()) {
            values = slot.getDefaultValues();
        }
        return safeCollection(values);
    }

    public Collection getTemplateSlots(Cls cls) {
        Collection slots = new HashSet(getDirectTemplateSlots(cls));
        Iterator i = getSuperclasses(cls).iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            slots.addAll(getDirectTemplateSlots(parent));
        }
        return slots;
    }

    public Object getTemplateSlotValue(Cls cls, Slot slot) {
        // return storage.getValue(cls, slot, null, true);
        return getTemplateFacetValue(cls, slot, getValuesFacet());
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getTemplateFacetValues(cls, slot, getValuesFacet());
        /*
        Collection values = getDirectTemplateSlotValues(cls, slot);
        if (values == null) {
            values = getInheritedTemplateSlotValues(cls, slot);
        }
        return values;
        */
    }

    public Collection getValues(Slot slot) {
        return _storage.getValues(slot, getValuesSlot(), null, false);
    }

    public Facet getValuesFacet() {
        return _knowledgeBase.getValuesFacet();
    }

    public Slot getValuesSlot() {
        return _knowledgeBase.getValuesSlot();
    }

    public boolean hasDirectlyOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        return !getDirectTemplateFacetValues(cls, slot, facet).isEmpty();
        /*
        boolean result = false;
        if (hasDirectTemplateSlot(cls, slot)) {
            result = hasOverriddenTemplateFacet(cls, slot, facet);
        } else {
            Collection directValues = getDirectTemplateFacetValues(cls, slot, facet);
            if (directValues.isEmpty()) {
            Collection parentValues = Collections.EMPTY_LIST;
            Iterator i = getDirectSuperclasses(cls).iterator();
            while (i.hasNext()) {
                Cls superclass = (Cls) i.next();
                if (hasTemplateSlot(superclass, slot)) {
                    // parentValues = getDirectTemplateFacetValues(superclass, slot, facet);
                    parentValues = getTemplateFacetValues(superclass, slot, facet);
                    break;
                }
            }
            result = !CollectionUtilities.equals(parentValues, directValues);
        }
        return result;
        */
    }

    public boolean hasDirectlyOverriddenTemplateSlot(Cls cls, Slot slot) {
        boolean hasDirectOverriddenTemplateSlot = false;
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext() && !hasDirectOverriddenTemplateSlot) {
            Facet facet = (Facet) i.next();
            hasDirectOverriddenTemplateSlot = hasDirectlyOverriddenTemplateFacet(cls, slot, facet);
        }
        return hasDirectOverriddenTemplateSlot;
    }

    public boolean hasDirectSubclass(Cls parent, Cls child) {
        return hasOwnSlotValue(parent, getDirectSubclassesSlot(), child);
    }

    public boolean hasDirectSuperclass(Cls child, Cls parent) {
        return hasOwnSlotValue(child, getDirectSuperclassesSlot(), parent);
    }

    public boolean hasDirectSuperslot(Slot slot, Slot superSlot) {
        return hasOwnSlotValue(slot, getDirectSuperslotsSlot(), superSlot);
    }

    public boolean hasDirectTemplateSlot(Cls cls, Slot slot) {
        return hasOwnSlotValue(cls, getDirectTemplateSlotsSlot(), slot);
    }

    public boolean hasDirectType(Instance instance, Cls type) {
        return hasOwnSlotValue(instance, getDirectTypeSlot(), type);
    }

    public boolean hasInheritedTemplateSlot(Cls cls, Slot slot) {
        boolean hasInheritedSlot = false;
        Iterator i = getDirectSuperclasses(cls).iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            if (hasTemplateSlot(parent, slot)) {
                hasInheritedSlot = true;
                break;
            }
        }
        return hasInheritedSlot;
    }

    public boolean hasOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        boolean result;
        Slot associatedSlot = facet.getAssociatedSlot();
        if (associatedSlot == null) {
            Log.getLogger().warning("no associated slot: " + facet);
            result = false;
        } else {
            Collection topLevelValues = getOwnSlotValues(slot, associatedSlot);
            /*
            Collection directValues = getDirectTemplateFacetValues(cls, slot, facet);
            if (directValues.isEmpty()) {
                result = false;
            } else if (topLevelValues.isEmpty()) {
                result = true;
            } else {
                result = !CollectionUtilities.equals(topLevelValues, directValues);
            }
            */
            Collection values = getTemplateFacetValues(cls, slot, facet);
            result = !CollectionUtilities.equalsSet(topLevelValues, values);
        }
        return result;
    }

    /*
     * "overridden" is defined relative to the top level slot
     */
    public boolean hasOverriddenTemplateSlot(Cls cls, Slot slot) {
        boolean hasOverriddenTemplateSlot = false;
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext() && !hasOverriddenTemplateSlot) {
            Facet facet = (Facet) i.next();
            hasOverriddenTemplateSlot = hasOverriddenTemplateFacet(cls, slot, facet);
        }
        return hasOverriddenTemplateSlot;
    }

    public boolean hasOwnFacet(Frame frame, Slot slot, Facet facet) {
        boolean result;
        Cls directType = ((Instance) frame).getDirectType();
        if (directType == null) {
            result = false;
        } else {
            result = hasTemplateFacet(directType, slot, facet);
        }
        return result;
    }

    public boolean hasOwnSlot(Frame frame, Slot slot) {
        return getOwnSlots(frame).contains(slot);
    }

    // private Storage storage = new MemoryStorage();

    private boolean hasOwnSlotValue(Frame frame, Slot slot, Object value) {
        return _storage.hasValue(frame, slot, null, false, value);
    }

    /*
     * public boolean hasSubclass(Cls parent, Cls child) {
     * return getSubclasses(parent).contains(child);
     * }
     */

    public boolean hasSubclass(Cls parent, Cls child) {
        Collection subclasses = new HashSet();
        return hasSubclass(parent, child, subclasses);
    }

    private boolean hasSubclass(Cls parent, Cls possibleChild, Collection checkedSubclasses) {
        boolean hasSubclass = false;
        Iterator i = getDirectSubclasses(parent).iterator();
        while (!hasSubclass && i.hasNext()) {
            Object o = i.next();
            if (o instanceof Cls) {
                Cls child = (Cls) o;
                if (equals(child, possibleChild)) {
                    hasSubclass = true;
                } else if (checkedSubclasses.add(child)) {
                    hasSubclass = hasSubclass(child, possibleChild, checkedSubclasses);
                }
            } else {
                Log.getLogger().warning("not a class: " + o);
            }
        }
        return hasSubclass;
    }

    public boolean hasSuperclass(Cls child, Cls superclass) {
        return getSuperclasses(child).contains(superclass);
    }

    public boolean hasTemplateFacet(Cls cls, Slot slot, Facet facet) {
        Collection facets = getTemplateFacets(cls, slot);
        return (facets == null) ? false : facets.contains(facet);
    }

    // ---------------------------------- template slots -------------------------------------
    public boolean hasTemplateSlot(Cls cls, Slot slot) {
        return getTemplateSlots(cls).contains(slot);
    }

    public boolean hasType(Instance instance, Cls type) {
        boolean hasType;
        Cls directType = getDirectType(instance);
        if (directType == null) {
            hasType = false;
        } else {
            hasType = equals(directType, type) || getSuperclasses(directType).contains(type);
        }
        return hasType;
    }

    public boolean hasValueAtSomeFrame(Slot slot, Facet facet, boolean isTemplate) {
        return _storage.hasValueAtSomeFrame(slot, facet, isTemplate);
    }

    private static boolean isModificationSlot(Slot slot) {
        FrameID id = slot.getFrameID();
        return equals(id, Model.SlotID.MODIFICATION_TIMESTAMP)
            || equals(id, Model.SlotID.MODIFIER)
            || equals(id, Model.SlotID.CREATOR)
            || equals(id, Model.SlotID.CREATION_TIMESTAMP);
    }

    /**
     * move child class to appear after "afterClass" in the subclasses of parent.
     * if afterClass is null then the child will appear as the first child first.
     */
    public void moveDirectSubclass(Cls parent, Cls child, Cls afterClass) {
        List subclasses = new ArrayList(parent.getDirectSubclasses());
        int from = subclasses.indexOf(child);
        int to = (afterClass == null) ? 0 : subclasses.indexOf(afterClass) + 1;
        if (from < to) {
            --to;
        }
        moveOwnSlotValue(parent, getDirectSubclassesSlot(), from, to);
    }

    public void moveDirectSuperclass(Cls child, Cls parent, Cls afterCls) {
        List superclasses = new ArrayList(child.getDirectSuperclasses());
        int from = superclasses.indexOf(child);
        int to = (afterCls == null) ? 0 : superclasses.indexOf(afterCls) + 1;
        if (from < to) {
            --to;
        }
        moveOwnSlotValue(child, getDirectSuperclassesSlot(), from, to);
    }

    public void moveOwnSlotValue(Frame frame, Slot slot, int fromIndex, int toIndex) {
        _storage.moveValue(frame, slot, null, false, fromIndex, toIndex);
        updateModificationFacetValues(frame, slot);
    }

    public void moveTemplateFacetValue(Cls cls, Slot slot, Facet facet, int fromIndex, int toIndex) {
        _storage.moveValue(cls, slot, facet, true, fromIndex, toIndex);
    }

    private void removeDirectSlotsWhichAreNowInherited(Cls child, Cls parent) {
        Collection newInheritedSlots = getTemplateSlots(parent);
        Collection directSlots = new HashSet(getDirectTemplateSlots(child));
        boolean changed = directSlots.removeAll(newInheritedSlots);
        if (changed) {
            setOwnSlotValues(child, getDirectTemplateSlotsSlot(), directSlots);
        }
    }

    public void removeDirectSuperclass(Cls child, Cls parent) {
        removeOwnSlotValue(child, getDirectSuperclassesSlot(), parent);
        removeOwnSlotValue(parent, getDirectSubclassesSlot(), child);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        removeOwnSlotValue(cls, getDirectTemplateSlotsSlot(), slot);
        directRemoveTemplateSlot(cls, slot);
        removeTemplateSlotFromSubclasses(cls, slot);
    }

    private void removeDirectTemplateSlots(Cls cls) {
        Iterator i = new ArrayList(getDirectTemplateSlots(cls)).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            removeDirectTemplateSlot(cls, slot);
        }
    }

    private void removeInheritedTemplateSlots(Cls cls) {
    }

    /*
     * public void addOwnSlot(Frame frame, Slot slot) {
     * setOwnSlotValues(frame, slot, slot.getDefaultValues());
     * }
     */

    public void removeOwnSlotValue(Frame frame, Slot slot, Object value) {
        _storage.removeSingleValue(frame, slot, null, false, value);
        updateModificationFacetValues(frame, slot);
    }

    public void removeTemplateFacetValueOverrides(Cls cls, Slot slot) {
        Iterator i = new ArrayList(getTemplateFacets(cls, slot)).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            setTemplateFacetValues(cls, slot, facet, Collections.EMPTY_LIST);
        }
    }

    private void removeTemplateSlotFromSubclasses(Cls cls, Slot slot) {
    }

    private void removeTemplateSlots(Cls cls) {
        removeDirectTemplateSlots(cls);
        removeInheritedTemplateSlots(cls);
        removeTemplateSlotsFromChildren(cls);
    }

    private void removeTemplateSlotsFromChildren(Cls cls) {
    }

    public void removeValues(Slot slot, Facet facet, boolean isTemplate, Cls cls) {
        _storage.removeValues(slot, facet, isTemplate, cls);
    }

    private Collection safeCollection(Collection c) {
        return (c == null) ? Collections.EMPTY_LIST : c;
    }

    public void setDefaultValues(Frame frame, Collection values) {
        _storage.setValues(frame, getDefaultValuesSlot(), null, false, values);
    }

    public void setDirectType(Instance instance, Cls newType) {
        Slot directTypeSlot = getDirectTypeSlot();
        Slot directInstancesSlot = getDirectInstancesSlot();
        if (directTypeSlot != null && directInstancesSlot != null) {
            Cls oldType = getDirectType(instance);
            if (!equals(oldType, newType)) {
                if (oldType != null) {
                    removeOwnSlotValue(oldType, directInstancesSlot, instance);
                }
                setOwnSlotValue(instance, directTypeSlot, newType);
                if (newType != null) {
                    addOwnSlotValue(newType, directInstancesSlot, instance);
                }
            }
        }
    }
    
    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public void setName(Frame frame, String newName) {
        Slot nameSlot = getNameSlot();
        if (nameSlot != null) {
            setOwnSlotValue(frame, nameSlot, newName);
        }
    }

    private void setOwnFacetValue(Frame frame, Slot slot, Facet facet, Object value) {
        _storage.setValue(frame, slot, facet, false, value);
    }

    public void setOwnSlotValue(Frame frame, Slot slot, Object value) {
        _storage.setValue(frame, slot, null, false, value);
        updateModificationFacetValues(frame, slot);
    }

    public void setOwnSlotValues(Frame frame, Slot slot, Collection values) {
        _storage.setValues(frame, slot, null, false, values);
        updateModificationFacetValues(frame, slot);
    }

    public void setStorage(Storage storage) {
        this._storage = storage;
    }

    public void setTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        setTemplateFacetValues(cls, slot, facet, CollectionUtilities.createCollection(value));
    }

    public void setTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection newValues) {
        /*
        Collection slotValues = _storage.getValues(slot, facet.getAssociatedSlot(), null, false);
        if (CollectionUtilities.equals(slotValues, newValues)) {
            newValues = Collections.EMPTY_LIST;
        }
        Collection oldValues = _storage.getValues(cls, slot, facet, true);
        */
        _storage.setValues(cls, slot, facet, true, newValues);
        /*
        Iterator i = getSubclasses(cls).iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            Collection subclassValues = _storage.getValues(subclass, slot, facet, true);
            if (CollectionUtilities.equals(subclassValues, oldValues)) {
                // Log.trace("propagating to " + subclass, this, "setTemplateFacetValues", cls, slot, facet, newValues);
                _storage.setValues(subclass, slot, facet, true, newValues);
            }
        }
        */
    }

    public void setTemplateSlotDefaultValues(Cls cls, Slot slot, Collection values) {
        _storage.setValues(cls, slot, getDefaultValuesFacet(), true, values);
    }

    public void setTemplateSlotValue(Cls cls, Slot slot, Object value) {
        _storage.setValue(cls, slot, getValuesFacet(), true, value);
    }

    public void setTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        _storage.setValues(cls, slot, getValuesFacet(), true, values);
    }

    public void setValues(Slot slot, Collection values) {
        _storage.setValues(slot, getValuesSlot(), null, false, values);
    }

    public boolean supportsTransactions() {
        return _storage.supportsTransactions();
    }

    /**
     *  replace all references to/from "from" with references to/from "to"
     *
     * @param  from  Description of Parameter
     * @param  to    Description of Parameter
     */
    public void swapInstance(Instance from, Instance to) {
        _storage.replace(from, to);
    }

    public String toString() {
        return "MemoryStorage";
    }

    private void updateCreationSlotValues(Frame frame) {
        updateModificationSlotValues(frame, Model.Slot.CREATOR, Model.Slot.CREATION_TIMESTAMP);
    }

    private void updateModificationFacetValues(Frame frame, Slot slot) {
        if (!isModificationSlot(slot)) {
            updateModificationSlotValues(frame);
            if (_knowledgeBase.isAutoUpdatingFacetValues()) {
                Facet modifierFacet = _knowledgeBase.getFacet(Model.Facet.MODIFIER);
                if (hasOwnFacet(frame, slot, modifierFacet)) {
                    String modifier = _knowledgeBase.getUserName();
                    setOwnFacetValue(frame, slot, modifierFacet, modifier);
                }
                Facet modificationTimestampFacet = _knowledgeBase.getFacet(Model.Facet.MODIFICATION_TIMESTAMP);
                if (hasOwnFacet(frame, slot, modificationTimestampFacet)) {
                    String timestamp = new StandardDateFormat().format(new Date());
                    setOwnFacetValue(frame, slot, modificationTimestampFacet, timestamp);
                }
            }
        }
    }

    private void updateModificationSlotValues(Frame frame) {
        updateModificationSlotValues(frame, Model.Slot.MODIFIER, Model.Slot.MODIFICATION_TIMESTAMP);
    }

    private void updateModificationSlotValues(Frame frame, String authorSlotName, String timestampSlotName) {
        if (_knowledgeBase.isAutoUpdatingFacetValues()) {
            Slot modifierSlot = _knowledgeBase.getSlot(authorSlotName);
            if (hasOwnSlot(frame, modifierSlot)) {
                String modifier = _knowledgeBase.getUserName();
                setOwnSlotValue(frame, modifierSlot, modifier);
                // the events have to be propagated manually...
                _knowledgeBase.postOwnSlotValueChanged(frame, modifierSlot);
            }
            Slot modificationTimestampSlot = _knowledgeBase.getSlot(timestampSlotName);
            if (hasOwnSlot(frame, modificationTimestampSlot)) {
                String timestamp = new StandardDateFormat().format(new Date());
                setOwnSlotValue(frame, modificationTimestampSlot, timestamp);
                _knowledgeBase.postOwnSlotValueChanged(frame, modificationTimestampSlot);
            }
        }
    }

    public int getSimpleInstanceCount() {
        return getFrameCount() - (getClsCount() + getSlotCount() + getFacetCount());
    }
}
