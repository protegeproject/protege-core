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
public class URIList extends AbstractSelectableComponent {
    private static final long serialVersionUID = 1611646775722808214L;
    private JList _uriList;
    private String _description;
    private String _extension;

    public URIList(String label, Collection uris, String extension, String description) {
        _description = description;
        _extension = extension;
        setLayout(new BorderLayout());
        LabeledComponent c = new LabeledComponent(label, createComponent(uris));
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
                        ComponentUtilities.addSelectedListValue(_uriList, files[i].toURI());
                    }
                }
                break;
            default:
                Assert.fail("bad result: " + openDialogResult);
                break;
        }
    }

    public void clearSelection() {
        _uriList.clearSelection();
    }

    private JComponent createComponent(Collection files) {
        _uriList = ComponentFactory.createList(newAddAction());
        _uriList.addListSelectionListener(new ListSelectionListenerAdapter(this));
        if (files != null) {
            ComponentUtilities.setListValues(_uriList, files);
        }
        return ComponentFactory.createScrollPane(_uriList);
    }

    public Collection getURIs() {
        return ComponentUtilities.getListValues(_uriList);
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_uriList);
    }

    private Action newAddAction() {
        return new AddAction(ResourceKey.VALUE_ADD) {
            private static final long serialVersionUID = -6469490466832990499L;

            public void onAdd() {
                browse();
            }
        };
    }

    private Action newRemoveAction() {
        return new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = 5235237616336460573L;

            public void onRemove(Collection values) {
                ComponentUtilities.removeListValues(_uriList, values);
            }
        };
    }
}
