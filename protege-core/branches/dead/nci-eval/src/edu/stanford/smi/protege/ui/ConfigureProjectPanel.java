package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * The global configuration panel.  This panel consists of a set of tabs that each handle a specific part of the 
 * configuration.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConfigureProjectPanel extends ValidatableTabComponent {
    public ConfigureProjectPanel(Project project) {
        addTab("Tab Widgets", new ConfigureTabsPanel(project));
        // addTab("Slot Widgets", new ConfigureWidgetsPanel(project));
        addTab("Options", new ConfigureOptionsPanel(project));
    }
}
