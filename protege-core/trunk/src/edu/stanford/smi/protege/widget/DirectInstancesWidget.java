package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.resource.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DirectInstancesWidget extends InstanceListWidget {
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), null, ResourceKey.DIRECT_INSTANCES_SLOT_WIDGET_LABEL);
    }
}
