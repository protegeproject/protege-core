package edu.stanford.smi.protege.action;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display the included projects but not allow them to be changed.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

class IncludedProjectsPanel extends JComponent {

    public IncludedProjectsPanel(Project project) {
        setLayout(new BorderLayout());
        JList list = ComponentFactory.createList(null);
        ComponentUtilities.setListValues(list, project.getIncludedProjects());
        add(new JScrollPane(list));
        setPreferredSize(new Dimension(300, 300));
    }
}
