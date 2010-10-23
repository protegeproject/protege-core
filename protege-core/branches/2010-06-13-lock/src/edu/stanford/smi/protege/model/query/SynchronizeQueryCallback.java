package edu.stanford.smi.protege.model.query;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.util.Log;

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
  private transient Logger   logger = Log.getLogger(SynchronizeQueryCallback.class);
  private transient Lock     readerLock;
  private transient Object   queryCondition = new Object();
  private volatile  Object   result;
  private volatile boolean   ready = false;

  public SynchronizeQueryCallback(Lock readerLock) {
    this.readerLock = readerLock;
    ready = false;
  }

  public void provideQueryResults(Collection<Frame> frames) {
    try {
      readerLock.lock();
      result = frames;
      ready = true;
      synchronized (queryCondition) {
    	  queryCondition.notifyAll();
      }
    }
    finally {
      readerLock.unlock();
    }
  }

  private void passException(Exception pe) {
      try {
          readerLock.lock();
          result = pe;
          ready = true;
          synchronized (queryCondition) {
        	  queryCondition.notifyAll();
          }
      }
      finally {
          readerLock.unlock();
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
		  readerLock.lock();
		  synchronized (queryCondition) {
			  while (!ready) {
				  try {
					  queryCondition.wait();
				  }
				  catch (InterruptedException e) {
					  logger.log(Level.WARNING, "Ouch - I certainly didn't expect that!", e);
				  }
			  }
		  }
		  o = result;
	  }
    finally {
      readerLock.unlock();
      result = null;
      ready = false;
    }
    if (o instanceof Collection) {
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
      readerLock = kb.getReaderLock();
  }
}

