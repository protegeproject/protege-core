package edu.stanford.smi.protege.server.framestore;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.AbstractEvent;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class Registration {
    private static final transient Logger cacheLog = ServerFrameStore.cacheLog;
  
    private FifoReader<AbstractEvent> events;
    private FifoReader<ValueUpdate> updates;
    private List<AbstractEvent> transactionEvents = new ArrayList<AbstractEvent>();
    private List<ValueUpdate> commits = new ArrayList<ValueUpdate>();
    private long lastHeartbeat = 0;


    public Registration(FifoWriter<AbstractEvent> events,
                        FifoWriter<ValueUpdate> updates) {
        this.events = new FifoReader<AbstractEvent>(events);
        this.updates = new FifoReader<ValueUpdate>(updates);
    }
 
    public FifoReader<AbstractEvent> getEvents() {
        return events;
    }

    public FifoReader<ValueUpdate> getUpdates() {
      return updates;
    }
    
    public void addCommittableUpdate(ValueUpdate vu) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Saving an update for commit/rollback " + vu);
      }
      commits.add(vu);
    }
    
    public List<ValueUpdate>  getCommits() {
      return commits;
    }

    public void clearCommits() {
      getCommits();
    }

    public void addTransactionEvent(AbstractEvent event) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Saving event " + event + " for commit/rollback");
      }
      transactionEvents.add(event);
    }

    public List<AbstractEvent> getTransactionEvents() {
      return transactionEvents;
    }

    public void endTransaction() {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Ending transaction: clearing transaction local events and updates");
      }
      commits = new ArrayList<ValueUpdate>();
      transactionEvents = new ArrayList<AbstractEvent>();
    }
    
    public long getLastHeartbeat() {
      return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
      this.lastHeartbeat = lastHeartbeat;
    }


}
