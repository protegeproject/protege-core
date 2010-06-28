package edu.stanford.smi.protege.plugin;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ChangeProjectFormatWizardPage extends ExportWizardPage {
    private Project project;
    private AbstractButton convertProjectButton;
    private BackendExportPlugin plugin;

    public ChangeProjectFormatWizardPage(ExportWizard wizard, BackendExportPlugin plugin, Project project) {
        super("change project format", wizard);
        this.plugin = plugin;
        this.project = project;
        AbstractButton button = createButton("Export Files and Leave Project Unchanged");
        button.setSelected(true);
        convertProjectButton = createButton("Convert Project to this Format");
        ButtonGroup group = new ButtonGroup();
        group.add(button);
        group.add(convertProjectButton);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(button);
        add(convertProjectButton);
        setPageComplete(true);
    }
    
    private AbstractButton createButton(String text) {
        Action action = new AbstractAction(text) {
            public void actionPerformed(ActionEvent event) {
                updateNextPage();
            }
        };
        AbstractButton button = ComponentFactory.createRadioButton(action);
        return button;
    }
    
    public WizardPage getNextPage() {
        WizardPage page;
        if (convertProject()) {
            page = plugin.createExportToNewFormatWizardPage(getExportProjectWizard(), project);
        } else {
            page = plugin.createExportWizardPage(getExportProjectWizard(), project);
        }
        return page;
    }
    
    public void onFinish() {
        ExportWizard wizard = (ExportWizard) getWizard();
        wizard.setExportToNewFormat(convertProject());
    }
    
    private boolean convertProject() {
        return convertProjectButton.isSelected();
    }
}
