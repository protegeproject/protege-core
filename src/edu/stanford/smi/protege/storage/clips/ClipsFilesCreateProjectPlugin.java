package edu.stanford.smi.protege.storage.clips;

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

    public ClipsFilesCreateProjectPlugin() {
        super(ClipsKnowledgeBaseFactory.DESCRIPTION);
    }
    
    public void setFiles(String clsesFileName, String instancesFileName) {
        this.clsesFileName = clsesFileName;
        this.instancesFileName = instancesFileName;
    }

    public boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources) {
        return factory.getClass() == ClipsKnowledgeBaseFactory.class;
    }
    
    protected void initializeSources(PropertyList sources) {
        ClipsKnowledgeBaseFactory.setSourceFiles(sources, clsesFileName, instancesFileName);
    }

    public WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard, boolean useExistingSources) {
        WizardPage page = null;
        if (useExistingSources) {
            page = new ClipsFilesWizardPage(wizard, this);
        }
        return page;
    }
}