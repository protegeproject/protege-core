package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.widget.*;
import edu.stanford.smi.protege.util.*;

/**
 * Event generated when a widget changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetEvent extends AbstractEvent {
    public final static int LABEL_CHANGED = 1;
    public final static int LAYOUT_CHANGED = 2;

    public WidgetEvent(Widget source, int type) {
        super(source, type);
    }

    public Widget getWidget() {
        return (Widget) getSource();
    }
}
