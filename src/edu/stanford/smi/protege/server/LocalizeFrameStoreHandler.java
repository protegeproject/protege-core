package edu.stanford.smi.protege.server;

import java.lang.reflect.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LocalizeFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private KnowledgeBase _kb;

    public LocalizeFrameStoreHandler(KnowledgeBase kb) {
        _kb = kb;
    }

    protected Object handleInvoke(Method method, Object[] args) {
        localize(args);
        return invoke(method, args);
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
