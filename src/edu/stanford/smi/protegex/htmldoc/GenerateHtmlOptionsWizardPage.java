package edu.stanford.smi.protegex.htmldoc;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to allow the user to set the options for generating HTML.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class GenerateHtmlOptionsWizardPage extends WizardPage {
    private Project project;
    private HTMLGeneratorExportPlugin plugin;
    private FileField directoryComponent;
    private SelectableList rootClsesComponent;
    private JCheckBox includeInstancesCheckBox;
    private static Collection savedRootNames = new ArrayList();
    private static String savedDirectory;
    private static boolean savedIncludeInstances;

    public GenerateHtmlOptionsWizardPage(Wizard wizard, Project project, HTMLGeneratorExportPlugin plugin) {
        super("generate html", wizard);
        this.plugin = plugin;
        this.project = project;
        setLayout(new BorderLayout(10, 10));
        add(createDirectoryPane(), BorderLayout.NORTH);
        add(createRootClsesPane(), BorderLayout.CENTER);
        add(createIncludeInstancesPane(), BorderLayout.SOUTH);
        restoreRoots();
    }
    
    public void onFinish() {
        Collection clses = ComponentUtilities.getListValues(rootClsesComponent);
        saveRoots(clses);
        plugin.setRootClses(clses);
        plugin.setIncludeInstances(includeInstancesCheckBox.isSelected());
        plugin.setOutputDirectory(directoryComponent.getFilePath());
    }

    private Action createAddClsAction() {
        return new AddAction(ResourceKey.CLASS_ADD) {
            public void onAdd() {
                Collection c = DisplayUtilities.pickClses(GenerateHtmlOptionsWizardPage.this, project.getKnowledgeBase());
                if (!c.isEmpty()) {
                    ComponentUtilities.addListValues(rootClsesComponent, c);
                }
            }
        };
    }

    private JComponent createDirectoryPane() {
        directoryComponent = new FileField("Output Directory", savedDirectory, "Output Directory");
        directoryComponent.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                updateFinishButton();
            }
        });
        return directoryComponent;
    }
    
    private void updateFinishButton() {
        boolean canFinish = false;
        String path = directoryComponent.getPath();
        if (path != null) {
	        File file = new File(path);
	        canFinish = !file.exists() || file.isDirectory();
        }
        setPageComplete(canFinish);
    }

    private Action createRemoveClsAction() {
        return new RemoveAction(ResourceKey.CLASS_REMOVE, rootClsesComponent) {
            public void onRemove(Collection values) {
                ComponentUtilities.removeListValues(rootClsesComponent, values);
            }
        };
    }

    private JComponent createRootClsesPane() {
        rootClsesComponent = ComponentFactory.createSelectableList(null);
        rootClsesComponent.setCellRenderer(FrameRenderer.createInstance());
        LabeledComponent c = new LabeledComponent("Root Classes", new JScrollPane(rootClsesComponent));
        c.addHeaderButton(createAddClsAction());
        c.addHeaderButton(createRemoveClsAction());
        return c;
    }

    private JComponent createIncludeInstancesPane() {
        includeInstancesCheckBox = ComponentFactory.createCheckBox("Include Instances");
        includeInstancesCheckBox.setSelected(savedIncludeInstances);
        return includeInstancesCheckBox;
    }

    private void restoreRoots() {
        Collection roots = new ArrayList();
        if (savedRootNames != null) {
            Iterator i = savedRootNames.iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                Frame frame = project.getKnowledgeBase().getFrame(name);
                if (frame instanceof Cls) {
                    roots.add(frame);
                }
            }
        }
        if (roots.isEmpty()) {
            roots.addAll(project.getKnowledgeBase().getRootClses());
        }
        ComponentUtilities.setListValues(rootClsesComponent, roots);
    }

    /* save the names rather than the frames in order to allow the frames
     * to be garbage collected.
     */
    private void saveRoots(Collection clses) {
        savedRootNames.clear();
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            savedRootNames.add(cls.getName());
        }
    }
    
}
