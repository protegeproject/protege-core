package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Method;

public class BusyFlagFrameStore extends AbstractFrameStoreInvocationHandler {
  public static final long QUIESCENCE_INTERVAL = 100;
  private static boolean busy = false;
  private static long quiescenceStarted = 0;
  
  
  @Override
  protected Object handleInvoke(Method method, Object[] args) {
    Object result;
    busy = true;
    result = invoke(method, args);
    quiescenceStarted = System.currentTimeMillis();
    busy = false;
    return result;
  }
  
  public static boolean isBusy() {
    if (busy) {
      return true;
    }
    return System.currentTimeMillis() <= quiescenceStarted + QUIESCENCE_INTERVAL;
  }
  
  
}
