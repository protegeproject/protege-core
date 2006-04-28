package edu.stanford.smi.protege.server.framestore;


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
}
