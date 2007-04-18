package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Comparator for LazyTreeNode that just delegates the comparison the the wrapped user objects.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LazyTreeNodeFrameComparator implements Comparator {
    private Comparator _frameComparator = new FrameComparator();

    public int compare(Object o1, Object o2) {
        if (o1 instanceof LazyTreeNode) {
            o1 = ((LazyTreeNode) o1).getUserObject();
        }
        if (o2 instanceof LazyTreeNode) {
            o2 = ((LazyTreeNode) o2).getUserObject();
        }
        return _frameComparator.compare(o1, o2);
    }
}
