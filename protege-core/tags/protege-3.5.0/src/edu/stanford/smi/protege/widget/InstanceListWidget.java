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
 * Standard widget for acquiring and displaying instances in an ordered list.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceListWidget extends AbstractListWidget {
    private static final long serialVersionUID = -3207320406959420859L;
    private AllowableAction _createInstanceAction;
    private AllowableAction _addInstancesAction;
    private AllowableAction _removeInstancesAction;
    private AllowableAction _deleteInstancesAction;
    private AllowableAction _viewInstanceAction;
    private boolean _showNewInstances = true;

    private FrameListener _instanceListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            super.browserTextChanged(event);
            // Log.trace("changed", this, "browserTextChanged");
            repaint();
        }
    };

    protected void addButtons(LabeledComponent c) {
        addButton(getViewInstanceAction());
        addButton(getCreateInstanceAction());
        addButton(new ReferencersAction(this), false);
        addButton(getAddInstancesAction());
        addButton(getRemoveInstancesAction());
        addButton(getDeleteInstancesAction(), false);
    }

    public void addItem(Object item) {
        super.addItem(item);
        addListener(CollectionUtilities.createCollection(item));
    }

    public void addItems(Collection items) {
        super.addItems(items);
        addListener(items);
    }

    protected void addListener(Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.addFrameListener(_instanceListener);
        }
    }

    public void dispose() {
        removeListener(getValues());
        super.dispose();
    }

    protected Action getAddInstancesAction() {
        _addInstancesAction = new AddAction(ResourceKey.INSTANCE_ADD) {
            private static final long serialVersionUID = -5397657826029556524L;

            public void onAdd() {
                handleAddAction();
            }
        };
        return _addInstancesAction;
    }

    public Action getCreateInstanceAction() {
        _createInstanceAction = new CreateAction(ResourceKey.INSTANCE_CREATE) {
            private static final long serialVersionUID = -511147408901936946L;

            public void onCreate() {
                handleCreateAction();
            }
        };
        return _createInstanceAction;
    }

    protected Action getDeleteInstancesAction() {
        _deleteInstancesAction = new DeleteInstancesAction(this);
        return _deleteInstancesAction;
    }

    protected Action getRemoveInstancesAction() {
        _removeInstancesAction = new RemoveAction(ResourceKey.INSTANCE_REMOVE, this) {
            private static final long serialVersionUID = 2415308209387049694L;

            public void onRemove(Collection instances) {
                handleRemoveAction(instances);
            }
        };
        return _removeInstancesAction;
    }

    protected Action getViewInstanceAction() {
        _viewInstanceAction = new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            private static final long serialVersionUID = 1848862060962178950L;

            public void onView(Object o) {
                handleViewAction((Instance) o);
            }
        };
        return _viewInstanceAction;
    }

    protected void handleAddAction() {
        Collection clses = getCls().getTemplateSlotAllowedClses(getSlot());
        String title = (String) _addInstancesAction.getValue(Action.NAME);
        addItems(DisplayUtilities.pickInstances(InstanceListWidget.this, clses, title));
    }

    protected void handleCreateAction() {
        Collection clses = getCls().getTemplateSlotAllowedClses(getSlot());
        Cls cls = DisplayUtilities.pickConcreteCls(InstanceListWidget.this, getKnowledgeBase(), clses);
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
            addItem(instance);
        }
    }

    protected void handleRemoveAction(Collection instances) {
        removeItems(instances);
    }

    protected void handleViewAction(Instance instance) {
        showInstance(instance);
    }

    public void initialize() {
        super.initialize();
        addButtons(getLabeledComponent());
        setRenderer(FrameRenderer.createInstance());
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            boolean isInstance = equals(cls.getTemplateSlotValueType(slot), ValueType.INSTANCE);
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isInstance && isMultiple;
        }
        return isSuitable;
    }

    protected void removeListener(Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.removeFrameListener(_instanceListener);
        }
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
    	
        setAllowed(_createInstanceAction, b);
        setAllowed(_addInstancesAction, b);
        setAllowed(_removeInstancesAction, b);
        setAllowed(_deleteInstancesAction, b);
    }

    public void setValues(Collection values) {
        removeListener(getValues());
        addListener(values);
        super.setValues(values);
    }

    public boolean getShowNewInstances() {
        return _showNewInstances;
    }

    public void setShowNewInstances(boolean b) {
        _showNewInstances = b;
    }
}
