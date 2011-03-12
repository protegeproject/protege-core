package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.util.*;

/**
 * Export plugins for the CLIPS file format.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsFilesExportProjectPlugin extends AbstractBackendExportPlugin implements ClipsFilesPlugin {
    private String clsesFileName;
    private String instancesFileName;

    public ClipsFilesExportProjectPlugin() {
        super(ClipsKnowledgeBaseFactory.DESCRIPTION);
    }

    public boolean canExport(Project project) {
        return true;
    }

    public boolean canExportToNewFormat(Project project) {
        return canExport(project);
    }
    
    public WizardPage createExportWizardPage(ExportWizard wizard, Project project) {
        return new ClipsFilesWizardPage(wizard, this);
    }

    public WizardPage createExportToNewFormatWizardPage(ExportWizard wizard, Project project) {
        return new ClipsExportToNewFormatWizardPage(wizard, project, this);
    }

    protected void overwriteDomainInformation(Project project, Collection errors) {
        KnowledgeBase kb = project.getKnowledgeBase();
        new ClipsKnowledgeBaseFactory().saveKnowledgeBase(kb, clsesFileName, instancesFileName, errors);
    }

    protected void initializeSources(Project project, Collection errors) {
        PropertyList sources = project.getSources();
        project.setKnowledgeBaseFactory(new ClipsKnowledgeBaseFactory());
        ClipsKnowledgeBaseFactory.setSourceFiles(sources, clsesFileName, instancesFileName);
    }

    public void setFiles(String clsesFileName, String instancesFileName) {
        this.clsesFileName = clsesFileName;
        this.instancesFileName = instancesFileName;
    }
    
    public void exportProject(Project project) {
        Writer clsesWriter = null;
        Writer instancesWriter = null;
        try {
            Collection errors = new ArrayList();
            KnowledgeBase kb = project.getKnowledgeBase();
            ClipsKnowledgeBaseFactory factory = new ClipsKnowledgeBaseFactory();
            clsesWriter = FileUtilities.getWriter(clsesFileName);
            instancesWriter = FileUtilities.getWriter(instancesFileName);
            factory.saveKnowledgeBase(kb, clsesWriter, instancesWriter, errors);
            handleErrors(errors);
        } finally {
            FileUtilities.close(clsesWriter);
            FileUtilities.close(instancesWriter);
        }
    }
}