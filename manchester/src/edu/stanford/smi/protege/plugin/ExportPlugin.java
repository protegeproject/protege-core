package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ExportPlugin extends Plugin {

    /**
     * Save the domain information out to "somewhere else".  The plugin is entirely responsible for 
     * prompting the user for the destination for the domain information.  The "project information" (forms, browser
     * slots, etc) do not have to be saved.
     */
    void handleExportRequest(Project project);
}
