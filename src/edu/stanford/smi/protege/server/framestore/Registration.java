package edu.stanford.smi.protege.server.framestore;


import java.util.ArrayList;
import java.util.List;

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
    private FifoReader<AbstractEvent> events;
    private FifoReader<ValueUpdate> updates;
    private List<AbstractEvent> transactionEvents = new ArrayList<AbstractEvent>();
    private List<ValueUpdate> rollbacks = new ArrayList<ValueUpdate>();
    private List<ValueUpdate> commits = new ArrayList<ValueUpdate>();

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
      commits.add(vu);
    }
    
    public List<ValueUpdate>  getCommits() {
      return commits;
    }

    public void clearCommits() {
      getCommits();
    }

    public void addTransactionEvent(AbstractEvent event) {
      transactionEvents.add(event);
    }

    public List<AbstractEvent> getTransactionEvents() {
      return transactionEvents;
    }

    public void endTransaction() {
      commits = new ArrayList<ValueUpdate>();
      transactionEvents = new ArrayList<AbstractEvent>();
    }

}
