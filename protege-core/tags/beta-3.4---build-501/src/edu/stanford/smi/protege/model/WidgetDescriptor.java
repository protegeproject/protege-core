package edu.stanford.smi.protege.model;

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Essentially a serialization of a widget. Objects of this class contain location and size information and other
 * configuration information for a widget. Actual widgets (the things on the screen) can be created from these objects.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetDescriptor implements Cloneable {
    private static final String CLASS_WIDGET = "Widget";
    private static final String SLOT_WIDGET_CLASSNAME = "widget_class_name";
    private static final String SLOT_PROPERTY_LIST = "property_list";
    private static final String SLOT_LABEL = "label";
    private static final String SLOT_NAME = "name";
    private static final String SLOT_HIDDEN = "is_hidden";
    private boolean _isTemporary;
    private boolean _isDirectlyCustomizedByUser;

    private Instance _instance;

    private WidgetDescriptor(Instance instance) {
        _instance = instance;
    }

    public Object clone() {
        WidgetDescriptor d;
        try {
            d = (WidgetDescriptor) super.clone();
            d._instance = (Instance) _instance.deepCopy(_instance.getKnowledgeBase(), null);
        } catch (CloneNotSupportedException e) {
            Log.getLogger().severe(Log.toString(e));
            d = null;
        }
        return d;
    }

    public boolean equals(Object o) {
        boolean equals;
        if (o instanceof WidgetDescriptor) {
            WidgetDescriptor other = (WidgetDescriptor) o;
            equals = _instance.equals(other._instance);
        } else {
            equals = false;
        }
        return equals;
    }

    public int hashCode() {
        return _instance.hashCode();
    }

    public static boolean isActiveWidget(Instance instance) {
        boolean isActive = false;
        Cls type = instance.getDirectType();
        if (type.getName().equals(CLASS_WIDGET)) {
            String className = (String) ModelUtilities.getDirectOwnSlotValue(instance, SLOT_WIDGET_CLASSNAME);
            isActive = className != null;
        }
        return isActive;
    }

    public static WidgetDescriptor create(Instance instance) {
        WidgetDescriptor d = null;
        Cls type = instance.getDirectType();
        if (type == null) {
            Log.getLogger().severe("instance has null type" + instance.getName());
        } else if (type.getName().equals(CLASS_WIDGET)) {
            d = new WidgetDescriptor(instance);
        } else {
            Log.getLogger().severe("not an instance of Widget: " + instance.getName());
        }
        return d;
    }

    public static WidgetDescriptor create(KnowledgeBase projectKB) {
        Cls widgetCls = projectKB.getCls(CLASS_WIDGET);
        Assert.assertNotNull("widgetCls", widgetCls);
        Instance instance = projectKB.createInstance(null, widgetCls);
        return create(instance);
    }

    private static void deletePropertyListInstance(Instance propertyInstance) {
        KnowledgeBase kb = propertyInstance.getKnowledgeBase();
        Collection roots = CollectionUtilities.createCollection(propertyInstance);
        Iterator i = kb.getReachableSimpleInstances(roots).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.delete();
        }
    }

    public Rectangle getBounds() {
        Rectangle r = InstanceUtilities.getRectangle(_instance);
        if (r.width == 0 || r.height == 0) {
            r = null;
        }
        return r;
    }

    public Instance getInstance() {
        return _instance;
    }

    private Instance getInstanceValue(String slotName) {
        return (Instance) getValue(slotName);
    }

    public String getLabel() {
        String label = getStringValue(SLOT_LABEL);
        return label;
    }

    public Point getLocation() {
        return getBounds().getLocation();
    }

    public String getName() {
        return getStringValue(SLOT_NAME);
    }

    public PropertyList getPropertyList() {
        Instance instance = getInstanceValue(SLOT_PROPERTY_LIST);
        if (instance == null) {
            KnowledgeBase kb = _instance.getKnowledgeBase();
            Cls cls = kb.getCls(PropertyList.CLASS_PROPERTY_LIST);
            instance = _instance.getKnowledgeBase().createInstance(null, cls);
            setValue(SLOT_PROPERTY_LIST, instance);
        }
        return new PropertyList(instance);
    }

    private String getStringValue(String slotName) {
        return (String) getValue(slotName);
    }

    private Object getValue(String slotName) {
        return (_instance == null) ? (Object) null : ModelUtilities.getDirectOwnSlotValue(_instance, slotName);
    }

    public String getWidgetClassName() {
        return getStringValue(SLOT_WIDGET_CLASSNAME);
    }

    public boolean isDirectlyCustomizedByUser() {
        return _isDirectlyCustomizedByUser;
    }

    public boolean isIncluded() {
        return _instance.isIncluded();
    }

    public boolean isNull() {
        return getWidgetClassName() == null;
    }

    public boolean isTemporary() {
        return _isTemporary;
    }

    public boolean isVisible() {
        Boolean isHidden = (Boolean) getValue(SLOT_HIDDEN);
        return (isHidden == null) ? true : !isHidden.booleanValue();
    }

    public void setBounds(Rectangle r) {
        InstanceUtilities.setRectangle(_instance, r);
    }

    public void setDirectlyCustomizedByUser(boolean b) {
        if (_isDirectlyCustomizedByUser != b) {
            _isDirectlyCustomizedByUser = b;
            _isTemporary = !_isDirectlyCustomizedByUser;
        }
    }

    public void setIncluded(boolean b) {
        setInstanceTreeIncluded(_instance, b);
    }

    private static void setInstanceTreeIncluded(Instance instance, boolean included) {
        setIncluded(instance, included);
        Iterator i = instance.getReachableSimpleInstances().iterator();
        while (i.hasNext()) {
            Instance reachableInstance = (Instance) i.next();
            setIncluded(reachableInstance, included);
        }
    }

    private static void setIncluded(Instance instance, boolean included) {
        instance.setIncluded(included);
    }

    public void setLabel(String label) {
        setValue(SLOT_LABEL, label);
    }

    public void setName(String name) {
        setValue(SLOT_NAME, name);
    }

    public void setPropertyList(PropertyList list) {
        Instance instance = (Instance) getValue(SLOT_PROPERTY_LIST);
        if (instance != null) {
            deletePropertyListInstance(instance);
        }
        Instance wrappedInstance = list.getWrappedInstance();
        setValue(SLOT_PROPERTY_LIST, wrappedInstance);
        setInstanceTreeIncluded(wrappedInstance, _instance.isIncluded());
    }

    public void setTemporary(boolean b) {
        _isTemporary = b;
    }

    private void setValue(String slotName, Object value) {
        ModelUtilities.setOwnSlotValue(_instance, slotName, value);
    }

    public void setVisible(boolean b) {
        setValue(SLOT_HIDDEN, Boolean.valueOf(!b));
    }

    public void setWidgetClassName(String name) {
        String oldName = getWidgetClassName();
        if (!SystemUtilities.equals(name, oldName)) {
            setValue(SLOT_WIDGET_CLASSNAME, name);
            getPropertyList().clear();
        }
    }

    public String toString() {
        String text = "WidgetDescriptor(name=" + getName();
        text += ", temp=" + isTemporary();
        text += ", included=" + isIncluded();
        text += ", classname=" + getWidgetClassName();
        text += ")";
        return text;
    }
}