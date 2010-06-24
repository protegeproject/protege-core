package edu.stanford.smi.protege.storage.clips;

import java.util.*;

import edu.stanford.smi.protege.util.*;

public class IncludedProjectsWizardPage extends WizardPage {
    private URIList includedProjectsList;
    private ClipsFilesCreateProjectPlugin plugin;

    public IncludedProjectsWizardPage(Wizard wizard, ClipsFilesCreateProjectPlugin plugin) {
        super("included projects", wizard);
        this.plugin = plugin;
        createComponents();
    }

    private void createComponents() {
        includedProjectsList = new URIList("Included Projects", null, ".pprj", "Project Files");
        add(includedProjectsList);
    }

    public void onFinish() {
        Collection includedProjects = includedProjectsList.getURIs();
        if (!includedProjects.isEmpty()) {
            plugin.setIncludedProjects(includedProjects);
        }
    }

}
