package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * An export plugin that also can handle changing the native file format.  This plug will always be 
 * associated one-to-one with a storage backend.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

interface BackendExportPlugin extends ExportProjectPlugin {
    boolean canExportToNewFormat(Project project);
    Project exportProjectToNewFormat(Project project);
    WizardPage createExportToNewFormatWizardPage(ExportWizard wizard, Project project);
}
