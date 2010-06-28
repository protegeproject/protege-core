package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * The target side handler for drag and drop operations on the InstancesTab.  The drop operation can only occur on
 * a class (to change the direct type of the dragged instance).
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class InstanceClsesTreeTarget extends TreeTarget {

    public InstanceClsesTreeTarget() {
        super(false);
    }

    private boolean confirm(JComponent c, Collection instances) {
        boolean result = true;
        if (SystemUtilities.modalDialogInDropWorks()) {
            String text = "Do you want to change the class of ";
            if (instances.size() == 1) {
                text += "this instance";
            } else {
                text += "these instances";
            }
            int rval = ModalDialog.showMessageDialog(c, text, ModalDialog.MODE_OK_CANCEL);
            result = rval == ModalDialog.OPTION_OK;
        }
        return result;
    }

    public boolean doDrop(JTree tree, Object source, int row, Object area) {
        boolean succeeded = false;

        TreePath path = tree.getPathForRow(row);
        Cls targetCls = (Cls) ((LazyTreeNode) path.getLastPathComponent()).getUserObject();
        Instance sourceInstance = (Instance) source;

        if (targetCls.isAbstract()) {
            // do nothing
        } else if (sourceInstance.hasDirectType(targetCls)) {
            Log.getLogger().warning("do nothing on drop");
        } else {
            if (sourceInstance instanceof Cls) {
                if (targetCls.isClsMetaCls()) {
                    sourceInstance.setDirectType(targetCls);
                    succeeded = true;
                }
            } else if (!targetCls.isClsMetaCls()) {
                sourceInstance.setDirectType(targetCls);
                succeeded = true;
            }
        }
        return succeeded;
    }

    public boolean doDrop(JTree tree, Collection sources, int row, Object area) {
        boolean succeeded = false;
        if (confirm(tree, sources)) {
            succeeded = super.doDrop(tree, sources, row, area);
        }
        return succeeded;
    }
}
