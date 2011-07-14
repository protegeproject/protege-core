package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slow widget for acquiring the maximum cardinality for a slot.  Logically this is a single value but this widget 
 * has two ways to set the value so it is somewhat complicated.  The desire is to maintain the simple concept of
 * "multiple" while still allowing the user to specify exactly how many if he desires.  Even if we left off the 
 * "multiple" check box (to simplify things) we would have to somehow specify how to say "any number".
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MaximumCardinalityWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = 1980239748634279491L;
    private JCheckBox _isMultipleComponent;
    private IntegerField _atMostComponent;

    private ActionListener _buttonListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            _atMostComponent.removeActionListener(_textFieldListener);
            if (_isMultipleComponent.isSelected()) {
                _atMostComponent.clearValue();
            } else {
                _atMostComponent.setValue(1);
            }
            _atMostComponent.addActionListener(_textFieldListener);
            valueChanged();
        }
    };
    private ActionListener _textFieldListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            Integer i = _atMostComponent.getValue();
            boolean multiple = i != null && i.intValue() > 1;
            _isMultipleComponent.removeActionListener(_buttonListener);
            _isMultipleComponent.setSelected(multiple);
            _isMultipleComponent.addActionListener(_buttonListener);
            valueChanged();
        }
    };

    private Component createAtMostComponent() {
        String label = LocalizedText.getText(ResourceKey.AT_MOST_LABEL);
        _atMostComponent = new IntegerField(label);
        _atMostComponent.addActionListener(_textFieldListener);
        JComponent c = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        c.add(_atMostComponent);
        return c;
    }

    private JComponent createIsMultipleComponent() {
        String label = LocalizedText.getText(ResourceKey.MULTIPLE_LABEL);
        _isMultipleComponent = ComponentFactory.createCheckBox(label);
        _isMultipleComponent.addActionListener(_buttonListener);
        return _isMultipleComponent;
    }

    public Collection getValues() {
        Integer max = _atMostComponent.getValue();
        return CollectionUtilities.createCollection(max);
    }

    public void initialize() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(createIsMultipleComponent());
        panel.add(createAtMostComponent());
        JPanel p = new JPanel(new BorderLayout());
        p.add(panel, BorderLayout.NORTH);
        add(p);
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.MAXIMUM_CARDINALITY);
    }

    public void setEditable(boolean editable) {
        _isMultipleComponent.setEnabled(editable);
        _atMostComponent.setEnabled(editable);
    }

    public void setValues(Collection values) {
        Integer i = (Integer) getFirstItem(values);
        boolean isMultiple = (i == null) || i.intValue() > 1;

        _isMultipleComponent.removeActionListener(_buttonListener);
        _atMostComponent.removeActionListener(_textFieldListener);

        _isMultipleComponent.setSelected(isMultiple);
        if (i == null) {
            _atMostComponent.clearValue();
        } else {
            _atMostComponent.setValue(i.intValue());
        }

        _isMultipleComponent.addActionListener(_buttonListener);
        _atMostComponent.addActionListener(_textFieldListener);
    }
}
