package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * A slot widget for acquiring the maximum value for both integer and floating point slots.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class NumericMaximumWidget extends FloatFieldWidget {

    private static final long serialVersionUID = 8011573789074682964L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.NUMERIC_MAXIMUM);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Maximum", ResourceKey.NUMERIC_MAXIMUM_SLOT_WIDGET_LABEL);
    }
}
