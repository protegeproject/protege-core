package edu.stanford.smi.protege.util;

import java.awt.*;
import java.net.*;

import javax.swing.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectChooser extends JFileChooser {
    private JTabbedPane pane;
    private static final int FILE_INDEX = 0;
    private static final int URL_INDEX = 1;
    private static final int REMOTE_INDEX = 2;
    private static final String TAB_INDEX_PROPERTY = "project_chooser.selected_tab";

    protected JDialog createDialog(Component parent) {
        JDialog dialog = super.createDialog(parent);
        Container contentPane = dialog.getContentPane();
        contentPane.remove(this);
        pane = ComponentFactory.createTabbedPane(false);
        pane.addTab("File", this);
        pane.addTab("URL", new JLabel("URL Tab"));
        pane.addTab("Server", new JLabel("Remote Tab"));

        pane.setSelectedIndex(ApplicationProperties.getIntegerProperty(TAB_INDEX_PROPERTY, 0));
        contentPane.add(pane);
        return dialog;
    }

    public void dispose() {
        int index = pane.getSelectedIndex();
        ApplicationProperties.setInt(TAB_INDEX_PROPERTY, index);
    }

    public URI getURI() {
        URI uri;
        switch (pane.getSelectedIndex()) {
            case FILE_INDEX:
                uri = getSelectedFile().toURI();
                break;
            case URL_INDEX:
                uri = null; // urlPane.getURI();
                break;
            case REMOTE_INDEX:
                uri = null; // remotePane.getURI();
                break;
            default:
                Log.getLogger().warning("bad index: " + pane.getSelectedIndex());
                uri = null;
                break;
        }
        return uri;
    }
}