package edu.stanford.smi.protege.model.query;

import java.util.Collection;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;

/**
 * This class is a QueryCallback that provides utilities for making the 
 * asynchronous executeQuery() call synchronous. The caller sequence should 
 * look like this
 *
 *   SynchronizeQueryCallback callback = new SynchronizeQueryCallback();
 *   executeQuery(query, callback);
 *   return callback.waitForResults():
 */

public class SynchronizeQueryCallback implements QueryCallback, Localizable {
  private Object kbLock;
  private Object result;
  private boolean ready = false;

  public SynchronizeQueryCallback(Object kbLock) {
    this.kbLock = kbLock;
  }

  public void provideQueryResults(Collection<Frame> frames) {
    synchronized (kbLock) {
      result = frames;
      ready = true;
      kbLock.notifyAll();
    }
  }

  private void passException(Exception pe) {
    synchronized (kbLock) {
      result = pe;
      ready = true;
      kbLock.notifyAll();
    } 
  }
  
  public void handleError(OntologyException oe) {
    passException(oe);
  }
  
  public void handleError(ProtegeIOException ioe) {
    passException(ioe);
  }
  
  public void handleError(ProtegeError pe) {
    passException(pe);
  }

  public Collection<Frame> waitForResults() throws OntologyException, ProtegeIOException {
    Object o = null;
    try {
      synchronized (kbLock) {
        while (!ready) {
          kbLock.wait();
        }
        o = result;
      }
    } catch (InterruptedException e) {
      throw new ProtegeIOException(e);
    } finally {
        synchronized (kbLock) {
            ready = false;
            result = null;
        }
    }
    if (o == null) {
        return null;
    }
    else if (o instanceof Collection) {
      return (Collection<Frame>) o;
    } 
    else if (o instanceof OntologyException) {
      throw (OntologyException) o;
    }
    else if (o instanceof ProtegeIOException) {
      throw (ProtegeIOException) o;
    }
    else {
      throw (ProtegeError) o;
    }
  }

  public void localize(KnowledgeBase kb) {
    kbLock = kb;
  }
}

