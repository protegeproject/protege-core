package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Text field for acquiring and displaying an integer.
 */

public class IntegerField extends JComponent {
    private static final long serialVersionUID = 6434174131636406043L;
    private ActionListener itsActionListener;
    private JTextField _textField;
    private JLabel _label;

    public IntegerField(String text) {
        setLayout(new BorderLayout());
        _label = ComponentFactory.createLabel(text);
        _label.setForeground(UIManager.getColor("Checkbox.foreground"));
        createTextField();

        JPanel north = new JPanel(new BorderLayout());
        north.add(_label, BorderLayout.WEST);
        north.add(_textField, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
    }

    public void addActionListener(ActionListener listener) {
        itsActionListener = listener;
    }

    public void clearValue() {
        _textField.setText("");
    }

    private JComponent createTextField() {
        _textField = ComponentFactory.createTextField();
        _textField.setHorizontalAlignment(SwingConstants.RIGHT);
        _textField.getDocument().addDocumentListener(new DocumentChangedListener() {
            public void stateChanged(ChangeEvent event) {
                notifyChanged();
            }
        });
        _textField.setColumns(3);
        return _textField;
    }

    public Integer getValue() {
        Integer i;
        try {
            String s = _textField.getText();
            i = new Integer(s);
        } catch (NumberFormatException e) {
            i = null;
        }
        return i;
    }

    private void notifyChanged() {
        if (itsActionListener != null) {
            itsActionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }
    }

    public void removeActionListener(ActionListener listener) {
        itsActionListener = null;
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        _textField.setEnabled(b);
        _label.setEnabled(b);
    }

    public void setValue(int i) {
        _textField.setText(String.valueOf(i));
        _textField.repaint();
    }
}
