package edu.stanford.smi.protege.ui;

import javax.swing.*;
import javax.swing.border.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class StatusBar extends JTextField {

    public StatusBar() {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setBackground(null);
    }
}
