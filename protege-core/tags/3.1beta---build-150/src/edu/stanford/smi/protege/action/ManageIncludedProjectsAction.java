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
    public ManageIncludedProjectsAction() {
        super(ResourceKey.PROJECT_MANAGE_INCLUDED);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent parent = getMainPanel();
        Project project = getProjectManager().getCurrentProject();
        if (project != null) {
            String title = LocalizedText.getText(ResourceKey.PROJECT_MANAGE_INCLUDED);
            ModalDialog.showDialog(parent, new ManageIncludedProjectsPanel(project), title,
                    ModalDialog.MODE_OK_CANCEL);
        }
    }
}