package edu.stanford.smi.protege.action;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Action to bring up a file chooser dialog and pick a file.
 * When the file is chosen and OK is pressed #onFileChosen is called.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class BrowseForFile extends AbstractAction {
    private static final long serialVersionUID = -6493484151672859764L;
    private String _extension;
    private String _description;
    private JComponent _parent;

    protected BrowseForFile(JComponent parent, String description) {
        this(parent, description, null);
    }

    protected BrowseForFile(JComponent parent, String description, String extension) {
        super("Browse for " + description, Icons.getFindIcon());
        _extension = extension;
        _description = description;
        _parent = parent;
    }

    public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = ComponentFactory.createFileChooser(_description, _extension);
        int rval = chooser.showOpenDialog(_parent);
        if (rval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            onFileChosen(file);
        }
    }

    public abstract void onFileChosen(File file);
}
