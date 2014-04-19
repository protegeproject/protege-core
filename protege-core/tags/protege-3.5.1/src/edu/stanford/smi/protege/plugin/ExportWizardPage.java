package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ExportWizardPage extends WizardPage {
    
    private static final long serialVersionUID = 8585145286015032917L;
    protected ExportWizardPage(String name, ExportWizard wizard) {
        super(name, wizard);
    }
    protected ExportWizard getExportProjectWizard() {
        return (ExportWizard) getWizard();
    }
}
