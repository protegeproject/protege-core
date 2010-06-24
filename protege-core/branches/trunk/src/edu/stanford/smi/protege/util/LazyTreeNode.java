package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.tree.TreeNode;

import edu.stanford.smi.protege.model.Frame;

/**
 * A tree node that doesn't check its children until it is asked for them. This lazy evaluation allows the system to do
 * "load on demand" from a database.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class LazyTreeNode implements TreeNode {
    private LazyTreeNode _parent;
    private Object _userObject;
    private List _childNodes;
    private int _childCount = -1;
    private boolean _isLoaded;
    private boolean _isDuplicate;
    private boolean _isSorted = false;

    protected LazyTreeNode(LazyTreeNode parent, Object userObject) {
    	this(parent, userObject, false);
    }
    
    protected LazyTreeNode(LazyTreeNode parent, Object userObject, boolean isSorted) {
        _parent = parent;
        _userObject = userObject;
        _isDuplicate = isDuplicate(userObject, parent);
        _isSorted = isSorted;
    }

    private static boolean isDuplicate(Object userObject, LazyTreeNode parent) {
        boolean isDuplicate = false;
        LazyTreeNode ancestor = parent;
        while (ancestor != null) {
            if (ancestor.getUserObject().equals(userObject)) {
                isDuplicate = true;
                break;
            }
            ancestor = ancestor.getLazyTreeNodeParent();
        }
        return isDuplicate;
    }

    public boolean isDuplicate() {
        return _isDuplicate;
    }

    public void childAdded(Object o) {
        int index = (_isLoaded) ? _childNodes.size() : -1;
        /*
         * Insertion is not in order right now.
         * Uncomment the next line to find the 
         * right index for the ordered insert index.
         * The implementation is not yet complete.
         */
    	//int index = getAddToIndex(o);
        childAdded(o, index);
    }
    
    private int getAddToIndex(Object o) {
    	if (!_isLoaded) {
    		return -1; //load later
    	}
    	
    	//children are already loaded, compute the right index to add    	
    	if (!_isSorted) {
    		return _childNodes.size();
    	}

    	//children are loaded and sorted
    	//insertion should be in order 	
    	return getSortedAddIndex(o);
    }

    private int getSortedAddIndex(Object o) {
    	//assumes children loaded
    	if (_childNodes == null) {
    		return -1;
    	}
    	
    	/*
    	 * Unfortunately, we don't have at this point a 
    	 * comparator for the object o and the user object
    	 * of a node. So, as a workaround, we implement
    	 * our own compareTo, that will work fine for 
    	 * Frame objects and strings, and for the rest of
    	 * node types it will just compare the toString()
    	 * of each node.
    	 */    
        int nChildren = _childNodes.size();
        int index = -1;
        for (int i = 0; i < nChildren; ++i) {
            LazyTreeNode node = (LazyTreeNode) _childNodes.get(i);
            if (compareTo(node.getUserObject(), o) > 0) {
            	return i;
            }           
        }
        return nChildren;
	}
    

	protected int compareTo(Object o1, Object o2) {
		if (o1 == null) {
			return (o2 == null) ? 0 : -1;
		}		
		if (o2 == null) {
			return (o1 == null) ? 0 : 1;
		}
		
		if (o1 instanceof Frame) {
			return (o2 instanceof Frame) ?
					((Frame)o1).compareTo((Frame)o2) :		
					((Frame)o1).getBrowserText().compareTo(o2.toString());			
		}
		
		return o1.toString().compareTo(o2.toString());
	}

	
	//TODO: check method before merging to head
	public void childAdded(Object o, int index) {
        if (_isLoaded) {
            int ti = getIndex(o);
            if (ti != -1) {
                LazyTreeNode child = (LazyTreeNode) _childNodes.get(ti);
                notifyChildNodeAdded(this, ti, child);
            } else {
                LazyTreeNode child = createNode(o);
                _childNodes.add(index, child);
                ++_childCount;
                notifyChildNodeAdded(this, index, child);
                // Log.trace("added", this, "childAdded", o, new
                // Integer(index));
            }
        } else {
            ensureChildrenLoaded();
            _childCount = _childNodes.size();
            index = getIndex(o);
            if (index != -1) {
                LazyTreeNode child = (LazyTreeNode) _childNodes.get(index);
                notifyChildNodeAdded(this, index, child);
            }
        }
    }
	

    public void childRemoved(Object o) {
        if (_childCount != -1) {
            --_childCount;
        }
        // Log.enter(this, "childRemoved", o);
        if (_isLoaded) {
            int index = getIndex(o);
            if (index < 0) {
                // Log.warning("node not found", this, "childRemoved", o);
                ++_childCount;
            } else {
                LazyTreeNode child = (LazyTreeNode) _childNodes.remove(index);
                child.dispose();
                notifyChildNodeRemoved(this, index, child);
            }
        }
    }

    public Enumeration children() {
        ensureChildrenLoaded();
        return Collections.enumeration(_childNodes);
    }

    private void clearNodes() {
        if (_childNodes != null) {
            _childCount = 0;
            Iterator i = _childNodes.iterator();
            while (i.hasNext()) {
                LazyTreeNode node = (LazyTreeNode) i.next();
                node.dispose();
            }
            _childNodes.clear();
        }
    }

    private LazyTreeNode createErrorNode(Object o) {
        return new ErrorLazyTreeNode(this, o);
    }

    protected abstract LazyTreeNode createNode(Object o);

    protected void dispose() {
        if (_childNodes != null) {
            Iterator i = _childNodes.iterator();
            while (i.hasNext()) {
                LazyTreeNode node = (LazyTreeNode) i.next();
                node.dispose();
            }
        }
    }

    private void ensureChildCountLoaded() {
        if (_childCount == -1) {
            _childCount = getChildObjectCount();
        }
    }

    private void ensureChildrenLoaded() {
        if (!_isLoaded) {
            loadNodes();
            _isLoaded = true;
        }
    }
    
    /**
     * Do not call this method unless you know exactly what you are doing.     
     */
    protected void setIsLoaded(boolean isLoaded) {
        _isLoaded = isLoaded;
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public TreeNode getChildAt(int i) {
        ensureChildrenLoaded();
        // Protect against children which fail to load, for whatever reason
        if (i >= _childNodes.size()) {
            i = _childNodes.size() - 1;
        }
        return (i == -1) ? null : (TreeNode) _childNodes.get(i);
    }

    public int getChildCount() {
        ensureChildCountLoaded();
        return _childCount;
    }

    protected abstract int getChildObjectCount();

    protected abstract Collection getChildObjects();

    protected abstract Comparator getComparator();

    public int getIndex(Object o) {
        int nChildren = _childNodes.size();
        int index = -1;
        for (int i = 0; i < nChildren; ++i) {
            LazyTreeNode node = (LazyTreeNode) _childNodes.get(i);
            if (equals(node.getUserObject(), o)) {
                index = i;
            }
        }
        return index;
    }

    public int getIndex(TreeNode node) {
        ensureChildrenLoaded();
        return _childNodes.indexOf(node);
    }

    public LazyTreeNode getLazyTreeNodeParent() {
        return _parent;
    }

    public TreeNode getParent() {
        return _parent;
    }

    public Object getUserObject() {
        return _userObject;
    }

    public int getUserObjectIndex(Object o) {
        ensureChildrenLoaded();
        int index = -1;
        int nNodes = _childNodes.size();
        for (int i = 0; i < nNodes; ++i) {
            LazyTreeNode node = (LazyTreeNode) _childNodes.get(i);
            if (equals(node.getUserObject(), o)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean isLeaf() {
        if (isDuplicate()) {
            return true;
        }
        ensureChildCountLoaded();
        return _childCount == 0;
    }

    /**
     * Do not override or call this method in a subclass, unless you
     * know what you are doing.
     * @param childObjects
     */
    protected void loadChildObjects(Collection childObjects) {
        if (_childNodes == null) {
            _childNodes = new ArrayList();
        } else {
            _childNodes.clear();
        }
        
        if (_isSorted) {
        	ArrayList sortedChildObject = new ArrayList(childObjects);
        	Collections.sort(sortedChildObject, getComparator());
        	childObjects = sortedChildObject;
        }
        
        Iterator i = childObjects.iterator();
        while (i.hasNext()) {
            Object child = i.next();
            TreeNode childNode;
            try {
                childNode = createNode(child);
            } catch (Exception e) {
              if (Log.getLogger().isLoggable(Level.FINE)) {
                Log.getLogger().log(Level.FINE, "Exception caught ", e);
              } else {
                Log.getLogger().warning("Exception caught " + e.toString());
                Log.getLogger().warning(" For more information use fine logging");
              }
              childNode = createErrorNode(child);
            }
            _childNodes.add(childNode);
        }
        _childCount = _childNodes.size();
        // Collections.sort(childNodes, getComparator());
    }

    private void loadNodes() {
        Collection childObjects = getChildObjects();
        loadChildObjects(childObjects);
    }

    public void notifyChildNodeAdded(LazyTreeNode parent, int index, LazyTreeNode child) {
        if (_parent == null) {
            Log.getLogger().warning("Notification message lost: " + child);
        } else {
            _parent.notifyChildNodeAdded(parent, index, child);
        }
    }

    public void notifyChildNodeRemoved(LazyTreeNode parent, int index, LazyTreeNode child) {
        if (_parent == null) {
            Log.getLogger().warning("Notification message lost: " + child);
        } else {
            _parent.notifyChildNodeRemoved(parent, index, child);
        }
    }

    public void notifyNodeChanged(LazyTreeNode node) {
        if (_parent == null) {
            Log.getLogger().warning("Notification message lost: " + node);
        } else {
            _parent.notifyNodeChanged(node);
        }
    }

    public void notifyNodeStructureChanged(LazyTreeNode node) {
        if (_parent == null) {
            Log.getLogger().warning("Notification message lost: " + node);
        } else {
            _parent.notifyNodeStructureChanged(node);
        }
    }

    public void reload() {
        clearNodes();
        loadNodes();
        // should make correct notification call
        notifyNodeStructureChanged(this);
    }

    public void reload(Object userObject) {
        _userObject = userObject;
        reload();
    }

    public String toString() {
        return "LazyTreeNode(" + _userObject + ")";
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

	public boolean isSorted() {
		return _isSorted;
	}

}