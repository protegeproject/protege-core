package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;

public class ReadOnlyFrameStoreHandler extends AbstractFrameStoreInvocationHandler {

    public Object handleInvoke(Method method, Object[] args) {
        if (isModification(method)) {
            throw new ModificationException(method.getName());
        }
        return invoke(method, args);
    }
}