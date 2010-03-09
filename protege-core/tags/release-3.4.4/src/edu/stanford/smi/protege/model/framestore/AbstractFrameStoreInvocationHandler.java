package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

public abstract class AbstractFrameStoreInvocationHandler implements InvocationHandler {
    private static final Logger log = Log.getLogger(AbstractFrameStoreInvocationHandler.class);
  
    private FrameStore _delegate;
    private static final Set<Method> specialMethods = new HashSet<Method>();

    static {
        try {
            specialMethods.add(getMethod(Object.class, "toString"));
            specialMethods.add(getMethod(Object.class, "equals", Object.class));
            specialMethods.add(getMethod("getDelegate"));
            specialMethods.add(getMethod("setDelegate", FrameStore.class));
            specialMethods.add(getMethod("close"));
            specialMethods.add(getMethod("reinitialize"));
            specialMethods.add(FrameStore.class
                                 .getMethod("executeQuery", 
                                            new Class[] { Query.class, QueryCallback.class }));
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }

    private static Method getMethod(String name) throws Exception {
        return getMethod(FrameStore.class, name);
    }

    private static Method getMethod(Class clas, String name) throws Exception {
        return clas.getMethod(name, (Class[]) null);
    }

    private static Method getMethod(String name, Class arg) throws Exception {
        return getMethod(FrameStore.class, name, arg);
    }

    private static Method getMethod(Class clas, String name, Class arg) throws Exception {
        return clas.getMethod(name, new Class[] { arg });
    }

    private static boolean isSpecial(Method method) {
        return specialMethods.contains(method);
    }

    protected FrameStore getDelegate() {
        return _delegate;
    }

    /**
     * This methods sets the delegate for this InvocationHandler and for
     * the associated frame store.  It is only called by the InvocationHandler but
     * it is overridden by classes such as the CallCachingFrameStore.
     * 
     * @param delegate the delegate FrameStore.
     */
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
            AbstractFrameStoreInvocationHandler handler = (AbstractFrameStoreInvocationHandler) getInstance(handlerClass, kb);
            fs = (FrameStore) Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class[] { FrameStore.class }, handler);
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
        if (log.isLoggable(Level.FINE)) {
          log.fine("invoking " + StringUtilities.getClassName(this));
        }
        
        if (isSpecial(method)) {
            o = invokeSpecial(proxy, method, args);
        } else if (_delegate != null) {
            o = handleInvoke(method, args);
        }
        return o;
    }

    private Object invokeSpecial(Object proxy, Method method, Object[] args) {
        Object o = null;
        String methodName = method.getName();
        if (methodName.equals("toString")) {
            o = toString();
        } 
        else if (methodName.equals("equals")) {
            o = Boolean.valueOf(proxy == args[0]);
        } 
        else if (methodName.equals("getDelegate")) {
            o = _delegate;
        } 
        else if (methodName.equals("setDelegate")) {
            setDelegate((FrameStore) args[0]);
        } 
        else if (methodName.equals("close")) {
            handleClose();
            _delegate = null;
        } 
        else if (methodName.equals("reinitialize")) {
            handleReinitialize();
        }
        else if (methodName.equals("executeQuery")) {
          executeQuery((Query) args[0], (QueryCallback) args[1]);
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
    
    protected abstract void executeQuery(Query q, QueryCallback qc);

    protected Object invoke(Method m, Object args[]) {
        return invoke(m, args, _delegate);
    }

    protected static Object invoke(Method m, Object args[], FrameStore frameStore) {
        Object result = null;
        try {
            result = m.invoke(frameStore, args);
        } catch (IllegalAccessException e) {
            Log.getLogger().warning(Log.toString(e));
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            Log.getLogger().warning(Log.toString(cause));
        }
        return result;
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }

}
