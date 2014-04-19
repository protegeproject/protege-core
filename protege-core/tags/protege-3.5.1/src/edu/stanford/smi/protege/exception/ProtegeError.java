package edu.stanford.smi.protege.exception;

/**
 * This is for errors in Protege that are caused by a programmer
 * mistake.  A reasonable catch for this error would provide an option
 * for e-mailing a bug report to Stanford.  It is deliberately meant to 
 * be unchecked so that we do  not have to declare our bugs for each method ;).
 * 
 * @author tredmond
 *
 */
public class ProtegeError extends RuntimeException {
  
  private static final long serialVersionUID = -7756694680618468454L;

public ProtegeError() {
    super();
  }

  public ProtegeError(String msg) {
    super(msg);
  }
  
  public ProtegeError(String msg, Throwable t) {
    super(msg, t);
  }
  
  public ProtegeError(Throwable t) {
    super(t);
  }
}
