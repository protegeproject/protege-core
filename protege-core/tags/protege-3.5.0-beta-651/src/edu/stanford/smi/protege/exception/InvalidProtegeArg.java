package edu.stanford.smi.protege.exception;

public class InvalidProtegeArg extends ProtegeError {
  private static final long serialVersionUID = 9094134655114398220L;

public InvalidProtegeArg() { }
  
  public InvalidProtegeArg(String msg) {
    super(msg);
  }
  
  public InvalidProtegeArg(String msg, Throwable t) {
    super(msg, t);
  }

}
