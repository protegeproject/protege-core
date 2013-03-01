package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * A panel that allows the user to edit a string.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class EditStringPanel extends JComponent {
    private static final long serialVersionUID = -9221546178390989869L;
    private JTextField _textField;

    public EditStringPanel(Object o, String label) {
        setLayout(new BorderLayout());
        _textField = ComponentFactory.createTextField();
        _textField.setText((o == null) ? "" : o.toString());
        add(new LabeledComponent(label, _textField));
        setPreferredSize(new Dimension(300, 60));
    }

    public void addNotify() {
        super.addNotify();
        ComponentUtilities.requestFocus(_textField);
    }

    public String getText() {
        return _textField.getText();
    }

}
