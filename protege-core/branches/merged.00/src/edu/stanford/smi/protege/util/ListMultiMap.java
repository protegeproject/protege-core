package edu.stanford.smi.protege.util;

import java.util.*;

/**
 *A multi - map whose values are stored in a List.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ListMultiMap extends MultiMap {

    public ListMultiMap() {
    }

    public ListMultiMap(int size) {
        super(size);
    }

    public Collection createCollection() {
        return new HashList();
    }

    public String toString() {
        return "ListMultiMap";
    }
}
