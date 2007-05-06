package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tree Root for "references" between two frames.
 */

public class ReferenceRoot extends LazyTreeRoot {
    private Slot _slot;

    public ReferenceRoot(KnowledgeBase kb, Frame frame, Slot slot) {
        super(CollectionUtilities.createCollection(frame));
        _slot = slot;
    }

    public LazyTreeNode createNode(Object o) {
        return new ReferenceNode(this, (Frame) o, _slot);
    }

    protected Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }
}
