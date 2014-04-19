package edu.stanford.smi.protege.util;

import java.util.*;

import javax.swing.tree.*;

/**
 * TreeModel for holding LazyTreeRoots and LazyTreeNodes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LazyTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = -1631559401681910463L;

    private static class EmptyRoot extends LazyTreeRoot {
        EmptyRoot() {
            super(Collections.EMPTY_LIST);
        }

        public LazyTreeNode createNode(Object o) {
            Assert.fail("no override");
            return null;
        }

        public Comparator getComparator() {
            return null;
        }
    }

    public LazyTreeModel() {
        this(null);
    }

    public LazyTreeModel(LazyTreeRoot root) {
        super(root == null ? new EmptyRoot() : root);
        getLazyTreeRoot().setModel(this);
    }

    public void dispose() {
        removeRoot();
    }

    private LazyTreeRoot getLazyTreeRoot() {
        return (LazyTreeRoot) getRoot();
    }

    private void removeRoot() {
        getLazyTreeRoot().dispose();
    }

    public void setRoot(TreeNode root) {
        if (root == null) {
            root = new EmptyRoot();
        }
        removeRoot();
        super.setRoot(root);
        if (root instanceof LazyTreeRoot) {
            ((LazyTreeRoot) root).setModel(this);
        } else {
            Log.getLogger().severe("LazyTreeeModel requires LazyTreeRoot");
        }
    }
}
