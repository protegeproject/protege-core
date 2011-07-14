package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Common functionality for widgets which are composed of a list box in a labeled component
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractListWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = 4810580360788052158L;
    private JList _list;
    private LabeledComponent _labeledComponent;
    private SwitchableListSelectionListener _listListener = new ListSelectionListenerAdapter(this);

    protected AbstractListWidget() {
        setPreferredColumns(2);
        setPreferredRows(2);
    }

    protected JList getList() {
        return _list;
    }

    public void addButton(Action action) {
        addButton(action, true);
    }

    public void addButton(Action action, boolean defaultState) {
        if (action != null) {
            addButtonConfiguration(action, defaultState);
            if (displayButton(action)) {
                _labeledComponent.addHeaderButton(action);
            }
        }
    }

    public void addItem(Object o) {
        ComponentUtilities.addListValue(_list, o);
    }

    public void addItems(Collection items) {
        ComponentUtilities.addListValues(_list, items);
    }

    public boolean contains(Object o) {
        return ComponentUtilities.listValuesContain(_list, o);
    }

    private JComponent createLabeledComponent(Action action) {
        _list = ComponentFactory.createList(action, true);
        _list.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent event) {
                valueChanged();
            }

            public void intervalAdded(ListDataEvent event) {
                valueChanged();
            }

            public void intervalRemoved(ListDataEvent event) {
                valueChanged();
            }
        });
        _list.setCellRenderer(createRenderer());
        _list.addListSelectionListener(_listListener);
        _labeledComponent = new LabeledComponent(getLabel(), ComponentFactory.createScrollPane(_list));
        return _labeledComponent;
    }

    //ESCA-JAVA0130 
    protected ListCellRenderer createRenderer() {
        return FrameRenderer.createInstance();
    }

    protected LabeledComponent getLabeledComponent() {
        return _labeledComponent;
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_list);
    }

    public Collection getValues() {
        return ComponentUtilities.getListValues(_list);
    }

    public void initialize() {
        initialize(getDoubleClickAction());
        
        setEditable(true);
    }

    public void initialize(Action action) {
        add(createLabeledComponent(action));
    }

    public void removeAllItems() {
        ComponentUtilities.clearListValues(_list);
    }

    public void removeItem(Object o) {
        if (isDirectValue(o)) {
            ComponentUtilities.removeListValue(_list, o);
        } else {
            SystemUtilities.beep();
        }
    }

    public void removeItems(Collection items) {
        if (canRemove(items)) {
            ComponentUtilities.removeListValues(_list, items);
        } else {
            SystemUtilities.beep();
        }
    }

    protected boolean canRemove(Collection items) {
        return areDirectValues(items);
    }

    public void replaceItem(Object oldItem, Object newItem) {
        ComponentUtilities.replaceListValue(_list, oldItem, newItem);
    }

    public void setEditable(boolean b) {
    	
    	b = b && !isReadOnlyConfiguredWidget();
    	
        Iterator i = _labeledComponent.getHeaderButtonActions().iterator();
        while (i.hasNext()) {
            Action action = (Action) i.next();
            if (action instanceof CreateAction || action instanceof AddAction || action instanceof RemoveAction
                    || action instanceof DeleteAction) {
                ((AllowableAction) action).setAllowed(b);
            }

        }
    }

    public void setInstance(Instance instance) {
        super.setInstance(instance);
        _list.setCellRenderer(new OwnSlotValueFrameRenderer(instance, getSlot()));
    }

    public void setRenderer(ListCellRenderer renderer) {
        _list.setCellRenderer(renderer);
    }

    public void setSelection(Object o) {
        _list.setSelectedValue(o, true);
    }

    public void setValues(Collection values) {
        ComponentUtilities.setListValues(_list, values);
    }
    
    @Override
    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
    	WidgetConfigurationPanel confPanel = super.createWidgetConfigurationPanel();
    	
    	confPanel.addTab("Options", new ReadOnlyWidgetConfigurationPanel(this));
    	
    	return confPanel;
    }
    
}
