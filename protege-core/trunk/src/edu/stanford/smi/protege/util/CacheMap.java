package edu.stanford.smi.protege.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class CacheMap<X,Y>  extends WeakHashMap<X,Y> {
    private static final int INIT_SIZE = 10007;
    private int maxSize;

    public CacheMap(int maxSize) {
        super();
        this.maxSize = maxSize;
    }
    
    public CacheMap() {
        this(Integer.MAX_VALUE);
    }



    public Y put(X key, Y value) {
        Y oldValue = super.put(key, value);
        fixSize();
        return oldValue;
    }
    
    private void fixSize() {
        if (size() > maxSize) {
            Log.getLogger().info("removing elements from call cache");
            int count = 0;
            int nremove = maxSize/5;
            Iterator<X> i = keySet().iterator();
            while (i.hasNext() && count++ < nremove) {
                i.next();
                i.remove();
            }
        }
    }

}
