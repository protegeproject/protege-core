package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A plugin that controls the creation and configuration of a Project and the possible import 
 * of existing sources files into the new project.
 * 
 * The create project plugin ends up being a state object that is passed around among the wizard pages
 * of the configuration dialog. When the user hits "finish" the createProject method gets called. The
 * pages are responsible for setting informatin in the ImportPlugin object in their onFinish() method
 * so that the ImportPlugin.importIntoProject method can execute.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface CreateProjectPlugin extends Plugin {
    boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources);
    WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard, boolean useExistingSources);
    Project createProject();
    void setKnowledgeBaseFactory(KnowledgeBaseFactory factory);
    void setUseExistingSources(boolean useExistingSources);
}