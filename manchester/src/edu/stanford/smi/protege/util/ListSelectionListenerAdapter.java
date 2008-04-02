package edu.stanford.smi.protege.util;


import javax.swing.event.*;

/**
 * An adapter that allows a generic {@link SelectionListener} to be notified when list selection events occur.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ListSelectionListenerAdapter extends SwitchableListSelectionListener implements ListSelectionListener {
    private Selectable _selectable;

    public ListSelectionListenerAdapter(Selectable selectable) {
        _selectable = selectable;
    }

    public void changed(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            _selectable.notifySelectionListeners();
        }
    }
}
