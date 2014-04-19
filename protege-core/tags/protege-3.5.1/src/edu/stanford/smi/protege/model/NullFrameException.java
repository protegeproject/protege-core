package edu.stanford.smi.protege.model;

/**
 * An exception thrown when an api user passes in a null when an api method was expecting a frame.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class NullFrameException extends IllegalArgumentException {

    private static final long serialVersionUID = 7772171144117779051L;

    public NullFrameException(String s) {
        super(s);
    }
}
