package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * A text field that accepts a file name.  It allows the user to browse for a file name with the standard file browser.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @author    Stanley Knutson 4-sep-03
 */
public class FileField extends JComponent {
    private JTextField _textField;
    private String _description;
    private String _extension;
    private ChangeListener _changeListener;
    private int _fileMode;

    /** Constructor for selecting a file */
    public FileField(String label, String path, String extension, String description) {
        this(label, path, extension, description, JFileChooser.FILES_ONLY);
    }
    /** Constructor for selecting a directory */
    public FileField(String label, String path, String description) {
        this(label, path, "", description, JFileChooser.DIRECTORIES_ONLY);
    }

    private FileField(String label, String path, String extension, String description, int mode) {
        _fileMode = mode;
        _description = description;
        _extension = extension;
        setLayout(new BorderLayout());
        LabeledComponent c = new LabeledComponent(label, createComponent(path));
        c.addHeaderButton(new AbstractAction("Browse for File", Icons.getAddIcon()) {
            public void actionPerformed(ActionEvent event) {
                browse();
            }
        });
        add(c);
    }

    public void addChangeListener(ChangeListener listener) {
        Assert.assertNull("existing change listener", _changeListener);
        _changeListener = listener;
    }

    protected void browse() {
        JFileChooser chooser = ComponentFactory.createFileChooser(_description, _extension);
        chooser.setApproveButtonText("Select");
        chooser.setFileSelectionMode(_fileMode);
        int openDialogResult = chooser.showOpenDialog(this);
        switch (openDialogResult) {
            case JFileChooser.ERROR_OPTION :
                // Get this on 'close"
                break;
            case JFileChooser.CANCEL_OPTION :
                break;
            case JFileChooser.APPROVE_OPTION :
                _textField.setText(chooser.getSelectedFile().getPath());
                break;
            default :
                Assert.fail("bad result: " + openDialogResult);
        }
    }

    private JComponent createComponent(String text) {
        _textField = ComponentFactory.createTextField();
        _textField.setColumns(40);
        if ((text == null || text.length() == 0) && _fileMode == JFileChooser.DIRECTORIES_ONLY) {
            text = SystemUtilities.getUserDirectory();
        }
        _textField.setText(text);
        _textField.getDocument().addDocumentListener(new DocumentChangedListener() {
            public void stateChanged(ChangeEvent event) {
                notifyListener();
            }
        });
        return _textField;
    }

    public String getPath() {
        String text = _textField.getText();
        if (text.length() == 0) {
            text = null;
        } else {
            try {
                text = new File(text).getAbsolutePath();
            } catch (SecurityException e) {
                // happens in applets
            }
        }
        return text;
    }
    
    public File getFilePath() {
        String path = getPath();
        return (path == null) ? null : new File(path);
    }

    private void notifyListener() {
        if (_changeListener != null) {
            _changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    public void setPath(String text) {
        _textField.setText(text);
    }
}