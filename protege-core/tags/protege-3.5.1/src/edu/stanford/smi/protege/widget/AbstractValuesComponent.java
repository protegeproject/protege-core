package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for the "values" component that appears beneath the "type selection" combobox in the slots form.  This
 * widge is different (or absent) depending on the value selected in the combobox.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
abstract class AbstractValuesComponent extends AbstractSelectableComponent implements ValuesComponent, Disposable {

    private static final long serialVersionUID = -4480165504667698341L;

    public AbstractValuesComponent() {
        setLayout(new BorderLayout());
    }

    public void dispose() {
    }

    public JComponent getComponent() {
        return this;
    }

    public KnowledgeBase getKnowledgeBase() {
        return getWidget().getKnowledgeBase();
    }

    public Collection getValues() {
        return null;
    }

    private SlotWidget getWidget() {
        return (SlotWidget) getParent();
    }

    protected Slot getSlotInstance() {
        return (Slot) getWidget().getInstance();
    }

    protected Cls getAssociatedCls() {
        return getWidget().getAssociatedCls();
    }

    public void setEditable(boolean b) {
        // do nothing
    }

    public void setValues(Collection values) {
    }

    public void valueChanged() {
        ((AbstractSlotWidget) getParent()).valueChanged();
    }

    protected boolean isOverride() {
        return getAssociatedCls() != null;
    }
}
