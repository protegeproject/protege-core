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
public class ShowAboutPluginsBox extends ProjectAction {

    private static final long serialVersionUID = -3804891317944308373L;

    public ShowAboutPluginsBox() {
        super(ResourceKey.HELP_MENU_ABOUT_PLUGINS);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent pane = getProjectManager().getMainPanel();
        String title = LocalizedText.getText(ResourceKey.ABOUT_PLUGINS_DIALOG_TITLE);
        ModalDialog.showDialog(pane, new AboutPluginsBox(), title, ModalDialog.MODE_CLOSE);
    }
}
