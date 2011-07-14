package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * Event generated when a widget changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetEvent extends AbstractEvent {
    private static final long serialVersionUID = -301963986368505049L;
    public static final int LABEL_CHANGED = 1;
    public static final int LAYOUT_CHANGED = 2;

    public WidgetEvent(Widget source, int type) {
        super(source, type);
    }

    public Widget getWidget() {
        return (Widget) getSource();
    }
}
