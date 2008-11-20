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
    private List<ValueUpdate> updates = new ArrayList<ValueUpdate>();
    private List<ValueUpdate> commits = new ArrayList<ValueUpdate>();
    private BandWidthPolicy bandwidthPolicy = new BandWidthPolicy();
    private long lastHeartbeat = 0;


    public Registration(FifoWriter<AbstractEvent> events) {
        this.events = new FifoReader<AbstractEvent>(events);
    }
 
    public FifoReader<AbstractEvent> getEvents() {
        return events;
    }

    public List<ValueUpdate> getUpdates() {
      return updates;
    }
    
    public void addUpdate(ValueUpdate vu) {
    	updates.add(vu);
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

    public void endTransaction() {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Ending transaction: clearing transaction local events and updates");
      }
      commits = new ArrayList<ValueUpdate>();
    }
    
    public BandWidthPolicy getBandWidthPolicy() {
        return bandwidthPolicy;
    }
    
    public long getLastHeartbeat() {
      return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
      this.lastHeartbeat = lastHeartbeat;
    }


}
