package edu.stanford.smi.protege.model;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Default implementation of Slot interface.  Forwards all method calls
 * to its DefaultKnowledgeBase.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultSlot extends DefaultInstance implements Slot {

    private static final long serialVersionUID = -8171388940662733885L;

    public DefaultSlot(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }

    public DefaultSlot() {

    }

    public void addDirectSuperslot(Slot superslot) {
        getDefaultKnowledgeBase().addDirectSuperslot(this, superslot);
    }

    public void addSlotListener(SlotListener listener) {
        getDefaultKnowledgeBase().addSlotListener(this, listener);
    }

    public Frame deepCopy(KnowledgeBase kb, Map valueMap) {
        Assert.fail("not implemented");
        return this;
    }

    public Collection getAllowedClses() {
        return getDefaultKnowledgeBase().getAllowedClses(this);
    }

    public Collection getAllowedParents() {
        return getDefaultKnowledgeBase().getAllowedParents(this);
    }

    public Collection getAllowedValues() {
        return getDefaultKnowledgeBase().getAllowedValues(this);
    }

    public boolean getAllowsMultipleValues() {
        return getDefaultKnowledgeBase().getAllowsMultipleValues(this);
    }

    public Facet getAssociatedFacet() {
        return getDefaultKnowledgeBase().getAssociatedFacet(this);
    }

    public Collection getDefaultValues() {
        return getDefaultKnowledgeBase().getDefaultValues(this);
    }

    public int getDirectSubslotCount() {
        return getDefaultKnowledgeBase().getDirectSubslotCount(this);
    }

    public Collection getDirectSubslots() {
        return getDefaultKnowledgeBase().getDirectSubslots(this);
    }

    public int getDirectSuperslotCount() {
        return getDefaultKnowledgeBase().getDirectSuperslotCount(this);
    }

    public Collection getDirectSuperslots() {
        return getDefaultKnowledgeBase().getDirectSuperslots(this);
    }

    public Collection getDocumentation() {
        return getDefaultKnowledgeBase().getDocumentation(this);
    }

    public Slot getInverseSlot() {
        return getDefaultKnowledgeBase().getInverseSlot(this);
    }

    public int getMaximumCardinality() {
        return getDefaultKnowledgeBase().getMaximumCardinality(this);
    }

    public Number getMaximumValue() {
        return getDefaultKnowledgeBase().getMaximumValue(this);
    }

    public int getMinimumCardinality() {
        return getDefaultKnowledgeBase().getMinimumCardinality(this);
    }

    public Number getMinimumValue() {
        return getDefaultKnowledgeBase().getMinimumValue(this);
    }

    public Collection getSubslots() {
        return getDefaultKnowledgeBase().getSubslots(this);
    }

    public Collection getSuperslots() {
        return getDefaultKnowledgeBase().getSuperslots(this);
    }

    public boolean hasDirectSuperslot(Slot slot) {
        return getDefaultKnowledgeBase().hasDirectSuperslot(this, slot);
    }

    public boolean hasSuperslot(Slot slot) {
        return getDefaultKnowledgeBase().hasSuperslot(this, slot);
    }

    public void moveDirectSubslot(Slot slotToMove, Slot afterSlot) {
        getDefaultKnowledgeBase().moveDirectSubslot(this, slotToMove, afterSlot);
    }

    /**
     * @deprecated
     */
    public Collection getTemplateSlotClses() {
        return getDirectDomain();
    }

    public Collection getDirectDomain() {
        return getDefaultKnowledgeBase().getDirectDomain(this);
    }

    public Collection getDomain() {
        return getDefaultKnowledgeBase().getDomain(this);
    }

    public Collection getValues() {
        return getDefaultKnowledgeBase().getValues(this);
    }

    public ValueType getValueType() {
        return getDefaultKnowledgeBase().getValueType(this);
    }

    public boolean hasValueAtSomeFrame() {
        return getDefaultKnowledgeBase().hasSlotValueAtSomeFrame(this);
    }

    public void removeDirectSuperslot(Slot superslot) {
        getDefaultKnowledgeBase().removeDirectSuperslot(this, superslot);
    }

    public void removeSlotListener(SlotListener listener) {
        getDefaultKnowledgeBase().removeSlotListener(this, listener);
    }

    public void setAllowedClses(Collection c) {
        getDefaultKnowledgeBase().setAllowedClses(this, c);
    }

    public void setAllowedParents(Collection c) {
        getDefaultKnowledgeBase().setAllowedParents(this, c);
    }

    public void setAllowedValues(Collection c) {
        getDefaultKnowledgeBase().setAllowedValues(this, c);
    }

    public void setAllowsMultipleValues(boolean b) {
        getDefaultKnowledgeBase().setAllowsMultipleValues(this, b);
    }

    public void setAssociatedFacet(Facet facet) {
        getDefaultKnowledgeBase().setAssociatedFacet(this, facet);
    }

    public void setDefaultValues(Collection values) {
        getDefaultKnowledgeBase().setDefaultValues(this, values);
    }

    public void setDirectTypeOfSubslots(Cls cls) {
        getDefaultKnowledgeBase().setDirectTypeOfSubslots(this, cls);
    }

    public void setDocumentation(String doc) {
        Collection docs = CollectionUtilities.createCollection(doc);
        getDefaultKnowledgeBase().setDocumentation(this, docs);
    }

    public void setInverseSlot(Slot slot) {
        getDefaultKnowledgeBase().setInverseSlot(this, slot);
    }

    public void setMaximumCardinality(int max) {
        getDefaultKnowledgeBase().setMaximumCardinality(this, max);
    }

    public void setMaximumValue(Number n) {
        getDefaultKnowledgeBase().setMaximumValue(this, n);
    }

    public void setMinimumCardinality(int min) {
        getDefaultKnowledgeBase().setMinimumCardinality(this, min);
    }

    public void setMinimumValue(Number n) {
        getDefaultKnowledgeBase().setMinimumValue(this, n);
    }

    public void setValues(Collection values) {
        getDefaultKnowledgeBase().setValues(this, values);
    }

    public void setValueType(ValueType type) {
        getDefaultKnowledgeBase().setValueType(this, type);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Slot(");
        buffer.append(getName());
        buffer.append(")");
        return buffer.toString();
    }

    public Icon getIcon() {
        return Icons.getSlotIcon(false, false, !isEditable(), !isVisible());
    }
    
    public Slot rename(String name) {
        return (Slot) super.rename(name);
    }
}
