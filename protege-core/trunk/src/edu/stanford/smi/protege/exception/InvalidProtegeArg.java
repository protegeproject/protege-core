package edu.stanford.smi.protege.exception;

public class InvalidProtegeArg extends ProtegeError {
  public InvalidProtegeArg() { }
  
  public InvalidProtegeArg(String msg) {
    super(msg);
  }
  
  public InvalidProtegeArg(String msg, Throwable t) {
    super(msg, t);
  }

}
