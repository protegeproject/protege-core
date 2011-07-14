package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for nodes on the "form tree".  This tree is really just the class hierarchy with different icons.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormRenderer extends DefaultRenderer {
    private static final long serialVersionUID = -6554406664266075365L;
    private Project _project;

    public FormRenderer(Project project) {
        _project = project;
    }

    public void load(Object o) {
        if (o instanceof Cls) {
            Cls cls = (Cls) o;
            setMainText(cls.getBrowserText());
            setMainIcon(Icons.getFormIcon(_project.hasCustomizedDescriptor(cls)));
            setBackgroundSelectionColor(Colors.getFormSelectionColor());
        }
    }
}
