package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.StandardAction;
import edu.stanford.smi.protege.util.SuperclassTraverser;
import edu.stanford.smi.protege.util.TreePopupMenuMouseListener;
import edu.stanford.smi.protege.util.WaitCursor;

/**
 * This component displays the superclass/subclass tree on the classes tab.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SubclassPane extends SelectableContainer {
    private static final long serialVersionUID = 5266489572482981600L;
    private KnowledgeBase _knowledgeBase;
    private Action _createClsAction;
    private Action _deleteClsAction;

    private static final int MAX_EXPANSIONS = 1000;

    public SubclassPane(Action doubleClickAction, Cls rootCls, Action createCls, Action deleteCls) {
        _knowledgeBase = rootCls.getKnowledgeBase();
        _createClsAction = createCls;
        _deleteClsAction = deleteCls;
        SelectableTree tree = createSelectableTree(doubleClickAction, rootCls);
        tree.setLargeModel(true);
        tree.setSelectionRow(0);
        tree.setAutoscrolls(true);
        setSelectable(tree);
        setLayout(new BorderLayout());
        JScrollPane pane = ComponentFactory.createScrollPane(tree);
        add(pane, BorderLayout.CENTER);
        add(new ClsTreeFinder(_knowledgeBase, tree), BorderLayout.SOUTH);
        setupDragAndDrop();
        getTree().setCellRenderer(FrameRenderer.createInstance());
        getTree().addMouseListener(new TreePopupMenuMouseListener(tree) {
            @Override
			public JPopupMenu getPopupMenu() {
                return SubclassPane.this.getPopupMenu();
            }
        });
    }

    //ESCA-JAVA0130 
    protected SelectableTree createSelectableTree(Action doubleClickAction, Cls rootCls) {
        return ComponentFactory.createSelectableTree(doubleClickAction, new ParentChildRoot(rootCls));
    }

    private Action createCollapseAllAction() {
        return new StandardAction(ResourceKey.CLASS_BROWSER_COLLAPSE_TREE_MENU_ITEM) {
            private static final long serialVersionUID = -5294993636227782853L;

            public void actionPerformed(ActionEvent event) {
                ComponentUtilities.fullSelectionCollapse(getTree());
            }
        };
    }

    private Action createExpandAllAction() {
        return new StandardAction(ResourceKey.CLASS_BROWSER_EXPAND_TREE_MENU_ITEM) {
            private static final long serialVersionUID = -3462333994306988331L;

            public void actionPerformed(ActionEvent event) {
                ComponentUtilities.fullSelectionExpand(getTree(), MAX_EXPANSIONS);
            }
        };
    }

    protected JPopupMenu createPopupMenu() {
        JPopupMenu menu = null;
        if (!getSelection().isEmpty()) {
            menu = new JPopupMenu();
            add(menu, _createClsAction);
            add(menu, getCreateClsWithMetaClsAction());
            add(menu, _deleteClsAction);
            addSeparator(menu);
            add(menu, getChangeMetaclassAction());
            add(menu, getChangeSubclassMetaclassAction());
            addSeparator(menu);
            add(menu, getHideClsAction());
            addSeparator(menu);
            add(menu, createSetClsMetaClsAction());
            add(menu, createSetSlotMetaClsAction());
            addSeparator(menu);
            add(menu, createExpandAllAction());
            add(menu, createCollapseAllAction());
        }
        return menu;
    }

    private static void add(JPopupMenu menu, Action action) {
        menu.add(action);
    }

    private static void addSeparator(JPopupMenu menu) {
        // never add two separators in a row
        int count = menu.getComponentCount();
        Component c = menu.getComponent(count - 1);
        if (!(c instanceof JSeparator)) {
            menu.addSeparator();
        }
    }

    private Action createSetClsMetaClsAction() {
        final Cls cls = (Cls) getSoleSelection();
        AbstractAction action = new StandardAction(ResourceKey.CLASS_BROWSER_SET_AS_DEFAULT_METACLASS_MENU_ITEM) {
            private static final long serialVersionUID = -2802257939885292526L;

            public void actionPerformed(ActionEvent event) {
                _knowledgeBase.setDefaultClsMetaCls(cls);
                repaint();
            }
        };
        boolean enabled = cls != null && cls.isClsMetaCls() && !cls.isDefaultClsMetaCls() && cls.isConcrete();
        action.setEnabled(enabled);
        return action;
    }

    private Action createSetSlotMetaClsAction() {
        final Cls cls = (Cls) getSoleSelection();
        final boolean isDefault = cls != null && cls.isDefaultSlotMetaCls();
        ResourceKey key = isDefault ? ResourceKey.CLASS_BROWSER_UNSET_AS_DEFAULT_METASLOT_MENU_ITEM
                : ResourceKey.CLASS_BROWSER_SET_AS_DEFAULT_METASLOT_MENU_ITEM;
        AbstractAction action = new StandardAction(key) {
            private static final long serialVersionUID = 7802992328694807093L;

            public void actionPerformed(ActionEvent event) {
                _knowledgeBase.setDefaultSlotMetaCls(isDefault ? null : cls);
                repaint();
            }
        };
        boolean enabled = isDefault || (cls != null && cls.isSlotMetaCls() && cls.isConcrete());
        action.setEnabled(enabled);
        return action;
    }

    public void extendSelection(Cls cls) {
        ComponentUtilities.extendSelection(getTree(), cls);
    }

    private Action getChangeMetaclassAction() {
        Action action = new StandardAction(ResourceKey.CLASS_BROWSER_CHANGE_METACLASS_MENU_ITEM) {
            private static final long serialVersionUID = 1762699423996754665L;

            public void actionPerformed(ActionEvent event) {
                Collection clsMetaClses = CollectionUtilities.createCollection(_knowledgeBase.getRootClsMetaCls());
                Cls metaclass = pickConcreteCls(clsMetaClses, "Select Concrete Metaclass");
                if (metaclass != null) {
                    Iterator i = getSelection().iterator();
                    while (i.hasNext()) {
                        Cls cls = (Cls) i.next();
                        if (!metaclass.equals(cls.getDirectType())) {
                            cls.setDirectType(metaclass);
                        }
                    }
                }
            }
        };
        action.setEnabled(canChangeMetaCls());
        return action;
    }

    private boolean canChangeMetaCls() {
        Cls rootMetaclass = _knowledgeBase.getRootClsMetaCls();
        final Collection c = CollectionUtilities.createCollection(rootMetaclass);
        boolean hasMultipleMetaclasses = DisplayUtilities.hasMultipleConcreteClses(_knowledgeBase, c);

        return hasMultipleMetaclasses && selectionIsEditable();
    }

    private boolean selectionIsEditable() {
        boolean isEditable = true;
        Iterator i = getSelection().iterator();
        while (i.hasNext() && isEditable) {
            Frame frame = (Frame) i.next();
            isEditable = frame.isEditable();
        }
        return isEditable;
    }

    protected Cls pickConcreteCls(Collection allowedClses, String text) {
        return DisplayUtilities.pickConcreteCls(this, _knowledgeBase, allowedClses, text);
    }

    private Action getChangeSubclassMetaclassAction() {
        Cls rootMetaclass = _knowledgeBase.getRootClsMetaCls();
        Collection c = CollectionUtilities.createCollection(rootMetaclass);
        boolean hasMultipleMetaclasses = DisplayUtilities.hasMultipleConcreteClses(_knowledgeBase, c);

        final Cls cls = (Cls) getSoleSelection();
        Action action = new StandardAction(ResourceKey.CLASS_BROWSER_CHANGE_METACLASS_OF_SUBCLASSES_MENU_ITEM) {
            private static final long serialVersionUID = -1533419814130384712L;

            public void actionPerformed(ActionEvent event) {
                Cls metaCls = cls.getDirectType();
                String text = "Change metaclass of all subclasses of ";
                text += cls.getName();
                text += " to " + metaCls.getName();
                int result = ModalDialog.showMessageDialog(SubclassPane.this, text, ModalDialog.MODE_OK_CANCEL);
                if (result == ModalDialog.OPTION_OK) {
                    WaitCursor waitCursor = new WaitCursor(SubclassPane.this);
                    try {
                        cls.setDirectTypeOfSubclasses(metaCls);
                    } finally {
                        waitCursor.hide();
                    }
                }
            }
        };
        boolean enabled = cls != null && hasMultipleMetaclasses && cls.getDirectSubclassCount() >= 1;
        action.setEnabled(enabled);
        return action;
    }

    private Action getCreateClsWithMetaClsAction() {
        AbstractAction action = new StandardAction(ResourceKey.CLASS_BROWSER_CREATE_SUBCLASS_USING_METACLASS_MENU_ITEM) {
            private static final long serialVersionUID = 1596972416401713147L;

            public void actionPerformed(ActionEvent event) {
                Cls rootMetaCls = _knowledgeBase.getRootClsMetaCls();
                Collection roots = CollectionUtilities.createCollection(rootMetaCls);
                Cls metaCls = pickConcreteCls(roots, "Select Metaclass");
                Collection parents = getSelection();
                if (metaCls != null && !parents.isEmpty()) {
                    Cls cls = _knowledgeBase.createCls(null, parents, metaCls);
                    extendSelection(cls);
                }
            }
        };
        boolean enabled = hasMultipleConcreteClsMetaClses();
        action.setEnabled(enabled);
        return action;
    }

    public Cls getDisplayParent() {
        TreePath path = getTree().getSelectionModel().getLeadSelectionPath().getParentPath();
        LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
        Object o = node.getUserObject();
        return (o instanceof Cls) ? (Cls) o : null;
    }

    public JComponent getDropComponent() {
        return getTree();
    }

    private Action getHideClsAction() {
        final Cls cls = (Cls) getSoleSelection();
        final boolean hide = cls == null || cls.isVisible();
        ResourceKey key = hide ? ResourceKey.CLASS_BROWSER_HIDE_CLASS_MENU_ITEM
                : ResourceKey.CLASS_BROWSER_UNHIDE_CLASS_MENU_ITEM;
        return new StandardAction(key) {
            private static final long serialVersionUID = -8865114599879876460L;

            public void actionPerformed(ActionEvent event) {
                cls.setVisible(!hide);
                repaint();
            }
        };
    }

    private JPopupMenu getPopupMenu() {
        return createPopupMenu();
    }

    private JTree getTree() {
        return (JTree) getSelectable();
    }

    private boolean hasMultipleConcreteClsMetaClses() {
        // Correct but slow
        /*
         * int nConcrete = 0; Collection metaClses =
         * _knowledgeBase.getRootClsMetaCls().getSubclasses(); Iterator i =
         * metaClses.iterator(); while (i.hasNext() && nConcrete < 2) { Cls cls =
         * (Cls) i.next(); if (cls.isConcrete()) { ++nConcrete; } } return
         * nConcrete > 1;
         */

        // Wrong but fast
        Cls standardCls = _knowledgeBase.getCls(Model.Cls.STANDARD_CLASS);
        return standardCls.getDirectSubclassCount() > 0;
    }

    public void removeSelection() {
        ComponentUtilities.removeSelection(getTree());
    }

    public void setExpandedCls(Cls cls, boolean expanded) {
        Collection path = ModelUtilities.getPathToRoot(cls);
        ComponentUtilities.setExpanded(getTree(), path, expanded);
    }

    public void setFinderComponent(JComponent c) {
        add(c, BorderLayout.SOUTH);
    }

    public void setRenderer(DefaultRenderer renderer) {
        getTree().setCellRenderer(renderer);
    }

    public void setSelectedCls(Cls cls) {
        if (!getSelection().contains(cls)) {
            Collection path = ModelUtilities.getPathToRoot(cls);
            ComponentUtilities.setSelectedObjectPath(getTree(), path);
        }
    }

    public void setSelectedClses(Collection clses) {
        Collection paths = new ArrayList();
        Iterator i = clses.iterator();
        while (i.hasNext()) {
        	Object o = i.next();
        	if (o instanceof Cls) {
        		Cls cls = (Cls) o;
        		paths.add(ModelUtilities.getPathToRoot(cls));
        	}
        }
        ComponentUtilities.setSelectedObjectPaths(getTree(), paths);
    }

    protected void setupDragAndDrop() {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(getTree(),
                DnDConstants.ACTION_COPY_OR_MOVE, new ClsesTreeDragSourceListener());
        new DropTarget(getTree(), DnDConstants.ACTION_COPY_OR_MOVE, new ClsesTreeTarget());
    }

    public void setDisplayParent(Cls cls) {
        ComponentUtilities.setDisplayParent(getTree(), cls, new SuperclassTraverser());
    }

    @Override
	public String toString() {
        return "SubclassPane";
    }
    
    @Override
    public void dispose() {
    	((SelectableTree)getSelectable()).setRoot(null);
    	((SelectableTree)getSelectable()).setModel(null);
    	_knowledgeBase = null;
    	super.dispose();
    }
}