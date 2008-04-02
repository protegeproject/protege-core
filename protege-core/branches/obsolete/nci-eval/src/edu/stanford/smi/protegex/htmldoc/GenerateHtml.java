package edu.stanford.smi.protegex.htmldoc;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Generate an HTML version of the project and write it into the file system.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class GenerateHtml extends ProjectAction {

    public GenerateHtml() {
        super(ResourceKey.PROJECT_GENERATE_HTML);
    }

    public void actionPerformed(ActionEvent event) {
        Project project = getProjectManager().getCurrentProject();
        if (project != null) {
            KnowledgeBase kb = project.getKnowledgeBase();
            GenerateHtmlOptionsPane panel = new GenerateHtmlOptionsPane(kb);
            JComponent parent = getProjectManager().getMainPanel();
            String title = LocalizedText.getText(ResourceKey.GENERATE_HTML_OPTIONS_DIALOG_TITLE);
            int choice = ModalDialog.showDialog(parent, panel, title, ModalDialog.MODE_OK_CANCEL);
            if (choice == ModalDialog.OPTION_OK) {
                Collection clses = panel.getRootClses();
                String output = panel.getOutputPath();
                boolean includeInstances = panel.getIncludeInstances();
                generateHtml(kb, clses, output, includeInstances);
            }
        }
    }

    private void generateHtml(KnowledgeBase kb, Collection clses, String outputDir,
            boolean includeInstances) {
        WaitCursor cursor = new WaitCursor(getProjectManager().getMainPanel());
        try {
            File file = new File(outputDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            ProtegeGenClassHierarchy.generateDocs(kb, clses, true, "index.html", outputDir,
                    includeInstances);
        } finally {
            cursor.hide();
        }
    }
}