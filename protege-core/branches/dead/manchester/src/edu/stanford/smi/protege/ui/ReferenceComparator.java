package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.*;

/**
 * Comparator for instance of the {@link Reference} class.  The comparsion is done first on slots and then on frames.
 */

public class ReferenceComparator extends FrameComparator {

    public int compare(Object o1, Object o2) {
        Reference r1 = (Reference) o1;
        Reference r2 = (Reference) o2;
        int result = super.compare(r1.getSlot(), r2.getSlot());
        if (result == 0) {
            result = super.compare(r1.getFrame(), r2.getFrame());
        }
        return result;
    }
}
