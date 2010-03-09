package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TreeNode to hold the parent form - child form relationship.  This relationship is the phyically same as super/sub 
 * class but we have a separate class because it might be logically different.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormParentChildNode extends ParentChildNode {
    private final ProjectListener _projectListener = new ProjectAdapter() {
        public void formChanged(ProjectEvent event) {
            if (LazyTreeNode.equals(event.getCls(), getCls())) {
                notifyNodeChanged();
            }
        }
    };

    public FormParentChildNode(LazyTreeNode parentNode, Cls parentCls) {
        super(parentNode, parentCls);
        parentCls.getProject().addProjectListener(_projectListener);
    }

    protected LazyTreeNode createNode(Object o) {
        return new FormParentChildNode(this, (Cls) o);
    }

    protected void dispose() {
        super.dispose();
        getCls().getProject().removeProjectListener(_projectListener);
    }
}
