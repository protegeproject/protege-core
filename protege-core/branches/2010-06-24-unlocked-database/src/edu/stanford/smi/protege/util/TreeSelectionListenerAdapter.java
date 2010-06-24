package edu.stanford.smi.protege.util;


import javax.swing.event.*;

/**
 * An adapter that listens for tree selection events and fires generic {@link Selectable} events.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TreeSelectionListenerAdapter implements TreeSelectionListener {
    private Selectable _selectable;

    public TreeSelectionListenerAdapter(Selectable selectable) {
        _selectable = selectable;
    }

    public void valueChanged(TreeSelectionEvent event) {
        _selectable.notifySelectionListeners();
    }
}
