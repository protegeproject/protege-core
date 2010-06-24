package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JTree;


public abstract class TreeSelectionHelper<X> {

	private JTree tree;

	public TreeSelectionHelper(JTree tree) {
		this.tree = tree;
	}

	protected abstract Collection<X> getParents(X child);

	protected boolean isVisible(X element) {
		return true;
	}
	
	protected Collection<X> getRoots() {
		Collection<X> roots = new ArrayList<X>((Collection<X>) ((LazyTreeNode) tree.getModel().getRoot()).getUserObject());
		return roots;
	}

	public void setSelectedNode(X value) {
		WaitCursor cursor = new WaitCursor(tree);	        
		ArrayList<X> frames = new ArrayList<X>();
		getVisiblePathToRoot(value, frames);
		Collections.reverse(frames);
		ComponentUtilities.setSelectedObjectPath(tree, frames);
		cursor.hide();
	}

	
	protected Collection<X> filterHiddenRoots(Collection<X> roots) {
		Iterator<X> i = roots.iterator();
		while (i.hasNext()) {
			X root =  i.next();
			if (!isVisible(root)) {
				i.remove();
			}
		}
		return roots;
	}
	

	public void getVisiblePathToRoot(X elem, Collection<X> path) {
		Collection<X> roots = filterHiddenRoots(getRoots());	
		path.add(elem);
		if (!roots.contains(elem)) {
			boolean succeeded = getVisiblePathToRoot(elem, roots, path);
			if (!succeeded) {
				Log.getLogger().warning("No visible path found for " + elem);
			}
		}
	}

	private boolean getVisiblePathToRoot(X elem, Collection<X> roots, Collection<X> path) {
		boolean found = false;
		Iterator<X> i = getParents(elem).iterator();
		while (i.hasNext() && !found) {
			X parent = i.next();
			if (isVisible(parent) && !path.contains(parent)) {
				path.add(parent);
				if (roots.contains(parent)) {
					found = true;
				} else {
					found = getVisiblePathToRoot(parent, roots, path);
				}
				if (!found) {
					path.remove(parent);
				}
			}
		}
		return found;
	}

}
