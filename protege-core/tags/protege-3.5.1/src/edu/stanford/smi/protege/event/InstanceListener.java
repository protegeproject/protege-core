package edu.stanford.smi.protege.event;

import java.util.*;

/**
 * Listener interface for instance events.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface InstanceListener extends EventListener {

    void directTypeAdded(InstanceEvent event);
    void directTypeRemoved(InstanceEvent event);
}
