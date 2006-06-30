package edu.stanford.smi.protege.exception;



public class TransactionException extends ProtegeStoreException {

  
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
