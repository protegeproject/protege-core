package edu.stanford.smi.protege.widget;

import javax.swing.*;
import javax.swing.text.*;

import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring a multiline string of arbitrary length.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TextFieldWidget extends TextComponentWidget {

    private static final long serialVersionUID = -4998153320957607144L;
    public JComponent createCenterComponent(JTextComponent textComponent) {
        return textComponent;
    }
    public JTextComponent createTextComponent() {
        return createTextField();
    }
    public JTextField getTextField() {
        return (JTextField) getTextComponent();
    }
    public JTextField createTextField() {
        return ComponentFactory.createTextField();
    }
    public void initialize() {
        super.initialize(false, 2, 1);
    }
}
