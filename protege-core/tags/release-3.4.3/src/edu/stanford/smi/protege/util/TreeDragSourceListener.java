package edu.stanford.smi.protege.util;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;

/**
 * Base class for a drag and drop source side listener on a JTree.  The actual move and copy operations are handled by
 * template methods.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class TreeDragSourceListener implements DragGestureListener, DragSourceListener {
    private Collection _paths;

    public abstract boolean canStartDrag(Collection objects);

    public abstract void doCopy(Collection paths);

    public abstract void doMove(Collection paths);

    public void dragDropEnd(DragSourceDropEvent e) {
        if (dropSucceeded(e)) {
            int action = e.getDropAction();
            if (action == DnDConstants.ACTION_MOVE) {
                doMove(_paths);
            } else if (action == DnDConstants.ACTION_COPY) {
                doCopy(_paths);
            } else {
                // do nothing
            }
        }
    }

    private static boolean dropSucceeded(DragSourceDropEvent e) {
        boolean succeeded;
        if (SystemUtilities.isMac()) {
            succeeded = TreeTarget.getLastDropSucceeded();
        } else {
            succeeded = e.getDropSuccess();
        }
        return succeeded;
    }

    public void dragEnter(DragSourceDragEvent e) {
    }

    public void dragExit(DragSourceEvent e) {
    }

    public void dragGestureRecognized(DragGestureEvent e) {
        JTree tree = (JTree) e.getComponent();
        Object[] selectionPaths = tree.getSelectionPaths();
        _paths = (selectionPaths == null) ? Collections.EMPTY_LIST : Arrays.asList(selectionPaths);
        Collection objects = ComponentUtilities.getSelection(tree);
        if (objects != null && canStartDrag(objects)) {
            Transferable t = new TransferableCollection(objects);
            e.startDrag(DragSource.DefaultMoveDrop, t, this);
        }
    }

    public void dragOver(DragSourceDragEvent e) {
    }

    public void dropActionChanged(DragSourceDragEvent e) {
    }
}
