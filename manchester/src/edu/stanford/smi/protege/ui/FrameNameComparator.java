package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A comparator for frames that only does a case insensitive comparison of the frame names.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameNameComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        int result;
        if (o1 instanceof Instance && o2 instanceof Instance) {
            String t1 = ((Frame) o1).getName();
            String t2 = ((Frame) o2).getName();
            // frame names should never be null unless the frame has been deleted.
            if (t1 == null) {
                result = (t2 == null) ? 0 : +1;
            } else if (t2 == null) {
                result = -1;
            } else {
                result = t1.compareToIgnoreCase(t2);
            }
        } else {
            Log.getLogger().warning("Invalid types: " + o1 + " " + o2);
            result = 0;
        }
        return result;
    }
}
