package edu.stanford.smi.protegex.htmldoc;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class HTMLGeneratorExportPlugin extends AbstractExportPlugin {
    private Collection rootClses;
    private File outputDirectory;
    private boolean includeInstances;

    public HTMLGeneratorExportPlugin() {
        super("HTML Files");
    }

    public WizardPage createExportWizardPage(ExportWizard wizard, Project project) {
        return new GenerateHtmlOptionsWizardPage(wizard, project, this);
    }

    public static void main(String[] args) {
        Application.main(args);
    }

    public void setRootClses(Collection rootClses) {
        this.rootClses = new ArrayList(rootClses);
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setIncludeInstances(boolean includeInstances) {
        this.includeInstances = includeInstances;
    }

    public void exportProject(Project project) {
        outputDirectory.mkdirs();
        ProtegeGenClassHierarchy.generateDocs(project.getKnowledgeBase(), rootClses, true,
                "index.html", outputDirectory.getPath(), includeInstances);
    }

}