package edu.stanford.smi.protege.storage.clips;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel which contains the fields needed for the Clips backend.  This is just the .pont (classes) 
 * and .pins (instances) file names.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FileSourcesPanel extends KnowledgeBaseSourcesEditor {
    private static final long serialVersionUID = -6725659436464810319L;
    private FileField _clsesField;
    private FileField _instancesField;

    public FileSourcesPanel(String projectURIString, PropertyList sources) {
        super(projectURIString, sources);
        Box box = Box.createVerticalBox();
        box.add(createClsesField());
        box.add(createInstancesField());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(box, BorderLayout.NORTH);
        add(panel);
    }

    //ESCA-JAVA0130 
    public URI getProjectURI() {
        return null;
        // return getProjectURI(_clsesField);
    }

    public void saveContents() {
        String clsesFileName = getBaseFile(_clsesField);
        String instancesFileName = getBaseFile(_instancesField);
        ClipsKnowledgeBaseFactory.setSourceFiles(getSources(), clsesFileName, instancesFileName);
    }

    public boolean validateContents() {
        return true;
    }
    protected JComponent createClsesField() {
        String file = ClipsKnowledgeBaseFactory.getClsesSourceFile(getSources());
        _clsesField = new FileField("Classes file name", file, ".pont", "Ontology");
        return _clsesField;
    }

    protected JComponent createInstancesField() {
        String file = ClipsKnowledgeBaseFactory.getInstancesSourceFile(getSources());
        _instancesField = new FileField("Instances file name", file, ".pins", "Instances");
        return _instancesField;
    }

    public void onProjectPathChange(String oldPath, String newPath) {
        super.onProjectPathChange(oldPath, newPath);
        if (newPath != null) {
            updatePath(_clsesField, newPath, ".pont");
            updatePath(_instancesField, newPath, ".pins");
        }
    }

}
