package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A plugin that controls the export of all or part of a knowledge to "somewhere else".
 * 
 * The export plugin ends up being a state object that is passed around among the wizard pages
 * of the configuration dialog. When the user hits "finish" the exportProject method gets called. The
 * pages are responsible for setting informatin in the exportPlugin object in their onFinish() method
 * so that the ExportPlugin.exportProject method can execute.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ExportProjectPlugin extends Plugin {
    boolean canExport(Project project);
    void exportProject(Project project);
    WizardPage createExportWizardPage(ExportWizard wizard, Project project);
}
