package edu.stanford.smi.protege.plugin;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectProjectTypeWizardPage extends WizardPage {
    private static final String SELECTED_FACTORY = "select_project_type.last_factory"; 
    private JCheckBox doBuildBox;
    private SelectableList list;
    private CreateProjectPlugin plugin;
    
    
    SelectProjectTypeWizardPage(CreateProjectWizard wizard) {
        super("select project type", wizard);
        JLabel label = ComponentFactory.createSmallFontLabel("Select a Project Type:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(label, BorderLayout.NORTH);
        list = createList();
        add(new JScrollPane(list), BorderLayout.CENTER);
       configureCheckBox();
        add(doBuildBox, BorderLayout.SOUTH);
    }
    
    private SelectableList createList() {
        KnowledgeBaseFactory selection = null;
        String selectedFactoryName = ApplicationProperties.getString(SELECTED_FACTORY);
        Collection factories = PluginUtilities.getAvailableFactories();
        Iterator i = factories.iterator();
        while (i.hasNext()) {
            KnowledgeBaseFactory factory = (KnowledgeBaseFactory) i.next();
            if (factory.getClass().getName() == selectedFactoryName) {
                selection = factory;
            }
        }
        list = ComponentFactory.createSelectableList(null);
        list.setModel(new SimpleListModel(factories));
        list.setCellRenderer(new FactoryRenderer());
        if (selection == null) {
            list.setSelectedIndex(0);
        } else {
            list.setSelectedValue(selection);
        }
        list.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                updateNextPage();
                updateDoBuildBox();
            }
        });
        return list;
    }
    
    private void configureCheckBox() {
        StandardAction action = new StandardAction("Create from Existing Sources") {
            public void actionPerformed(ActionEvent event) {
                updateNextPage();
            }
        };
        action.setMnemonic('C');
        doBuildBox = new JCheckBox(action);
    }
    
    public KnowledgeBaseFactory getSelectedFactory() {
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) list.getSelectedValue();
        ApplicationProperties.setString(SELECTED_FACTORY, factory.getClass().getName());
        return factory;
    }
    
    private CreateProjectWizard getCreateProjectWizard() {
        return (CreateProjectWizard) getWizard();
    }
    
    private void updateDoBuildBox() {
        Collection plugins = getAppropriateCreateProjectPlugins(getSelectedFactory(), getUseExistingSources());
        if (plugins.size() == 0) {
            doBuildBox.setSelected(false);
            doBuildBox.setEnabled(false);
        } else {
            doBuildBox.setEnabled(true);
        }
    }
    
    private boolean getUseExistingSources() {
        return doBuildBox.isSelected();
    }
    
    public void onFinish() {
        plugin.setKnowledgeBaseFactory(getSelectedFactory());
        plugin.setUseExistingSources(getUseExistingSources());
        if (plugin != null) {
            getCreateProjectWizard().setPlugin(plugin);
        }
    }
    
    public WizardPage getNextPage() {
        WizardPage page = null;
        Collection plugins = getAppropriateCreateProjectPlugins(getSelectedFactory(), getUseExistingSources());
        if (plugins.size() == 0) {
            plugin = new DefaultCreateProjectPlugin();
        } else if (plugins.size() == 1) {
            plugin = (CreateProjectPlugin) plugins.iterator().next();
            page = plugin.createCreateProjectWizardPage(getCreateProjectWizard(), getUseExistingSources());
        } else {
            plugin = null;
            // page = new PickImportPluginPage(plugins);
        }
        return page;
    }
    
    private Collection getAppropriateCreateProjectPlugins(KnowledgeBaseFactory factory, boolean useExistingSources) {
        Collection appropriatePlugins = new ArrayList();
        Iterator i = PluginUtilities.getAvailableCreateProjectPluginClassNames().iterator();
        while (i.hasNext()) {
            String className = (String) i.next();
            CreateProjectPlugin availablePlugin = (CreateProjectPlugin) SystemUtilities.newInstance(className);
            if (availablePlugin.canCreateProject(factory, useExistingSources)) {
                appropriatePlugins.add(availablePlugin);
            }
        }
        return appropriatePlugins;
    }
}

class DefaultCreateProjectPlugin extends AbstractCreateProjectPlugin {
    
    public DefaultCreateProjectPlugin() {
        super("default import plugin");
    }
    
    public boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources) {
        return !useExistingSources;
    }

    public WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard, boolean useExistingSources) {
        return null;
    }
}

