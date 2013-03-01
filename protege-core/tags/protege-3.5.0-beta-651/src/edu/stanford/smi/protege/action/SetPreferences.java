package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Bring up the configuration panel for the current project.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SetPreferences extends StandardAction {

    private static final long serialVersionUID = 1269609122020605397L;

    public SetPreferences() {
        super(ResourceKey.APPLICATION_PREFERENCES);
    }

    public void actionPerformed(ActionEvent event) {
        PreferencesPanel panel = new PreferencesPanel();
        JComponent parent = ProjectManager.getProjectManager().getMainPanel();
        String title = LocalizedText.getText(ResourceKey.PREFERENCES_DIALOG_TITLE);
        int rval = ModalDialog.showDialog(parent, panel, title, ModalDialog.MODE_OK_CANCEL);
        if (rval == ModalDialog.OPTION_OK) {
            ProjectManager.getProjectManager().reloadUI(true);
         }
    }
}

