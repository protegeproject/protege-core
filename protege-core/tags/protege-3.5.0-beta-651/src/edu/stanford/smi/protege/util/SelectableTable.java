package edu.stanford.smi.protege.util;

import java.util.*;

import javax.swing.*;
// import javax.swing.table.*;

/**
 * A JTable that implements the {@link Selectable} interface.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectableTable extends JTable implements Selectable {
    private static final long serialVersionUID = -9096314274427842787L;
    private ListenerCollection _selectionListeners = new ListenerList(new SelectionEventDispatcher());

    public SelectableTable() {
        getSelectionModel().addListSelectionListener(new ListSelectionListenerAdapter(this));
        ComponentFactory.configureTable(this);
    }

    public void addSelectionListener(SelectionListener listener) {
        _selectionListeners.add(this, listener);
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(this);
    }

    public void notifySelectionListeners() {
        _selectionListeners.postEvent(this, SelectionEvent.SELECTION_CHANGED);
    }

    public void removeSelectionListener(SelectionListener listener) {
        _selectionListeners.remove(this, listener);
    }

    public String toString() {
        return "SelectableTable";
    }
}
