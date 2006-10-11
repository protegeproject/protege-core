package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArchiveProject extends LocalProjectAction {

    public ArchiveProject(boolean large) {
        super(ResourceKey.PROJECT_ARCHIVE, large);
    }

    public void actionPerformed(ActionEvent e) {
        if (getProject().getProjectURI() == null) {
            String text = LocalizedText.getText(ResourceKey.ARCHIVE_FAILED_DIALOG_TEXT);
            ModalDialog.showMessageDialog(getMainPanel(), text);
        } else {
            getProjectManager().archiveProjectRequest();
        }
    }

}
