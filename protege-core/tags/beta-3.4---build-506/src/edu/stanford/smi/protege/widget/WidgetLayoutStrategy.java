package edu.stanford.smi.protege.widget;

import java.awt.*;

/**
 * Interface for the strategy pattern for laying out the slot widgets on a cls widget.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface WidgetLayoutStrategy {

    public void layout(Container c, int index);
}
