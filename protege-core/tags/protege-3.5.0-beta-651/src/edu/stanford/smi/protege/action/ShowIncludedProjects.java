package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Action to show all of the included projects for the application (including the nested includes)
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ShowIncludedProjects extends ProjectAction {

    private static final long serialVersionUID = -7442984759538902965L;

    public ShowIncludedProjects() {
        super(ResourceKey.PROJECT_SHOW_INCLUDED);
    }

    public void actionPerformed(ActionEvent e) {
        JComponent parent = getMainPanel();
        Project project = getProjectManager().getCurrentProject();
        if (project != null) {
            String title = LocalizedText.getText(ResourceKey.INCLUDED_PROJECTS_DIALOG_TITLE);
            ModalDialog.showDialog(
                parent,
                new IncludedProjectsPanel(project),
                title,
                ModalDialog.MODE_OK_CANCEL);
        }
    }
}
