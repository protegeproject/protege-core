package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

public class BusyFlagFrameStore extends AbstractFrameStoreInvocationHandler {
  private static transient Logger log = Log.getLogger(BusyFlagFrameStore.class);
  public static final long QUIESCENCE_INTERVAL = 100;
  private static boolean busy = false;
  private static long quiescenceStarted = 0;
  
  
  @Override
  protected Object handleInvoke(Method method, Object[] args) {
    Object result;
    busy = true;
    if (log.isLoggable(Level.FINE)) {
      log.fine("Started executing method " + method + " at " + System.currentTimeMillis());
    }
    result = invoke(method, args);
    quiescenceStarted = System.currentTimeMillis();
    if (log.isLoggable(Level.FINE)) {
      log.fine("Ended executing method " + method + " at " + quiescenceStarted);
    }
    busy = false;
    return result;
  }
  
  public static boolean isBusy() {
    boolean ret;
    long busyCheckTime = System.currentTimeMillis();
    if (busy) {
      ret = true;
    } else {
      ret = (busyCheckTime <= quiescenceStarted + QUIESCENCE_INTERVAL);
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Checking if busy at " + busyCheckTime + " result = " + ret);
    }
    return ret;
  }
  
  
}
