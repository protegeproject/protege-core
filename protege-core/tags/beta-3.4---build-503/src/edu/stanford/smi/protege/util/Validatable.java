package edu.stanford.smi.protege.util;

/**
 * An interface that allows the implementer to veto a change or to record whatever is necessary when a save succeeds.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Validatable {

    /**
     * Save the contents of the current fields of this component to the
     * underlying data structure.  This method is called only after validateContents() has returned true for this 
     * object and, possibly, for other objects.
     */
    void saveContents();

    /**
     * Check whether the current values of the fields in this component are
     * valid. If they are, return true. If not, prompt the user with a dialog
     * explaining the problem and return false.
     */
    boolean validateContents();
}
