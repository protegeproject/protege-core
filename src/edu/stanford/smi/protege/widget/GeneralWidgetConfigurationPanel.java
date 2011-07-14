package edu.stanford.smi.protege.widget;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * The "general" panel on the slot widget configuration dialog.  This panel appears for all widgets.  It give the class
 * and slot of the widget and allows the user to set the label displayed on the widget.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class GeneralWidgetConfigurationPanel extends AbstractValidatableComponent {
    private static final long serialVersionUID = -3148523589303750172L;
    private JTextField _labelField;
    private JTextField _tooltipField;
    private SlotWidget _widget;

    public GeneralWidgetConfigurationPanel(SlotWidget widget) {
        _widget = widget;
        setLayout(new GridLayout(0, 1, 10, 10));
        add(createClsNameField(widget));
        add(createSlotNameField(widget));
        add(createLabelNameField(widget));
        add(createToolTipNameField(widget));
    }

    private JComponent createClsNameField(SlotWidget widget) {
        return createFrameField("Class", widget.getCls());
    }

    private JTextField createField(String text) {
        JTextField field = ComponentFactory.createTextField();
        field.setText(text);
        return field;
    }

    private JComponent createFrameField(String label, Instance frame) {
        String text = (frame == null) ? "" : frame.getName();
        JTextField field = createField(text);
        field.setEditable(false);
        return new LabeledComponent(label, field);
    }

    private JComponent createLabelNameField(Widget widget) {
        _labelField = createField(widget.getLabel());
        return new LabeledComponent("Label", _labelField);
    }

    private JComponent createSlotNameField(SlotWidget widget) {
        return createFrameField("Slot", widget.getSlot());
    }

    private JComponent createToolTipNameField(SlotWidget widget) {
        _tooltipField = createField(widget.getDefaultToolTip());
        return new LabeledComponent("Default Tool Tip", _tooltipField);
    }

    public String getLabel() {
        return _labelField.getText();
    }

    public void saveContents() {
        _widget.setLabel(_labelField.getText());
        _widget.setDefaultToolTip(_tooltipField.getText());
    }

    public boolean validateContents() {
        return true;
    }
}
