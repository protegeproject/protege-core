package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.widget.ClsWidget;

/**
 * Event generated when the project changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectEvent extends AbstractEvent {
    private static final long serialVersionUID = -8732325118619806882L;
    public static final int FORM_CHANGED = 1;
    public static final int PROJECT_SAVED = 2;
    public static final int PROJECT_CLOSED = 3;
    public static final int RUNTIME_CLS_WIDGET_CREATED = 4;
    public static final int SERVER_SESSION_LOST = 5;

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
