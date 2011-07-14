package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.util.*;

/**
 * Convenience base class for slot widgets that contain a JTree.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractTreeWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -1864634171376661448L;
    private JTree _tree;
    private LabeledComponent _labeledComponent;

    public AbstractTreeWidget() {
        setPreferredColumns(2);
        setPreferredRows(6);
    }

    public void addButton(Action action) {
        _labeledComponent.addHeaderButton(action);
    }

    private JComponent createMainComponent() {
        _labeledComponent = new LabeledComponent(getLabel(), ComponentFactory.createScrollPane(createTree()));
        return _labeledComponent;
    }

    public abstract LazyTreeRoot createRoot();

    private JComponent createTree() {
        _tree = ComponentFactory.createTree(null);
        _tree.addTreeSelectionListener(new TreeSelectionListenerAdapter(this));
        _tree.addMouseListener(new TreePopupMenuMouseListener(_tree) {
            public JPopupMenu getPopupMenu() {
                return AbstractTreeWidget.this.getPopupMenu();
            }
        });

        return _tree;
    }

    public void dispose() {
        super.dispose();
        LazyTreeModel model = (LazyTreeModel) _tree.getModel();
        model.dispose();
    }

    public void expandAll() {
        // TODO
    }

    public void expandRoot() {
        TreeNode root = (TreeNode) _tree.getModel().getRoot();
        if (root.getChildCount() == 1) {
            TreeNode child = root.getChildAt(0);
            TreePath path = new TreePath(new Object[] { root, child });
            _tree.expandPath(path);
        }
    }

    public Object getFirstSelectionParent() {
        return ComponentUtilities.getFirstSelectionParent(_tree);
    }

    protected JPopupMenu getPopupMenu() {
        return null;
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_tree);
    }

    public JTree getTree() {
        return _tree;
    }

    public void initialize() {
        add(createMainComponent());
        reload();
    }

    public void reload() {
        TreeModel model = new LazyTreeModel(createRoot());
        _tree.setModel(model);
        _tree.setSelectionRow(0);
        notifySelectionListeners();
        expandRoot();
    }

    public static void setDisplayParent(JTree tree, Object parent) {
        ComponentUtilities.setDisplayParent(tree, parent, new SuperclassTraverser());
    }

    public static void setDisplayParent(JTree tree, Object parent, Object child) {
        ComponentUtilities.setDisplayParent(tree, parent, child, new SuperclassTraverser());
    }

    public void setFooter(JComponent c) {
        _labeledComponent.setFooterComponent(c);
    }

    public void setHeaderComponent(JComponent c) {
        _labeledComponent.setHeaderComponent(c);
    }

    public void setRenderer(TreeCellRenderer renderer) {
        _tree.setCellRenderer(renderer);
    }

    public void setSelectedObjectPath(Collection objectPath) {
        ComponentUtilities.setSelectedObjectPath(_tree, objectPath);
        notifySelectionListeners();
    }
}
