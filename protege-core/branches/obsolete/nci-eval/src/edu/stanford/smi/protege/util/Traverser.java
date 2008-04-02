package edu.stanford.smi.protege.util;

/**
 * Traverse from one object to the next
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Traverser {
    Object get(Object o);
}
