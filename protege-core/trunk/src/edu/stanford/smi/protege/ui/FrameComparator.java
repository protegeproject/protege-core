package edu.stanford.smi.protege.ui;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;

/**
 * Comparator for frames.  The actually comparison criterion is delegated to the Frame.compareTo() method.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameComparator<X extends Frame> implements Comparator<X> {

    public int compare(X f1, X f2) {
        return  f1.compareTo(f2);
    }
}
