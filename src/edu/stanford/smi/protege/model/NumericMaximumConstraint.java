package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Constraint on the maximum value of a slot.  This only has meaning for Integer and Float slots.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class NumericMaximumConstraint extends AbstractFacetConstraint {

    private static final long serialVersionUID = 8433560444246377129L;

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        String result = null;
        Number n = (Number) CollectionUtilities.getFirstItem(facetValues);
        if (n != null) {
            double max = n.doubleValue();
            Iterator i = slotValues.iterator();
            while (result == null && i.hasNext()) {
                Object value = i.next();
                result = getInvalidValueText(max, value);
            }
        }
        return result;
    }

    private static String getInvalidValueText(double max, Object value) {
        String result = null;
        if (value instanceof Number) {
            Number n = (Number) value;
            if (n.doubleValue() > max) {
                result = "The maximum value is " + max;
            }
        }
        return result;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object value, Collection facetValues) {
        String result = null;
        Number n = (Number) CollectionUtilities.getFirstItem(facetValues);
        if (n != null) {
            double max = n.doubleValue();
            result = getInvalidValueText(max, value);
        }
        return result;
    }

    public Collection resolve(Collection existingValues, Collection newValues) {
        // lower value wins
        double n1 = getValue(existingValues);
        double n2 = getValue(newValues);
        return n1 < n2 ? existingValues : newValues;
    }

    private static double getValue(Collection values) {
        Number n = (Number) CollectionUtilities.getFirstItem(values);
        return (n == null) ? Double.MAX_VALUE : n.doubleValue();
    }
}
