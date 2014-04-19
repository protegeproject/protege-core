package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * SlotWidget for acquiring and displaying a cardinality single Cls object.  Note that this widget does not allow you
 * to create a new class, in part because of the need to also specify its parents.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsFieldWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -2832081267119267599L;
    private JList _list;
    private Instance _instance;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;

    private FrameListener _instanceListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
        }
        public void ownSlotValueChanged(FrameEvent event) {
            repaint();
        }
        public void replaceFrame(FrameEvent event) {
            repaint();
        }
    };

    public void addButton(LabeledComponent c, Action action) {
        addButtonConfiguration(action);
        if (displayButton(action)) {
            c.addHeaderButton(action);
        }
    }

    protected void addButtons(LabeledComponent c) {
        addButton(c, getViewClsAction());
        addButton(c, getSelectClsAction());
        addButton(c, getRemoveClsAction());
    }

    public JList createList() {
        JList list = ComponentFactory.createSingleItemList(getDoubleClickAction());
        list.setCellRenderer(FrameRenderer.createInstance());
        return list;
    }

    public void dispose() {
        super.dispose();
        if (this._instance != null) {
            this._instance.removeFrameListener(this._instanceListener);
        }
    }

    protected Action getRemoveClsAction() {
        _removeAction = new RemoveAction(ResourceKey.CLASS_REMOVE, this) {
            private static final long serialVersionUID = 1835515889359535963L;

            public void onRemove(Object o) {
                handleRemoveAction();
            }
        };
        return _removeAction;
    }

    protected Action getSelectClsAction() {
        _addAction = new AddAction(ResourceKey.CLASS_ADD) {
            private static final long serialVersionUID = 6865317283085544137L;

            public void onAdd() {
                handleAddAction();
            }
        };
        return _addAction;
    }

    public Collection getSelection() {
        return CollectionUtilities.createCollection(this._instance);
    }

    public Collection getValues() {
        return CollectionUtilities.createList(this._instance);
    }

    protected Action getViewClsAction() {
        return new ViewAction(ResourceKey.CLASS_VIEW, this) {
            private static final long serialVersionUID = 5462508960435720753L;

            public void onView(Object o) {
                handleViewAction((Instance) o);
            }
        };
    }

    protected void handleAddAction() {
        Collection clses = getCls().getTemplateSlotAllowedParents(getSlot());
        Instance instance = DisplayUtilities.pickCls(ClsFieldWidget.this, getKnowledgeBase(), clses);
        if (instance != null) {
            setDisplayedInstance(instance);
        }
    }

    protected void handleRemoveAction() {
        removeDisplayedInstance();
    }

    protected void handleViewAction(Instance instance) {
        showInstance(instance);
    }

    public void initialize() {
        _list = createList();
        LabeledComponent c = new LabeledComponent(getLabel(), _list);
        addButtons(c);
        add(c);
        setPreferredColumns(2);
        setPreferredRows(1);
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            boolean isInstance = cls.getTemplateSlotValueType(slot) == ValueType.CLS;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isInstance && !isMultiple;
        }
        return isSuitable;
    }

    protected void removeDisplayedInstance() {
        replaceInstance(null);
        updateList();
        valueChanged();
    }

    protected void replaceInstance(Instance instance) {
        if (this._instance != null) {
            this._instance.removeFrameListener(this._instanceListener);
        }
        this._instance = instance;
        if (this._instance != null) {
            this._instance.addFrameListener(this._instanceListener);
        }
        notifySelectionListeners();
    }

    protected void setDisplayedInstance(Instance instance) {
        replaceInstance(instance);
        updateList();
        valueChanged();
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
    	
        setAllowed(_addAction, b);
        setAllowed(_removeAction, b);
    }

    public void setValues(Collection values) {
        Instance value = (Instance) CollectionUtilities.getFirstItem(values);
        replaceInstance(value);
        updateList();
    }

    protected void updateList() {
        ComponentUtilities.setListValues(_list, CollectionUtilities.createCollection(this._instance));
    }
    
    @Override
    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
    	WidgetConfigurationPanel confPanel = super.createWidgetConfigurationPanel();
    	
    	confPanel.addTab("Options", new ReadOnlyWidgetConfigurationPanel(this));
    	
    	return confPanel;
    }
}
