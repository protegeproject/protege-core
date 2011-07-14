package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;

/**
 * A Page used with the Wizard framework.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class WizardPage extends JComponent {
    private static final long serialVersionUID = 7009741655044631780L;
    private Wizard wizard;
    private boolean pageComplete = true;

    protected WizardPage(String name, Wizard wizard) {
        setName(name);
        this.wizard = wizard;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    public Wizard getWizard() {
        return wizard;
    }

    /**
     * Marks this page as "complete" (requiring no further input). When all pages are marked complete 
     * the Finish button is enabled. The default complete state is "true".
     */
    public void setPageComplete(boolean complete) {
        pageComplete = complete;
        wizard.notifyChanged(this);
    }

    public boolean isPageComplete() {
        return pageComplete;
    }

    /**
     * Called by the system when the Finish button is pressed. Pages should override this
     * to commit their state to the state object.
     */
    public void onFinish() {
        // do nothing
    }

    public void onCancel() {
        // do nothing
    }

    /**
     * Override to return the next page to be displayed.  Return null if no more pages are 
     * needed. The default behavior is to return null.
     */
    //ESCA-JAVA0130 
    public WizardPage getNextPage() {
        return null;
    }

    /**
     * Call this when the state on a page changes in a way the next wizard page needs to be
     * changes and/or reinitialized.
     */
    public void updateNextPage() {
        wizard.updateNextPage(this);
    }
}
