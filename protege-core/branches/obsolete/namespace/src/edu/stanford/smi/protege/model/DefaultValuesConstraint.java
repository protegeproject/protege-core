package edu.stanford.smi.protege.model;

import java.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultValuesConstraint extends AbstractFacetConstraint {

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        return null;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object value, Collection facetValues) {
        return null;
    }

    public Collection resolve(Collection existingValues, Collection newValues) {
        Collection values;
        if (existingValues.isEmpty()) {
            values = newValues;
        } else {
            values = existingValues;
        }
        return values;
    }

}
