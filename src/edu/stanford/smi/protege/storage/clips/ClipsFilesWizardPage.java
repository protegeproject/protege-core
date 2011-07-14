package edu.stanford.smi.protege.storage.clips;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsFilesWizardPage extends WizardPage {
    private static final long serialVersionUID = -3081726266101640734L;
    private FileField clsesFileField;
    private FileField instancesFileField;
    private ClipsFilesPlugin plugin;

    public ClipsFilesWizardPage(Wizard wizard, ClipsFilesPlugin plugin) {
        super("clips files", wizard);
        this.plugin = plugin;
        createComponents();
        layoutComponents();
        updateSetPageComplete();
    }

    private void createComponents() {
        clsesFileField = new FileField("Classes (.pont) File", null, ".pont", "Classes File");
        instancesFileField = new FileField("Instances (.pins) File", null, ".pins", "Instances File");

        clsesFileField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                onClsFieldChanged();
                updateSetPageComplete();
            }
        });
    }

    private void onClsFieldChanged() {
        String name = getClsesFileName();
        String instancesName = FileUtilities.replaceExtension(name, ".pins");
        instancesFileField.setPath(instancesName);
    }

    private void updateSetPageComplete() {
        setPageComplete(getClsesFileName() != null);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        Box panel = Box.createVerticalBox();
        panel.add(clsesFileField);
        panel.add(instancesFileField);
        add(panel, BorderLayout.NORTH);
    }

    public void onFinish() {
        String clsesFileName = getClsesFileName();
        String instancesFileName = getInstancesFileName();
        plugin.setFiles(clsesFileName, instancesFileName);
    }

    private String getClsesFileName() {
        return getPath(clsesFileField, ".pont");
    }

    private String getInstancesFileName() {
        return getPath(instancesFileField, ".pins");
    }

    private static String getPath(FileField field, String extension) {
        String path = field.getPath();
        return FileUtilities.ensureExtension(path, extension);
    }

    public WizardPage getNextPage() {
        WizardPage page;
        if (plugin instanceof ClipsFilesCreateProjectPlugin) {
            ClipsFilesCreateProjectPlugin createPlugin = (ClipsFilesCreateProjectPlugin) plugin;
            page = new IncludedProjectsWizardPage(getWizard(), createPlugin);
        } else {
            page = super.getNextPage();
        }
        return page;
    }
}
