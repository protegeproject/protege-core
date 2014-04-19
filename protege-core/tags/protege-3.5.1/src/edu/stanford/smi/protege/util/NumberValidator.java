package edu.stanford.smi.protege.util;

/**
 * Interface to convert a string to a number.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface NumberValidator extends Validator {

    Number convertToNumber(String string);
}
