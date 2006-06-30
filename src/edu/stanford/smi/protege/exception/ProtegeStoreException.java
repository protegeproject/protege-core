package edu.stanford.smi.protege.exception;


/**
 * This exception represents errors accessing the backing store that 
 * are not related to problems with the ontology.  These exceptions encapsulate
 * file io exceptions, sql exceptions and remote exceptions.
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
  
  public ProtegeStoreException(String msg, Throwable t) {
    super(msg, t);
  }
  
  public ProtegeStoreException(Throwable t) {
    super(t);
  }
  
}
