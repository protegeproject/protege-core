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
public class Cut extends ProjectAction {
    public Cut(boolean large) {
        super(ResourceKey.CUT_ACTION, large);
    }
    
    public void actionPerformed(ActionEvent event) {
        Component component = FocusManager.getCurrentManager().getPermanentFocusOwner();
        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            textComponent.cut();
        }
    }
}
