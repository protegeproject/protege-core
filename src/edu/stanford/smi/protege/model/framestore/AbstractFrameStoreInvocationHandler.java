package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

public abstract class AbstractFrameStoreInvocationHandler implements InvocationHandler {
    private FrameStore _delegate;

    protected FrameStore getDelegate() {
        return _delegate;
    }

    protected void setDelegate(FrameStore delegate) {
        _delegate = delegate;
    }

    public static FrameStore newInstance(Class handlerClass) {
        return newInstance(handlerClass, null);
    }

    public static Object getInstance(Class clas, KnowledgeBase kb) throws IllegalAccessException,
            InstantiationException, InvocationTargetException {
        Object instance;
        try {
            Constructor constructor = clas.getConstructor(new Class[] { KnowledgeBase.class });
            instance = constructor.newInstance(new Object[] { kb });
        } catch (NoSuchMethodException ex) {
            instance = clas.newInstance();
        }
        return instance;
    }

    public static FrameStore newInstance(Class handlerClass, KnowledgeBase kb) {
        FrameStore fs = null;
        try {
            AbstractFrameStoreInvocationHandler handler = (AbstractFrameStoreInvocationHandler) getInstance(
                    handlerClass, kb);
            fs = (FrameStore) Proxy.newProxyInstance(handlerClass.getClassLoader(),
                    new Class[] { FrameStore.class }, handler);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return fs;
    }

    protected static boolean isQuery(Method method) {
        String name = method.getName();
        return name.startsWith("get");
    }

    protected static boolean isModification(Method method) {
        return !isQuery(method) && !method.getName().equals("reinitialize");
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        Object o = null;
        String methodName = method.getName();
        if (methodName.equals("toString")) {
            o = toString();
        } else if (methodName.equals("equals")) {
            o = Boolean.valueOf(proxy == args[0]);
        } else if (methodName.equals("getDelegate")) {
            o = _delegate;
        } else if (methodName.equals("setDelegate")) {
            _delegate = (FrameStore) args[0];
        } else if (methodName.equals("close")) {
            handleClose();
            _delegate = null;
        } else if (methodName.equals("reinitialize")) {
            handleReinitialize();
        } else if (_delegate != null) {
            o = handleInvoke(method, args);
        }
        return o;
    }

    protected void handleReinitialize() {
        // do nothing
    }

    protected void handleClose() {
        // do nothing;
    }

    protected abstract Object handleInvoke(Method method, Object[] args);

    protected Object invoke(Method m, Object args[]) {
        return invoke(m, args, getDelegate());
    }

    protected static Object invoke(Method m, Object args[], FrameStore frameStore) {
        Object result = null;
        try {
            result = m.invoke(frameStore, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            cause.printStackTrace();
        }
        return result;
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}