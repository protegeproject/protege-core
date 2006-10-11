package edu.stanford.smi.protege.server.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the writer part of a pair of linked classes, the FifoWriter
 * and the FifoReader.
 * 
 * These classes implement a Fifo queue as one writer and many readers.
 * 
 * @author tredmond
 *
 * @param <X>
 */

public class FifoWriter<X> {
  private Logger log;
  private String name;
  /**
   * The Fifo Writer holds the very tail of a linked list
   * which contains a null element and the next item on this
   * queue is null.
   */
  private LinkedList<X> queue = new LinkedList<X>();
  
  public synchronized void write(X x) {
    queue.setElement(x);
    queue.setNext(new LinkedList<X>());
    queue = queue.next();
    if (log != null && log.isLoggable(Level.FINE)) {
      log.fine("Adding " + name + " update " + x);
    }
  }
  
  protected LinkedList<X> getQueue() {
    return queue;
  }
  
  public int getCounter() {
    return queue.getCounter();
  }
  
  public void setLogger(Logger log, String name) {
    this.log = log;
    this.name = name;
  }
}
