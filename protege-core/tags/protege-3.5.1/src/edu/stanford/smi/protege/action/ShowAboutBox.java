package edu.stanford.smi.protege.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.JComponent;

import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.ui.AboutBox;
import edu.stanford.smi.protege.util.ModalDialog;

/**
 * Display the "About Box" for the application.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ShowAboutBox extends ProjectAction {

    private static final long serialVersionUID = 1963320962882327869L;

    public ShowAboutBox() {
        super(ResourceKey.HELP_MENU_ABOUT);
        substituteIntoName(Text.getProgramName());
        setEnabled(true);
    }
    
    public void actionPerformed(ActionEvent event) {
        JComponent pane = getProjectManager().getMainPanel();
        String title = LocalizedText.getText(ResourceKey.ABOUT_APPLICATION_DIALOG_TITLE, Text.getProgramName());
        URL url = Text.getAboutURL();
        
        AboutBox aboutProtege = new AboutBox(url, new Dimension(575, 475));
        ModalDialog.showDialog(pane, aboutProtege, title, ModalDialog.MODE_CLOSE);
    }
}
