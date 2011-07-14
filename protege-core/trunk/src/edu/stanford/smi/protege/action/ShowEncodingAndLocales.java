package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Display the "Encodings and Locales" dialog for the application.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ShowEncodingAndLocales extends ProjectAction {

    private static final long serialVersionUID = -5201414854613908962L;

    public ShowEncodingAndLocales() {
        super(ResourceKey.PROJECT_FILE_ENCODINGS);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent pane = getProjectManager().getMainPanel();
        String title = LocalizedText.getText(ResourceKey.ENCODINGS_DIALOG_TITLE);
        EncodingsPanel panel = new EncodingsPanel();
        int rVal = ModalDialog.showDialog(pane, panel, title, ModalDialog.MODE_OK_CANCEL);
        if (rVal == ModalDialog.OPTION_OK) {
            panel.commitChanges();
        }
    }
}
