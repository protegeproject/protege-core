package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * A slot widget that allows the user to set the inverse slot for a given slot.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InverseSlotWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = 2385835722545274787L;
    private JList _list;
    private AllowableAction _viewAction;
    private AllowableAction _createAction;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;

    private FrameListener _frameListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            updateWidget();
        }
    };

    private Slot createInverseSlot() {
        Slot inverseSlot = null;
        try {
            Slot forwardSlot = (Slot) getInstance();
            beginTransaction("create inverse slot for " + forwardSlot.getName(), (forwardSlot == null ? null : "inverse_of_" + forwardSlot.getName()));
            String slotName = "inverse_of_" + forwardSlot.getName();
            while (getKnowledgeBase().getFrame(slotName) != null) {
                slotName += "_";
            }
            Collection inverseSuperslots = getSuperslotInverses(forwardSlot);
            Cls metaSlot = getKnowledgeBase().getDefaultSlotMetaCls();
            inverseSlot = getKnowledgeBase().createSlot(slotName, metaSlot, inverseSuperslots, true);
            Collection range = forwardSlot.getAllowedClses();
            Collection domain = forwardSlot.getDirectDomain();
            inverseSlot.setAllowedClses(domain);
            setDomain(inverseSlot, range);
            inverseSlot.setAllowsMultipleValues(true);
            setInverseSlot(inverseSlot);
            commitTransaction();
        } catch (Exception e) {
        	rollbackTransaction();
			Log.getLogger().warning("Could not create inverse slot for: " + getInstance());
		}
        return inverseSlot;
    }

    private void setDomain(Slot slot, Collection domain) {
        Iterator i = domain.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.addDirectTemplateSlot(slot);
        }
    }

    private Collection getSuperslotInverses(Slot forwardSlot) {
        Collection superslotInverses = new LinkedHashSet();
        Iterator i = forwardSlot.getSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            Slot superslotInverse = superslot.getInverseSlot();
            if (superslotInverse != null) {
                superslotInverses.add(superslotInverse);
            }
        }
        return superslotInverses;
    }

    private JList createList() {
        JList list = ComponentFactory.createSingleItemList(getViewAction());
        list.setCellRenderer(FrameRenderer.createInstance());
        return list;
    }

    private Action getAddAction() {
        if (_addAction == null) {
            _addAction = new AddAction(ResourceKey.SLOT_ADD) {
                private static final long serialVersionUID = -1866653350636802271L;

                public void onAdd() {
                    selectSlotToAdd();
                }
            };
        }
        return _addAction;
    }

    private Action getCreateAction() {
        if (_createAction == null) {
            _createAction = new CreateAction(ResourceKey.SLOT_CREATE) {
                private static final long serialVersionUID = 8559254382775291576L;

                public void onCreate() {
                    Slot slot = createInverseSlot();
                    getProject().show(slot);
                }
            };
        }
        return _createAction;
    }

    protected Collection getPossibleInverses() {
        Collection possibleInverses = new ArrayList();
        Iterator i = getKnowledgeBase().getSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            ValueType type = slot.getValueType();
            Slot inverseSlot = slot.getInverseSlot();
            if (!slot.isSystem() && equals(type, ValueType.INSTANCE) && inverseSlot == null) {
                possibleInverses.add(slot);
            }
        }
        return possibleInverses;
    }

    private Action getRemoveAction() {
        if (_removeAction == null) {
            _removeAction = new RemoveAction(ResourceKey.SLOT_REMOVE, this) {
                private static final long serialVersionUID = -4572085279905671010L;

                public void onRemove(Object o) {
                    setInverseSlot(null);
                }
            };
        }
        return _removeAction;
    }

    public Collection getSelection() {
        return getValues();
    }

    public Collection getValues() {
        return ComponentUtilities.getListValues(_list);
    }

    private Action getViewAction() {
        if (_viewAction == null) {
            _viewAction = new ViewAction(ResourceKey.SLOT_VIEW, this) {
                private static final long serialVersionUID = 1522315576409668965L;

                public void onView(Object o) {
                    Slot slot = (Slot) o;
                    getProject().show(slot);
                }
            };
        }
        return _viewAction;
    }

    public void initialize() {
        _list = createList();
        LabeledComponent c = new LabeledComponent(getLabel(), _list);
        c.addHeaderButton(getViewAction());
        c.addHeaderButton(getCreateAction());
        c.addHeaderButton(getAddAction());
        c.addHeaderButton(getRemoveAction());
        if (isSlotAtCls()) {
            _createAction.setAllowed(false);
            _addAction.setAllowed(false);
            _removeAction.setAllowed(false);
        }
        add(c);
        setPreferredColumns(2);
        setPreferredRows(1);
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.INVERSE);
    }

    private void selectSlotToAdd() {
        Collection possibleInverses = getPossibleInverses();
        if (possibleInverses.isEmpty()) {
            String text = "There are no existing slots which can be used as an inverse.";
            ModalDialog.showMessageDialog(this, text);
        } else {
            Slot slot = DisplayUtilities.pickSlot(this, possibleInverses);
            if (slot != null) {
                setInverseSlot(slot);
            }
        }
    }

    public void setEditable(boolean b) {
        updateWidget();
    }

    public void setInstance(Instance newInstance) {
        Instance oldInstance = getInstance();
        if (oldInstance != null && !isSlotAtCls()) {
            oldInstance.removeFrameListener(_frameListener);
        }
        super.setInstance(newInstance);
        if (newInstance != null && !isSlotAtCls()) {
            newInstance.addFrameListener(_frameListener);
        }
    }

    private void setInverseSlot(Slot slot) {
        Collection values = CollectionUtilities.createCollection(slot);
        ComponentUtilities.setListValues(_list, values);
        valueChanged();
    }

    public void setValues(Collection c) {
        ComponentUtilities.setListValues(_list, c);
    }

    private void updateWidget() {
        Instance instance = getInstance();
        boolean editable = !isSlotAtCls() && instance.isEditable();
        if (editable && instance instanceof Slot) {
            ValueType type = ((Slot) instance).getValueType();
            editable = equals(type, ValueType.INSTANCE) || equals(type, ValueType.CLS);
        }
        _createAction.setAllowed(editable);
        _addAction.setAllowed(editable);
        _removeAction.setAllowed(editable);

    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Inverse Slot", ResourceKey.INVERSE_SLOT_WIDGET_LABEL);
    }
}
