package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Comparator for frames.  The actually comparison criterion is delegated to the Frame.compareTo() method.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        int result;
        if (o1 instanceof Frame && o2 instanceof Frame) {
            result = ((Frame) o1).compareTo(o2);
        } else {
            Log.getLogger().warning("Invalid types: " + o1 + " " + o2);
            result = 0;
        }
        return result;
    }
}
