package edu.stanford.smi.protege.plugin;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectOutputFormatWizardPage extends ExportWizardPage {
    private static final long serialVersionUID = -5850115778441040873L;
    private static final String SELECTED_PLUGIN = "export_project.last_selected_plugin";
    private SelectableList list;
    private Project project;
    
    
    SelectOutputFormatWizardPage(ExportWizard wizard, Project project) {
        super("select output format", wizard);
        this.project = project;
        JLabel label = ComponentFactory.createSmallFontLabel("Select an Output Format:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(label, BorderLayout.NORTH);
        list = createList();
        add(ComponentFactory.createScrollPane(list), BorderLayout.CENTER);
    }
    
    private SelectableList createList() {
        Collection plugins = new ArrayList();
        Object selection = null;
        String selectedFactoryName = ApplicationProperties.getString(SELECTED_PLUGIN);
        Iterator i = PluginUtilities.getAvailableExportProjectPluginClassNames().iterator();
        while (i.hasNext()) {
            String className = (String) i.next();
            ExportProjectPlugin plugin = (ExportProjectPlugin) SystemUtilities.newInstance(className);
            if (plugin.canExport(project)) {
                plugins.add(plugin);
	            if (plugin.getClass().getName().equals(selectedFactoryName)) {
	                selection = plugin;
	            }
            }
        }
        list = ComponentFactory.createSelectableList(null);
        list.setModel(new SimpleListModel(plugins));
        list.setCellRenderer(new ExportPluginRenderer());
        if (selection == null) {
            list.setSelectedIndex(0);
        } else {
            list.setSelectedValue(selection);
        }
        list.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                updateNextPage();
            }
        });
        return list;
    }
    
    public ExportProjectPlugin getSelectedPlugin() {
        ExportProjectPlugin plugin = (ExportProjectPlugin) list.getSelectedValue();
        ApplicationProperties.setString(SELECTED_PLUGIN, plugin.getClass().getName());
        return plugin;
    }
    
    public WizardPage getNextPage() {
        ExportProjectPlugin plugin = getSelectedPlugin();
        WizardPage nextPage = null;
        if (canChangeFormat(plugin)) {
            nextPage = new ChangeProjectFormatWizardPage(getExportProjectWizard(), (BackendExportPlugin) plugin, project);
        } else {
            nextPage = plugin.createExportWizardPage(getExportProjectWizard(), project);
        }
        return nextPage;
    }
    
    private boolean canChangeFormat(ExportProjectPlugin plugin2) {
        boolean canChangeFormat = false;
        if (plugin2 instanceof BackendExportPlugin) {
            BackendExportPlugin backendPlugin = (BackendExportPlugin) plugin2;
            canChangeFormat = backendPlugin.canExportToNewFormat(project);
        }
        return canChangeFormat;
    }
    
    public void onFinish() {
        ExportWizard wizard = (ExportWizard) getWizard();
        wizard.setExportPlugin(getSelectedPlugin());
    }
}

class ExportPluginRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 4680341715985207495L;

    public void load(Object o) {
        ExportProjectPlugin plugin = (ExportProjectPlugin) o;
        setMainText(plugin.getName());
    }
}

