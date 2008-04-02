package edu.stanford.smi.protege.util;


import java.util.*;

/**
 * The Root node for a lazy tree.  Note that this root is never displayed.  Its child lazy tree nodes are the apparent
 * roots of the tree. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class LazyTreeRoot extends LazyTreeNode {
    private LazyTreeModel _model;

    public LazyTreeRoot(Object o) {
        super(null, CollectionUtilities.createCollection(o));
    }

    public LazyTreeRoot(Collection c) {
        super(null, c);
    }

    protected int getChildObjectCount() {
        return getChildObjects().size();
    }

    public Collection getChildObjects() {
        return (Collection) getUserObject();
    }

    public void notifyChildNodeAdded(LazyTreeNode parent, int index, LazyTreeNode child) {
        // Log.enter("LazyTreeRoot.notifyChildNodeAdded", parent, new Integer(index), child);
        _model.nodesWereInserted(parent, new int[]{index});
    }

    public void notifyChildNodeRemoved(LazyTreeNode parent, int index, LazyTreeNode child) {
        // Log.enter("LazyTreeRoot.notifyChildNodeRemoved", parent, new Integer(index), child);
        _model.nodesWereRemoved(parent, new int[]{index}, new Object[]{child});
    }

    public void notifyNodeChanged(LazyTreeNode node) {
        // Log.enter("LazyTreeRoot.notifyNodeChanged", node);
        _model.nodeChanged(node);
    }

    public void notifyNodeStructureChanged(LazyTreeNode node) {
        // Log.enter("LazyTreeRoot.notifyNodeStructureChanged", node);
        _model.nodeStructureChanged(node);
    }

    public void setModel(LazyTreeModel model) {
        _model = model;
    }
}
