package edu.stanford.smi.protege.ui;

import java.util.*;

import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Source listener drag and drop support for the SlotsTree.  All this guy has to do is to disconnect a class from its
 * superclass on a successful move/drop operation.

 *  * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsesTreeDragSourceListener extends TreeDragSourceListener {

    public boolean canStartDrag(Collection objects) {
        boolean canStartDrag = true;
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (!frame.isEditable()) {
                canStartDrag = false;
                break;
            }
        }
        return canStartDrag;
    }

    public void doCopy(Collection paths) {
    }

    public void doMove(Collection paths) {
        Iterator i = paths.iterator();
        while (i.hasNext()) {
            TreePath path = (TreePath) i.next();
            LazyTreeNode draggedNode = (LazyTreeNode) path.getLastPathComponent();
            LazyTreeNode draggedNodeParent = (LazyTreeNode) draggedNode.getParent();
            Cls draggedCls = (Cls) draggedNode.getUserObject();
            Cls draggedClsParent = (Cls) draggedNodeParent.getUserObject();
            draggedCls.removeDirectSuperclass(draggedClsParent);
        }
    }
}
