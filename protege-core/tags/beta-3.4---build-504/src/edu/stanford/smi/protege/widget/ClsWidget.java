package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;

/**
 * The interface for display forms that display the contents of an entire class and all the contents to be edited.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ClsWidget extends Widget {

    Cls getCls();

    Instance getInstance();

    SlotWidget getSlotWidget(Slot slot);
    void replaceWidget(Slot slot, String className);

    void layoutLikeCls(Cls cls);

    void relayout();

    void removeCustomizations();

    Cls getAssociatedCls();
    void setAssociatedCls(Cls associatedCls);

    void setInstance(Instance instance);

    void setup(WidgetDescriptor descriptor, boolean isDesignTime, Project project, Cls cls);

    void addWidgetListener(WidgetListener listener);
    void removeWidgetListener(WidgetListener listener);
}
