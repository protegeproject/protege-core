package edu.stanford.smi.protegex.htmldoc;

import java.awt.*;
import java.util.*;

import javax.swing.*;

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
public class GenerateHtmlOptionsPane extends JComponent {
    private KnowledgeBase kb;
    private FileField directoryComponent;
    private SelectableList rootClsesComponent;
    private JCheckBox _includeInstancesCheckBox;
    private static Collection savedRootNames = new ArrayList();

    public GenerateHtmlOptionsPane(KnowledgeBase kb) {
        this.kb = kb;
        setLayout(new BorderLayout(10, 10));
        add(createDirectoryPane(), BorderLayout.NORTH);
        add(createRootClsesPane(), BorderLayout.CENTER);
        add(createIncludeInstancesPane(), BorderLayout.SOUTH);
        restoreRoots();
    }

    private Action createAddClsAction() {
        return new AddAction(ResourceKey.CLASS_ADD) {
            public void onAdd() {
                Collection c = DisplayUtilities.pickClses(GenerateHtmlOptionsPane.this, kb);
                if (!c.isEmpty()) {
                    ComponentUtilities.addListValues(rootClsesComponent, c);
                }
            }
        };
    }

    private JComponent createDirectoryPane() {
        directoryComponent = new FileField("Output Directory", null, "Output Directory");
        return directoryComponent;
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
        _includeInstancesCheckBox = ComponentFactory.createCheckBox("Include Instances");
        return _includeInstancesCheckBox;
    }

    public String getOutputPath() {
        return directoryComponent.getPath();
    }

    public Collection getRootClses() {
        Collection c = ComponentUtilities.getListValues(rootClsesComponent);
        saveRoots(c);
        return c;
    }

    public boolean getIncludeInstances() {
        return _includeInstancesCheckBox.isSelected();
    }

    private void restoreRoots() {
        Collection roots = new ArrayList();
        if (savedRootNames != null) {
            Iterator i = savedRootNames.iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                Frame frame = kb.getFrame(name);
                if (frame instanceof Cls) {
                    roots.add(frame);
                }
            }
        }
        if (roots.isEmpty()) {
            roots.addAll(kb.getRootClses());
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
