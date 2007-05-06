package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ImportPlugin extends Plugin {

    /**
     * Create a new project from information given "somewhere else".  The plugin is entirely responsible for 
     * prompting the user for the source of the domain information as well as creating and loading the Project.
     * @return the newly created Project
     */
    Project handleImportRequest();

}
