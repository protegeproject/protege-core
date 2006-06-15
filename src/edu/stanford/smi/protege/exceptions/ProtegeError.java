package edu.stanford.smi.protege.exception;

/**
 * This represents an exception denoting a programmer error and should be reported
 * to the developer group.  It is deliberately typed as unchecked because it doesn't 
 * make sense to declare programmer errors ahead of time ;-).
 * 
 * @author tredmond
 *
 */
public class ProtegeError extends RuntimeException {
  
  public ProtegeError() {
    super();
  }

  public ProtegeError(String msg) {
    super(msg);
  }
  
  public ProtegeError(Throwable t) {
    super(t);
  }
}
