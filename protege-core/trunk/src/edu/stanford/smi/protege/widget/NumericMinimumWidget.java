package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * A slot widget for acquiring the minimum value for both integer and floating point slots.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class NumericMinimumWidget extends FloatFieldWidget {

    private static final long serialVersionUID = -2619432290791581529L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.NUMERIC_MINIMUM);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Minimum", ResourceKey.NUMERIC_MINIMUM_SLOT_WIDGET_LABEL);
    }
}
