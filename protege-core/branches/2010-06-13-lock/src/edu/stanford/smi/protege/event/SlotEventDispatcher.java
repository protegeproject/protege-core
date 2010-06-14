package edu.stanford.smi.protege.event;

import java.util.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Dispatcher to send slot events to their listeners.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3) {
        SlotEvent event = new SlotEvent((Slot) source, type, (Frame) arg1);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            SlotListener listener = (SlotListener) i.next();
            switch (type) {
                case SlotEvent.TEMPLATE_SLOT_CLS_ADDED :
                    listener.templateSlotClsAdded(event);
                    break;
                case SlotEvent.TEMPLATE_SLOT_CLS_REMOVED :
                    listener.templateSlotClsRemoved(event);
                    break;
                default :
                    Assert.fail("bad type: " + type);
                    break;
            }
        }
    }
}
