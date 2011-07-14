package edu.stanford.smi.protege.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Default implementation of Frame interface. Forwards all method calls to its DefaultKnowledgeBase.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DefaultFrame implements Frame, Localizable, Serializable {
    private static final long serialVersionUID = -894053746814542694L;

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

    protected DefaultFrame() {

    }

    //ESCA-JAVA0016 
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
    //ESCA-JAVA0130 
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
    	if (s1 != null && s1.length() != 0 && s1.charAt(0) == '\'') {
    		s1 = s1.substring(1);
    	}
    	if (s2 != null && s2.length() != 0 && s2.charAt(0) == '\'') {
    		s2 = s2.substring(1);
    	}
        int result = s1.compareToIgnoreCase(s2);
        if (result == 0) {
            result = s1.compareTo(s2);
        }
        return result;
    }

    public int compareTo(Frame f2) {
        int result;
        String t1 = getBrowserText();
        String t2 = f2.getBrowserText();
        if (isSpecialName(t1)) {
            result = isSpecialName(t2) ? compareStrings(t1, t2) : +1;
        } else if (isSpecialName(t2)) {
            result = -1;
        } else {
            result = compareStrings(t1, t2);
        }
        if (result == 0) {
            result = compareStrings(getName(), f2.getName());
        }
        return result;
    }

    private static boolean isSpecialName(String s) {
        return s.length() > 0 && s.charAt(0) == SPECIAL_NAME_CHAR;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private static void deepCopyFrameBindingValues(Collection values, Frame copyFrame, Slot copySlot, Map valueMap) {

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
        Collection origValues = getDirectOwnSlotValues(origSlot);
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
        id.localize(kb);
    }

    public String getName() {
        return getFrameID().getName();
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

    public Collection<Slot> getOwnSlots() {
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

    public Collection<Reference> getReferences() {
        return getReferences(0);
    }

    public Collection<Reference> getReferences(int maxReferences) {
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
        return knowledgeBase.getSystemFrames().isSystem(this);
    }

    public boolean isValidOwnSlotValue(Slot slot, Object o) {
        return getDefaultKnowledgeBase().isValidOwnSlotValue(this, slot, o);
    }

    public boolean isVisible() {
        return !getProject().isHidden(this);
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

    public Icon getIcon() {
        return null;
    }
    
    public Frame rename(String name) {
        return getKnowledgeBase().rename(this, name);
    }
    
    public void assertFrameName() {
    	getKnowledgeBase().assertFrameName(this);
    }
}