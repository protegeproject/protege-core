package edu.stanford.smi.protege.model;

import java.util.*;

/**
 * Programmatic implementation of the constraint expressed by a specific facet.  For example, an implementation of 
 * this interface would know about what it means to have "maximum cardinality" and would be able to check to see if
 * a particular value was consistent with this constraint.  The Facet itself is really just a placeholder and has no 
 * knowledge of the actual constraint that it represents.  The logic is all in the implementations of this interface.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FacetConstraint {

    boolean areValidValues(Frame frame, Slot slot, Collection slotValues, Collection facetValues);

    String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues);

    String getInvalidValueText(Frame frame, Slot slot, Object value, Collection facetValues);

    boolean isValidValue(Frame frame, Slot slot, Object item, Collection facetValues);

    Collection resolve(Collection existingValues, Collection newValues);
}
