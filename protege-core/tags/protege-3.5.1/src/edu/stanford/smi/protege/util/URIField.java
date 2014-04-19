package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * A text field that accepts a URI.  It allows the user to browse for a file name with the standard file browser.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class URIField extends JComponent {
    private static final long serialVersionUID = -4728998781503944838L;
    private JTextField _textField;
    private String _description;
    private String _extension;
    private ChangeListener _changeListener;

    public URIField(String label, URI uri, String extension, String description) {
        _description = description;
        _extension = extension;
        setLayout(new BorderLayout());
        LabeledComponent c = new LabeledComponent(label, createComponent(uri));
        c.addHeaderButton(new AbstractAction("Browse for File", Icons.getAddIcon()) {
            private static final long serialVersionUID = 1162670958348183368L;

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
        int openDialogResult = chooser.showOpenDialog(this);
        switch (openDialogResult) {
            case JFileChooser.ERROR_OPTION:
                // Get this on 'close"
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                _textField.setText(chooser.getSelectedFile().getPath());
                break;
            default:
                Assert.fail("bad result: " + openDialogResult);
                break;
        }
    }

    private JComponent createComponent(URI uri) {
        _textField = ComponentFactory.createTextField();
        _textField.setColumns(40);
        setURI(uri);
        _textField.getDocument().addDocumentListener(new DocumentChangedListener() {
            public void stateChanged(ChangeEvent event) {
                notifyListener();
            }
        });
        return _textField;
    }

    public URI getAbsoluteURI() {
        String text = _textField.getText();
        return URIUtilities.createURI(text);
    }

    public URI getRelativeURI() {
        URI uri = null;
        String text = _textField.getText().trim();
        if (text.length() > 0) {
            try {
                uri = new URI(text);
            } catch (Exception e) {
                // We assume that is is an absolute file path and strip off the name.
                File file = new File(text);
                try {
                    uri = new URI(file.getName());
                } catch (Exception ex) {
                    // do nothing
                }
            }
        }
        return uri;
    }

    private void notifyListener() {
        if (_changeListener != null) {
            _changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    public void setURI(URI uri) {
        String text = (uri == null) ? null : uri.toString();
        _textField.setText(text);
    }
}
