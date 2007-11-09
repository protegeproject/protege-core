package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Root for a set to nodes to hold the parent form - child form relationship.  This relationship is the phyically 
 * same as super/sub class but we have a separate class because it might be logically different.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormParentChildRoot extends ParentChildRoot {

    public FormParentChildRoot(Cls root) {
        super(root);
    }

    public FormParentChildRoot(Collection roots) {
        super(roots);
    }

    public LazyTreeNode createNode(Object o) {
        return new FormParentChildNode(this, (Cls) o);
    }
}
