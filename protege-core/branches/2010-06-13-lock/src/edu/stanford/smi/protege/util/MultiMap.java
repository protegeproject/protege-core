package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Abstract implementation of a "multimap" (a map from a key to a collection of values).  This inteface leaves unspecified
 * the type of collection that contains the values.  The collection type is then specified in the derived classes.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class MultiMap<X,Y> {
    private Map<X,Collection<Y>> _map;

    protected MultiMap() {
        this(11);
    }

    protected MultiMap(int initSize) {
        _map = new HashMap<X, Collection<Y>>(initSize);
    }

    public void addValue(X key, Y value) {
        Collection<Y> c =  _map.get(key);
        if (c == null) {
            c = createCollection();
            _map.put(key, c);
        }
        c.add(value);
    }

    public void addValues(X key, Collection<Y> values) {
        Collection<Y> c = _map.get(key);
        if (c == null) {
            c = createCollection();
            _map.put(key, c);
        }
        c.addAll(values);
    }

    public abstract Collection<Y> createCollection();

    public Collection<X> getKeys() {
        return _map.keySet();
    }

    public Collection<Y> getValues(X key) {
        return _map.get(key);
    }

    public Collection<Y> removeKey(X key) {
        return _map.remove(key);
    }

    public void removeValue(X key, Y value) {
        Collection<Y> c =  _map.get(key);
        if (c == null) {
            // Log.trace("key not found", this, "removeValue", key, value);
        } else {
            boolean succeeded = c.remove(value);
            if (!succeeded) {
                Log.getLogger().warning("value not found: " + value);
            }
        }
    }

    public void removeValues(X key, Collection<Y> values) {
        Collection<Y> c = _map.get(key);
        c.removeAll(values);
    }
    
    public void clear() {
        _map.clear();
    }
}
