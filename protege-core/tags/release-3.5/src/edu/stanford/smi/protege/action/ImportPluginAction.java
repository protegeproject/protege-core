package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.plugin.*;

/**
 * Action to build a project from component pieces (files)
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ImportPluginAction extends ProjectAction {
    private static final long serialVersionUID = 4866956221397012527L;
    private ImportPlugin plugin;

    public ImportPluginAction(ImportPlugin plugin) {
        super(plugin.getName());
        this.plugin = plugin;
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().importProjectRequest(plugin);
    }
}
