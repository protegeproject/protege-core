package edu.stanford.smi.protege.util;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A JList that implements the {@link Selectable} interface.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectableList extends JList implements Selectable {
    private static final long serialVersionUID = -5479458103259963384L;

    private ListenerCollection _listeners = new ListenerList(new SelectionEventDispatcher());

    // This ugly flag allows us to work around the problem caused by dragging causing
    // the list selection to change
    private boolean _isDragEvent;
    private boolean _isMultiSelectPressEvent;
    private boolean _isDeferringSelection;

    public SelectableList() {
        addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    notifySelectionListeners();
                }
            }
        });
    }

    public void addSelectionListener(SelectionListener listener) {
        // Log.enter(this, "addSelectionListener", listener);
        _listeners.add(this, listener);
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(this);
    }

    public void setSelectedValue(Object value) {
        ComponentUtilities.setSelectedValue(this, value);
    }

    public void setListenerNotificationEnabled(boolean enabled) {
        _listeners.setPostingEnabled(enabled);
    }

    public void notifySelectionListeners() {
        _listeners.postEvent(this, SelectionEvent.SELECTION_CHANGED);
    }

    public void processMouseEvent(MouseEvent event) {
        // Log.enter(this, "processMouseEvent", new Integer(event.getID()));
        _isMultiSelectPressEvent = false;
        int index = locationToIndex(event.getPoint());
        int id = event.getID();
        if (id == MouseEvent.MOUSE_PRESSED) {
            if (index == -1) {
                clearSelection();
            } else if (isSelectedIndex(index)) {
                _isMultiSelectPressEvent = true;
                if (!event.isPopupTrigger()) {
                	_isDeferringSelection = true;
                }
            }
        }
        if (_isDeferringSelection && id == MouseEvent.MOUSE_RELEASED) {
            setSelectionInterval(index, index);
            _isDeferringSelection = false;
        }
        super.processMouseEvent(event);
        _isMultiSelectPressEvent = false;
    }

    public void processMouseMotionEvent(MouseEvent event) {
        boolean dragDropEnabled = ComponentUtilities.isDragAndDropEnabled(this);
        _isDragEvent = dragDropEnabled && (event.getID() == MouseEvent.MOUSE_DRAGGED);
        super.processMouseMotionEvent(event);
        _isDragEvent = false;
    }

    public void removeSelectionListener(SelectionListener listener) {
        // Log.enter(this, "removeSelectionListener", listener);
        _listeners.remove(this, listener);
    }

    public void setSelectionInterval(int r1, int r2) {
        if (!_isDragEvent && !_isMultiSelectPressEvent) {
            super.setSelectionInterval(r1, r2);
        } else {
            // Log.trace("skipped", this, "setSelectionInterval", new Integer(r1), new Integer(r2));
        }
    }
    
    public String toString() {
        return "SelectableList";
    }
}
