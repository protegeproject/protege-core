package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ManageIncludedProjectsAction extends LocalProjectAction {
    private static final long serialVersionUID = 7666063531709645920L;

    public ManageIncludedProjectsAction() {
        super(ResourceKey.PROJECT_MANAGE_INCLUDED);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent parent = getMainPanel();
        Project project = getProjectManager().getCurrentProject();
        if (project != null) {
            if (project.getProjectURI() == null) {
                ModalDialog.showMessageDialog(parent, "Cannot include projects until project has been saved");
            } else {
                String title = LocalizedText.getText(ResourceKey.PROJECT_MANAGE_INCLUDED_DIALOG);
                ModalDialog.showDialog(parent, new ManageIncludedProjectsPanel(project), title,
                        ModalDialog.MODE_OK_CANCEL);
            }
        }
    }
}
