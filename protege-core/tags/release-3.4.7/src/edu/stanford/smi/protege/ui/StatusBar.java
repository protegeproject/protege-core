package edu.stanford.smi.protege.ui;

import javax.swing.*;
import javax.swing.border.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class StatusBar extends JTextField {

    private static final long serialVersionUID = 1907671672358848444L;

    public StatusBar() {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setBackground(null);
    }
}
