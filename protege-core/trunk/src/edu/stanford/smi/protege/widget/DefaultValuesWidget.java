package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Widget for acquiring default values.  Note that the type of the value depends on the current type of the slot.  The
 * logic to make this happen all occurs in the base class.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultValuesWidget extends SlotValuesWidget {

    private static final long serialVersionUID = -3830676259276684642L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.DEFAULTS);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Default", ResourceKey.DEFAULT_SLOT_WIDGET_LABEL);
    }
}
