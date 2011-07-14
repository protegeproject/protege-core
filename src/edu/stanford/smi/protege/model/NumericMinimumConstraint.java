package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Constraint on the minimum value of a slot.  This only has meaning for Integer and Float slots.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class NumericMinimumConstraint extends AbstractFacetConstraint {

    private static final long serialVersionUID = -7642501607185450946L;

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        String result = null;
        Number n = (Number) CollectionUtilities.getFirstItem(facetValues);
        if (n != null) {
            double min = n.doubleValue();
            Iterator i = slotValues.iterator();
            while (i.hasNext()) {
                Object value = i.next();
                result = getInvalidValueText(min, value);
            }
        }
        return result;
    }

    private static String getInvalidValueText(double min, Object value) {
        String result = null;
        if (value instanceof Number) {
            Number n = (Number) value;
            if (n.doubleValue() < min) {
                result = "The minimum value is " + min;
            }
        }
        return result;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object o, Collection facetValues) {
        String result = null;
        Number n = (Number) CollectionUtilities.getFirstItem(facetValues);
        if (n != null) {
            double min = n.doubleValue();
            result = getInvalidValueText(min, o);
        }
        return result;
    }

    public Collection resolve(Collection existingValues, Collection newValues) {
        // higher value wins
        double d1 = getValue(existingValues);
        double d2 = getValue(newValues);
        return d1 > d2 ? existingValues : newValues;
    }

    private static double getValue(Collection values) {
        Number n = (Number) CollectionUtilities.getFirstItem(values);
        return (n == null) ? Double.MIN_VALUE : n.doubleValue();
    }
}
