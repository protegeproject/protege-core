package edu.stanford.smi.protege.util;

import java.lang.ref.*;
import java.util.*;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class CacheMap {
    private static final int INIT_SIZE = 10007;
    private Map keyToReferenceMap = new HashMap(INIT_SIZE);
    private Map referenceToKeyMap = new HashMap(INIT_SIZE);
    private ReferenceQueue referenceQueue = new ReferenceQueue();
    private int maxSize;

    public CacheMap(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public CacheMap() {
        this(Integer.MAX_VALUE);
    }

    private void pollQueue() {
        Reference ref;
        while ((ref = referenceQueue.poll()) != null) {
            Object key = referenceToKeyMap.remove(ref);
            keyToReferenceMap.remove(key);
        }
    }

    public Object get(Object key) {
        pollQueue();
        SoftReference ref = (SoftReference) keyToReferenceMap.get(key);
        Object value = (ref == null) ? null : ref.get();
        return value;
    }

    public void put(Object key, Object value) {
        pollQueue();
        if (value == null) {
            keyToReferenceMap.put(key, value);
        } else {
            Reference reference = new SoftReference(value, referenceQueue);
            keyToReferenceMap.put(key, reference);
            referenceToKeyMap.put(reference, key);
        }
        fixSize();
    }
    
    private void fixSize() {
        if (keyToReferenceMap.size() > maxSize) {
            Log.trace("removing elements from call cache", this, "fixSize");
            int count = 0;
            int nremove = maxSize/5;
            Iterator i = keyToReferenceMap.entrySet().iterator();
            while (i.hasNext() && count++ < nremove) {
                Object key = i.next();
                Object value = keyToReferenceMap.get(key);
                referenceToKeyMap.remove(value);
                i.remove();
            }
        }
    }

    public void remove(Object key) {
        pollQueue();
        Object reference = keyToReferenceMap.remove(key);
        referenceToKeyMap.remove(reference);
    }

    public void clear() {
        keyToReferenceMap.clear();
        referenceToKeyMap.clear();
        referenceQueue = new ReferenceQueue();
    }

    public Collection getKeys() {
        pollQueue();
        return new ArrayList(keyToReferenceMap.keySet());
    }

    public String toString() {
        pollQueue();
        return StringUtilities.getClassName(this);
    }

}
