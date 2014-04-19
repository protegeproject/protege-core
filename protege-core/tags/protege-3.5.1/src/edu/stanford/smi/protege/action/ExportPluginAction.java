package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.plugin.*;

/**
 * Action to build a project from component pieces (files)
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ExportPluginAction extends ProjectAction {
    private static final long serialVersionUID = -1901281745434640608L;
    private ExportPlugin plugin;

    public ExportPluginAction(ExportPlugin plugin) {
        super(plugin.getName());
        this.plugin = plugin;
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().exportProjectRequest(plugin);
    }
}
