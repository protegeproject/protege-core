package edu.stanford.smi.protege.plugin;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractBackendExportPlugin extends AbstractExportPlugin implements BackendExportPlugin {
    private String newProjectPath;
    
    protected AbstractBackendExportPlugin(String name) {
        super(name);
    }
    
    public boolean canExportToNewFormat(Project project) {
        return true;
    }

    public void setNewProjectPath(String newProjectPath) {
        this.newProjectPath = newProjectPath;
    }

    protected abstract void initializeSources(Project project, Collection errors);
    protected abstract void overwriteDomainInformation(Project project, Collection errors);

    public Project exportProjectToNewFormat(Project project) {
        Collection errors = new ArrayList();
        if (isCompatibleProject(project)) {
            saveAndPreserveCustomizations(project, errors);
        } else {
            saveAndDiscardCustomizations(project, errors);
        }
        Project newProject = Project.loadProjectFromFile(newProjectPath, errors);
        handleErrors(errors);
        return newProject;
    }

    //ESCA-JAVA0130 
    protected boolean isCompatibleProject(Project project) {
        String factoryName = project.getKnowledgeBaseFactory().getClass().getName();
        return factoryName.indexOf("OWL") == -1;
    }

    private void saveAndPreserveCustomizations(Project project, Collection errors) {
        project.setProjectFilePath(newProjectPath);
        initializeSources(project, errors);
        project.save(errors);
    }

    protected void saveAndDiscardCustomizations(Project project, Collection errors) {
        Project newProject = Project.createNewProject(null, errors);
        newProject.setProjectFilePath(newProjectPath);
        newProject.save(errors);
        overwriteDomainInformation(project, errors);
    }
}
