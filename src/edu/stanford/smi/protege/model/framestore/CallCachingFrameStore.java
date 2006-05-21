package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Method;
import java.util.Collection;

import edu.stanford.smi.protege.util.CacheMap;
import edu.stanford.smi.protege.util.TransactionMonitor;

public class CallCachingFrameStore extends AbstractFrameStoreInvocationHandler {
    private static final int MAX_SIZE = 100 * 1000;
    private CacheMap _cache = new CacheMap(MAX_SIZE);
    private MethodCall _lookupMethodCall = new MethodCall();
    private TransactionMonitor transactionMonitor;

    protected Object handleInvoke(Method method, Object[] args) {
        Object result;
        /*
         * I think that the theory is that this first check (for events) will make the 
         * call caching work with the client-server but I don't think it is sufficient.  
         * For now we are disabling the call caching on the client.
         */
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
            /*
             * Don't update the cache during a transaction.  Different clients see
             * different values.
             */
            if (transactionMonitor == null || !transactionMonitor.existsTransaction()) {
              _cache.put(new MethodCall(method, args), o);
            }
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

    protected void setDelegate(FrameStore delegate) {
      super.setDelegate(delegate);
      if (delegate != null) {
        transactionMonitor = delegate.getTransactionStatusMonitor();
      }
    }
}
