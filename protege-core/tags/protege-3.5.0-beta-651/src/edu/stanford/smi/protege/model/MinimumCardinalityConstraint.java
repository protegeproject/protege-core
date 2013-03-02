package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * A constraint on the minimum number of values that a slot may contain.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MinimumCardinalityConstraint extends AbstractFacetConstraint {

    private static final long serialVersionUID = 4399402013983647812L;

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        String result = null;
        Integer i = (Integer) CollectionUtilities.getFirstItem(facetValues);
        if (i != null) {
            int min = i.intValue();
            if (slotValues.size() < min) {
                if (min == 1) {
                    result = "A value is required";
                } else {
                    result = "At least " + min + " values are required";
                }
            }
        }
        return result;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object o, Collection facetValues) {
        return null;
    }

    public static int getValue(Integer i) {
        return (i == null) ? 0 : i.intValue();
    }

    public static Integer getValue(int i) {
        return (i == 0) ? null : new Integer(i);
    }

    public Collection resolve(Collection existingValues, Collection newValues) {
        // higher value wins
        int i1 = getValue(existingValues);
        int i2 = getValue(newValues);
        return i1 >= i2 ? existingValues : newValues;
    }

    private static int getValue(Collection values) {
        Integer i = (Integer) CollectionUtilities.getFirstItem(values);
        return (i == null) ? Integer.MIN_VALUE : i.intValue();
    }
}
