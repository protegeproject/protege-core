package edu.stanford.smi.protege.util;

import java.awt.event.*;

/**
 * An action listener which can be temporarily switched off.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class SwitchableActionListener extends SwitchableListener implements ActionListener {

    public final void actionPerformed(ActionEvent event) {
        if (isEnabled()) {
            changed(event);
        }
    }

    public abstract void changed(ActionEvent e);
}
