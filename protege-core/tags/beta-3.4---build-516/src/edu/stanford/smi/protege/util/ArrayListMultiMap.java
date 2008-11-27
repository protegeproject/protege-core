package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Multimap implementation where the value collection is a set.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArrayListMultiMap<X,Y> extends MultiMap<X,Y> {

    public ArrayListMultiMap() {
    }

    public ArrayListMultiMap(int size) {
        super(size);
    }

    public List<Y> createCollection() {
        return new ArrayList<Y>();
    }
    
    public List<Y> getValues(X key) {
        return (List<Y>) super.getValues(key);
    }
    
    public List<Y> removeKey(X key) {
        return (List<Y>) super.removeKey(key);
    }
}
