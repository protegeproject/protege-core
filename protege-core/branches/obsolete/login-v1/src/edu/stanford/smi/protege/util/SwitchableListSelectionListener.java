package edu.stanford.smi.protege.util;

import javax.swing.event.*;

/**
 * An adapter that converts a selection listener into a list selection listener.  This listener can also be 
 * temporarily switched off.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class SwitchableListSelectionListener extends SwitchableListener implements ListSelectionListener {

    public abstract void changed(ListSelectionEvent event);

    public final void valueChanged(ListSelectionEvent event) {
        if (isEnabled() && !event.getValueIsAdjusting()) {
            changed(event);
        }
    }
}
