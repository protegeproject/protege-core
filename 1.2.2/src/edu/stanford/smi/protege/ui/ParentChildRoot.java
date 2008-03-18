package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tree Root for the superclass-subclass relationship
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ParentChildRoot extends LazyTreeRoot {

    public ParentChildRoot(Cls root) {
        super(root);
    }

    public ParentChildRoot(Collection roots) {
        super(filter(roots));
    }

    public LazyTreeNode createNode(Object o) {
        return new ParentChildNode(this, (Cls) o);
    }

    private static Collection filter(Collection roots) {
        Collection visibleRoots = new ArrayList(roots);
        Cls firstRoot = (Cls) CollectionUtilities.getFirstItem(roots);
        if (firstRoot != null && !firstRoot.getProject().getDisplayHiddenClasses()) {
            Iterator i = visibleRoots.iterator();
            while (i.hasNext()) {
                Cls cls = (Cls) i.next();
                if (!cls.isVisible()) {
                    i.remove();
                }
            }
        }
        return visibleRoots;
    }

    public Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }
}
