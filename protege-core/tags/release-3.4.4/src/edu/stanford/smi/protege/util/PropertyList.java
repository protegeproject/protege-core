package edu.stanford.smi.protege.util;

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * A property list used by widgets. This is a wrapper around a Project knowledge
 * base instance. I probably should have just used a java.util.Properties class
 * but I wanted to provide methods for working with types of objects (e.g.
 * Rectangles) other than strings. Also the Properties class inheritance is
 * messed up and provides a bunch of dangerous methods.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PropertyList implements Cloneable {
    public static final String CLASS_PROPERTY_LIST = "Property_List";
    public static final String CLASS_WIDGET_DESCRIPTOR = "Widget";
    public static final String CLASS_STRING = "String";
    public static final String CLASS_INTEGER = "Integer";
    public static final String CLASS_BOOLEAN = "Boolean";
    public static final String CLASS_RECTANGLE = "Rectangle";
    public static final String SLOT_NAME = "name";
    public static final String SLOT_BOOLEAN_VALUE = "boolean_value";
    public static final String SLOT_INTEGER_VALUE = "integer_value";
    public static final String SLOT_STRING_VALUE = "string_value";
    public static final String SLOT_PROPERTIES = "properties";

    private Map<String, Slot> nameToSlotMap = new HashMap<String, Slot>();

    private Instance _instance;

    public PropertyList(Instance instance) {
        Assert.assertNotNull("instance", instance);
        Assert.assertEquals("class name", instance.getDirectType().getName(), CLASS_PROPERTY_LIST);
        _instance = instance;
    }

    private void addProperty(Instance property) {
        Assert.assertNotNull("property", property);
        addValue(SLOT_PROPERTIES, property);
    }

    public void clear() {
        KnowledgeBase kb = _instance.getKnowledgeBase();
        Collection values = getValues(SLOT_PROPERTIES);
        Iterator i = kb.getReachableSimpleInstances(values).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            kb.deleteInstance(instance);
        }
        setValue(SLOT_PROPERTIES, null);
    }

    public Object clone() {
        // Instance instance =
        // itsInstance.getKnowledgeBase().createInstance(null,
        // itsInstance.getDirectType());
        Instance instance = (Instance) _instance.deepCopy(null, null);
        return new PropertyList(instance);
    }

    public static PropertyList create(KnowledgeBase kb) {
        Cls cls = kb.getCls(CLASS_PROPERTY_LIST);
        Instance instance = kb.createInstance(null, cls);
        return new PropertyList(instance);
    }

    private Instance createInstance(String clsName) {
        KnowledgeBase kb = _instance.getKnowledgeBase();
        Cls cls = kb.getCls(clsName);
        return kb.createInstance(null, cls);
    }

    private Instance createProperty(String name, String propertyClsName) {
        Instance property = createInstance(propertyClsName);
        setValue(property, SLOT_NAME, name);
        addProperty(property);
        return property;
    }

    public WidgetDescriptor createWidgetDescriptor(String name) {
        Instance instance = getOrCreateProperty(name, CLASS_WIDGET_DESCRIPTOR);
        WidgetDescriptor desc = WidgetDescriptor.create(instance);
        return desc;
    }

    public Boolean getBoolean(String name) {
        Instance property = getProperty(name);
        return (property == null) ? (Boolean) null : (Boolean) getValue(property, SLOT_BOOLEAN_VALUE);
    }

    public Dimension getDimension(String name) {
        Instance instance = getProperty(name);
        return (instance == null) ? (Dimension) null : InstanceUtilities.getDimension(instance);
    }

    public Integer getInteger(String name) {
        Instance property = getProperty(name);
        return (property == null) ? (Integer) null : (Integer) getValue(property, SLOT_INTEGER_VALUE);
    }

    public KnowledgeBase getKnowledgeBase() {
        return _instance.getKnowledgeBase();
    }

    public Collection getNames() {
        Collection names = new ArrayList();
        Iterator i = getValues(SLOT_PROPERTIES).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            names.add(getValue(instance, SLOT_NAME));
        }
        return names;
    }

    public Collection getLiveWidgetDescriptors() {
        Collection widgetDescriptors = new ArrayList();
        Iterator i = getValues(SLOT_PROPERTIES).iterator();
        while (i.hasNext()) {
            Instance property = (Instance) i.next();
            if (WidgetDescriptor.isActiveWidget(property)) {
                WidgetDescriptor d = WidgetDescriptor.create(property);
                widgetDescriptors.add(d);
            }
        }
        return widgetDescriptors;
    }

    private Instance getOrCreateProperty(String propertyName, String propertyClsName) {
        Instance property = getProperty(propertyName);
        if (property == null) {
            property = createProperty(propertyName, propertyClsName);
        } else if (!property.getDirectType().getName().equals(propertyClsName)) {
            property = replaceProperty(propertyName, property, propertyClsName);
        }
        return property;
    }

    private Instance getProperty(String name) {
        Instance property = null;
        if (name != null) {
            Iterator i = getValues(SLOT_PROPERTIES).iterator();
            while (i.hasNext()) {
                Instance instance = (Instance) i.next();
                String propertyName = (String) getValue(instance, SLOT_NAME);
                // Assert.notNull(propertyName, "Instance: " + instance);
                if (name.equals(propertyName)) {
                    property = instance;
                    break;
                }
            }
        }
        return property;
    }

    public PropertyList getPropertyList(String name) {
        Instance instance = getOrCreateProperty(name, CLASS_PROPERTY_LIST);
        return new PropertyList(instance);
    }

    public Rectangle getRectangle(String name) {
        Instance instance = getProperty(name);
        return (instance == null) ? (Rectangle) null : InstanceUtilities.getRectangle(instance);
    }

    public String getString(String name) {
        Instance property = getProperty(name);
        return (property == null) ? (String) null : (String) getValue(property, SLOT_STRING_VALUE);
    }

    private Collection getValues(String name) {
        return getValues(_instance, name);
    }

    public WidgetDescriptor getWidgetDescriptor(String name) {
        Instance instance = getProperty(name);
        WidgetDescriptor d;
        if (instance == null) {
            d = null;
        } else {
            d = WidgetDescriptor.create(instance);
        }
        return d;
    }

    public Instance getWrappedInstance() {
        return _instance;
    }

    public void remove(String name) {
        Assert.assertNotNull("name", name);
        Iterator i = getValues(SLOT_PROPERTIES).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            String slotName = (String) getValue(instance, SLOT_NAME);
            if (name.equals(slotName)) {
                removeValue(SLOT_PROPERTIES, instance);
                break;
            }
        }
    }

    private void removeProperty(Instance property) {
        Assert.assertNotNull("property", property);
        removeValue(SLOT_PROPERTIES, property);
    }

    private void replaceProperty(Instance instance) {
        String name = (String) getValue(instance, SLOT_NAME);
        if (getProperty(name) != null) {
            remove(name);
        }
        addProperty(instance);
    }

    private Instance replaceProperty(String name, Instance property, String propertyClsName) {
        removeProperty(property);
        Instance result = createProperty(name, propertyClsName);
        return result;
    }

    public void setBoolean(String name, Boolean b) {
        setProperty(name, CLASS_BOOLEAN, SLOT_BOOLEAN_VALUE, b);
    }

    public void setBoolean(String name, boolean b) {
        setProperty(name, CLASS_BOOLEAN, SLOT_BOOLEAN_VALUE, Boolean.valueOf(b));
    }

    public void setInteger(String name, int i) {
        setProperty(name, CLASS_INTEGER, SLOT_INTEGER_VALUE, new Integer(i));
    }

    public void setInteger(String name, Integer i) {
        setProperty(name, CLASS_INTEGER, SLOT_INTEGER_VALUE, i);
    }

    private void setProperty(String name, String className, String slotName, Object value) {
        Instance property = getOrCreateProperty(name, className);
        setValue(property, slotName, value);
    }

    public void setRectangle(String name, Rectangle r) {
        Instance property = getOrCreateProperty(name, CLASS_RECTANGLE);
        InstanceUtilities.setRectangle(property, r);
    }

    public void setString(String name, String value) {
        setProperty(name, CLASS_STRING, SLOT_STRING_VALUE, value);
    }

    private void setValue(String slotName, Object value) {
        setValue(_instance, slotName, value);
    }

    public void setWidgetDescriptor(WidgetDescriptor d) {
        replaceProperty(d.getInstance());
    }

    public String toString() {
        String s = CollectionUtilities.toString(getNames());
        return "PropertyList(" + s + ")";
    }

    private Slot getSlot(String name) {
        Slot slot = nameToSlotMap.get(name);
        if (slot == null) {
            slot = _instance.getKnowledgeBase().getSlot(name);
            nameToSlotMap.put(name, slot);
        }
        return slot;
    }

    private void addValue(String slotName, Object value) {
        _instance.addOwnSlotValue(getSlot(slotName), value);
    }

    private void removeValue(String slotName, Object value) {
        _instance.removeOwnSlotValue(getSlot(slotName), value);
    }

    private Collection getValues(Instance instance, String slotName) {
        return instance.getDirectOwnSlotValues(getSlot(slotName));
    }

    private Object getValue(Instance instance, String slotName) {
        return instance.getDirectOwnSlotValue(getSlot(slotName));
    }

    private void setValue(Instance instance, String slotName, Object value) {
        instance.setDirectOwnSlotValue(getSlot(slotName), value);
    }
}