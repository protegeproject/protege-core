package edu.stanford.smi.protege.plugin;

import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractCreateProjectPlugin implements CreateProjectPlugin {
    private String name;
    private KnowledgeBaseFactory knowledgeBaseFactory;
    private boolean useExistingSources;

    public AbstractCreateProjectPlugin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void dispose() {
        // do nothing
    }

    protected void handleErrors(Collection errors) {
        if (!errors.isEmpty()) {
            Object error = errors.iterator().next();
            Log.getLogger().severe(error.toString());
        }
    }

    public void setKnowledgeBaseFactory(KnowledgeBaseFactory factory) {
        this.knowledgeBaseFactory = factory;
    }

    public void setUseExistingSources(boolean b) {
        this.useExistingSources = b;
    }

    public Project createProject() {
        Project project = null;
        if (useExistingSources) {
            project = buildNewProject(knowledgeBaseFactory);
        } else {
            project = createNewProject(knowledgeBaseFactory);
        }
        return project;
    }

    protected Project createNewProject(KnowledgeBaseFactory factory) {
        Collection errors = new ArrayList();
        Project project = Project.createNewProject(factory, errors);
        handleErrors(errors);
        return project;
    }

    protected Project buildNewProject(KnowledgeBaseFactory factory) {
        Collection errors = new ArrayList();
        Project project = Project.createBuildProject(factory, errors);
        initializeSources(project.getSources());
        project.createDomainKnowledgeBase(factory, errors, true);
        URI uri = getBuildProjectURI();
        if (uri != null) {
            project.setProjectURI(uri);
        }
        handleErrors(errors);
        return project;
    }

    protected URI getBuildProjectURI() {
        return null;
    }

    protected void initializeSources(PropertyList sources) {
        throw new UnsupportedOperationException();
    }

    /*
     * protected void setRequiresReloadAfterCreate(boolean b) { requiresReloadAfterCreate = b; }
     * 
     * public void handleImportIntoExistingProjectRequest(Project project) { boolean succeeded = promptForSources(); if
     * (succeeded) { Collection errors = new ArrayList(); importIntoProject(project, errors); handleErrors("Failed to
     * import project.", errors); } }
     * 
     * public Project handleImportIntoNewProjectRequest() { Project project = createProject();
     * handleImportIntoExistingProjectRequest(project); return project; }
     * 
     * private Project createProject() { Collection errors = new ArrayList(); Project project =
     * Project.createNewProject(factory, errors); if (requiresReloadAfterCreate) { project =
     * saveAndReloadProject(project, errors); } return project; }
     * 
     * protected Project saveAndReloadProject(Project project, Collection errors) { PropertyList sources =
     * project.getSources(); KnowledgeBaseSourcesEditor editor = project.getKnowledgeBaseFactory()
     * .createKnowledgeBaseSourcesEditor(null, sources); editor.setShowProject(true); JComponent parent =
     * ProjectManager.getProjectManager().getMainPanel(); String title =
     * LocalizedText.getText(ResourceKey.DATABASE_CONFIGURATION_DIALOG_TITLE); int rval = ModalDialog.showDialog(parent,
     * editor, title, ModalDialog.MODE_OK_CANCEL); if (rval == ModalDialog.OPTION_OK) { editor.saveContents();
     * WaitCursor cursor = createWaitCursor(); try { URI uri = URIUtilities.createURI(editor.getProjectPath());
     * project.setProjectURI(uri); project.save(errors); String saveTitle = LocalizedText
     * .getText(ResourceKey.SAVE_PROJECT_FAILED_DIALOG_TITLE); handleErrors(saveTitle, errors); if (errors.isEmpty()) {
     * project.dispose(); project = Project.loadProjectFromURI(uri, errors); String reloadTitle = LocalizedText
     * .getText(ResourceKey.RELOAD_PROJECT_FAILED_DIALOG_TITLE); handleErrors(reloadTitle, errors); if
     * (!errors.isEmpty()) { project = null; } } else { project = null; } } finally { cursor.hide(); } } return project; }
     */

}