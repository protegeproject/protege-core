package edu.stanford.smi.protege.util;

import java.util.*;

/**
 *A multi - map whose values are stored in a List.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @deprecated - use ArrayListMultiMap instead
 */
public class ListMultiMap<X,Y> extends MultiMap<X,Y> {

    public ListMultiMap() {
    }

    public ListMultiMap(int size) {
        super(size);
    }

    @SuppressWarnings("unchecked")
    public Collection<Y> createCollection() {
        return (Collection<Y>) new HashList();
    }

    public String toString() {
        return "ListMultiMap";
    }
}
