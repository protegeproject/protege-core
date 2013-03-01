package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.Transaction;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.DeleteAction;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.SuperslotTraverser;
import edu.stanford.smi.protege.util.TreePopupMenuMouseListener;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protege.util.WaitCursor;

/**
 * This component displays the superslot/subslot tree on the slots tab.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SubslotPane extends SelectableContainer {
    private static final long serialVersionUID = 7779648620738685152L;
    private Project _project;
    private KnowledgeBase _knowledgeBase;
    private Action _createSlotAction;
    private Action _createSubslotAction;
    private Action _deleteSlotAction;
    private Action _viewSlotAction;
    private LabeledComponent _labeledComponent;

    private static final int MAX_EXPANSIONS = 100;

    public SubslotPane(Project p) {
        _project = p;
        _knowledgeBase = _project.getKnowledgeBase();
        _createSlotAction = getCreateAction();
        _deleteSlotAction = getDeleteAction();
        _createSubslotAction = getCreateSubslotAction();
        _viewSlotAction = getViewAction();

        LazyTreeRoot root = createRoot(_knowledgeBase);
        SelectableTree tree = ComponentFactory.createSelectableTree(_viewSlotAction, root);
        tree.setCellRenderer(new SlotHierarchyRenderer());
        tree.setShowsRootHandles(true);
        tree.setSelectionRow(0);
        tree.setLargeModel(true);
        setSelectable(tree);
        setLayout(new BorderLayout());
        String slotHierarchyLabel = LocalizedText.getText(ResourceKey.SLOT_BROWSER_HIERARCHY_LABEL);
        _labeledComponent = new LabeledComponent(slotHierarchyLabel, ComponentFactory.createScrollPane(tree));
        _labeledComponent.setBorder(ComponentUtilities.getAlignBorder());
        addButtons(_labeledComponent);
        add(createHeader(), BorderLayout.NORTH);
        add(_labeledComponent, BorderLayout.CENTER);
        add(new SlotsTreeFinder(_knowledgeBase, tree), BorderLayout.SOUTH);
        tree.addMouseListener(new TreePopupMenuMouseListener(tree) {
            @Override
			public JPopupMenu getPopupMenu() {
                return SubslotPane.this.getPopupMenu();
            }
        });
        setupDragAndDrop();
        // Necessary because the actions don't get notified when the tree is initialized.
        _viewSlotAction.setEnabled(true);
        _deleteSlotAction.setEnabled(true);
    }

    public LabeledComponent getLabeledComponent() {
        return _labeledComponent;
    }

    private JComponent createHeader() {
        JLabel label = ComponentFactory.createLabel(Icons.getProjectIcon());
        label.setText(_project.getName());
        String slotBrowserLabel = LocalizedText.getText(ResourceKey.SLOT_BROWSER_TITLE);
        String forProjectLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
        HeaderComponent header = new HeaderComponent(slotBrowserLabel, forProjectLabel, label);
        header.setColor(Colors.getSlotColor());
        return header;
    }

    //ESCA-JAVA0130 
    protected LazyTreeRoot createRoot(KnowledgeBase kb) {
        return new SlotSubslotRoot(kb);
    }

    protected void addButtons(LabeledComponent c) {
        c.addHeaderButton(_viewSlotAction);
        c.addHeaderButton(_createSlotAction);
        c.addHeaderButton(_deleteSlotAction);
    }

    protected Action createCollapseAllAction() {
        return new AbstractAction("Collapse") {
            private static final long serialVersionUID = 1348386770476732031L;

            public void actionPerformed(ActionEvent event) {
                ComponentUtilities.fullSelectionCollapse(getTree());
            }
        };
    }

    protected Action createExpandAllAction() {
        return new AbstractAction("Expand") {
            private static final long serialVersionUID = -4254186270874090400L;

            public void actionPerformed(ActionEvent event) {
                ComponentUtilities.fullSelectionExpand(getTree(), MAX_EXPANSIONS);
            }
        };
    }

    public void extendSelection(Slot slot) {
        ComponentUtilities.extendSelection(getTree(), slot);
    }

    protected Action getChangeSlotMetaclassAction(final Slot slot) {
        Cls rootMetaclass = _knowledgeBase.getRootSlotMetaCls();
        final Collection c = CollectionUtilities.createCollection(rootMetaclass);
        boolean hasMultipleMetaclasses = DisplayUtilities.hasMultipleConcreteClses(_knowledgeBase, c);

        Action action = new AbstractAction("Change slot metaclass...") {
            private static final long serialVersionUID = 1869916325108349252L;

            public void actionPerformed(ActionEvent event) {
                Cls metaclass = pickConcreteCls(c, "Select Slot Metaclass");
                if (metaclass != null && !metaclass.equals(slot.getDirectType())) {
                    slot.setDirectType(metaclass);
                }
            }
        };
        action.setEnabled(hasMultipleMetaclasses && slot.isEditable());
        return action;
    }

    protected Cls pickConcreteCls(Collection allowedClses, String text) {
        return DisplayUtilities.pickConcreteCls(this, _knowledgeBase, allowedClses, text);
    }

    protected Action getChangeSubslotSlotMetaclassAction(final Slot slot) {
        Cls rootMetaclass = _knowledgeBase.getRootSlotMetaCls();
        Collection c = CollectionUtilities.createCollection(rootMetaclass);
        boolean hasMultipleMetaclasses = DisplayUtilities.hasMultipleConcreteClses(_knowledgeBase, c);

        Action action = new AbstractAction("Change slot metaclass of subslots") {
            private static final long serialVersionUID = -5279771465147048253L;

            public void actionPerformed(ActionEvent event) {
                Cls metaCls = slot.getDirectType();
                String text = "Change slot metaclass of all subslots of ";
                text += slot.getName();
                text += " to " + metaCls.getName();
                int result = ModalDialog.showMessageDialog(SubslotPane.this, text, ModalDialog.MODE_OK_CANCEL);
                if (result == ModalDialog.OPTION_OK) {
                    WaitCursor waitCursor = new WaitCursor(SubslotPane.this);
                    try {
                        slot.setDirectTypeOfSubslots(metaCls);
                    } finally {
                        waitCursor.hide();
                    }
                }
            }
        };
        boolean enabled = slot.isEditable() && hasMultipleMetaclasses && slot.getDirectSubslotCount() >= 1;
        action.setEnabled(enabled);
        return action;
    }

    protected Action getCreateAction() {
        return new CreateAction(ResourceKey.SLOT_CREATE) {
            private static final long serialVersionUID = -1623934054467544637L;

            @Override
			public void onCreate() {
                Transaction<Slot> t = new Transaction<Slot>(_knowledgeBase, "Create Slot (random name)") {
                    private Slot slot;
                    
                    @Override
                    public boolean doOperations() {
                        slot = _knowledgeBase.createSlot(null);
                        return true;
                    }
                    
                    @Override
					public Slot getResult() {
                        return slot;
                    }
                };
                t.execute();
                setSelectedSlot(t.getResult());
            }
        };
    }

    protected Action getCreateSlotWithSlotMetaclassAction() {
        AbstractAction action = new AbstractAction("Create subslot using slot metaclass...") {
            private static final long serialVersionUID = 3598104978460935980L;

            public void actionPerformed(ActionEvent event) {
                Cls rootMetaCls = _knowledgeBase.getRootSlotMetaCls();
                Collection roots = CollectionUtilities.createCollection(rootMetaCls);
                Cls metaCls = pickConcreteCls(roots, "Select Slot Metaclass");
                Collection parents = SubslotPane.this.getSelection();
                if (metaCls != null && !parents.isEmpty()) {
                    Slot slot = _knowledgeBase.createSlot(null, metaCls, parents, true);
                    extendSelection(slot);
                }
            }
        };
        boolean enabled = hasMultipleConcreteSlotMetaClses();
        action.setEnabled(enabled);
        return action;
    }

    protected Action getCreateSubslotAction() {
        return new CreateAction(ResourceKey.SLOT_CREATE_SUBSLOT) {
            private static final long serialVersionUID = -2902180791403135103L;

            @Override
			public void onCreate() {
                // SystemUtilities.debugBreak();
                Collection superslots = SubslotPane.this.getSelection();
                Slot firstSuperslot = (Slot) CollectionUtilities.getFirstItem(superslots);
                if (firstSuperslot != null) {
                	try {
                        _knowledgeBase.beginTransaction("Create subslot of " + superslots);
                        Cls metaCls = firstSuperslot.getDirectType();
                        Slot slot = _knowledgeBase.createSlot(null, metaCls, superslots, true);
                        createInverseSlot(slot, superslots);
                        _knowledgeBase.commitTransaction();
                        extendSelection(slot);
					} catch (Exception e) {
						_knowledgeBase.rollbackTransaction();					
						Log.getLogger().warning("Error at creating subslot of " + firstSuperslot);
					}                    
                }
            }
        };
    }

    private void createInverseSlot(Slot slot, Collection superslots) {
        Collection superInverses = new ArrayList();
        Cls metaCls = null;
        Iterator i = superslots.iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            Slot inverse = superslot.getInverseSlot();
            if (inverse != null) {
                superInverses.add(inverse);
                if (metaCls == null) {
                    metaCls = inverse.getDirectType();
                }
            }
        }
        if (!superInverses.isEmpty()) {
            Slot inverse = _knowledgeBase.createSlot("inverse_of_" + slot.getName(), metaCls, superInverses, true);
            slot.setInverseSlot(inverse);
        }
    }

    protected Action getDeleteAction() {
        return new DeleteAction(ResourceKey.SLOT_DELETE, this) {
            private static final long serialVersionUID = 4967576578770750208L;

            @Override
			public void onDelete(Collection slots) {
                handleDelete(slots);
            }

            @Override
			public void onSelectionChange() {
                Slot slot = (Slot) CollectionUtilities.getFirstItem(this.getSelection());
                if (slot != null) {
                    setAllowed(slot.isEditable());
                }
            }
        };
    }

    protected void handleDelete(Collection slots) {
        removeSelection();
        try {
            _knowledgeBase.beginTransaction("Delete slots " + slots);
            Iterator i = slots.iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                _knowledgeBase.deleteSlot(slot);
            }
            _knowledgeBase.commitTransaction();
        } catch (Exception e) {
        	_knowledgeBase.rollbackTransaction();
        	Log.getLogger().warning("Error at deleting slots " + slots);
		}
    }

    public Slot getDisplayParent() {
        Slot slot = null;
        TreePath childPath = getTree().getSelectionModel().getLeadSelectionPath();
        if (childPath != null) {
            TreePath path = childPath.getParentPath();
            if (path != null) {
            	LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
            	Object o = node.getUserObject();
            	slot = (o instanceof Slot) ? (Slot) o : null;
            }
        }
        return slot;
    }

    public JTree getDropComponent() {
        return getTree();
    }

    public List getPath(Slot slot, List list) {
        list.add(0, slot);
        Slot superslot = (Slot) CollectionUtilities.getFirstItem(slot.getDirectSuperslots());
        if (superslot != null) {
            getPath(superslot, list);
        }
        return list;
    }

    protected JPopupMenu getPopupMenu() {
        JPopupMenu menu = null;
        Collection selection = getSelection();
        if (selection.size() == 1) {
            Slot slot = (Slot) CollectionUtilities.getFirstItem(selection);
            menu = new JPopupMenu();
            menu.add(_createSlotAction);
            menu.add(_createSubslotAction);
            menu.add(getCreateSlotWithSlotMetaclassAction());
            menu.add(_deleteSlotAction);
            menu.addSeparator();
            menu.add(getChangeSlotMetaclassAction(slot));
            menu.add(getChangeSubslotSlotMetaclassAction(slot));
            menu.addSeparator();
            menu.add(createExpandAllAction());
            menu.add(createCollapseAllAction());
        }
        return menu;
    }

    private JTree getTree() {
        return (JTree) getSelectable();
    }

    protected Action getViewAction() {
        return new ViewAction(ResourceKey.SLOT_VIEW, this) {
            private static final long serialVersionUID = -8304507739111498105L;

            @Override
			public void onView(Object o) {
                _project.show((Slot) o);
            }
        };
    }

    private boolean hasMultipleConcreteSlotMetaClses() {
        int nConcrete = 0;
        Collection metaClses = _knowledgeBase.getRootSlotMetaCls().getSubclasses();
        Iterator i = metaClses.iterator();
        while (i.hasNext() && nConcrete < 2) {
            Cls cls = (Cls) i.next();
            if (cls.isConcrete()) {
                ++nConcrete;
            }
        }
        return nConcrete > 1;
    }

    public void removeSelection() {
        ComponentUtilities.removeSelection(getTree());
    }

    public void setExpandedSlot(Slot slot, boolean expanded) {
        ComponentUtilities.setExpanded(getTree(), getPath(slot, new LinkedList()), expanded);
    }

    public void setFinderComponent(JComponent c) {
        add(c, BorderLayout.SOUTH);
    }

    public void setRenderer(DefaultRenderer renderer) {
        getTree().setCellRenderer(renderer);
    }

    public void setSelectedSlot(Slot slot) {
        if (!getSelection().contains(slot)) {
            ComponentUtilities.setSelectedObjectPath(getTree(), getPath(slot, new LinkedList()));
        }
    }

    private void setupDragAndDrop() {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(getTree(),
                DnDConstants.ACTION_COPY_OR_MOVE, new SlotsTreeDragSourceListener());
        new DropTarget(getTree(), DnDConstants.ACTION_COPY_OR_MOVE, new SlotsTreeTarget());
    }

    public void setDisplayParent(Slot slot) {
        ComponentUtilities.setDisplayParent(getTree(), slot, new SuperslotTraverser());
    }

    @Override
	public String toString() {
        return "SubclassPane";
    }
    
    @Override
    public void dispose() {
       	super.dispose();
    	_project = null;
    	_knowledgeBase = null;
    }
}
