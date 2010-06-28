package edu.stanford.smi.protege.event;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Dispatcher to send instance events to their listeners.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3) {
        InstanceEvent event = new InstanceEvent((Instance) source, type, arg1);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            InstanceListener listener = (InstanceListener) i.next();
            switch (type) {
                case InstanceEvent.DIRECT_TYPE_ADDED :
                    listener.directTypeAdded(event);
                    break;
                case InstanceEvent.DIRECT_TYPE_REMOVED :
                    listener.directTypeRemoved(event);
                    break;
                default :
                    Assert.fail("bad type: " + type);
                    break;
            }
        }
    }
}
