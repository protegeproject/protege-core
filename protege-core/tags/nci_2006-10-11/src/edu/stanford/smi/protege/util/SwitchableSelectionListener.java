package edu.stanford.smi.protege.util;

/**
 * A base class that adds the ability to switch on and off a listener to the Selection listener interface.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class SwitchableSelectionListener extends SwitchableListener implements SelectionListener {

    public abstract void changed(SelectionEvent event);

    public final void selectionChanged(SelectionEvent event) {
        if (isEnabled()) {
            changed(event);
        }
    }
}
