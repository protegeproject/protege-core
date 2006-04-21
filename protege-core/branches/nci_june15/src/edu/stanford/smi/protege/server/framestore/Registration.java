package edu.stanford.smi.protege.server.framestore;

import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class Registration {
    private FifoReader<ServerEventWrapper> events;

    public Registration(FifoWriter<ServerEventWrapper> events) {
        this.events = new FifoReader<ServerEventWrapper>(events);
    }
 
    public FifoReader<ServerEventWrapper> getEvents() {
        return events;
    }
}
