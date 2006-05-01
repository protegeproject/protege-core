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
    private int transactionLocation;
    private List<ValueUpdate> rollbacks = new ArrayList<ValueUpdate>();

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
    
    public void addRollbackableUpdate(ValueUpdate vu) {
      rollbacks.add(vu);
    }
    
    public List<ValueUpdate> endTransaction() {
      List<ValueUpdate> ret = rollbacks;
      rollbacks = new ArrayList<ValueUpdate>();
      return ret;
    }

    public int getTransactionLocation() {
      return transactionLocation;
    }

    public void setTransactionLocation(int transactionLocation) {
      this.transactionLocation = transactionLocation;
    }
}
