package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * Event generated when the project changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectEvent extends AbstractEvent {
    public static final int FORM_CHANGED = 1;
    public static final int PROJECT_SAVED = 2;
    public static final int PROJECT_CLOSED = 3;
    public static final int RUNTIME_CLS_WIDGET_CREATED = 4;

    public ProjectEvent(Project project, int type, ClsWidget widget) {
        super(project, type, widget);
    }

    public ProjectEvent(Project project, int type) {
        super(project, type);
    }

    public Cls getCls() {
        return getClsWidget().getCls();
    }

    public ClsWidget getClsWidget() {
        return (ClsWidget) getArgument();
    }
}
