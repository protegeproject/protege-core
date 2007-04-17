package edu.stanford.smi.protege.event;

import java.util.*;

/**
 * Listener interface for widgets.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface WidgetListener extends EventListener {

    void labelChanged(WidgetEvent event);

    void layoutChanged(WidgetEvent event);
}
