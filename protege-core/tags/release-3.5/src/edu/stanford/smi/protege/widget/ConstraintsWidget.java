package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * SlotWidget for displaying "constraints".  This is really no different at the moment than a standard list box. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConstraintsWidget extends InstanceListWidget {

    private static final long serialVersionUID = 8250523663174086212L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable = InstanceListWidget.isSuitable(cls, slot, facet);
        if (isSuitable) {
            isSuitable = slot.getName().equals(Model.Slot.CONSTRAINTS);
        }
        return isSuitable;
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Constraints", ResourceKey.CONSTRAINTS_SLOT_WIDGET_LABEL);
    }
}
