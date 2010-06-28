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
    private List<ValueUpdate> commitableUpdates = new ArrayList<ValueUpdate>();
    private BandWidthPolicy bandwidthPolicy = new BandWidthPolicy();
    private long lastHeartbeat = 0;


    public Registration(FifoWriter<AbstractEvent> events) {
        this.events = new FifoReader<AbstractEvent>(events);
    }
 
    public FifoReader<AbstractEvent> getEvents() {
        return events;
    }

    public List<ValueUpdate> getAndClearValueUpdates() {
    	List<ValueUpdate> result = updates;
    	updates = new ArrayList<ValueUpdate>();
    	return result;
    }
    
    public void addUpdate(ValueUpdate vu) {
    	updates.add(vu);
    }
    
    public void addCommittableUpdate(ValueUpdate vu) {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Saving an update for commit/rollback " + vu);
      }
      commitableUpdates.add(vu);
    }
    
    public List<ValueUpdate>  getCommits() {
    	return commitableUpdates;
    }


    public void endTransaction() {
      if (cacheLog.isLoggable(Level.FINE)) {
        cacheLog.fine("Ending transaction: clearing transaction local events and updates");
      }
      commitableUpdates = new ArrayList<ValueUpdate>();
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
