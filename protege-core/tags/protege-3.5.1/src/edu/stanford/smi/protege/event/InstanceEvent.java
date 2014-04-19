package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Event generated when an instance changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceEvent extends AbstractEvent {
    private static final long serialVersionUID = -6242074730702808309L;

    private static final int BASE = 200;

    public static final int DIRECT_TYPE_ADDED = BASE + 2;
    public static final int DIRECT_TYPE_REMOVED = BASE + 3;

    // public final static int DIRECT_TYPE_CHANGED = DIRECT_TYPE_ADDED;

    public InstanceEvent(Instance instance, int type, Object cls) {
        super(instance, type, cls);
    }
    
    public Instance getInstance() {
        return (Instance) getSource();
    }

    public Cls getCls() {
        return (Cls) getArgument();
    }
    
    public boolean isDeletingInstanceEvent() {
        return getInstance().isBeingDeleted();
    }
}
