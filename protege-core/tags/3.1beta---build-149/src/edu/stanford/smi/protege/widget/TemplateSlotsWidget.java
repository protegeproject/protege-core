package edu.stanford.smi.protege.widget;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for displaying the template slots.  We try to display as much facet information as possible.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TemplateSlotsWidget extends AbstractTableWidget {
    private AllowableAction _viewAction;
    private AllowableAction _viewAtClsAction;
    protected AllowableAction _createAction;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;
    private AllowableAction _removeOverrideAction;
    private Collection _currentClsTemplateSlots;

    private ClsListener _clsListener = new ClsAdapter() {
        public void templateSlotAdded(ClsEvent event) {
            Slot slot = event.getSlot();
            slot.addFrameListener(_slotListener);
            _currentClsTemplateSlots.add(slot);
            reload();
        }

        public void templateSlotRemoved(ClsEvent event) {
            Slot slot = event.getSlot();
            slot.removeFrameListener(_slotListener);
            _currentClsTemplateSlots.remove(slot);
            reload();
        }

        public void templateFacetValueChanged(ClsEvent event) {
            repaint();
        }
        public void directSuperclassAdded(ClsEvent event) {
            reload();
        }
        public void directSuperclassRemoved(ClsEvent event) {
            reload();
        }
    };
    private FrameListener _slotListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            repaint();
        }
    };

    private KnowledgeBaseListener _knowledgeBaseListener = new KnowledgeBaseAdapter() {
        public void frameNameChanged(KnowledgeBaseEvent event) {
            repaint();
        }
    };

    protected void addInheritedTemplateSlots(Collection slots, Cls cls) {
        // TODO we really want the superclasses breath first.  getSuperclasses() returns an unordered set instead
        Iterator i = cls.getSuperclasses().iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            addDirectTemplateSlots(slots, superclass);
        }
    }

    private void addSlots(Collection c) {
        Cls cls = getBoundCls();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            addDirectTemplateSlot(cls, slot);
        }
    }

    private void addDirectTemplateSlot(Cls cls, Slot slot) {
        cls.addDirectTemplateSlot(slot);
        // allow this "slot on a slot" to be overriden
        if (cls.isSlotMetaCls() && slot.getAssociatedFacet() == null) {
            Facet facet = getKnowledgeBase().createFacet(null);
            slot.setAssociatedFacet(facet);
        }
    }

    protected void addTemplateSlots(Collection slots, Cls cls) {
        // addDirectTemplateSlots(slots, cls);
        // addInheritedTemplateSlots(slots, cls);
        List templateSlots = new ArrayList(cls.getVisibleTemplateSlots());
        Collections.sort(templateSlots);
        slots.addAll(templateSlots);
    }

    private void addDirectTemplateSlots(Collection slots, Cls cls) {
        Iterator i = getDirectTemplateSlots(cls).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (!slots.contains(slot)) {
                slots.add(slot);
            }
        }
    }

    private Collection getDirectTemplateSlots(Cls cls) {
        return cls.getDirectTemplateSlots();
        // return cls.getOwnSlotValues(getSlot());
    }

    private void changeSlotIndex(Slot slot, int delta) {
        List slots = (List) getBoundCls().getDirectTemplateSlots();
        int oldIndex = slots.indexOf(slot);
        int newIndex = oldIndex + delta;
        if (0 <= newIndex && newIndex < slots.size()) {
            getBoundCls().moveDirectTemplateSlot(slot, newIndex);
            reload();
        }
    }

    private Action createMoveDownAction() {
        return new AbstractAction("Move down", Icons.getDownIcon()) {
            {
                putValue(Action.SHORT_DESCRIPTION, "Move selected slot down");
            }
            public void actionPerformed(ActionEvent event) {
                handleMoveDownAction();
            }
        };
    }

    private Action createMoveUpAction() {
        return new AbstractAction("Move up", Icons.getUpIcon()) {
            {
                putValue(Action.SHORT_DESCRIPTION, "Move selected slot up");
            }
            public void actionPerformed(ActionEvent event) {
                handleMoveUpAction();
            }
        };
    }

    public TableModel createTableModel() {
        List slots;
        Cls cls = (Cls) getInstance();
        if (cls == null) {
            slots = Collections.EMPTY_LIST;
        } else {
            slots = getSlots(cls);
        }
        RowTableModel model = new RowTableModel(getTable());
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            FrameSlotCombination o = new FrameSlotCombination(cls, slot);
            model.addRow(o);
        }
        return model;
    }

    public void dispose() {
        super.dispose();
        getKnowledgeBase().removeKnowledgeBaseListener(_knowledgeBaseListener);
        Cls cls = (Cls) getInstance();
        if (cls != null) {
            cls.removeClsListener(_clsListener);
        }
    }

    public Action getAddSlotsAction() {
        _addAction = new AddAction(ResourceKey.SLOT_ADD) {
            public void onAdd() {
                Cls slotMetaCls = getBaseAllowedSlotMetaCls();
                List slots = new ArrayList(getKnowledgeBase().getInstances(slotMetaCls));
                slots.removeAll(getBoundCls().getTemplateSlots());
                String label = "Select " + getLabel();
                addSlots(DisplayUtilities.pickSlots(TemplateSlotsWidget.this, slots, label));
            }
        };
        return _addAction;
    }

    private Cls getBaseAllowedSlotMetaCls() {
        Collection allowedClses = getBoundCls().getDirectType().getTemplateSlotAllowedClses(getSlot());
        return (Cls) CollectionUtilities.getFirstItem(allowedClses);
    }

    protected Cls getBaseSlotMetaCls() {
        Cls baseSlotMetaCls;
        Cls kbSlotMetaCls = getKnowledgeBase().getDefaultSlotMetaCls();
        Cls facetSlotMetaCls = getBaseAllowedSlotMetaCls();
        if (kbSlotMetaCls.hasSuperclass(facetSlotMetaCls)) {
            baseSlotMetaCls = kbSlotMetaCls;
        } else {
            baseSlotMetaCls = facetSlotMetaCls;
        }
        return baseSlotMetaCls;
    }

    private Cls getBoundCls() {
        return (Cls) getInstance();
    }

    protected Action getCreateSlotAction() {
        _createAction = new CreateAction(ResourceKey.SLOT_CREATE) {
            public void onCreate() {
                Cls cls = getBoundCls();
                if (cls.isEditable()) {
                    KnowledgeBase kb = getKnowledgeBase();
                    Cls slotMetaCls = kb.getDefaultSlotMetaCls();
                    Cls baseSlotMetaCls = getBaseSlotMetaCls();
                    if (!baseSlotMetaCls.equals(slotMetaCls)) {
                        Collection allowedClses = cls.getDirectType().getTemplateSlotAllowedClses(getSlot());
                        slotMetaCls =
                            DisplayUtilities.pickConcreteCls(
                                TemplateSlotsWidget.this,
                                getKnowledgeBase(),
                                allowedClses);
                    }
                    if (slotMetaCls != null) {
                        Slot slot = getKnowledgeBase().createSlot(null, slotMetaCls);
                        addDirectTemplateSlot(cls, slot);
                        showInstance(slot);
                    }
                }
            }
        };
        return _createAction;
    }

    public Action getDoubleClickAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                FrameSlotCombination c = (FrameSlotCombination) CollectionUtilities.getFirstItem(getSelection());
                if (c != null) {
                    SlotViewPanel panel = new SlotViewPanel();
                    int result =
                        ModalDialog.showDialog(
                            TemplateSlotsWidget.this,
                            panel,
                            "Select Slot View",
                            ModalDialog.MODE_OK_CANCEL);
                    if (result == ModalDialog.OPTION_OK) {
                        if (panel.viewTopLevelSlot()) {
                            _viewAction.actionPerformed(event);
                        } else {
                            _viewAtClsAction.actionPerformed(event);
                        }
                    }
                }
            }
        };
    }

    //    private int getInitialMaxWidth() {
    //        int tableWidth = getTable().getWidth();
    //        int viewPortWidth = getWidth() - (getInsets().left + getInsets().right + 3);
    //        int currentWidth = getTable().getColumnModel().getColumn(4).getWidth();
    //        int initialWidth = currentWidth + (viewPortWidth - tableWidth);
    //        return initialWidth;
    //    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(_viewAction);
        menu.add(_viewAtClsAction);
        menu.add(_createAction);
        menu.add(_addAction);
        menu.add(_removeAction);
        return menu;
    }

    public Action getRemoveOverrideAction() {
        _removeOverrideAction = new AllowableAction(ResourceKey.SLOT_REMOVE_FACET_OVERRIDES, this) {
            public void actionPerformed(ActionEvent event) {
                Iterator i = this.getSelection().iterator();
                while (i.hasNext()) {
                    FrameSlotCombination pair = (FrameSlotCombination) i.next();
                    ((Cls) pair.getFrame()).removeTemplateFacetOverrides(pair.getSlot());
                }
            }
        };
        return _removeOverrideAction;
    }

    public Action getRemoveSlotsAction() {
        _removeAction = new RemoveAction(ResourceKey.SLOT_REMOVE, this) {
            public void onRemove(Collection combinations) {
                handleRemoveCombinations(combinations);
            }

            public void onSelectionChange() {
                Object o = CollectionUtilities.getFirstItem(this.getSelection());
                FrameSlotCombination combination = (FrameSlotCombination) o;
                Slot slot = (combination == null) ? (Slot) null : combination.getSlot();
                setAllowed(slot != null && getBoundCls().hasDirectTemplateSlot(slot));
            }
        };
        return _removeAction;
    }

    protected void handleRemoveCombinations(Collection combinations) {
        try {
            beginTransaction("Remove slots from " + getCls());
            Iterator i = combinations.iterator();
            while (i.hasNext()) {
                FrameSlotCombination combination = (FrameSlotCombination) i.next();
                handleRemoveCombination(combination);
            }
        } finally {
            endTransaction();
        }
    }

    private void handleRemoveCombination(FrameSlotCombination combination) {
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();
        cls.removeDirectTemplateSlot(slot);
    }
    private Slot getSelectedDirectSlot() {
        Slot slot = getSelectedSlot();
        boolean isDirect = getBoundCls().hasDirectTemplateSlot(slot);
        return isDirect ? slot : null;
    }

    private Slot getSelectedSlot() {
        Slot slot = null;
        Collection c = getSelection();
        if (c.size() == 1) {
            FrameSlotCombination combo = (FrameSlotCombination) CollectionUtilities.getFirstItem(c);
            slot = combo.getSlot();
        }
        return slot;

    }

    protected List getSlots(Cls cls) {
        List slots = new ArrayList();
        addTemplateSlots(slots, cls);

        return slots;
    }

    private Action getViewSlotAction() {
        _viewAction = new ViewAction(ResourceKey.SLOT_VIEW_TOP_LEVEL, this) {
            public void onView(Object o) {
                FrameSlotCombination combination = (FrameSlotCombination) o;
                showInstance(combination.getSlot());
            }
        };
        return _viewAction;
    }

    private Action getViewSlotAtClassAction() {
        _viewAtClsAction = new ViewAction(ResourceKey.SLOT_VIEW_FACET_OVERRIDES, this) {
            public void onView(Object o) {
                FrameSlotCombination combination = (FrameSlotCombination) o;
                show((Cls) combination.getFrame(), combination.getSlot());
            }
        };
        return _viewAtClsAction;
    }

    protected void handleMoveDownAction() {
        Slot slot = getSelectedDirectSlot();
        if (slot != null) {
            changeSlotIndex(slot, +1);
        }
    }

    protected void handleMoveUpAction() {
        Slot slot = getSelectedDirectSlot();
        if (slot != null) {
            changeSlotIndex(slot, -1);
        }
    }

    public void initialize() {
        Action viewSlotAction = getViewSlotAction();
        super.initialize(getDoubleClickAction());

        addButton(viewSlotAction);
        addButton(getViewSlotAtClassAction());
        addButton(getCreateSlotAction());
        addButton(getRemoveOverrideAction());
        addButton(getAddSlotsAction());
        addButton(getRemoveSlotsAction());
        addButton(createMoveUpAction(), false);
        addButton(createMoveDownAction(), false);

        addColumn(200, ResourceKey.TEMPLATE_SLOTS_SLOT_WIDGET_NAME, SlotPairRenderer.createInstance());
        addColumn(80, ResourceKey.TEMPLATE_SLOTS_SLOT_WIDGET_CARDINALITY, new CardinalityFacetRenderer(getKnowledgeBase()));
        addColumn(200, ResourceKey.TEMPLATE_SLOTS_SLOT_WIDGET_TYPE, new TypeFacetRenderer());
        addColumn(400, ResourceKey.TEMPLATE_SLOTS_SLOT_WIDGET_OTHER_FACETS, new OtherFacetsRenderer());
        getTable().setAutoCreateColumnsFromModel(false);
        getKnowledgeBase().addKnowledgeBaseListener(_knowledgeBaseListener);
    }
    
    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isMetacls = cls.isClsMetaCls();
        Slot templateSlotsSlot = slot.getKnowledgeBase().getSlot(Model.Slot.DIRECT_TEMPLATE_SLOTS);
        return isMetacls && (slot.equals(templateSlotsSlot) || slot.hasSuperslot(templateSlotsSlot));
    }

    /**
     * @deprecated
     */
    //    public void reshape(int x, int y, int w, int h) {
    //        super.reshape(x, y, w, h);
    //        setOtherFacetsWidth();
    //    }

    public void setEditable(boolean b) {
        _createAction.setAllowed(b);
        _addAction.setAllowed(b);
        _removeAction.setAllowed(b);
        _removeOverrideAction.setAllowed(b);
    }

    public void setInstance(Instance instance) {
        Cls currentCls = (Cls) getInstance();
        if (currentCls != null) {
            currentCls.removeClsListener(_clsListener);
            Iterator i = _currentClsTemplateSlots.iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                slot.removeFrameListener(this._slotListener);
            }
        }
        super.setInstance(instance);
        if (instance != null) {
            Cls cls = (Cls) instance;
            cls.addClsListener(_clsListener);
            _currentClsTemplateSlots = new ArrayList(cls.getTemplateSlots());
            Iterator i = _currentClsTemplateSlots.iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                slot.addFrameListener(this._slotListener);
            }
        }
    }

    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Template Slots", ResourceKey.TEMPLATE_SLOTS_SLOT_WIDGET_LABEL);
    }
    
    public String toString() {
        return "TemplateSlotsWidget";
    }
}
