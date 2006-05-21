package edu.stanford.smi.protege.util.exceptions;

public class TransactionException extends Exception {
  public TransactionException() {
    super();
  }
  
  public TransactionException(String  msg) {
    super(msg);
  }
  
  public TransactionException(Throwable t) {
    super(t);
  }

}
