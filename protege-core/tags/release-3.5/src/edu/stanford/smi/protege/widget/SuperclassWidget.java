package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Slot widget for displaying the superclasses of a given class.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SuperclassWidget extends ClsListWidget {

    private static final long serialVersionUID = -8447260232288915005L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return ClsListWidget.isSuitable(cls, slot, facet);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), null, ResourceKey.SUPERCLASSES_SLOT_WIDGET_LABEL);
    }
}
