package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AboutPluginsBox extends JPanel {
    private static final long serialVersionUID = -6678212119027124508L;
    private JList pluginsList;
    private JEditorPane aboutViewer;

    public AboutPluginsBox() {
        pluginsList = createPluginsList();
        aboutViewer = createAboutViewer();
        layoutComponents();
    }

    private JList createPluginsList() {
        SelectableList list = ComponentFactory.createSelectableList(null);
        ListModel model = createPluginsModel();
        list.setModel(model);
        list.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                String componentName = (String) pluginsList.getSelectedValue();
                if (componentName != null) {
                    URL url = PluginUtilities.getPluginComponentAboutURL(componentName);
                    try {
                        aboutViewer.setPage(url);
                    } catch (IOException e) {
                        Log.getLogger().warning(e.toString());
                    }
                }
            }
        });
        list.setPreferredSize(new Dimension(150, 150));
        return list;
    }

    private static ListModel createPluginsModel() {
        List names = new ArrayList(PluginUtilities.getPluginComponentNames());
        Iterator i = names.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            URL url = PluginUtilities.getPluginComponentAboutURL(name);
            if (url == null) {
                i.remove();
            }
        }
        Collections.sort(names);
        return new SimpleListModel(names);
    }

    private static JEditorPane createAboutViewer() {
        JEditorPane pane = ComponentFactory.createHTMLBrowser(null);
        pane.setPreferredSize(new Dimension(600, 600));
        return pane;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        JComponent left = new LabeledComponent("Installed Plugins", new JScrollPane(pluginsList));
        JComponent right = new LabeledComponent("About Selected Plugin", new JScrollPane(aboutViewer));
        JSplitPane pane = ComponentFactory.createLeftRightSplitPane(left, right);
        add(pane);
    }
}
