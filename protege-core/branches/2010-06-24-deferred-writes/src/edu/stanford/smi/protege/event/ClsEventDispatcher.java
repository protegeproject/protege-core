package edu.stanford.smi.protege.event;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Dispatches Cls events to ClsListener instances.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3) {
        ClsEvent event = new ClsEvent((Cls) source, type, arg1, arg2);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ClsListener listener = (ClsListener) i.next();
            switch (type) {
                case ClsEvent.DIRECT_SUPERCLASS_ADDED :
                    listener.directSuperclassAdded(event);
                    break;
                case ClsEvent.DIRECT_SUPERCLASS_REMOVED :
                    listener.directSuperclassRemoved(event);
                    break;
                case ClsEvent.DIRECT_SUBCLASS_ADDED :
                    listener.directSubclassAdded(event);
                    break;
                case ClsEvent.DIRECT_SUBCLASS_REMOVED :
                    listener.directSubclassRemoved(event);
                    break;
                case ClsEvent.DIRECT_SUBCLASS_MOVED :
                    listener.directSubclassMoved(event);
                    break;
                case ClsEvent.DIRECT_INSTANCE_ADDED :
                    listener.directInstanceAdded(event);
                    break;
                case ClsEvent.DIRECT_INSTANCE_REMOVED :
                    listener.directInstanceRemoved(event);
                    break;
                case ClsEvent.TEMPLATE_SLOT_ADDED :
                    listener.templateSlotAdded(event);
                    break;
                case ClsEvent.TEMPLATE_SLOT_REMOVED :
                    listener.templateSlotRemoved(event);
                    break;
                case ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED :
                    listener.templateSlotValueChanged(event);
                    break;
                case ClsEvent.TEMPLATE_FACET_ADDED :
                    listener.templateFacetAdded(event);
                    break;
                case ClsEvent.TEMPLATE_FACET_REMOVED :
                    listener.templateFacetRemoved(event);
                    break;
                case ClsEvent.TEMPLATE_FACET_VALUE_CHANGED :
                    listener.templateFacetValueChanged(event);
                    break;
                default :
                    Assert.fail("bad type: " + type);
                    break;
            }
        }
    }
}
