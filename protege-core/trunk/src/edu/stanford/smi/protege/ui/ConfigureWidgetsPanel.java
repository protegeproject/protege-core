package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Configure the widgets in the application.  This panel does nothing at the moment.  The idea is that widgets could 
 * be globally configured here.  You could, for example, configure the buttons on an InstanceListWidget and then all
 * such widgets by default would get this set of buttons.
 * 
 * @author  Ray Fergerson <fergerson@smi.stanford.edu>
 */
class ConfigureWidgetsPanel extends AbstractValidatableComponent {

    private static final long serialVersionUID = -2959046238583135281L;

    ConfigureWidgetsPanel(Project project) {
        setLayout(new BorderLayout());
        add(ComponentFactory.createLabel("Configure Widgets", SwingConstants.CENTER));
    }

    public void saveContents() {
    }

    public boolean validateContents() {
        return true;
    }
}
