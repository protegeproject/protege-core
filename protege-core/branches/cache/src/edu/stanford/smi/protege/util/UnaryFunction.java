package edu.stanford.smi.protege.util;

/**
 * An interface that encapsulates a unary (takes one argument) function.  The return value depends on the function and
 * may be null.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface UnaryFunction<X,Y> {

    Y apply(X o);
}
