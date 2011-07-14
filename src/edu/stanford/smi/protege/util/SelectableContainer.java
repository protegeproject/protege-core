package edu.stanford.smi.protege.util;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JComponent;

/**
 * A swing container component that contains a Selectable component.  All Selectable calls are delegated to the 
 * instance of Selectable.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectableContainer extends JComponent implements Selectable, Disposable {
    private static final long serialVersionUID = -3442680055824459200L;
    private ListenerCollection _listeners = new ListenerList(new SelectionEventDispatcher());
    private Selectable _selectable;

    private SelectionListener _selectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent event) {
            onSelectionChange();
            notifySelectionListeners();
        }
    };

    public SelectableContainer() {
        setLayout(new BorderLayout());
    }

    public SelectableContainer(Selectable s) {
        this();
        addSelectable(s);
    }

    private void addSelectable(Selectable selectable) {
        _selectable = selectable;
        if (_selectable != null) {
            _selectable.addSelectionListener(_selectionListener);
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        // Log.enter(this, "addSelectionListener", listener);
        _listeners.add(this, listener);
    }

    public void clearSelection() {
        _selectable.clearSelection();
    }

    public void dispose() {
        removeSelectable();
    }

    public Selectable getSelectable() {
        return _selectable;
    }

    public Collection getSelection() {
        return _selectable == null ? null : _selectable.getSelection();
    }
    
    /**
     * @return The selected item if there is only one selected item.  Returns null otherwise.
     */
    public Object getSoleSelection() {
        Object soleSelection = null;
        Collection selection = getSelection();
        if (selection.size() == 1) {
            soleSelection = CollectionUtilities.getFirstItem(selection);
        }
        return soleSelection;
    }

    public void notifySelectionListeners() {
        _listeners.postEvent(this, SelectionEvent.SELECTION_CHANGED);
    }

    public void onSelectionChange() {
        // do nothing
    }

    private void removeSelectable() {
        if (_selectable != null) {
            _selectable.removeSelectionListener(_selectionListener);
        }
        _selectable = null;
    }

    public void removeSelectionListener(SelectionListener listener) {
        _listeners.remove(this, listener);
    }

    public boolean setNotificationsEnabled(boolean b) {
        return _listeners.setPostingEnabled(b);
    }

    public void setSelectable(Selectable s) {
        removeSelectable();
        addSelectable(s);
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}
