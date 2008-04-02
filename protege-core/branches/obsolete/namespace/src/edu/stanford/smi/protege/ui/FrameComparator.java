package edu.stanford.smi.protege.ui;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.Log;

/**
 * Comparator for frames.  The actually comparison criterion is delegated to the Frame.compareTo() method.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameComparator implements Comparator<Frame> {

    public int compare(Frame f1, Frame f2) {
        return  f1.compareTo(f2);
    }
}
