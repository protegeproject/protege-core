package edu.stanford.smi.protege.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 * @deprecated use the transaction cache utilities.
 */
@Deprecated
public class CacheMap<X,Y>  extends HashMap<X,Y> {
    private static final long serialVersionUID = 1104711979831307296L;
    public static final String DEFAULT_CACHE_MAP_SIZE_PROPERTY="default.cache.map.size";
    public static final int DEFAULT_CACHE_MAP_SIZE=ApplicationProperties.getIntegerProperty(DEFAULT_CACHE_MAP_SIZE_PROPERTY, 50000);
    
    private int maxSize;

    public CacheMap(int maxSize) {
        super();
        this.maxSize = maxSize;
    }
    
    public CacheMap() {
        this(DEFAULT_CACHE_MAP_SIZE);
    }



    public Y put(X key, Y value) {
        Y oldValue = super.put(key, value);
        fixSize();
        return oldValue;
    }
    
    private void fixSize() {
        if (size() > maxSize) {
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
