package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Multimap implementation where the value collection is a set.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SetMultiMap extends MultiMap {

    public SetMultiMap() {
    }

    public SetMultiMap(int size) {
        super(size);
    }

    public Collection createCollection() {
        return new HashSet();
    }
}
