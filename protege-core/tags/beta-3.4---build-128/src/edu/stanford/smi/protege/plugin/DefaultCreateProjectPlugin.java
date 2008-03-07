package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

class DefaultCreateProjectPlugin extends AbstractCreateProjectPlugin {

    DefaultCreateProjectPlugin() {
        super("default import plugin");
    }

    public boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources) {
        return !useExistingSources;
    }

    public WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard, boolean useExistingSources) {
        return null;
    }
}
