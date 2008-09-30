package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsFilesCreateProjectPlugin extends AbstractCreateProjectPlugin implements ClipsFilesPlugin {
    private String clsesFileName;
    private String instancesFileName;
    private Collection<URI> includedProjects;

    public ClipsFilesCreateProjectPlugin() {
        super(ClipsKnowledgeBaseFactory.DESCRIPTION);
    }

    public void setFiles(String clsesFileName, String instancesFileName) {
        this.clsesFileName = clsesFileName;
        this.instancesFileName = instancesFileName;
    }

    public void setIncludedProjects(Collection<URI> includedProjects) {
        this.includedProjects = new ArrayList<URI>(includedProjects);
    }

    public boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources) {
        return factory.getClass() == ClipsKnowledgeBaseFactory.class;
    }

    protected void initialize(Project project) {
        super.initialize(project);
        if (includedProjects != null) {
            Iterator<URI> i = includedProjects.iterator();
            while (i.hasNext()) {
                URI uri = i.next();
                project.includeProject(uri, false, null);
            }
        }
    }

    protected void initializeSources(PropertyList sources) {
        String className = FileUtilities.getName(clsesFileName);
        String instancesName = FileUtilities.getName(instancesFileName);
        ClipsKnowledgeBaseFactory.setSourceFiles(sources, className, instancesName);
    }

    protected URI getBuildProjectURI() {
        String name = clsesFileName.substring(0, clsesFileName.length() - 5);
        name += ".pprj";
        File file = new File(name);
        return file.toURI();
    }

    public WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard, boolean useExistingSources) {
        WizardPage page = null;
        if (useExistingSources) {
            page = new ClipsFilesWizardPage(wizard, this);
        }
        return page;
    }
}