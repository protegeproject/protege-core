package edu.stanford.smi.protege.plugin;

import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
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
            ProjectManager.getProjectManager().displayErrors("Create Project Errors", errors);
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

    protected void initialize(Project project) {
        initializeSources(project.getSources());
    }

    protected Project buildNewProject(KnowledgeBaseFactory factory) {
        Collection errors = new ArrayList();
        Project project = Project.createBuildProject(factory, errors);
        initialize(project);
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

}