package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * The panel that holds the "forms hierarchy" on the Forms tab.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormsPanel extends SelectableContainer {
    private Project _project;
    private SelectableTree _tree;

    private ProjectListener _projectListener = new ProjectAdapter() {
        public void formChanged(ProjectEvent event) {
            TreePath path = _tree.getSelectionPath();
            if (path != null) {
                LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
                node.notifyNodeChanged(node);
            }
            _tree.repaint();
        }
    };

    public FormsPanel(Project p) {
        _project = p;

        _project.addProjectListener(_projectListener);

        _tree = createTree();
        String formsLabel = LocalizedText.getText(ResourceKey.FORM_BROWSER_FORMS_LABEL);
        LabeledComponent c = new LabeledComponent(formsLabel, ComponentFactory.createScrollPane(_tree));
        c.setBorder(ComponentUtilities.getAlignBorder());
        c.addHeaderButton(createCustomizationsAction());
        c.addHeaderButton(createRemoveCustomizationsAction());
        c.addHeaderButton(createRelayoutAction());
        c.addHeaderButton(createLayoutLikeOtherFormAction());
        c.setFooterComponent(createFinderControl());

        _tree.setCellRenderer(new FormRenderer(_project));
        add(c, BorderLayout.CENTER);
        add(createHeader(), BorderLayout.NORTH);
        setSelectable(_tree);
    }
    
    private JComponent createHeader() {
        JLabel label = ComponentFactory.createLabel(Icons.getProjectIcon());
        label.setText(_project.getName());
        String formBrowserLabel = LocalizedText.getText(ResourceKey.FORM_BROWSER_TITLE);
        String forProjectLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
        HeaderComponent header = new HeaderComponent(formBrowserLabel, forProjectLabel, label);
        header.setColor(Colors.getFormColor());
        return header;
    }

    private JComponent createFinderControl() {
        return new ClsTreeFinder(getKnowledgeBase(), _tree, ResourceKey.FORM_SEARCH_FOR);
    }

    private Action createLayoutLikeOtherFormAction() {
        Action action = new StandardAction(ResourceKey.FORM_LAYOUT_LIKE) {
            public void actionPerformed(ActionEvent event) {
                Collection selection = getSelection();
                if (!selection.isEmpty()) {
                    Cls cls = DisplayUtilities.pickForm(FormsPanel.this, _project);
                    if (cls != null) {
                        WaitCursor cursor = new WaitCursor(FormsPanel.this);
                        try {
                            Iterator i = selection.iterator();
                            while (i.hasNext()) {
                                Cls widgetCls = (Cls) i.next();
                                ClsWidget widget = getClsWidget(widgetCls);
                                widget.layoutLikeCls(cls);
                            }
                        } finally {
                            cursor.hide();
                        }
                    }
                }
            }
        };
        return action;
    }

    private Action createRelayoutAction() {
        return new StandardAction(ResourceKey.FORM_RELAYOUT) {
            public void actionPerformed(ActionEvent event) {
                Iterator i = getSelection().iterator();
                while (i.hasNext()) {
                    Cls cls = (Cls) i.next();
                    ClsWidget widget = getClsWidget(cls);
                    widget.relayout();
                }
            }
        };
    }

    private Action createRemoveCustomizationsAction() {
        return new StandardAction(ResourceKey.FORM_REMOVE_CUSTOMIZATIONS) {
            public void actionPerformed(ActionEvent event) {
                Iterator i = getSelection().iterator();
                while (i.hasNext()) {
                    Cls cls = (Cls) i.next();
                    ClsWidget widget = getClsWidget(cls);
                    widget.removeCustomizations();
                }
            }
        };
    }

    private Action createCustomizationsAction() {
        return new StandardAction(ResourceKey.FORM_VIEW_CUSTOMIZATIONS) {
            public void actionPerformed(ActionEvent event) {
                Iterator i = getSelection().iterator();
                while (i.hasNext()) {
                    Cls cls = (Cls) i.next();
                    ClsWidget widget = getClsWidget(cls);
                    widget.configure();
                }
            }
        };
    }

    public LazyTreeRoot createRoot() {
        return new FormParentChildRoot(getKnowledgeBase().getRootCls());
    }

    private SelectableTree createTree() {
        Cls root = getKnowledgeBase().getRootCls();
        SelectableTree tree = ComponentFactory.createSelectableTree(null, new ParentChildRoot(root));
        tree.setLargeModel(true);
        tree.addMouseListener(new TreePopupMenuMouseListener(tree) {
            public JPopupMenu getPopupMenu() {
                return FormsPanel.this.getPopupMenu();
            }
        });
        return tree;
    }

    public void dispose() {
        _project.removeProjectListener(_projectListener);
    }

    public JTree getFormsTree() {
        return _tree;
    }

    private ClsWidget getClsWidget(Cls cls) {
        return _project.getDesignTimeClsWidget(cls);
    }

    private KnowledgeBase getKnowledgeBase() {
        return _project.getKnowledgeBase();
    }

    private JPopupMenu getPopupMenu() {
        JPopupMenu menu = null;
        Collection selection = getSelection();
        if (selection.size() == 1) {
            menu = new JPopupMenu();
            menu.add(getRemoveDecendentCustomizations());
        }
        return menu;
    }

    private Action getRemoveDecendentCustomizations() {
        return new AbstractAction("Remove subclass customizations") {
            public void actionPerformed(ActionEvent event) {
                WaitCursor cursor = new WaitCursor(FormsPanel.this);
                try {
                    removeCustomizations();
                } finally {
                    cursor.hide();
                }
            }
        };
    }

    private void removeCustomizations() {
        Cls cls = (Cls) CollectionUtilities.getFirstItem(getSelection());
        Iterator i = cls.getSubclasses().iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            if (_project.hasCustomizedDescriptor(subclass)) {
                ClsWidget widget = getClsWidget(subclass);
                widget.removeCustomizations();
            }
        }
    }
}
