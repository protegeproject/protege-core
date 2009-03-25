package edu.stanford.smi.protege.model.query;

import java.util.Collection;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Frame;

public class QueryCallbackClone implements QueryCallback {
  private QueryCallback qc;
  
  public QueryCallbackClone(QueryCallback qc) {
    this.qc = qc;
  }
  
  public QueryCallback getInnerCallback() {
    return qc;
  }

  public void provideQueryResults(Collection<Frame> results) {
    qc.provideQueryResults(results);
  }

  public void handleError(OntologyException exception) {
    qc.handleError(exception);
  }

  public void handleError(ProtegeIOException exception) {
    qc.handleError(exception);
  }

  public void handleError(ProtegeError error) {
    qc.handleError(error);
  }

}
