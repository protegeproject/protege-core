package edu.stanford.smi.protege.model;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.util.*;

/**
 * Default implementation of Frame interface. Forwards all method calls to its DefaultKnowledgeBase.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DefaultFrame implements Frame, Localizable, Externalizable {
    private static final char SPECIAL_NAME_CHAR = ':';

    private transient KnowledgeBase knowledgeBase;
    private FrameID id;

    /**
     * This set of booleans is optimized to a "state" object to cut down on memory consumption.
     * Large projects use many frame objects.  Unfortunately the Java VM stores each "boolean"
     * variable in an "int" object.
     */
    private static final int READONLY_MASK = 1 << 0;
    private static final int INCLUDED_MASK = 1 << 1;
    private static final int DELETING_MASK = 1 << 2;
    private static final int DELETED_MASK = 1 << 3;
    private int state;

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (FrameID) in.readObject();
        state = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeInt(state);
    }

    protected DefaultFrame() {

    }

    DefaultFrame(KnowledgeBase kb, FrameID id) {
        if (id == null) {
            Log.getLogger().severe("null frame id");
        }
        knowledgeBase = kb;
        this.id = id;
    }

    /**
     * @deprecated
     */
    public URI getDefiningProjectURI() {
        return null;
    }

    public FrameID getFrameID() {
        return id;
    }

    public boolean isDeleted() {
        return getState(DELETED_MASK);
    }

    public void markDeleted(boolean deleted) {
        setState(DELETED_MASK, deleted);
    }

    private void setState(int mask, boolean value) {
        if (value) {
            state |= mask;
        } else {
            state &= ~mask;
        }
    }

    private boolean getState(int mask) {
        return (state & mask) != 0;
    }

    public boolean isBeingDeleted() {
        return getState(DELETING_MASK);
    }

    public void markDeleting() {
        setState(DELETING_MASK, true);
    }

    /**
     * @deprecated
     */
    public boolean isValid() {
        return !isDeleted();
    }

    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof DefaultFrame) {
            DefaultFrame rhs = (DefaultFrame) o;
            equals = equals(id, rhs.id) && knowledgeBase == rhs.knowledgeBase;
        }
        return equals;
    }

    public final int hashCode() {
        return id.hashCode();
    }

    public void addFrameListener(FrameListener listener) {
        getDefaultKnowledgeBase().addFrameListener(this, listener);
    }

    public boolean addOwnFacetValue(Slot slot, Facet facet, Object value) {
        Assert.fail("not implemented");
        return false;
    }

    public void addOwnSlotValue(Slot slot, Object value) {
        getDefaultKnowledgeBase().addOwnSlotValue(this, slot, value);
    }

    public boolean areValidOwnSlotValues(Slot slot, Collection c) {
        return getDefaultKnowledgeBase().areValidOwnSlotValues(this, slot, c);
    }

    private static int compareStrings(String s1, String s2) {
        int result = s1.compareToIgnoreCase(s2);
        if (result == 0) {
            result = s1.compareTo(s2);
        }
        return result;
    }

    /*
     * public int compareTo(Object o) { int result; if (o instanceof Frame) { Frame f2 = (Frame) o; String t1 =
     * this.getBrowserText(); String t2 = f2.getBrowserText(); if (t1.charAt(0) == SPECIAL_NAME_CHAR) { result =
     * (t2.charAt(0) == SPECIAL_NAME_CHAR) ? compareStrings(t1, t2) : +1; } else if (t2.charAt(0) == SPECIAL_NAME_CHAR) {
     * result = -1; } else { result = compareStrings(t1, t2); } } else { result = 0; } return result; }
     */

    public int compareTo(Object o) {
        int result;
        if (o instanceof Frame) {
            Frame f2 = (Frame) o;
            String t1 = this.getBrowserText();
            String t2 = f2.getBrowserText();
            if (isSpecialName(t1)) {
                result = isSpecialName(t2) ? compareStrings(t1, t2) : +1;
            } else if (isSpecialName(t2)) {
                result = -1;
            } else {
                result = compareStrings(t1, t2);
            }
        } else {
            result = 0;
        }
        return result;
    }

    private static boolean isSpecialName(String s) {
        return s.length() > 0 && s.charAt(0) == SPECIAL_NAME_CHAR;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private void deepCopyFrameBindingValues(Collection values, Frame copyFrame, Slot copySlot, Map valueMap) {

        KnowledgeBase copyKB = copyFrame.getKnowledgeBase();
        Collection copyValues = new ArrayList();
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Frame origFrame = (Frame) i.next();
            Frame copyValue = (Frame) valueMap.get(origFrame);
            if (copyValue == null) {
                copyValue = origFrame.deepCopy(copyKB, valueMap);
            }
            copyValues.add(copyValue);
        }
        copyFrame.setOwnSlotValues(copySlot, copyValues);
    }

    private void copyOwnSlot(DefaultFrame copyFrame, Slot origSlot, Map valueMap, boolean isDeep) {
        Slot copySlot = (Slot) valueMap.get(origSlot);
        Assert.assertNotNull("copy slot", copySlot);
        copyOwnSlotValues(copyFrame, copySlot, origSlot, valueMap, isDeep);
    }

    private void copyOwnSlots(DefaultFrame copyFrame, Map valueMap, boolean isDeep) {
        Iterator i = getDefaultKnowledgeBase().getOwnSlots(this).iterator();
        while (i.hasNext()) {
            Slot origSlot = (Slot) i.next();
            if (!origSlot.isSystem()) {
                copyOwnSlot(copyFrame, origSlot, valueMap, isDeep);
            }
        }
    }

    private void copyOwnSlotValues(Frame copyFrame, Slot copySlot, Slot origSlot, Map valueMap, boolean isDeep) {
        ValueType type = getOwnSlotValueType(origSlot);
        Collection origValues = getOwnSlotValues(origSlot);
        if (isDeep && (type == ValueType.INSTANCE || type == ValueType.CLS)) {
            deepCopyFrameBindingValues(origValues, copyFrame, copySlot, valueMap);
        } else {
            copyFrame.setOwnSlotValues(copySlot, origValues);
        }
    }

    public Frame copy(KnowledgeBase kb, Map valueMap, boolean isDeep) {

        Assert.assertNotNull("knowledge base", kb);
        Assert.assertNotNull("value map", valueMap);
        DefaultFrame copy = (DefaultFrame) valueMap.get(this);
        Assert.assertNotNull(null, copy);
        copyOwnSlots(copy, valueMap, isDeep);
        return copy;
    }

    public void delete() {
        getDefaultKnowledgeBase().deleteFrame(this);
    }

    protected KnowledgeBase getDefaultKnowledgeBase() {
        return knowledgeBase;
    }

    public Collection getDocumentation() {
        return getDefaultKnowledgeBase().getDocumentation(this);
    }

    public String getInvalidOwnSlotValuesText(Slot slot, Collection c) {
        return getDefaultKnowledgeBase().getInvalidOwnSlotValuesText(this, slot, c);
    }

    public String getInvalidOwnSlotValueText(Slot slot, Object o) {
        return getDefaultKnowledgeBase().getInvalidOwnSlotValueText(this, slot, o);
    }

    public final KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public void localize(KnowledgeBase kb) {
        knowledgeBase = kb;
    }

    public String getName() {
        String name;
        if (isDeleted()) {
            name = "<<deleted>>";
        } else {
            KnowledgeBase kb = getDefaultKnowledgeBase();
            if (kb == null) {
                name = "<<missing kb, frameid=" + id + ">>";
            } else {
                name = kb.getName(this);
            }
        }
        return name;
    }

    public boolean getOwnSlotAllowsMultipleValues(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotAllowsMultipleValues(this, slot);
    }

    public Collection getOwnSlotAndSubslotValues(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotAndSubslotValues(this, slot);
    }

    public Collection getOwnSlotDefaultValues(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotDefaultValues(this, slot);
    }

    public Collection getOwnSlotFacets(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotFacets(this, slot);
    }

    public Collection getOwnSlotFacetValues(Slot slot, Facet facet) {
        return getDefaultKnowledgeBase().getOwnSlotFacetValues(this, slot, facet);
    }

    public Collection getOwnSlots() {
        return getDefaultKnowledgeBase().getOwnSlots(this);
    }

    public Object getDirectOwnSlotValue(Slot slot) {
        return getDefaultKnowledgeBase().getDirectOwnSlotValue(this, slot);
    }

    public List getDirectOwnSlotValues(Slot slot) {
        return getDefaultKnowledgeBase().getDirectOwnSlotValues(this, slot);
    }

    public Object getOwnSlotValue(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotValue(this, slot);
    }

    public int getOwnSlotValueCount(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotValueCount(this, slot);
    }

    public Collection getOwnSlotValues(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotValues(this, slot);
    }

    public ValueType getOwnSlotValueType(Slot slot) {
        return getDefaultKnowledgeBase().getOwnSlotValueType(this, slot);
    }

    public Project getProject() {
        return knowledgeBase.getProject();
    }

    public Collection getReferences() {
        return getReferences(0);
    }

    public Collection getReferences(int maxReferences) {
        return getDefaultKnowledgeBase().getReferences(this, maxReferences);
    }

    public boolean hasOwnSlot(Slot slot) {
        return getDefaultKnowledgeBase().hasOwnSlot(this, slot);
    }

    public boolean isEditable() {
        return !getState(READONLY_MASK) && !isIncluded() && !isInReadonlyProject();
    }

    private boolean isInReadonlyProject() {
        return getProject() != null && getProject().isReadonly();
    }

    public boolean isIncluded() {
        return getState(INCLUDED_MASK) || isSystem();
    }

    public boolean isSystem() {
        return (id == null) ? false : id.isSystem();
    }

    public boolean isValidOwnSlotValue(Slot slot, Object o) {
        return getDefaultKnowledgeBase().isValidOwnSlotValue(this, slot, o);
    }

    public boolean isVisible() {
        return !isValid() || !getProject().isHidden(this);
    }

    public void moveDirectOwnSlotValue(Slot slot, int fromIndex, int toIndex) {
        getDefaultKnowledgeBase().moveDirectOwnSlotValue(this, slot, fromIndex, toIndex);
    }

    public void removeFrameListener(FrameListener listener) {
        getDefaultKnowledgeBase().removeFrameListener(this, listener);
    }

    public void removeOwnSlotValue(Slot slot, Object value) {
        getDefaultKnowledgeBase().removeOwnSlotValue(this, slot, value);
    }

    public void setDocumentation(String documentation) {
        getDefaultKnowledgeBase().setDocumentation(this, documentation);
    }

    public void setDocumentation(Collection documentation) {
        getDefaultKnowledgeBase().setDocumentation(this, documentation);
    }

    public void setEditable(boolean b) {
        setState(READONLY_MASK, !b);
    }

    public void setIncluded(boolean b) {
        setState(INCLUDED_MASK, b);
    }

    public void setName(String newName) {
        getDefaultKnowledgeBase().setFrameName(this, newName);
    }

    public void setOwnFacetValue(Slot slot, Facet facet, Object value) {
        Assert.fail("not implemented");
    }

    public void setOwnFacetValues(Slot slot, Facet facet, Collection values) {
        Assert.fail("not implemented");
    }

    public void setOwnSlotValue(Slot slot, Object value) {
        setOwnSlotValues(slot, CollectionUtilities.createCollection(value));
    }

    public void setDirectOwnSlotValue(Slot slot, Object value) {
        setDirectOwnSlotValues(slot, CollectionUtilities.createCollection(value));
    }

    public void setDirectOwnSlotValues(Slot slot, Collection values) {
        getDefaultKnowledgeBase().setDirectOwnSlotValues(this, slot, values);
    }

    public void setOwnSlotValues(Slot slot, Collection values) {
        getDefaultKnowledgeBase().setOwnSlotValues(this, slot, values);
    }

    public void setSystem(boolean b) {
        // do nothing
    }

    public void setVisible(boolean v) {
        getProject().setHidden(this, !v);
        getDefaultKnowledgeBase().notifyVisibilityChanged(this);
    }

    public abstract String toString();

    public javax.swing.Icon getIcon() {
        return null;
    }
}