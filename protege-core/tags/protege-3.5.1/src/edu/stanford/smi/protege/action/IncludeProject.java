package edu.stanford.smi.protege.action;

import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Action to prompt the user for a new project to be included and then include it.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class IncludeProject extends LocalProjectAction {

    private static final long serialVersionUID = -6587569406820125552L;

    public IncludeProject() {
        super(ResourceKey.PROJECT_INCLUDE);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent parent = getProjectManager().getMainPanel();
        Project project = getProjectManager().getCurrentProject();
        if (project != null) {
            JFileChooser chooser = ComponentFactory.createFileChooser("Included Project", "pprj");
            int rval = chooser.showOpenDialog(parent);
            switch (rval) {
                case JFileChooser.ERROR_OPTION:
                    // Get this on 'close"
                    break;
                case JFileChooser.CANCEL_OPTION:
                    break;
                case JFileChooser.APPROVE_OPTION:
                    Collection includedProjectURIs = new ArrayList(project.getDirectIncludedProjectURIs());
                    URI newProject = chooser.getSelectedFile().toURI();
                    String currentProjectFileName = project.getName() + ".pprj";
                    if (newProject.equals(currentProjectFileName)) {
                        String text = LocalizedText
                                .getText(ResourceKey.INCLUDE_PROJECT_FAILED_DIALOG_RECURSIVE_INCLUDE_TEXT);
                        ModalDialog.showMessageDialog(parent, text);
                    } else if (includedProjectURIs.contains(newProject)) {
                        String text = LocalizedText
                                .getText(ResourceKey.INCLUDE_PROJECT_FILED_DIALOG_ALREADY_INCLUDED_TEXT);
                        ModalDialog.showMessageDialog(parent, text);
                    } else {
                        includedProjectURIs.add(newProject);
                        getProjectManager().changeIncludedProjectURIsRequest(includedProjectURIs);
                    }
                    break;
                default:
                    Log.getLogger().warning("Bad value: " + rval);
                    break;
            }
        }
    }
}
