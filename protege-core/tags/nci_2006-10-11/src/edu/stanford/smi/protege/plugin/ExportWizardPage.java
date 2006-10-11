package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ExportWizardPage extends WizardPage {
    
    protected ExportWizardPage(String name, ExportWizard wizard) {
        super(name, wizard);
    }
    protected ExportWizard getExportProjectWizard() {
        return (ExportWizard) getWizard();
    }
}
