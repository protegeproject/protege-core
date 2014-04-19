package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConfigureArchive extends LocalProjectAction {

    private static final long serialVersionUID = -9040274094107500244L;
    public ConfigureArchive() {
        super(ResourceKey.PROJECT_CONFIGURE_ARCHIVE);
    }
    public void actionPerformed(ActionEvent e) {
        Project p = getProject();
        if (p != null) {
            ConfigureArchivePanel panel = new ConfigureArchivePanel(p);
            String title = LocalizedText.getText(ResourceKey.CONFIGURE_ARCHIVE_DIALOG_TITLE);
            ModalDialog.showDialog(getMainPanel(), panel, title, ModalDialog.MODE_OK_CANCEL);
        }
    }

}
