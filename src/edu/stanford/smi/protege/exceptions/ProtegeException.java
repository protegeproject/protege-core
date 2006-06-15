package edu.stanford.smi.protege.exception;

public class ProtegeException extends RuntimeException {
  
    public ProtegeException() { }
    
    public ProtegeException(String msg) {
      super(msg);
    }
    
    public ProtegeException(String msg, Throwable t) {
      super(msg, t);
    }
  
    public ProtegeException(Throwable t) {
        super(t.toString());
    }
}