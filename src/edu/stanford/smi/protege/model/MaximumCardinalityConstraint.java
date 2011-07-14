package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Constraint on the maximum number of values that a slot may have.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MaximumCardinalityConstraint extends AbstractFacetConstraint {
    private static final long serialVersionUID = 1151034475072958857L;
    private static final Integer CARDINALITY_SINGLE = new Integer(1);

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        String result = null;
        Integer i = (Integer) CollectionUtilities.getFirstItem(facetValues);
        if (i != null) {
            int max = i.intValue();
            if (slotValues.size() > max) {
                result = "Only " + max + " value" + ((max == 1) ? " is" : "s are") + " allowed";
                result += " - " + CollectionUtilities.toString(slotValues);
            }
        }
        return result;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object o, Collection facetValues) {
        return null;
    }

    public static boolean allowsMultipleValues(Integer i) {
        return (i == null) ? true : i.intValue() > 1;
    }

    public static int getValue(Integer i) {
        return (i == null) ? edu.stanford.smi.protege.model.KnowledgeBase.MAXIMUM_CARDINALITY_UNBOUNDED : i.intValue();
    }

    public static Integer getValue(int i) {
        return (i < 0) ? null : new Integer(i);
    }

    public static Integer getValue(boolean allowsMultiple) {
        return allowsMultiple ? null : CARDINALITY_SINGLE;
    }

    public static Collection getValues(boolean allowsMultiple) {
        return allowsMultiple ? Collections.EMPTY_SET : Collections.singleton(CARDINALITY_SINGLE);
    }

    public Collection resolve(Collection existingValues, Collection newValues) {
        // lower value wins
        int i1 = getValue(existingValues);
        int i2 = getValue(newValues);
        return i1 <= i2 ? existingValues : newValues;
    }

    private static int getValue(Collection values) {
        Integer i = (Integer) CollectionUtilities.getFirstItem(values);
        return (i == null) ? Integer.MAX_VALUE : i.intValue();
    }
}