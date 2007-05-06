package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * The cls tree display on the instances panel.  This is different from the one on the classes panel in that it is less
 * functional.  The user can not create or delete classes from this panel.  The user also cannot change the "browse slot"
 * from this panel.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceClsesPanel extends SelectableContainer {
    private Project _project;
    private SelectableTree _clsTree;

    public InstanceClsesPanel(Project project) {
        _project = project;
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createClsesPanel(), BorderLayout.CENTER);
        setSelectable(_clsTree);
    }
    
    private JComponent createHeaderPanel() {
        JLabel label = ComponentFactory.createLabel(_project.getName());
        label.setIcon(Icons.getProjectIcon());
        String classBrowserLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_TITLE);
        String forProjectLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
        HeaderComponent header = new HeaderComponent(classBrowserLabel, forProjectLabel, label);
        header.setColor(Colors.getClsColor());
        return header;
    }

    private JComponent createClsesPanel() {
        Cls root = _project.getKnowledgeBase().getRootCls();
        _clsTree = ComponentFactory.createSelectableTree(null, new ParentChildRoot(root));
        _clsTree.setLargeModel(true);

        FrameRenderer renderer = FrameRenderer.createInstance();
        renderer.setDisplayDirectInstanceCount(true);
        _clsTree.setCellRenderer(renderer);
        _clsTree.setSelectionRow(0);
        String classHiearchyLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
        LabeledComponent c = new LabeledComponent(classHiearchyLabel, ComponentFactory.createScrollPane(_clsTree));
        c.setBorder(ComponentUtilities.getAlignBorder());
        c.addHeaderButton(getViewClsAction());
        c.setFooterComponent(new ClsTreeFinder(_project.getKnowledgeBase(), _clsTree));
        return c;
    }

    public JTree getDropComponent() {
        return _clsTree;
    }

    private Action getViewClsAction() {
        return new ViewAction(ResourceKey.CLASS_VIEW, this) {
            public void onView(Object o) {
                Cls cls = (Cls) o;
                _project.show(cls);
            }
        };
    }

    public void setSelectedCls(Cls cls) {
        Collection path = ModelUtilities.getPathToRoot(cls);
        ComponentUtilities.setSelectedObjectPath(_clsTree, path);
    }
}
