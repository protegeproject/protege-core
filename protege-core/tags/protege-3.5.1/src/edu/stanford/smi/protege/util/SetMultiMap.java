package edu.stanford.smi.protege.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Multimap implementation where the value collection is a set.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SetMultiMap<X,Y> extends MultiMap<X,Y> {

    public SetMultiMap() {
    }

    public SetMultiMap(int size) {
        super(size);
    }

    public Collection<Y> createCollection() {
        return new HashSet<Y>();
    }
    
    public Set<Y> getValues(X key) {
        return (Set<Y>) super.getValues(key);
    }
    
    public Set<Y> removeKey(X key) {
        return (Set<Y>) super.removeKey(key);
    }
}
