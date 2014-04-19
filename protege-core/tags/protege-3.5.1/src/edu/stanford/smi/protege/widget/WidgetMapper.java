package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * An interface for implementations which can map a cls/slot/facet combination to a slot widget.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface WidgetMapper {

    WidgetDescriptor createWidgetDescriptor(Cls cls, Slot slot, Facet facet);

    String getDefaultWidgetClassName(Cls cls, Slot slot, Facet facet);

    Collection getSuitableWidgetClassNames(Cls cls, Slot slot, Facet facet);

    boolean isSuitableWidget(Cls cls, Slot slot, Facet facet, WidgetDescriptor d);
}
