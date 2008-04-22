package edu.stanford.smi.protege.util;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SuperslotTraverser implements Traverser {
    public Object get(Object o) {
        return CollectionUtilities.getFirstItem(((Slot) o).getDirectSuperslots());
    }

}
