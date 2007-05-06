package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * Comparator for the "frame slot pairs" that are displayed in the template slot window.  First the comparison is done
 * on frame names and then on slot names.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameSlotPairComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        FrameSlotCombination combination1 = (FrameSlotCombination) o1;
        FrameSlotCombination combination2 = (FrameSlotCombination) o2;
        String fn1 = combination1.getFrame().getName();
        String fn2 = combination2.getFrame().getName();
        int result = fn1.compareToIgnoreCase(fn2);
        if (result == 0) {
            String sn1 = combination1.getSlot().getName();
            String sn2 = combination2.getSlot().getName();
            result = sn1.compareToIgnoreCase(sn2);
        }
        return result;
    }
}
