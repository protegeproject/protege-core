package edu.stanford.smi.protege.action;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * Display a dialog that shows all of the icons used by the application along with a description of what they mean.
 * 
 * @author Jennifer Vendetti 
 */

public class ShowIconDialog extends ProjectAction {

    private static final long serialVersionUID = 5053098556181884123L;

    public ShowIconDialog() {
        super(ResourceKey.HELP_MENU_ICONS);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent ae) {
        JComponent parent = getProjectManager().getMainPanel();
        if (parent != null) {
            Window window = SwingUtilities.windowForComponent(parent);
            if (window instanceof Frame) {
                IconDialog dialog = new IconDialog((Frame) window, Text.getProgramName() + ": Icons", false);
                dialog.setSize(500, 300);
                dialog.setLocationRelativeTo(parent);
                dialog.setVisible(true);
            }
        }
    }
}
