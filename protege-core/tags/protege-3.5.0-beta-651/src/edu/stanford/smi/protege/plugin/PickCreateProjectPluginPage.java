package edu.stanford.smi.protege.plugin;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

class PickCreateProjectPluginPage extends WizardPage {
    private static final long serialVersionUID = 7411364041871092526L;
    private SelectableList list;
    private KnowledgeBaseFactory factory;
    
    PickCreateProjectPluginPage(Collection plugins, KnowledgeBaseFactory factory, Wizard wizard) {
        super("pick plugin", wizard);
        this.factory = factory;
        createList(plugins);
        add(new LabeledComponent("Existing Source Type", ComponentFactory.createScrollPane(list)));
        list.setCellRenderer(new CreateProjectPluginRenderer());
    }
    
    private JList createList(Collection plugins) {
        list = ComponentFactory.createSelectableList(null);
        ComponentUtilities.setListValues(list, plugins);
        list.setSelectedIndex(0);
        list.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                updateNextPage();
            }
        });
        return list;
    }
    
    public void onFinish() {
        CreateProjectPlugin plugin = (CreateProjectPlugin) list.getSelectedValue();
        plugin.setKnowledgeBaseFactory(factory);
        plugin.setUseExistingSources(true);
        CreateProjectWizard wizard = (CreateProjectWizard) getWizard();
        wizard.setPlugin(plugin);
    }
    
    public WizardPage getNextPage() {
        CreateProjectPlugin plugin = (CreateProjectPlugin) list.getSelectedValue();
        return plugin.createCreateProjectWizardPage((CreateProjectWizard) getWizard(), true);
    }
}

