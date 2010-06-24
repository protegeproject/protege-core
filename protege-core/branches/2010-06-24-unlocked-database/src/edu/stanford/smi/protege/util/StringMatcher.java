package edu.stanford.smi.protege.util;

/**
 * An interface that can be used to query whether a particular value is a match for another string.  The other string
 * might contains wildcards, regexp, etc.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface StringMatcher {

    boolean isMatch(String value);
}
