package edu.stanford.smi.protege.ui;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Target support for Clses tree drag and drop support.  Note that both move and copy drop operations result in an
 * add of a superclass.  The source will disconnect the old superclass if the operation was a move.
 */
public class ClsesTreeTarget extends TreeTarget {

    public ClsesTreeTarget() {
        super(true);
    }

    private boolean addSuperclass(Cls source, Cls parent) {
        boolean succeeded = false;
        if (parent == source) {
            //
        } else if (parent.hasSuperclass(source)) {
            // Log.warning("avoiding recursive inheritance", this, "drop", source, parent);
        } else if (source.hasDirectSuperclass(parent)) {
            // Log.warning("avoiding duplicate direct inheritance", this, "drop");
        } else {
            source.addDirectSuperclass(parent);
            succeeded = true;
        }
        return succeeded;
    }

    public boolean doDrop(JTree tree, Object source, int targetRow, Object area) {
        boolean succeeded = false;
        TreePath path = tree.getPathForRow(targetRow);
        LazyTreeNode targetNode = (LazyTreeNode) path.getLastPathComponent();
        Cls targetCls = (Cls) targetNode.getUserObject();
        Cls sourceCls = (Cls) source;
        LazyTreeNode parentNode;
        boolean addedSuperclass = false;
        if (area == DefaultRenderer.DROP_TARGET_AREA_ON) {
            parentNode = targetNode;
            succeeded = addSuperclass(sourceCls, targetCls);
            addedSuperclass = succeeded;
        } else {
            Cls parentCls;
            if (sourceCls.hasDirectSuperclass(targetCls)) {
                parentNode = targetNode;
            } else {
                parentNode = targetNode.getLazyTreeNodeParent();
            }
            parentCls = (Cls) parentNode.getUserObject();
            boolean isOK = true;
            if (!sourceCls.hasDirectSuperclass(parentCls)) {
                isOK = addSuperclass(sourceCls, parentCls);
                addedSuperclass = isOK;
                // Log.trace("addSuperclass=" + targetParent, this, "doDrop");
            }
            if (isOK) {
                parentCls.moveDirectSubclass(sourceCls, targetCls);
                // Log.trace("moveDirectSubclass=" + targetIndex, this, "doDrop");
                succeeded = true;
            }
        }
        if (succeeded) {
            int newIndex = parentNode.getUserObjectIndex(sourceCls);
            TreeNode newNode = parentNode.getChildAt(newIndex);
            ComponentUtilities.setSelectedNode(tree, newNode);
        }

        // HACK: return false if we didn't add a superclass so the darn thing doesn't get
        // deleted on the other side!
        return succeeded && addedSuperclass;
    }

    public String toString() {
        return "ClsesTreeTarget";
    }
}
