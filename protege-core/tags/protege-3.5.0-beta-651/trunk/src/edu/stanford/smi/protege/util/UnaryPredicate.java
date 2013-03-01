package edu.stanford.smi.protege.util;

/**
 * An interface that encapsulates a unary (takes one argument) predicate (function that returns a boolean).
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface UnaryPredicate {

    boolean apply(Object o);
}
