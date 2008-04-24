package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Display the "About Box" for the application.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ShowAboutBox extends ProjectAction {

    public ShowAboutBox() {
        super(ResourceKey.HELP_MENU_ABOUT);
        substituteIntoName(Text.getProgramName());
        setEnabled(true);
    }
    
    public void actionPerformed(ActionEvent event) {
        JComponent pane = getProjectManager().getMainPanel();
        String title = LocalizedText.getText(ResourceKey.ABOUT_APPLICATION_DIALOG_TITLE, Text.getProgramName());
        ModalDialog.showDialog(pane, new AboutBox(), title, ModalDialog.MODE_CLOSE);
    }
}
