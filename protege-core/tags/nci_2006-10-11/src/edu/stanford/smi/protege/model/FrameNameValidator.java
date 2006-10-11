package edu.stanford.smi.protege.model;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameNameValidator {
    boolean isValid(String name, Frame frame);

    String getErrorMessage(String name, Frame frame);
}
