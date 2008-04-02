package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Method;

import edu.stanford.smi.protege.exception.ModificationException;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;

public class ReadOnlyFrameStoreHandler extends AbstractFrameStoreInvocationHandler {

    public Object handleInvoke(Method method, Object[] args) {
        if (isModification(method)) {
            throw new ModificationException(method.getName());
        }
        return invoke(method, args);
    }
    
    public void executeQuery(Query q, QueryCallback qc) {
      getDelegate().executeQuery(q, qc);
    }
}