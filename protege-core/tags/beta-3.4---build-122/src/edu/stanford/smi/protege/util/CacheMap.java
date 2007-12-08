package edu.stanford.smi.protege.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class CacheMap<X,Y> {
    private static final int INIT_SIZE = 10007;
    private Map<X,Reference<? extends Y>> keyToReferenceMap 
      = new HashMap<X,Reference<? extends Y>>(INIT_SIZE);
    private Map<Reference<? extends Y>,X> referenceToKeyMap 
      = new HashMap<Reference<? extends Y>,X>(INIT_SIZE);
    private ReferenceQueue<Y> referenceQueue = new ReferenceQueue<Y>();
    private int maxSize;

    public CacheMap(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public CacheMap() {
        this(Integer.MAX_VALUE);
    }

    private void pollQueue() {
        Reference<? extends Y> ref;
        while ((ref = referenceQueue.poll()) != null) {
            X key = referenceToKeyMap.remove(ref);
            keyToReferenceMap.remove(key);
        }
    }

    public Y get(X key) {
        pollQueue();
        SoftReference<? extends Y> ref = (SoftReference<? extends Y>) keyToReferenceMap.get(key);
        Y value = (ref == null) ? null : ref.get();
        return value;
    }

    public void put(X key, Y value) {
        pollQueue();
        if (value == null) {
            keyToReferenceMap.put(key, (Reference<Y>) null);
        } else {
            Reference<Y> reference 
              = new SoftReference<Y>(value, referenceQueue);
            keyToReferenceMap.put(key, reference);
            referenceToKeyMap.put(reference, key);
        }
        fixSize();
    }
    
    private void fixSize() {
        if (keyToReferenceMap.size() > maxSize) {
            Log.getLogger().info("removing elements from call cache");
            int count = 0;
            int nremove = maxSize/5;
            Iterator<X> i = keyToReferenceMap.keySet().iterator();
            while (i.hasNext() && count++ < nremove) {
                X key = i.next();
                Reference<? extends Y> value = keyToReferenceMap.get(key);
                referenceToKeyMap.remove(value);
                i.remove();
            }
        }
    }

    public void remove(X key) {
        pollQueue();
        Reference<? extends Y> reference = keyToReferenceMap.remove(key);
        referenceToKeyMap.remove(reference);
    }

    public void clear() {
        keyToReferenceMap.clear();
        referenceToKeyMap.clear();
        referenceQueue = new ReferenceQueue();
    }

    public Collection<X> getKeys() {
        pollQueue();
        return new ArrayList<X>(keyToReferenceMap.keySet());
    }

    public String toString() {
        pollQueue();
        return StringUtilities.getClassName(this);
    }

}
