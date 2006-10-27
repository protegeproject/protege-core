package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/** 
 * Comparator for browser key text for two frames.  The comparison is case insensitive.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class BrowserComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        String s1;
        String s2;
        if (o1 instanceof String) {
            s1 = (String) o1;
            s2 = ((Instance) o2).getBrowserText();
        } else {
            s1 = ((Instance) o1).getBrowserText();
            s2 = (String) o2;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
    }
}
