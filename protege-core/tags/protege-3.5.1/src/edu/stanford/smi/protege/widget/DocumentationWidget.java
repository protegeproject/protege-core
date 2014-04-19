package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * The slot widget for displaying and editing frame documentation.  This looks trivial but there is something funny 
 * going on behind the scenes.  Documentation is a cardinality multiple slot because the OKBC spec declares it to be so.
 * This would be clumsy (and pointless) to use though, especially since there is no "key" for each of the "values" of 
 * documentation.  Thus we just use a cardinality single widget to edit this cardinality multiple slot.  The down side
 * of this is that if the slot actually does have multiple values (set with the api, for example), not only will they
 * not get displayed, they will get lost when the user edits the displayed (first) value.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DocumentationWidget extends TextAreaWidget {

    private static final long serialVersionUID = 6168430319953436846L;

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.DOCUMENTATION);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Documentation", ResourceKey.DOCUMENTATION_SLOT_WIDGET_LABEL);
    }
}
