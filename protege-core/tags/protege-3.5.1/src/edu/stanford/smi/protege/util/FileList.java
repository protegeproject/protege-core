package edu.stanford.smi.protege.util;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * A list box that is specialized to work with file names.  It allows the user to browse for names using the standard
 * file browser.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FileList extends AbstractSelectableComponent {
    private static final long serialVersionUID = -2010583722771917590L;
    private JList _fileList;
    private String _description;
    private String _extension;

    public FileList(String label, Collection files, String extension, String description) {
        _description = description;
        _extension = extension;
        setLayout(new BorderLayout());
        LabeledComponent c = new LabeledComponent(label, createComponent(files));
        c.addHeaderButton(newAddAction());
        c.addHeaderButton(newRemoveAction());
        add(c);
        setPreferredSize(new Dimension(300, 200));
    }

    private void browse() {
        JFileChooser chooser = ComponentFactory.createFileChooser(_description, _extension);
        chooser.setMultiSelectionEnabled(true);
        int openDialogResult = chooser.showOpenDialog(this);
        switch (openDialogResult) {
            case JFileChooser.ERROR_OPTION:
                // Get this on 'close"
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File[] files = chooser.getSelectedFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; ++i) {
                        ComponentUtilities.addSelectedListValue(_fileList, files[i].getPath());
                    }
                }
                break;
            default:
                Assert.fail("bad result: " + openDialogResult);
                break;
        }
    }

    public void clearSelection() {
        _fileList.clearSelection();
    }

    private JComponent createComponent(Collection files) {
        _fileList = ComponentFactory.createList(newAddAction());
        _fileList.addListSelectionListener(new ListSelectionListenerAdapter(this));
        if (files != null) {
            ComponentUtilities.setListValues(_fileList, files);
        }
        return ComponentFactory.createScrollPane(_fileList);
    }

    public Collection getPaths() {
        return ComponentUtilities.getListValues(_fileList);
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_fileList);
    }

    private Action newAddAction() {
        return new AddAction(ResourceKey.VALUE_ADD) {
            private static final long serialVersionUID = -4235411306185033490L;

            public void onAdd() {
                browse();
            }
        };
    }

    private Action newRemoveAction() {
        return new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = -7910730547550960702L;

            public void onRemove(Collection values) {
                ComponentUtilities.removeListValues(_fileList, values);
            }
        };
    }
}
