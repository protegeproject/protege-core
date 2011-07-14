package edu.stanford.smi.protege.widget;

import javax.swing.*;
import javax.swing.text.*;

import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring a multiline string of arbitrary length.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TextAreaWidget extends TextComponentWidget {

    private static final long serialVersionUID = 4891531863687177825L;
    public JComponent createCenterComponent(JTextComponent textComponent) {
        return ComponentFactory.createScrollPane(textComponent);
    }
    public JTextComponent createTextComponent() {
        return createTextArea();
    }
    public JTextArea getTextArea() {
        return (JTextArea) getTextComponent();
    }
    public JTextArea createTextArea() {
        return ComponentFactory.createTextArea();
    }
    public void initialize() {
        super.initialize(true, 2, 2);
    }
}
