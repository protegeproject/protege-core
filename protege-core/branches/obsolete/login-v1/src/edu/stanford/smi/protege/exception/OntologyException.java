package edu.stanford.smi.protege.exception;

public class OntologyException extends ProtegeException {
  private static final long serialVersionUID = 5320154500539948796L;

  public OntologyException() {
    super();
  }

  public OntologyException(String msg) {
    super(msg);
  }
  
  public OntologyException(String msg, Throwable t) {
    super(msg, t);
  }
  
  public OntologyException(Throwable t) {
    super(t);
  }
}

