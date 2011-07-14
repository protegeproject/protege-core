package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Slot Widget for displaying the subclasses of a given class.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SubclassWidget extends ClsListWidget {

    private static final long serialVersionUID = 5048613688794087001L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return ClsListWidget.isSuitable(cls, slot, facet);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), null, ResourceKey.SUBCLASSES_SLOT_WIDGET_LABEL);
    }
}
