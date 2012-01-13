package edu.stanford.smi.protege.storage.clips;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsFilePanel extends JComponent {
    private static final long serialVersionUID = 45294829620851847L;
    private FileField clsesFileField;
    private FileField instancesFileField;

    public ClipsFilePanel() {
        createComponents();
        layoutComponents();
    }

    private void createComponents() {
        clsesFileField = new FileField("Classes File", null, ".pont", "Classes File");
        instancesFileField = new FileField("Instances File", null, ".pins", "Instances File");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        Box panel = Box.createVerticalBox();
        panel.add(clsesFileField);
        panel.add(instancesFileField);
        add(panel, BorderLayout.NORTH);
    }

    public String getClsesFileName() {
        return getPath(clsesFileField, ".pont");
    }

    public String getInstancesFileName() {
        return getPath(instancesFileField, ".pins");
    }

    private static String getPath(FileField field, String extension) {
        String path = field.getPath();
        return FileUtilities.ensureExtension(path, extension);
    }
}
