package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * A slot widget that is capable of acquiring a value of any type.  The allowed type depends on the current setting of
 * the value-type facet.  This capability is needed for acquiring both template slot values and default values.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotValuesWidget extends AbstractListWidget {
    private static final long serialVersionUID = 103665542520867791L;
    private Action _viewAction;
    private AllowableAction _createAction;
    private AllowableAction _addAction;
    private Action _removeAction;

    private ClsListener _clsListener = new ClsAdapter() {
        public void templateFacetValueChanged(ClsEvent event) {
            super.templateFacetValueChanged(event);
            if (event.getFacet().getName().equals(Model.Facet.VALUE_TYPE)) {
                updateButtons();
            }
        }
    };
    private FrameListener _frameListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            if (event.getSlot().getName().equals(Model.Slot.VALUE_TYPE)) {
                updateButtons();
            }
        }
    };

    private Collection addItems() {
        Collection items = null;
        ValueType type = getCurrentType();
        if (equals(type, ValueType.CLS)) {
            Collection c = getAllowedParents();
            items = DisplayUtilities.pickClses(this, getKnowledgeBase(), c);
        } else if (equals(type, ValueType.INSTANCE)) {
            Collection c = getAllowedClses();
            items = DisplayUtilities.pickInstances(this, getKnowledgeBase(), c);
        } else if (equals(type, ValueType.SYMBOL)) {
            items = CollectionUtilities.createCollection(editSymbol(null));
        } else {
            Log.getLogger().warning("bad type: " + type);
        }
        return items;
    }

    private Boolean editBoolean(Object o) {
        Collection c = Arrays.asList(new Object[] { "true", "false" });
        String s = (o == null) ? (String) null : o.toString();
        s = editSymbol(s, c);
        return (s == null) ? null : new Boolean(s);
    }

    private Object editItem(Object originalItem) {
        Object editedItem;
        ValueType type = getCurrentType();

        if (equals(type, ValueType.BOOLEAN)) {
            editedItem = editBoolean(originalItem);
        } else if (equals(type, ValueType.CLS)) {
            showInstance((Instance) originalItem);
            editedItem = originalItem;
        } else if (equals(type, ValueType.FLOAT)) {
            editedItem = editText("Float", originalItem, FloatValidator.getInstance());
        } else if (equals(type, ValueType.INSTANCE)) {
            if (originalItem == null) {
                Cls cls = DisplayUtilities.pickConcreteCls(this, getKnowledgeBase(), getAllowedClses());
                editedItem = cls.createDirectInstance(null);
                showInstance((Instance) editedItem);
            } else {
                showInstance((Instance) originalItem);
                editedItem = originalItem;
            }
        } else if (equals(type, ValueType.INTEGER)) {
            editedItem = editText("Integer", originalItem, IntegerValidator.getInstance());
        } else if (equals(type, ValueType.STRING)) {
            editedItem = editText("String", originalItem, null);
        } else if (equals(type, ValueType.SYMBOL)) {
            editedItem = editSymbol(originalItem);
        } else {
            Log.getLogger().warning("bad type: " + type);
            editedItem = originalItem;
        }
        return editedItem;
    }

    private String editSymbol(Object o) {
        return editSymbol((String) o, getAllowedValues());
    }

    private String editSymbol(String value, Collection allowedValues) {
        return (String) DisplayUtilities.pickSymbol(this, "Value", value, allowedValues);
    }

    private Object editText(String name, Object o, NumberValidator v) {
        String text = DisplayUtilities.editString(this, name + " Value", o, v);
        Object output;
        if (text == null) {
            output = null;
        } else if (v == null) {
            output = text;
        } else {
            output = v.convertToNumber(text);
        }
        return output;
    }

    private Action getAddAction() {
        _addAction = new AddAction(ResourceKey.VALUE_ADD) {

            private static final long serialVersionUID = -8390731086548880897L;

            public void onAdd() {
                Collection c = addItems();
                if (c != null) {
                    addItems(c);
                }
            }
        };

        return _addAction;
    }

    private Slot getSlotInstance() {
        return (Slot) getInstance();
    }

    private Collection getAllowedClses() {
        Collection clses;
        Slot slot = getSlotInstance();
        if (isSlotAtCls()) {
            clses = getAssociatedCls().getTemplateSlotAllowedClses(slot);
        } else {
            clses = slot.getAllowedClses();
        }
        return rootCollection(clses);
    }

    private Collection getAllowedParents() {
        Collection parents;
        Slot slot = getSlotInstance();
        if (isSlotAtCls()) {
            parents = getAssociatedCls().getTemplateSlotAllowedParents(slot);
        } else {
            parents = slot.getAllowedParents();
        }
        return rootCollection(parents);
    }

    private Collection getAllowedValues() {
        Collection values;
        Slot slot = getSlotInstance();
        if (isSlotAtCls()) {
            values = getAssociatedCls().getTemplateSlotAllowedValues(slot);
        } else {
            values = slot.getAllowedValues();
        }
        return values;
    }

    private Action getCreateAction() {
        _createAction = new CreateAction(ResourceKey.VALUE_CREATE) {

            private static final long serialVersionUID = -4397623316890492860L;

            public void onCreate() {
                Object o = editItem(null);
                if (o != null) {
                    addItem(o);
                }
            }
        };

        return _createAction;
    }

    private ValueType getCurrentType() {
        ValueType type;
        Slot slot = getSlotInstance();
        if (slot == null) {
            type = ValueType.ANY;
        } else if (isSlotAtCls()) {
            type = getAssociatedCls().getTemplateSlotValueType(slot);
        } else {
            type = slot.getValueType();
        }
        return type;
    }

    private Action getEditAction() {
        _viewAction = new ViewAction(this) {
            private static final long serialVersionUID = -7273113512035501651L;

            public void onView(Object o) {
                Object editedItem = editItem(o);
                if (!o.equals(editedItem)) {
                    replaceItem(o, editedItem);
                }
            }
        };
        return _viewAction;
    }

    private Action getRemoveAction() {
        _removeAction = new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = -2334952871995157304L;

            public void onRemove(Collection values) {
                removeItems(values);
            }
        };
        return _removeAction;
    }

    public void initialize() {
        Action editAction = getEditAction();
        super.initialize(editAction);
        addButton(editAction);
        addButton(getCreateAction());
        addButton(getAddAction());
        addButton(getRemoveAction());
        updateButtons();
    }

    private boolean isEditable() {
        boolean result;
        Slot slot = getSlotInstance();
        if (slot == null) {
            result = false;
        } else if (isSlotAtCls()) {
            result = getAssociatedCls().isEditable();
        } else {
            result = slot.isEditable();
        }
        
        result = result && !isReadOnlyConfiguredWidget();
        return result;
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.VALUES);
    }

    private Collection rootCollection(Collection c) {
        return (c.isEmpty()) ? getKnowledgeBase().getRootClses() : c;
    }

    public void setInstance(Instance newInstance) {
        Instance oldInstance = getInstance();
        if (oldInstance != null) {

            oldInstance.removeFrameListener(_frameListener);

        }

        super.setInstance(newInstance);
        if (newInstance != null) {

            newInstance.addFrameListener(_frameListener);
        }
    }

    public void setAssociatedCls(Cls cls) {
        Cls oldAssociatedCls = getAssociatedCls();
        if (oldAssociatedCls != null) {
            oldAssociatedCls.removeClsListener(_clsListener);
        }
        super.setAssociatedCls(cls);
        if (cls != null) {
            cls.addClsListener(_clsListener);
        }
    }

    public void setValues(Collection c) {
        // Log.enter(this, "setValues", c);
        super.setValues(c);
        updateButtons();
    }

    private void updateButtons() {
        ValueType type = getCurrentType();
        // Log.trace("type=" + type + ", instance=" + getInstance(), this, "updateButtons");
        boolean fixedValueType = equals(type, ValueType.INSTANCE) || equals(type, ValueType.CLS)
                || equals(type, ValueType.SYMBOL);
        boolean addAllowed = fixedValueType;
        _addAction.setAllowed(addAllowed && isEditable());
        boolean createAllowed = !(equals(type, ValueType.ANY) || fixedValueType);
        _createAction.setAllowed(createAllowed && isEditable());
    }

    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Template Values", ResourceKey.TEMPLATE_VALUES_SLOT_WIDGET_LABEL);
    }
}