package edu.stanford.smi.protege.util;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;

/**
 * A drag source listener for a list box.  This implementation has two template methods to delegate the copy or move
 * operation to subclasses.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ListDragSourceListener implements DragGestureListener, DragSourceListener {
    private int[] _indexes;
    private Collection _objects;

    public abstract void doCopy(JComponent c, int[] indexes, Collection objects);

    public abstract void doMove(JComponent c, int[] indexes, Collection objects);

    public void dragDropEnd(DragSourceDropEvent e) {
        if (e.getDropSuccess()) {
            JComponent c = (JComponent) e.getDragSourceContext().getComponent();
            int action = e.getDropAction();
            if (action == DnDConstants.ACTION_MOVE) {
                doMove(c, _indexes, _objects);
            } else if (action == DnDConstants.ACTION_COPY) {
                doCopy(c, _indexes, _objects);
            } else {
                // do nothing
            }
        }
    }

    public void dragEnter(DragSourceDragEvent e) {
    }

    public void dragExit(DragSourceEvent e) {
    }

    public void dragGestureRecognized(DragGestureEvent e) {
        // Log.enter(this, "dragGestureRecognized", e);
        JList list = (JList) e.getComponent();
        if (ComponentUtilities.isDragAndDropEnabled(list)) {
            _indexes = list.getSelectedIndices();
            _objects = Arrays.asList(list.getSelectedValues());
            if (!_objects.isEmpty()) {
                Transferable t = new TransferableCollection(_objects);
                e.startDrag(DragSource.DefaultMoveDrop, t, this);
            }
        }
    }

    public void dragOver(DragSourceDragEvent e) {
    }

    public void dropActionChanged(DragSourceDragEvent e) {
    }
}
