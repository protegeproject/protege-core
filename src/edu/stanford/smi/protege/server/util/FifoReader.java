package edu.stanford.smi.protege.server.util;

public class FifoReader<X> {
  private LinkedList<X> queue;
  private FifoWriter<X> writer;

  public FifoReader(FifoWriter<X> writer) {
    this.writer = writer;
    queue = writer.getQueue();
  }
  
  public FifoReader(FifoReader<X> fr) {
    queue = fr.queue;
    writer = fr.writer;
  }
  
  public X read() {
    synchronized (writer) {
      if (queue.next() == null) {
        return null;
      }
      X x = queue.element();
      queue = queue.next();
      return x;
    }
  }
  
  /*
   * A weird thing to be doing for sure.
   */
  public void prepend(X x) {
      LinkedList<X> prequeue = new LinkedList<X>();
      prequeue.setElement(x);
      prequeue.setNext(queue);
      queue = prequeue;
  }
  
  public int getCounter() {
    return queue.getCounter();
  }
}
