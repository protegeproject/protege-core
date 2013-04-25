package edu.stanford.smi.protege.exception;



public class TransactionException extends ProtegeIOException {

  
  private static final long serialVersionUID = 8245262074237593250L;

public TransactionException() {
    super();
  }

  public TransactionException(String msg) {
    super(msg);
  }
  
  public TransactionException(String msg, Throwable t) {
    super(msg, t);
  }
  
  public TransactionException(Throwable t) {
    super(t);
  }
}
