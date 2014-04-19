package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * The default cardinality single widget for acquiring and displaying an instance.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceFieldWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = 376700956825053322L;
    private JList _list;
    private Instance _instance;
    private AllowableAction _createAction;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;
    private AllowableAction _deleteAction;
    private boolean _showNewInstances = true;

    private FrameListener _instanceListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            _list.repaint();
        }
    };

    protected void addButton(LabeledComponent c, Action action, boolean defaultState) {
        if (action != null) {
            addButtonConfiguration(action, defaultState);
            if (displayButton(action)) {
                c.addHeaderButton(action);
            }
        }
    }

    protected void addButtons(LabeledComponent c) {
        addButton(c, getViewInstanceAction(), true);
        addButton(c, new ReferencersAction(this), false);
        addButton(c, getCreateInstanceAction(), true);
        addButton(c, getSelectInstanceAction(), true);
        addButton(c, getRemoveInstanceAction(), true);
        addButton(c, getDeleteInstancesAction(), false);
    }

    public JList createList() {
        JList list = ComponentFactory.createSingleItemList(getDoubleClickAction());
        list.setCellRenderer(FrameRenderer.createInstance());
        return list;
    }

    public void dispose() {
        if (_instance != null) {
            _instance.removeFrameListener(_instanceListener);
        }
        super.dispose();
    }

    protected Action getCreateInstanceAction() {
        _createAction = new CreateAction(ResourceKey.INSTANCE_CREATE) {
            private static final long serialVersionUID = 1867649088443463104L;

            public void onCreate() {
                handleCreateAction();
            }
        };
        return _createAction;
    }

    protected Action getDeleteInstancesAction() {
        _deleteAction = new DeleteInstancesAction(this);
        return _deleteAction;
    }

    protected Action getRemoveInstanceAction() {
        _removeAction = new RemoveAction(ResourceKey.INSTANCE_REMOVE, this) {
            private static final long serialVersionUID = 2629621785174837136L;

            public void onRemove(Object o) {
                handleRemoveAction();
            }
        };
        return _removeAction;
    }

    protected Action getSelectInstanceAction() {
        _addAction = new AddAction(ResourceKey.INSTANCE_ADD) {
            private static final long serialVersionUID = -5463810763929014137L;

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

    protected Action getViewInstanceAction() {
        return new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            private static final long serialVersionUID = 7309253827733187916L;

            public void onView(Object o) {
                handleViewAction((Instance) o);
            }
        };
    }

    protected void handleAddAction() {
        Collection clses = getCls().getTemplateSlotAllowedClses(getSlot());
        Instance instance = DisplayUtilities.pickInstance(InstanceFieldWidget.this, clses);
        if (instance != null) {
            setDisplayedInstance(instance);
        }
    }

    protected void handleCreateAction() {
        Collection clses = getCls().getTemplateSlotAllowedClses(getSlot());
        Cls cls = DisplayUtilities.pickConcreteCls(InstanceFieldWidget.this, getKnowledgeBase(), clses);
        if (cls != null) {
            Instance instance = getKnowledgeBase().createInstance(null, cls);
            if (instance instanceof Cls) {
                Cls newcls = (Cls) instance;
                if (newcls.getDirectSuperclassCount() == 0) {
                    newcls.addDirectSuperclass(getKnowledgeBase().getRootCls());
                }
            }
            if (_showNewInstances) {
                showInstance(instance);
            }
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
            boolean isInstance = cls.getTemplateSlotValueType(slot) == ValueType.INSTANCE;
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
        if (_instance != null) {
            _instance.removeFrameListener(_instanceListener);
        }
        _instance = instance;
        if (_instance != null) {
            _instance.addFrameListener(_instanceListener);
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
    	
        setAllowed(_createAction, b);
        setAllowed(_addAction, b);
        setAllowed(_removeAction, b);
        setAllowed(_deleteAction, b);
    }

    public void setValues(Collection values) {
        Instance value = (Instance) CollectionUtilities.getFirstItem(values);
        replaceInstance(value);
        updateList();
    }

    protected void updateList() {
        ComponentUtilities.setListValues(_list, CollectionUtilities.createCollection(_instance));
    }

    public boolean getShowNewInstances() {
        return _showNewInstances;
    }

    public void setShowNewInstances(boolean b) {
        _showNewInstances = b;
    }
    
    @Override
    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
    	WidgetConfigurationPanel confPanel = super.createWidgetConfigurationPanel();
    	
    	confPanel.addTab("Options", new ReadOnlyWidgetConfigurationPanel(this));
    	
    	return confPanel;
    }
}
