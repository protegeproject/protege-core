package edu.stanford.smi.protege.ui;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Reference;

/**
 * Comparator for instance of the {@link Reference} class.  The comparsion is done first on slots and then on frames.
 */

public class ReferenceComparator implements Comparator<Reference> {

    public int compare(Reference r1,  Reference r2) {
        int result = r1.getSlot().compareTo(r2.getSlot());
        if (result == 0) {
            result = r1.getFrame().compareTo(r2.getFrame());
        }
        return result;
    }
}
