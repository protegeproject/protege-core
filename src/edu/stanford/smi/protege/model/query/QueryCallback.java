package edu.stanford.smi.protege.model.query;

import java.util.Collection;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Frame;

public interface QueryCallback {
  
  public void provideQueryResults(Collection<Frame> results);

  public void handleError(OntologyException exception);
  
  public void handleError(ProtegeIOException exception);
  
  public void handleError(ProtegeError error);

}
