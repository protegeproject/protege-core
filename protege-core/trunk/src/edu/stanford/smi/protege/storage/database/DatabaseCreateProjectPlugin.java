package edu.stanford.smi.protege.storage.database;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseCreateProjectPlugin extends AbstractCreateProjectPlugin implements
        DatabasePlugin {
    private String driver;
    private String table;
    private String username;
    private String password;
    private String url;

    public DatabaseCreateProjectPlugin() {
        super(DatabaseKnowledgeBaseFactory.DESCRIPTION);
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources) {
        return factory.getClass() == DatabaseKnowledgeBaseFactory.class;
    }

    public Project createNewProject(KnowledgeBaseFactory factory) {
        Collection errors = new ArrayList();
        Project project = super.createNewProject(factory);
        initializeSources(project.getSources());
        try {
            File tempProjectFile = File.createTempFile("protege", "temp");
            project.setProjectFilePath(tempProjectFile.getPath());
            project.save(errors);
            project = Project.loadProjectFromFile(tempProjectFile.getPath(), errors);
            handleErrors(errors);
            project.setProjectFilePath(null);
            tempProjectFile.delete();
        } catch (IOException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return project;
    }

    protected void initializeSources(PropertyList sources) {
        DatabaseKnowledgeBaseFactory.setSources(sources, driver, url, table, username, password);
    }

    public WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard,
            boolean useExistingSources) {
        return new DatabaseWizardPage(wizard, this);
    }
}