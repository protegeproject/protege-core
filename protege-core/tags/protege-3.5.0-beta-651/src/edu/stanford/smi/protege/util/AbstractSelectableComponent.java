package edu.stanford.smi.protege.util;


import javax.swing.*;

/**
 *  Base class implementation for the {@link Selectable} interface.  Just supplies listener support.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractSelectableComponent extends JComponent implements Selectable {
    private static final long serialVersionUID = -6600231746695440045L;
    private ListenerCollection _listeners = new ListenerList(new SelectionEventDispatcher());

    public void addSelectionListener(SelectionListener listener) {
        _listeners.add(this, listener);
    }

    public void notifySelectionListeners() {
        _listeners.postEvent(this, SelectionEvent.SELECTION_CHANGED);
    }

    public void removeSelectionListener(SelectionListener listener) {
        _listeners.remove(this, listener);
    }
}
