package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Abstract implementation of a "multimap" (a map from a key to a collection of values).  This inteface leaves unspecified
 * the type of collection that contains the values.  The collection type is then specified in the derived classes.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class MultiMap {
    private Map _map;

    protected MultiMap() {
        this(11);
    }

    protected MultiMap(int initSize) {
        _map = new HashMap(initSize);
    }

    public void addValue(Object key, Object value) {
        Collection c = (Collection) _map.get(key);
        if (c == null) {
            c = createCollection();
            _map.put(key, c);
        }
        c.add(value);
    }

    public void addValues(Object key, Collection values) {
        Collection c = (Collection) _map.get(key);
        c.addAll(values);
    }

    public abstract Collection createCollection();

    public Collection getKeys() {
        return _map.keySet();
    }

    public Collection getValues(Object key) {
        return (Collection) _map.get(key);
    }

    public Collection removeKey(Object key) {
        return (Collection) _map.remove(key);
    }

    public void removeValue(Object key, Object value) {
        Collection c = (Collection) _map.get(key);
        if (c == null) {
            // Log.trace("key not found", this, "removeValue", key, value);
        } else {
            boolean succeeded = c.remove(value);
            if (!succeeded) {
                Log.getLogger().warning("value not found: " + value);
            }
        }
    }

    public void removeValues(Object key, Collection values) {
        Collection c = (Collection) _map.get(key);
        c.removeAll(values);
    }
}
