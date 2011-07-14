package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slow widget for acquiring the minimum cardinality for a slot.  Logically this is a single value but this widget 
 * has two ways to set the value so it is somewhat complicated.  The desire is to maintain the simple concept of
 * "required" while still allowing the user to specify exactly how many if he desires.  Even if we left off the 
 * "required" check box (to simplify things) we would have to somehow specify how to say "not required".
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MinimumCardinalityWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -28100150611436999L;
    private JCheckBox _isRequiredComponent;
    private IntegerField _atLeastComponent;

    private ActionListener _buttonListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            _atLeastComponent.removeActionListener(_textFieldListener);
            if (_isRequiredComponent.isSelected()) {
                _atLeastComponent.setValue(1);
            } else {
                _atLeastComponent.clearValue();
            }
            _atLeastComponent.addActionListener(_textFieldListener);
            valueChanged();
        }
    };
    private ActionListener _textFieldListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            Integer i = _atLeastComponent.getValue();
            boolean optional = i == null || i.intValue() == 0;
            _isRequiredComponent.removeActionListener(_buttonListener);
            _isRequiredComponent.setSelected(!optional);
            _isRequiredComponent.addActionListener(_buttonListener);
            valueChanged();
        }
    };

    private Component createAtLeastComponent() {
        String label = LocalizedText.getText(ResourceKey.AT_LEAST_LABEL);
        _atLeastComponent = new IntegerField(label);
        _atLeastComponent.addActionListener(_textFieldListener);
        JComponent c = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        c.add(_atLeastComponent);
        return c;
    }

    private JComponent createIsRequiredComponent() {
        String label = LocalizedText.getText(ResourceKey.REQUIRED_LABEL);
        _isRequiredComponent = ComponentFactory.createCheckBox(label);
        _isRequiredComponent.addActionListener(_buttonListener);
        return _isRequiredComponent;
    }

    public Collection getValues() {
        Integer min = _atLeastComponent.getValue();
        return CollectionUtilities.createCollection(min);
    }

    public void initialize() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(createIsRequiredComponent());
        panel.add(createAtLeastComponent());
        add(new LabeledComponent(getLabel(), panel, false));
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.MINIMUM_CARDINALITY);
    }

    public void setEditable(boolean editable) {
        _isRequiredComponent.setEnabled(editable);
        _atLeastComponent.setEnabled(editable);
    }

    public void setValues(Collection values) {
        Integer i = (Integer) getFirstItem(values);
        boolean isRequired = i != null && i.intValue() > 0;
        _isRequiredComponent.setSelected(isRequired);
        if (isRequired) {
            _atLeastComponent.setValue(i.intValue());
        } else {
            _atLeastComponent.clearValue();
        }
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Cardinality", ResourceKey.MINIMUM_CARDINALITY_SLOT_WIDGET_LABEL);
    }
}
