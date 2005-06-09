package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

public class CallCachingFrameStore extends AbstractFrameStoreInvocationHandler {
    private static final int MAX_SIZE = 100 * 1000;
    private CacheMap _cache = new CacheMap(MAX_SIZE);
    private MethodCall _lookupMethodCall = new MethodCall();

    protected Object handleInvoke(Method method, Object[] args) {
        Object result;
        if (isGetEvents(method)) {
            result = invoke(method, args);
            Collection c = (Collection) result;
            if (!c.isEmpty()) {
                clearCache();
            }
        } else if (isQuery(method)) {
            result = query(method, args);
        } else {
            _cache.clear();
            result = invoke(method, args);
        }
        return result;
    }

    private static boolean isGetEvents(Method m) {
        return m.getName().equals("getEvents");
    }

    private int hit = 0;
    private int miss = 0;

    private Object query(Method method, Object[] args) {
        _lookupMethodCall.set(method, args);
        Object o = _cache.get(_lookupMethodCall);
        if (o == null) {
            ++miss;
            o = invoke(method, args);
            _cache.put(new MethodCall(method, args), o);
        } else {
            ++hit;
        }
        return o;
    }

    private void clearCache() {
        _cache.clear();
    }

    public void handleClose() {
        _cache.clear();
        _cache = null;
    }

    protected void handleReinitialize() {
        _cache.clear();
    }
}
