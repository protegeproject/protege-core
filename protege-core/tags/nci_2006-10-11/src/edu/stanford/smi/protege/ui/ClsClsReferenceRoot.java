package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A TreeRoot for Cls-Cls preferences.
 * 
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */

public class ClsClsReferenceRoot extends LazyTreeRoot {
    private Slot _slot;

    public ClsClsReferenceRoot(KnowledgeBase kb, Slot slot) {
        super(getReferencingClses(kb, slot));
        _slot = slot;
    }

    public LazyTreeNode createNode(Object o) {
        return new ClsClsReferenceNode(this, (Cls) o, _slot);
    }

    protected Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    private static Collection getReferencingClses(KnowledgeBase kb, Slot slot) {
        Collection clses = new ArrayList();
        if (slot == null) {
            clses = kb.getClses();
            /*
             * Not sure this is desired
             * Iterator i = clses.iterator();
             * while (i.hasNext()) {
             * Cls cls = (Cls) i.next();
             * if (!hasInstanceSlot(cls)) {
             * i.remove();
             * }
             * }
             */
        } else {
            clses = slot.getDirectDomain();
        }
        return clses;
    }

}
