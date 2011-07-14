package edu.stanford.smi.protege.model;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Base class for implementions of FacetConstraint.  Implementations are provided for the is/are valid methods that just
 * get the "invalid text" descriptions of the problems and test it.  If there is "invalid text" then the values are
 * not valid.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractFacetConstraint implements FacetConstraint, Serializable {

    private static final long serialVersionUID = 3517596380804103055L;

    public boolean areValidValues(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        return getInvalidValuesText(frame, slot, slotValues, facetValues) == null;
    }

    public boolean isValidValue(Frame frame, Slot slot, Object o, Collection facetValues) {
        return getInvalidValueText(frame, slot, o, facetValues) == null;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}
