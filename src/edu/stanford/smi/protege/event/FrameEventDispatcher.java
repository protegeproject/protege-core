package edu.stanford.smi.protege.event;

import java.util.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Dispatcher to send frame events to their listeners.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameEventDispatcher implements EventDispatcher {

    public void postEvent(Collection c, Object source, int type, Object arg1, Object arg2, Object arg3) {
        FrameEvent event = new FrameEvent((Frame) source, type, arg1, arg2);
        Iterator i = c.iterator();
        while (i.hasNext()) {
            FrameListener listener = (FrameListener) i.next();
            switch (type) {
                case FrameEvent.NAME_CHANGED :
                    listener.nameChanged(event);
                    break;
                case FrameEvent.OWN_SLOT_ADDED :
                    listener.ownSlotAdded(event);
                    break;
                case FrameEvent.OWN_SLOT_REMOVED :
                    listener.ownSlotRemoved(event);
                    break;
                case FrameEvent.BROWSER_TEXT_CHANGED :
                    listener.browserTextChanged(event);
                    break;
                case FrameEvent.DELETED :
                    listener.deleted(event);
                    break;
                case FrameEvent.OWN_FACET_ADDED :
                    listener.ownFacetAdded(event);
                    break;
                case FrameEvent.OWN_FACET_REMOVED :
                    listener.ownFacetRemoved(event);
                    break;
                case FrameEvent.OWN_SLOT_VALUE_CHANGED :
                    listener.ownSlotValueChanged(event);
                    break;
                case FrameEvent.OWN_FACET_VALUE_CHANGED :
                    listener.ownFacetValueChanged(event);
                    break;
                case FrameEvent.VISIBILITY_CHANGED :
                    listener.visibilityChanged(event);
                    break;
                default :
                    Assert.fail("bad type: " + type);
            }
        }
    }
}
