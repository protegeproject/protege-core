package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Slot widget for displaying the superclasses of a given class.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SuperclassWidget extends ClsListWidget {

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return ClsListWidget.isSuitable(cls, slot, facet);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), null, ResourceKey.SUPERCLASSES_SLOT_WIDGET_LABEL);
    }
}
