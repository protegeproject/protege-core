package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Dispatcher for {@link Selectable} selection events.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectionEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3) {
        SelectionEvent event = new SelectionEvent((Selectable) source, type);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            SelectionListener listener = (SelectionListener) i.next();
            switch (type) {
                case SelectionEvent.SELECTION_CHANGED:
                    listener.selectionChanged(event);
                    break;
                default:
                    // do nothing
                    break;
            }
        }
    }
}
