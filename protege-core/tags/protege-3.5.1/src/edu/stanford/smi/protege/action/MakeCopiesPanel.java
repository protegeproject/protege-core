package edu.stanford.smi.protege.action;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

class MakeCopiesPanel extends JComponent {
    private static final long serialVersionUID = 8117747607093156822L;
    private static boolean _lastIsDeep = false;
    private static Integer _lastNumberOfCopies = new Integer(1);
    private JTextField _textField = new JTextField();
    private JCheckBox _checkBox = new JCheckBox("Deep Copy (copy all referenced instances)");

    MakeCopiesPanel() {
        setLayout(new GridLayout(0, 1));
        JPanel panel = new JPanel();
        panel.add(ComponentFactory.createLabel("Number of Copies"));
        panel.add(_textField);
        add(panel);
        add(_checkBox);
        _checkBox.setSelected(_lastIsDeep);
        _textField.setText(_lastNumberOfCopies.toString());
        _textField.setColumns(5);
    }

    public Integer getNumberOfCopies() {
        String text = _textField.getText();
        text = text.trim();
        Integer i = null;
        try {
            i = Integer.valueOf(text);
        } catch (NumberFormatException e) {
            // do nothing
        }
        if (i != null) {
            _lastNumberOfCopies = i;
        }
        return i;
    }

    public boolean getIsDeepCopy() {
        _lastIsDeep = _checkBox.isSelected();
        return _lastIsDeep;
    }
}
