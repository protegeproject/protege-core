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
    private static final long serialVersionUID = -8843058597898410690L;
    private static final String SELECTED_FACTORY = "select_project_type.last_factory";
    private JCheckBox doBuildBox;
    private SelectableList list;
    private CreateProjectPlugin plugin;

    SelectProjectTypeWizardPage(CreateProjectWizard wizard) {
        super("select project type", wizard);
        JLabel label = ComponentFactory.createSmallFontLabel("Select a Project Type:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(label, BorderLayout.NORTH);
        list = createList();
        listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        configureCheckBox();
        add(listPanel, BorderLayout.CENTER);
        add(doBuildBox, BorderLayout.NORTH);
    }

    private SelectableList createList() {
        KnowledgeBaseFactory selection = null;
        String selectedFactoryName = ApplicationProperties.getString(SELECTED_FACTORY);
        Collection<KnowledgeBaseFactory> factories = PluginUtilities.getAvailableFactories();
        Iterator<KnowledgeBaseFactory> i = factories.iterator();
        while (i.hasNext()) {
            KnowledgeBaseFactory factory = i.next();
            if (factory.getClass().getName().equals(selectedFactoryName)) {
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
            private static final long serialVersionUID = 3873947894902634872L;

            public void actionPerformed(ActionEvent event) {
                updateNextPage();
            }
        };
        action.setMnemonic('C');
        doBuildBox = new JCheckBox(action);
    }

    public KnowledgeBaseFactory getSelectedFactory() {
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) list.getSelectedValue();
        if (factory != null) {
            ApplicationProperties.setString(SELECTED_FACTORY, factory.getClass().getName());
        }
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
        if (plugin != null) {
            plugin.setKnowledgeBaseFactory(getSelectedFactory());
            plugin.setUseExistingSources(getUseExistingSources());
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
            page = new PickCreateProjectPluginPage(plugins, getSelectedFactory(), getWizard());
        }
        return page;
    }

    private static Collection getAppropriateCreateProjectPlugins(KnowledgeBaseFactory factory,
            boolean useExistingSources) {
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


