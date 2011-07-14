package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.util.*;

/**
 * Configuration panel for a form widget.  This is just a holder for the tabs where the actually configurations are 
 * done.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormWidgetConfigurationPanel extends ValidatableTabComponent {

    private static final long serialVersionUID = 7694908346926041635L;

    public FormWidgetConfigurationPanel(FormWidget widget) {
        addTab("Widgets", new FormWidgetConfigurationWidgetsTab(widget));
        addTab("Layout", new FormWidgetConfigurationLayoutTab(widget));
    }
}
