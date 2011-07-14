package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Allow the user to change the included projects.  This forces a save/reload cycle.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ChangeIncludedProjects extends LocalProjectAction {

    private static final long serialVersionUID = -838478269348967777L;

    public ChangeIncludedProjects() {
        super(ResourceKey.PROJECT_CHANGE_INCLUDED);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent parent = getProjectManager().getMainPanel();
        Project project = getProjectManager().getCurrentProject();
        if (project != null) {
            ChangeIncludedProjectsPanel panel = new ChangeIncludedProjectsPanel(project);
            String title = LocalizedText.getText(ResourceKey.DIRECTLY_INCLUDED_PROJECTS_DIALOG_TITLE);
            int rval = ModalDialog.showDialog(parent, panel, title, ModalDialog.MODE_OK_CANCEL);
            if (rval == ModalDialog.OPTION_OK) {
                getProjectManager().changeIncludedProjectURIsRequest(panel.getIncludedProjectURIs());
            }
        }
    }
}
