package edu.stanford.smi.protege.exception;

/**
 * This class represents a exception accessing the backing store such
 * as an IO exception or a SQL exception.  It generally should not represent
 * a problem with an ontology or a programmer error.
 * 
 * @author tredmond
 *
 */
public class ProtegeStoreException extends ProtegeException {

  public ProtegeStoreException() {
    super();
  }
  
  public ProtegeStoreException(String msg) {
    super(msg);
  }
  
  public ProtegeStoreException(Throwable t) {
    super(t);
  }
}
