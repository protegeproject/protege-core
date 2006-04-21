package edu.stanford.smi.protege.server.framestore;

import java.util.EventObject;

import edu.stanford.smi.protege.server.RemoteSession;

/**
 * This class is a wrapper for the EventObject.  The point is that when
 * a transaction is rolled back, the server needs to destroy all events 
 * that are initiated by the client doing the rollback - all the way to the 
 * beginning of the transaction.  So when the event objects are stored in the 
 * server event queue, the server needs to keep track of who initiated the
 * event.  This is a simple POJO.
 * 
 * @author tredmond
 *
 */

public class ServerEventWrapper {
  private EventObject event;
  private boolean blowCache = false;
  
  public ServerEventWrapper(EventObject event) {
    this.event = event;
  }

  public EventObject getEvent() {
    return event;
  }
  
  public boolean blowCache() {
    return blowCache;
  }

  public void setBlowCache(boolean blowCache) {
    this.blowCache = blowCache;
  }

}
