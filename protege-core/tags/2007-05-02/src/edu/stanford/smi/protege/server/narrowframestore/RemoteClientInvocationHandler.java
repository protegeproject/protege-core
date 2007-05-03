package edu.stanford.smi.protege.server.narrowframestore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;


public class RemoteClientInvocationHandler implements InvocationHandler {
  private transient static Logger log = Log.getLogger(RemoteClientInvocationHandler.class);
  
  private KnowledgeBase kb;
  private RemoteServerNarrowFrameStore delegate;
  private static Map<Method, Method> methodMap = new HashMap<Method, Method>();
  static {
    Method [] methods = NarrowFrameStore.class.getMethods();
    for (Method method : methods) {
      try {
        Method remoteMethod = 
            RemoteServerNarrowFrameStore.class.getMethod(method.getName(), 
                                                         (Class []) method.getParameterTypes());
        methodMap.put(method, remoteMethod);
        if (log.isLoggable(Level.FINE)) {
          log.fine("Mapped " + method + " to " + remoteMethod);
        }
      } catch (Exception e) {
        log.warning("NarrowFrameStore method " + method + " not found in RemoteServerNarrowFrameStore");
      }
      method.getParameterTypes();
    }
  }
  
  
  public RemoteClientInvocationHandler(KnowledgeBase kb,
                                       RemoteServerNarrowFrameStore delegate) {
    this.kb = kb;
    this.delegate = delegate;
  }
  
  public NarrowFrameStore getNarrowFrameStore() {
    return
      (NarrowFrameStore) Proxy.newProxyInstance(NarrowFrameStore.class.getClassLoader(),
                                                new Class[] {NarrowFrameStore.class},
                                                this);
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Client invoking remote operation " + method.getName() + " on object " + delegate.getClass());
      if (args != null) {
        for (Object arg : args) {
          log.fine("\tArgument = " + arg);
        }
      } else {
        log.fine("No arguments");
      }
    }
    ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader correctLoader = kb.getClass().getClassLoader();
    if (currentLoader != correctLoader) {
      if (log.isLoggable(Level.FINE)) {
        Log.getLogger().fine("Changing loader from " + currentLoader + " to " + correctLoader);
      }
      Thread.currentThread().setContextClassLoader(correctLoader);
    }
    try {
      Method remoteMethod = methodMap.get(method);
      Object o =  remoteMethod.invoke(delegate, args);
      
      LocalizeUtils.localize(o, kb);
      return o;
    } catch (InvocationTargetException ite) {
      Throwable cause = ite.getCause();
      if (cause instanceof RemoteException) {
        throw new RuntimeException(cause);
      } else {
        throw cause;
      }
    }
  }
 
}
