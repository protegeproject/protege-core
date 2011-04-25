package edu.stanford.smi.protege.util;

import java.util.logging.Level;

/**
 * This class is a Runnable that only allows one thread to be executing at a time.
 * It includes methods that allow a caller to wait for the execution to start or finish.
 * This class implements its own notions of running and aborted.
 * 
 * Is this implemented in Java Core?
 * 
 * @author tredmond
 *
 */
public abstract class ExclusiveRunnable implements Runnable {
  private boolean running = false;
  private boolean aborted = false;
  private Object lock = new Object();
  
  /**
   * This method is where the developer implements his own execute procedure.
   *
   */
  public abstract void execute();

  /**
   * It is generally expected that the developer will not override this method.  
   * The actual code to execute should be placed in the execute() method.
   */
  public void run() {
    synchronized (lock) {
      waitForShutdown();
      running = true;
      aborted = false;
      lock.notifyAll();
    }
    execute();
    synchronized (lock) {
      running = false;
      lock.notifyAll();
    }
  }
  
  /**
   * Waits for the execution to start.
   *
   */
  public void waitForStartup() {
    synchronized (lock) {
      while (!running) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          Log.getLogger().log(Level.WARNING,"Unexpected interrupt", e);
        }       
      }
    }
  }
  
  /**
   * Wait for execution to complete.
   *
   */
  public void waitForShutdown() {
    synchronized (lock) {
      while (running) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          Log.getLogger().log(Level.WARNING,"Unexpected interrupt", e);
        }
      }
    }
  }
  
  /**
   * Sets the aborted flag for the current runnable.
   * 
   * If you don't wait for the runnable to be running then setting the aborted
   * flag won't do what you want.  It is possible to start a thread (but it isn't
   * running yet), abort  the thread (still not running) and for the thread to start
   * running with the abort flag reset to false.
   *
   */
  public void abort() {
    synchronized (lock) {
      aborted = true;
    }
  }
  
  public boolean isAborted() {
    synchronized (lock) {
      return aborted;
    }
  }
}
