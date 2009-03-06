package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.util.*;

/**
 * Default configuration panel for slot widgets.  This is basically just a JTabbedPane.  There is one default tab (the 
 * "General" tab.  Individual widgets can and their own tabs.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetConfigurationPanel extends ValidatableTabComponent {

    public WidgetConfigurationPanel(SlotWidget widget) {
        addGeneralTab(widget);
    }

    public void addGeneralTab(SlotWidget widget) {
        addTab("General", new GeneralWidgetConfigurationPanel(widget));
    }
}
