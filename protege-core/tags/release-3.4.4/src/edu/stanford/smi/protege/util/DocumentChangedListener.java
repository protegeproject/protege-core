package edu.stanford.smi.protege.util;

import javax.swing.event.*;

/**
 * An adapter that changes the document events that come from swing text widgets into simple "change" events.  This 
 * listener can also be enabled or disabled.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DocumentChangedListener extends SwitchableListener implements DocumentListener, ChangeListener {

    public void changedUpdate(DocumentEvent event) {
        if (isEnabled()) {
            stateChanged(new ChangeEvent(event.getDocument()));
        }
    }

    public void insertUpdate(DocumentEvent event) {
        if (isEnabled()) {
            stateChanged(new ChangeEvent(event.getDocument()));
        }
    }

    public void removeUpdate(DocumentEvent event) {
        if (isEnabled()) {
            stateChanged(new ChangeEvent(event.getDocument()));
        }
    }
}
