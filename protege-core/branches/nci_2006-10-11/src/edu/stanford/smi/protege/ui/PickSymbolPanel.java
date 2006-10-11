package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * Panel to allow the user to pick a symbol value from a set of symbols.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PickSymbolPanel extends JComponent {
    private JComboBox _comboBox;

    public PickSymbolPanel(String label, Object value, Collection allowedValues) {
        setLayout(new BorderLayout());
        _comboBox = ComponentFactory.createComboBox();
        _comboBox.setModel(new DefaultComboBoxModel(allowedValues.toArray()));
        if (value == null) {
            value = CollectionUtilities.getFirstItem(allowedValues);
        }
        _comboBox.setSelectedItem(value);
        add(new LabeledComponent(label, _comboBox));
        setPreferredSize(new Dimension(300, 60));
    }

    public Object getSelectedValue() {
        return _comboBox.getSelectedItem();
    }
}
