package edu.stanford.smi.protege.server.util;

/**
 * This is a trivial support class for the Fifo queue. We are following 
 * the convention that the linked list ends in an element containing
 * null.
 * 
 * @author tredmond
 *
 * @param <X> the type of element being entered into the linked list.
 */
class LinkedList<X> {
  private static int lastCount = 0;
  private int counter = lastCount++;
  private LinkedList<X> next;
  private X entry;
  
  public LinkedList() {
    next = null;
    entry = null;
  }
  
  public LinkedList<X> next() {
    return next;
  }
  
  public X element() {
    return entry;
  }

  protected void setNext(LinkedList<X> next) {
    this.next = next;
  }

  protected void setElement(X x) {
    entry = x;
  }
  
  public int getCounter() {
    return counter;
  }
}
