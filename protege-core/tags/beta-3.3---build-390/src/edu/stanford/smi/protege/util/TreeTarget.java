package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Standard drop target side drag and drop support for a tree control.  The actual processing associated with the 
 * drop is handled by a template method.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class TreeTarget implements DropTargetListener {
    private int _dropSelectionRow;
    private Object _dropSelectionArea;
    private boolean _allowsBetweenDrops;
    private static boolean lastDropSucceeded; // Hack for Mac 1.4.2 JDK bug

    protected TreeTarget(boolean allowsBetweenDrops) {
        _allowsBetweenDrops = allowsBetweenDrops;
    }

    private void clearDropSelection(JTree tree) {
        tree.putClientProperty(DefaultRenderer.DROP_TARGET, null);
        tree.putClientProperty(DefaultRenderer.DROP_TARGET_AREA, null);
        _dropSelectionRow = -1;
        _dropSelectionArea = null;
        tree.repaint();
    }

    public abstract boolean doDrop(JTree tree, Object source, int row, Object area);

    protected boolean doDrop(JTree tree, Collection sources, int row, Object area) {
        boolean succeeded = true;
        Iterator i = sources.iterator();
        while (i.hasNext()) {
            Object source = i.next();
            succeeded = doDrop(tree, source, _dropSelectionRow, _dropSelectionArea);
        }
        return succeeded;
    }

    public void dragEnter(DropTargetDragEvent e) {
    }

    public void dragExit(DropTargetEvent e) {
        clearDropSelection(getTree(e));
    }

    public void dragOver(DropTargetDragEvent e) {
        setDropSelection(e);
    }

    public void drop(DropTargetDropEvent e) {
        boolean succeeded = false;
        DataFlavor flavor = TransferableCollection.getCollectionFlavor();
        JTree tree = getTree(e);
        if (e.isDataFlavorSupported(flavor)) {
            try {
                int action = e.getDropAction();
                setDropSelection(getTree(e), e.getLocation());
                if (_dropSelectionRow != -1) {
                    Collection sources = (Collection) (e.getTransferable().getTransferData(flavor));
                    succeeded = doDrop(tree, sources, _dropSelectionRow, _dropSelectionArea);
                }
                e.acceptDrop(action);
            } catch (Exception ex) {
                Log.getLogger().warning(Log.toString(ex));
            }
        } else {
            e.rejectDrop();
            Log.getLogger().warning("unsupported flavor: " + e);
        }
        lastDropSucceeded = succeeded;
        e.dropComplete(succeeded);
        clearDropSelection(tree);
    }

    public static boolean getLastDropSucceeded() {
        return lastDropSucceeded;
    }

    public void dropActionChanged(DropTargetDragEvent e) {
    }

    private static JTree getTree(DropTargetDragEvent e) {
        return (JTree) e.getDropTargetContext().getComponent();
    }

    private static JTree getTree(DropTargetEvent e) {
        return (JTree) e.getDropTargetContext().getComponent();
    }

    private void setDropSelection(DropTargetDragEvent e) {
        setDropSelection(getTree(e), e.getLocation());
    }

    private void setDropSelection(JTree tree, Point p) {
        _dropSelectionRow = tree.getRowForLocation(p.x, p.y);
        if (_dropSelectionRow == -1) {
            clearDropSelection(tree);
        } else {
            if (_allowsBetweenDrops) {
                Rectangle r = tree.getRowBounds(_dropSelectionRow);
                if (p.y < r.y + r.height / 4) {
                    if (_dropSelectionRow == 0) {
                        _dropSelectionArea = DefaultRenderer.DROP_TARGET_AREA_ABOVE;
                    } else {
                        --_dropSelectionRow;
                        _dropSelectionArea = DefaultRenderer.DROP_TARGET_AREA_BELOW;
                    }
                } else if (p.y < r.y + 3 * r.height / 4) {
                    _dropSelectionArea = DefaultRenderer.DROP_TARGET_AREA_ON;
                } else {
                    _dropSelectionArea = DefaultRenderer.DROP_TARGET_AREA_BELOW;
                }
            } else {
                _dropSelectionArea = DefaultRenderer.DROP_TARGET_AREA_ON;
            }

            TreePath path = tree.getPathForRow(_dropSelectionRow);
            LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
            tree.putClientProperty(DefaultRenderer.DROP_TARGET, node);
            tree.putClientProperty(DefaultRenderer.DROP_TARGET_AREA, _dropSelectionArea);
            tree.repaint();
            Thread.yield();
        }
    }
}
