package edu.stanford.smi.protege.storage.xml;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel which contains the fields needed for the XML backend.  
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FileSourcesPanel extends KnowledgeBaseSourcesEditor {
    private static final long serialVersionUID = 8651897057537306329L;
    private FileField _field;

    public FileSourcesPanel(String projectURIString, PropertyList sources) {
        super(projectURIString, sources);
        Box box = Box.createVerticalBox();
        box.add(createField());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(box, BorderLayout.NORTH);
        add(panel);
    }

    public void saveContents() {
        String fileName = getBaseFile(_field);
        XMLKnowledgeBaseFactory.setSourceFile(getSources(), fileName);
    }

    public boolean validateContents() {
        return true;
    }

    protected JComponent createField() {
        String file = XMLKnowledgeBaseFactory.getSourceFile(getSources());
        _field = new FileField("XML file name", file, ".xml", null);
        return _field;
    }

    public void onProjectPathChange(String oldPath, String newPath) {
        super.onProjectPathChange(oldPath, newPath);
        if (newPath != null) {
            updatePath(_field, newPath, ".xml");
        }
    }

}
