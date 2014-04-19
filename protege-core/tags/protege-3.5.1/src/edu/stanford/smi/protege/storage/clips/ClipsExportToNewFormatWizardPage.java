package edu.stanford.smi.protege.storage.clips;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsExportToNewFormatWizardPage extends WizardPage {
    private static final long serialVersionUID = -5274698158929238115L;
    private FileField projectFileField;
    private JTextField clsesFileField;
    private JTextField instancesFileField;
    private ClipsFilesExportProjectPlugin plugin;

    public ClipsExportToNewFormatWizardPage(Wizard wizard, Project project, ClipsFilesExportProjectPlugin plugin) {
        super("clips export", wizard);
        this.plugin = plugin;
        createComponents(null); // project.getProjectFile());
        layoutComponents();
        updateSetPageComplete();
    }

    private void createComponents(File projectPath) {
        projectFileField = new FileField("Project (.pprj) File", null, ".pprj", "Project File");
        clsesFileField = ComponentFactory.createTextField();
        clsesFileField.setEnabled(false);
        instancesFileField = ComponentFactory.createTextField();
        instancesFileField.setEnabled(false);
        
        projectFileField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                onProjectFieldChanged();
                updateSetPageComplete();
            }
        });
    }
    
    private void onProjectFieldChanged() {
        String name = getProjectFileName();
        replaceName(instancesFileField, ".pins", name);
        replaceName(clsesFileField, ".pont", name);
    }
    
    private static void replaceName(JTextField field, String extension, String baseName) {
        String name = FileUtilities.replaceExtension(baseName, extension);
        name = new File(name).getName();
        field.setText(name);
    }
    
    private void updateSetPageComplete() {
        setPageComplete(getProjectFileName() != null);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        Box panel = Box.createVerticalBox();
        panel.add(projectFileField);
        panel.add(new LabeledComponent("Classes (.pont) File", clsesFileField));
        panel.add(new LabeledComponent("Instances (.pins) File", instancesFileField));
        add(panel, BorderLayout.NORTH);
    }
    
    public void onFinish() {
        String projectName = getProjectFileName();
        String clsesFileName = clsesFileField.getText();
        String instancesFileName = instancesFileField.getText();
        plugin.setNewProjectPath(projectName);
        plugin.setFiles(clsesFileName, instancesFileName);
    }

    private String getProjectFileName() {
        return getPath(projectFileField, ".pprj");
    }

    private static String getPath(FileField field, String extension) {
        String path = field.getPath();
        return FileUtilities.ensureExtension(path, extension);
    }
}
