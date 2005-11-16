package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;

/**
 * A standard implementation of the drag target interface for dragging items around in a list box.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ListTarget implements DropTargetListener {
    private int _dropSelectionIndex = -1;
    private Object _dropTargetArea;

    private void clearDropSelection(JList list) {
        list.putClientProperty(DefaultRenderer.DROP_TARGET, null);
        list.putClientProperty(DefaultRenderer.DROP_TARGET_AREA, null);
        setDropSelectionIndex(list, -1);
    }

    //ESCA-JAVA0130 
    public void doDrop(JList list, Collection sources, int targetIndex, Object dropArea) {
        int dropIndex = targetIndex;
        if (dropArea == DefaultRenderer.DROP_TARGET_AREA_BELOW) {
            ++dropIndex;
        }
        SimpleListModel model = (SimpleListModel) list.getModel();
        int[] indices = list.getSelectedIndices();
        for (int i = 0; i < indices.length; ++i) {
            int sourceIndex = indices[i];
            model.moveValue(sourceIndex, dropIndex);
            if (sourceIndex > dropIndex) {
                ++dropIndex;
            }
        }
        list.setSelectionInterval(indices[0], indices[indices.length - 1]);
    }

    public void dragEnter(DropTargetDragEvent e) {
    }

    public void dragExit(DropTargetEvent e) {
        // Log.enter(this, "dragExit", e);
        clearDropSelection((JList) e.getDropTargetContext().getComponent());
    }

    public void dragOver(DropTargetDragEvent e) {
        // Log.enter(this, "dragOver", e);
        e.acceptDrag(e.getDropAction());
        setDropSelection(e);
    }

    public void dragScroll(DropTargetDragEvent e) {
    }

    public void drop(DropTargetDropEvent e) {
        // Log.enter(this, "drop", e);
        boolean succeeded = false;
        JList list = (JList) e.getDropTargetContext().getComponent();
        setDropSelection(list, e.getLocation());
        if (_dropSelectionIndex != -1 && e.isDataFlavorSupported(TransferableCollection.getCollectionFlavor())) {
            try {
                int action = e.getDropAction();
                e.acceptDrop(action);
                Collection sources = (Collection) e.getTransferable().getTransferData(
                        TransferableCollection.getCollectionFlavor());
                doDrop(list, sources, _dropSelectionIndex, _dropTargetArea);
            } catch (Exception ex) {
                Log.getLogger().severe(Log.toString(ex));
            }
        } else {
            e.rejectDrop();
            Log.getLogger().warning("unsupported flavor: " + e);
        }
        e.dropComplete(succeeded);
        clearDropSelection(list);
    }

    public void dropActionChanged(DropTargetDragEvent e) {
    }

    private void setDropSelection(DropTargetDragEvent e) {
        setDropSelection((JList) e.getDropTargetContext().getComponent(), e.getLocation());
    }

    private void setDropSelection(JList list, Point p) {
        int index = list.locationToIndex(p);
        if (index == -1) {
            clearDropSelection(list);
        } else {
            Object object = list.getModel().getElementAt(index);
            Rectangle r = list.getCellBounds(index, index);
            if (p.y < r.y + r.height / 2) {
                _dropTargetArea = DefaultRenderer.DROP_TARGET_AREA_ABOVE;
            } else if (index == list.getModel().getSize() - 1) {
                _dropTargetArea = DefaultRenderer.DROP_TARGET_AREA_BELOW;
            } else {
                ++index;
                _dropTargetArea = DefaultRenderer.DROP_TARGET_AREA_ABOVE;
                object = list.getModel().getElementAt(index);
            }
            list.putClientProperty(DefaultRenderer.DROP_TARGET, object);
            list.putClientProperty(DefaultRenderer.DROP_TARGET_AREA, _dropTargetArea);
            setDropSelectionIndex(list, index);
        }
    }

    private void setDropSelectionIndex(JList list, int index) {
        _dropSelectionIndex = index;
        list.repaint();
    }
}
