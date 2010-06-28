package edu.stanford.smi.protege.model;

/**
 * An exception thrown when an api user passes in a null when an api method was expecting a frame.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class NullFrameException extends IllegalArgumentException {

    public NullFrameException(String s) {
        super(s);
    }
}
