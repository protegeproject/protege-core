package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;

/**
 * Fundamental interface for all slot widgets.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface SlotWidget extends Widget {

    Cls getCls();

    Instance getInstance();

    Slot getSlot();

    Cls getAssociatedCls();
    void setAssociatedCls(Cls associatedCls);

    void setInstance(Instance instance);

    void setup(WidgetDescriptor descriptor, boolean isDesignTime, Project project, Cls cls, Slot slot);
}
