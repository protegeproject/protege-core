package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Method;
import java.util.Collection;

import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.CacheMap;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

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
    
    public void executeQuery(Query q, QueryCallback qc) {
      getDelegate().executeQuery(q, qc);
    }

    private static boolean isGetEvents(Method m) {
        return m.getName().equals("getEvents");
    }

    private int hit = 0;
    private int miss = 0;

    private Object query(Method method, Object[] args) {
        /*
         * If the transaction isolation level is serializable then we must go to the database
         * to do the read to let it know how to the locking.
         */
        boolean mustRead = false;
        try {
          mustRead = transactionMonitor != null &&
                     transactionMonitor.inTransaction() && 
                     transactionMonitor.getTransationIsolationLevel() == TransactionIsolationLevel.SERIALIZABLE;
        } catch (TransactionException te) {
          mustRead = true;
        }
        _lookupMethodCall.set(method, args);
        Object o = _cache.get(_lookupMethodCall);
        if (o == null || mustRead) {
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
