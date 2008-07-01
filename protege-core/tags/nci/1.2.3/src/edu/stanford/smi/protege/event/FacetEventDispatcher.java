package edu.stanford.smi.protege.event;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Dispatcher to send Facet events to FacetListeners
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FacetEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3) {
        FacetEvent event = new FacetEvent((Facet) source, type, (Frame) arg1, (Slot) arg2);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            FacetListener listener = (FacetListener) i.next();
            switch (type) {
                case FacetEvent.FRAME_SLOT_REFERENCE_ADDED:
                    listener.frameSlotReferenceAdded(event);
                    break;
                case FacetEvent.FRAME_SLOT_REFERENCE_REMOVED:
                    listener.frameSlotReferenceRemoved(event);
                    break;
                default:
                    Assert.fail("bad type: " + type);
                    break;
            }
        }
    }
}
