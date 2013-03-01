package edu.stanford.smi.protege.model;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.Icon;

import edu.stanford.smi.protege.event.FacetListener;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;

/**
 * Default implementation of Facet interface.  Forwards all method calls
 * to its DefaultKnowledgeBase.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultFacet extends DefaultInstance implements Facet {

    private static final long serialVersionUID = 1557703868805025229L;
    private FacetConstraint _constraint;

    /* from Externalizable interface
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _constraint = (FacetConstraint) in.readObject();
        super.readExternal(in);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_constraint);
        super.writeExternal(out);
    }
    */

    public DefaultFacet(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }

    public DefaultFacet() {

    }

    public void addFacetListener(FacetListener listener) {
        getDefaultKnowledgeBase().addFacetListener(this, listener);
    }

    public boolean areValidValues(Frame frame, Slot slot, Collection slotValues) {
        boolean result = true;
        if (_constraint != null) {
            Collection facetValues = frame.getOwnSlotFacetValues(slot, this);
            result = _constraint.areValidValues(frame, slot, slotValues, facetValues);
        }
        return result;
    }

    public Slot getAssociatedSlot() {
        return getDefaultKnowledgeBase().getAssociatedSlot(this);
    }

    public FacetConstraint getConstraint() {
        return _constraint;
    }

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues) {
        String result = null;
        if (_constraint != null) {
            Collection facetValues = frame.getOwnSlotFacetValues(slot, this);
            result = _constraint.getInvalidValuesText(frame, slot, slotValues, facetValues);
        }
        return result;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object item) {
        String result = null;
        if (_constraint != null) {
            Collection facetValues = frame.getOwnSlotFacetValues(slot, this);
            result = _constraint.getInvalidValueText(frame, slot, item, facetValues);
        }
        return result;
    }

    public ValueType getValueType() {
    	Slot associatedSlot = getAssociatedSlot();
    	
    	if (associatedSlot == null) {
    		Log.getLogger().warning("Cannot find associated slot of " + this + ". Assuming value type is " + ValueType.ANY);
    		return ValueType.ANY;
    	}
        return associatedSlot.getValueType();
    }

    public boolean getAllowsMultipleValues() {
    	Slot associatedSlot = getAssociatedSlot();
    	
    	if (associatedSlot == null) {
    		Log.getLogger().warning("Cannot find associated slot of " + this + ". Assuming cardinality is multiple");
    		return true;
    	}
        return getAssociatedSlot().getAllowsMultipleValues();
    }

    public boolean isValidValue(Frame frame, Slot slot, Object value) {
        boolean result = true;
        if (_constraint != null) {
            Collection facetValues = frame.getOwnSlotFacetValues(slot, this);
            result = _constraint.isValidValue(frame, slot, value, facetValues);
        }
        return result;
    }

    public void removeFacetListener(FacetListener listener) {
        getDefaultKnowledgeBase().removeFacetListener(this, listener);
    }

    public void setAssociatedSlot(Slot slot) {
        getDefaultKnowledgeBase().setAssociatedSlot(this, slot);
    }

    public void setConstraint(FacetConstraint c) {
        _constraint = c;
    }

    public Collection resolveValues(Collection existingValues, Collection newValues) {
        Collection values;
        if (CollectionUtilities.equalsList(existingValues, newValues)) {
            values = existingValues;
        } else if (_constraint == null) {
            if (getAllowsMultipleValues()) {
                values = new LinkedHashSet();
                values.addAll(existingValues);
                values.addAll(newValues);
            } else {
                values = existingValues;
            }
        } else {
            values = _constraint.resolve(existingValues, newValues);
        }
        return values;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Facet(");
        buffer.append(getName());
        buffer.append(")");
        return buffer.toString();
    }

    public Icon getIcon() {
        return Icons.getFacetIcon(!isEditable(), !isVisible());
    }
    
    public Facet rename(String name) {
        return (Facet) super.rename(name);
    }
}
