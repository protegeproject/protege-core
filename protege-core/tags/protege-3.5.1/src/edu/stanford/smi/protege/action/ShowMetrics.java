package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Show a panel to display interesting metrics about the current Project
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ShowMetrics extends ProjectAction {

    private static final long serialVersionUID = 3040315622772444408L;

    public ShowMetrics() {
        super(ResourceKey.PROJECT_METRICS);
    }

    public void actionPerformed(ActionEvent event) {
        JComponent root = getProjectManager().getMainPanel();
        Project p = getProjectManager().getCurrentProject();
        if (p != null) {
            WaitCursor cursor = new WaitCursor(root);
            JComponent c = new MetricsPanel(p);
            cursor.hide();
            String title = LocalizedText.getText(ResourceKey.METRICS_DIALOG_TITLE);
            ModalDialog.showDialog(root, c, title, ModalDialog.MODE_CLOSE);
        }
    }
}
