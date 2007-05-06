package edu.stanford.smi.protege.util;

import java.awt.event.*;

/**
 * An item listener that can be temporarily switched off.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class SwitchableItemListener extends SwitchableListener implements ItemListener {

    public abstract void changed(ItemEvent e);

    public final void itemStateChanged(ItemEvent event) {
        if (isEnabled()) {
            changed(event);
        }
    }
}
