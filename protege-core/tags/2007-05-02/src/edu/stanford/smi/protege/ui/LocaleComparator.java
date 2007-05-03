package edu.stanford.smi.protege.ui;

import java.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LocaleComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        Locale lhs = (Locale) o1;
        Locale rhs = (Locale) o2;
        return lhs.getDisplayName().compareTo(rhs.getDisplayName());
    }
}

