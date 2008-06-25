package edu.stanford.smi.protege.util;

/**
 * An interface that encapsulates the logic that decides if a particular object is "valid" in some context.  If the
 * object is invalid then an error message is supplied.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Validator {

    String getErrorMessage(Object o);

    boolean isValid(Object o);
}
