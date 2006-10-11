package edu.stanford.smi.protege.ui;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Target support for Slots tree drag and drop support.  Note that both move and copy drop operations result in an
 * add of a superslot.  The source will disconnect the old superclass if the operation was a move.
 * 
 */
/*
 * Unfortunately this code is a cut and paste fromt the equivalent clses tree code.  Some refactoring is needed here.
 */
public class SlotsTreeTarget extends TreeTarget {

    public SlotsTreeTarget() {
        super(true);
    }

    private static boolean addSuperslot(Slot source, Slot parent) {
        boolean succeeded = false;
        if (parent == source) {
            //
        } else if (parent.hasSuperslot(source)) {
            // Log.warning("avoiding recursive inheritance", this, "drop", source, parent);
        } else if (source.hasDirectSuperslot(parent)) {
            // Log.warning("avoiding duplicate direct inheritance", this, "drop");
        } else {
            source.addDirectSuperslot(parent);
            succeeded = true;
        }
        return succeeded;
    }

    public boolean doDrop(JTree tree, Object source, int targetRow, Object area) {
        boolean succeeded = false;
        TreePath path = tree.getPathForRow(targetRow);
        LazyTreeNode targetNode = (LazyTreeNode) path.getLastPathComponent();
        Slot targetSlot = (Slot) targetNode.getUserObject();
        Slot sourceSlot = (Slot) source;
        LazyTreeNode parentNode;
        boolean addedSuperslot = false;
        if (area == DefaultRenderer.DROP_TARGET_AREA_ON) {
            parentNode = targetNode;
            succeeded = addSuperslot(sourceSlot, targetSlot);
            addedSuperslot = succeeded;
        } else {
            Slot parentSlot;
            if (sourceSlot.hasDirectSuperslot(targetSlot)) {
                parentNode = targetNode;
            } else {
                parentNode = targetNode.getLazyTreeNodeParent();
            }
            boolean isOK = true;
            Object parentObject = parentNode.getUserObject();
            if (parentObject instanceof Slot) {
                parentSlot = (Slot) parentObject;
                if (!sourceSlot.hasDirectSuperslot(parentSlot)) {
                    isOK = addSuperslot(sourceSlot, parentSlot);
                    addedSuperslot = isOK;
                    // Log.trace("addSuperclass=" + targetParent, this, "doDrop");
                }
                if (isOK) {
                    parentSlot.moveDirectSubslot(sourceSlot, targetSlot);
                    // Log.trace("moveDirectSubclass=" + targetIndex, this, "doDrop");
                    succeeded = true;
                }
            }
        }
        if (succeeded) {
            int newIndex = parentNode.getUserObjectIndex(sourceSlot);
            TreeNode newNode = parentNode.getChildAt(newIndex);
            ComponentUtilities.setSelectedNode(tree, newNode);
        }

        // HACK: return false if we didn't add a superclass so the darn thing doesn't get
        // deleted on the other side!
        return succeeded && addedSuperslot;
    }

    public String toString() {
        return "ClsesTreeTarget";
    }
}
