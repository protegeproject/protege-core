package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.smi.protege.ui.FrameComparator;

/**
 * The Root node for a lazy tree.  Note that this root is never displayed.  Its child lazy tree nodes are the apparent
 * roots of the tree. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class LazyTreeRoot extends LazyTreeNode {
    private LazyTreeModel _model;

    protected LazyTreeRoot(Object o) {
        this(CollectionUtilities.createCollection(o), false);
    }

    protected LazyTreeRoot(Collection c) {
        this(c, false);
    }
    
    protected LazyTreeRoot(Object o, boolean isSorted) {
        super(null, CollectionUtilities.createCollection(o), isSorted);
    }

    protected LazyTreeRoot(Collection c, boolean isSorted) {
        super(null, getRootCollection(c, isSorted), isSorted);
    }

    private static Collection getRootCollection(Collection c, boolean isSorted) {
    	if (isSorted) {
    		ArrayList sortedList = new ArrayList(c);
    		//TODO: how to get the comparator?
    		Collections.sort(sortedList, new FrameComparator());
    		c = sortedList;
    	}
    	return c;
    }
       
    @Override
	protected int getChildObjectCount() {
        return getChildObjects().size();
    }

    @Override
	public Collection getChildObjects() {
        return (Collection) getUserObject();
    }

    public void childReplaced(Object oldUserObj, Object newUserObj) {
    	List userObj = (List) getUserObject();    	
		int index = userObj.indexOf(oldUserObj);
		if (index < 0) {
			if (Log.getLogger().isLoggable(Level.FINE)) {
				Log.getLogger().fine("Could not replace object " + oldUserObj +
						" with " + newUserObj + " in tree root. Object not found.");
			}			
			return;
		}
		userObj.set(index, newUserObj);
    }
    
    @Override
	public void notifyChildNodeAdded(LazyTreeNode parent, int index, LazyTreeNode child) {
        // Log.enter("LazyTreeRoot.notifyChildNodeAdded", parent, new Integer(index), child);
        _model.nodesWereInserted(parent, new int[] { index });
    }

    @Override
	public void notifyChildNodeRemoved(LazyTreeNode parent, int index, LazyTreeNode child) {
        // Log.enter("LazyTreeRoot.notifyChildNodeRemoved", parent, new Integer(index), child);
        _model.nodesWereRemoved(parent, new int[] { index }, new Object[] { child });
    }

    @Override
	public void notifyNodeChanged(LazyTreeNode node) {
        // Log.enter("LazyTreeRoot.notifyNodeChanged", node);
        _model.nodeChanged(node);
    }

    @Override
	public void notifyNodeStructureChanged(LazyTreeNode node) {
        // Log.enter("LazyTreeRoot.notifyNodeStructureChanged", node);
        _model.nodeStructureChanged(node);
    }

    public void setModel(LazyTreeModel model) {
        _model = model;
    }
}
