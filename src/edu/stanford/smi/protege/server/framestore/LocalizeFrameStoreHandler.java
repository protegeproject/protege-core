package edu.stanford.smi.protege.server.framestore;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.AbstractFrameStoreInvocationHandler;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LocalizeFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private static transient Logger log = Log.getLogger(LocalizeFrameStoreHandler.class);
    private int counter = 0;
    
    private KnowledgeBase _kb;

    public LocalizeFrameStoreHandler(KnowledgeBase kb) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Entering the Localize constructor");
        }
        _kb = kb;
    }

    protected Object handleInvoke(Method method, Object[] args) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Received Remote Invocation of " + method);
          log.fine("Count = " + (++counter));
        }
        localize(args);
        return invoke(method, args);
    }
    
    @Override
    protected void executeQuery(Query q, QueryCallback qc) {
      q.localize(_kb);
      LocalizeUtils.localize(qc, _kb);
      getDelegate().executeQuery(q, qc);
    }

    private void localize(Object[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                Object o = args[i];
                LocalizeUtils.localize(o, _kb);
            }
        }
    }

    public FrameStore newFrameStore() {
        ClassLoader loader = getClass().getClassLoader();
        Class[] classes = new Class[] { FrameStore.class };
        return (FrameStore) Proxy.newProxyInstance(loader, classes, this);
    }


}
