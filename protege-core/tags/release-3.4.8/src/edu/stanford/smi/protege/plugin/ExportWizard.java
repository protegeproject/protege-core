package edu.stanford.smi.protege.plugin;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ExportWizard extends Wizard {
    private static final long serialVersionUID = -8695949089034649639L;
    private Project originalProject;
    private Project newProject;
    private ExportProjectPlugin plugin;
    private boolean exportToNewFormat = false;

    public ExportWizard(JComponent owner, Project project) {
        super(owner, "Export Project");
        setSize(400, 400);
        this.originalProject = project;
        addPage(new SelectOutputFormatWizardPage(this, project));
    }

    public Project getNewProject() {
        return newProject;
    }

    public void setExportPlugin(ExportProjectPlugin plugin) {
        this.plugin = plugin;
    }

    public void setExportToNewFormat(boolean exportToNewFormat) {
        this.exportToNewFormat = exportToNewFormat;
    }

    public void onFinish() {
        super.onFinish();
        doExport();
    }

    private void doExport() {
        WaitCursor cursor = new WaitCursor(this);
        try {
            if (exportToNewFormat) {
                newProject = ((BackendExportPlugin) plugin)
                        .exportProjectToNewFormat(originalProject);
            } else {
                plugin.exportProject(originalProject);
            }
        } finally {
            cursor.hide();
        }
    }
}