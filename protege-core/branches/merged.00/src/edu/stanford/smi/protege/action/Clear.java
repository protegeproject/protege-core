package edu.stanford.smi.protege.action;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import edu.stanford.smi.protege.resource.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Clear extends ProjectAction {
    public Clear(boolean large) {
        super(ResourceKey.CLEAR_ACTION, large);
    }
    
    public void actionPerformed(ActionEvent event) {
        Component component = FocusManager.getCurrentManager().getPermanentFocusOwner();
        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            textComponent.cut();
        }
    }
}
