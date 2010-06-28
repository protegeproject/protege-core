package edu.stanford.smi.protege.event;

import java.util.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * Dispatcher to send widget events to their listeners.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object a1, Object a2, Object a3) {
        WidgetEvent event = new WidgetEvent((Widget) source, type);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            WidgetListener listener = (WidgetListener) i.next();
            switch (type) {
                case WidgetEvent.LABEL_CHANGED :
                    listener.labelChanged(event);
                    break;
                case WidgetEvent.LAYOUT_CHANGED :
                    listener.layoutChanged(event);
                    break;
                default :
                    Assert.fail("bad type: " + type);
                    break;
            }
        }
    }
}
