package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ProjectPlugin extends Plugin {

    /**
     * Called after a new project is created but before it is displayed
     */
    void afterCreate(Project p);

    /**
     * Called after an existing project is loaded but before it is displayed
     */
    void afterLoad(Project p);
    
    /**
     * Called after a project has been saved.
     */
    void afterSave(Project p);


    /**
     * Called after the view has been added to the screen
     */
    void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar);

    /**
     * Called before a save operation for a project
     */
    void beforeSave(Project p);

    /**
     * Call before the view has been removed from the screen
     */
    void beforeHide(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar);

    /**
     * Called before the close operation for a project
     */
    void beforeClose(Project p);
}
