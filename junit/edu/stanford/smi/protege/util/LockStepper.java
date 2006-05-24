package edu.stanford.smi.protege.util;

import java.util.logging.Level;

import junit.framework.AssertionFailedError;


/**
 * This is a class to be used for testing the execution of multiple threads.
 * We use an enumeration to define the stages of a test.  A thread can wait for 
 * other threads to get to a given stage in the test or can signal that a given 
 * stage of the test has been acheived.
 * 
 * @author tredmond
 *
 */
public class LockStepper<X extends Enum> {

  private X testStage;
  private Object lock = new Object();
  private Object passedObject;
  
  public LockStepper(X start) {
    testStage = start;
  }
  
  public Object waitForStage(X stage) {
    return waitForStage(stage, 0);
  }
  
  public Object waitForStage(X stage, int timeout) throws AssertionFailedError {
    synchronized (lock) {
      while (!stageReached(stage)) {
        try {
          if (timeout <= 0) {
            lock.wait();
          } else {
            lock.wait(timeout);
          }
        } catch (InterruptedException e) {
          Assert.fail("Execution interrupted");
        }
      }
      if (passedObject instanceof AssertionFailedError) {
        throw (AssertionFailedError) passedObject;
      }
      return passedObject;
    }
  }
  
  public void stageAchieved(X stage, Object o) {
    synchronized (lock) {
      testStage = stage;
      passedObject = o;
      lock.notifyAll();
    }
  }
  
  public void fail(X stage, Throwable failure) {
    Log.getLogger().log(Level.SEVERE, "Exception in other thread", failure);
    AssertionFailedError fail = new AssertionFailedError("Exception in other thread");
    fail.initCause(failure);
    stageAchieved(stage, fail);
  }
  
  private boolean stageReached(X stage) {
    return testStage.ordinal() >= stage.ordinal();
  }
  
}
