package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.util.*;

/**
 * Basic interface for all widgets.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Widget extends Selectable, Plugin {

    boolean configure();

    WidgetDescriptor getDescriptor();

    KnowledgeBase getKnowledgeBase();

    String getLabel();

    Project getProject();

    void initialize();

    void setLabel(String label);
}
