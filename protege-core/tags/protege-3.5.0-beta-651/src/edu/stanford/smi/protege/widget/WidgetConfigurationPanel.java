package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.util.*;

/**
 * Default configuration panel for slot widgets.  This is basically just a JTabbedPane.  There is one default tab (the 
 * "General" tab.  Individual widgets can and their own tabs.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetConfigurationPanel extends ValidatableTabComponent {

    private static final long serialVersionUID = 6251741594097939583L;

    public WidgetConfigurationPanel(SlotWidget widget) {
        addGeneralTab(widget);
    }

    public void addGeneralTab(SlotWidget widget) {
        addTab("General", new GeneralWidgetConfigurationPanel(widget));
    }
}
