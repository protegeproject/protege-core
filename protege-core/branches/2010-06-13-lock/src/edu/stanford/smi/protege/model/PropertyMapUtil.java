package edu.stanford.smi.protege.model;

import java.awt.Rectangle;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Utilities for working with the PropertyMap
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PropertyMapUtil {

    private static final String MAP_CLASS = "Map";
    private static final String ENTRIES_SLOT = "entries";
    private static final String REFERENCED_MAPS_SLOT = "referenced_maps";

    private static final String MAP_ENTRY_CLASS = "Map_Entry";
    private static final String KEY_SLOT = "key";
    private static final String KEY_CLASS_SLOT = "key_class";
    private static final String VALUE_SLOT = "value";
    private static final String VALUE_CLASS_SLOT = "value_class";

    private static KnowledgeBase _domainKB;
    private static KnowledgeBase _projectKB;
    private static Map _valuesToMaps = new HashMap();

    private static class Entry {
        private String value;
        String valueClass;

        Entry(String v, Class c) {
            value = v;
            valueClass = c.getName();
        }

        public String toString() {
            return value;
        }
    }

    private static Instance createEntryInstance(Instance mapInstance) {
        Cls cls = _projectKB.getCls(MAP_ENTRY_CLASS);
        Instance instance = _projectKB.createInstance(null, cls);
        ModelUtilities.addOwnSlotValue(mapInstance, ENTRIES_SLOT, instance);
        return instance;
    }

    private static Instance createMapInstance() {
        Cls cls = _projectKB.getCls(MAP_CLASS);
        return _projectKB.createInstance(null, cls);
    }

    public static void emptyMapInstance(Instance mapInstance) {
        // transitive delete of referenced instances
    }

    private static Entry getEntry(Object value) {
        Entry result = null;
        if (value instanceof Frame) {
            Frame frame = (Frame) value;
            if (!frame.isDeleted()) {
                result = new Entry(frame.getName(), Frame.class);
            }
        } else if (value instanceof Rectangle) {
            result = new Entry(rectToString((Rectangle) value), Rectangle.class);
        } else if (value instanceof Map) {
            result = new Entry(mapToString((Map) value), Map.class);
        } else if (value instanceof Class) {
            result = new Entry(((Class) value).getName(), Class.class);
        } else {
            result = new Entry(value.toString(), value.getClass());
        }
        return result;
    }

    private static Object getFrame(String value) {
        Frame frame = _domainKB.getFrame(value);
        if (frame == null) {
            Log.getLogger().warning("unable to load frame: " + value);
        }
        return frame;
    }

    private static Object getJavaClass(String className) {
        Class clas = SystemUtilities.forName(className);
        if (clas == null) {
            Log.getLogger().warning("unable to load class: " + className);
        }
        return clas;
    }

    private static Object getJavaClassObject(String className, String value) {
        Object result = null;
        Class c = SystemUtilities.forName(className, true);
        if (c == null) {
            Log.getLogger().warning("Unable to load class: " + className);
        } else {
            try {
                Constructor constructor = c.getConstructor(new Class[] { String.class });
                if (constructor == null) {
                    Log.getLogger().warning("Class has no string constructor" + className);
                } else {
                    result = constructor.newInstance(new Object[] { value });
                }
            } catch (Exception e) {
                Log.getLogger().warning(e.getMessage());
            }
        }
        return result;
    }

    private static Object getMap(String value) {
        Map map = (Map) _valuesToMaps.get(value);
        if (map == null) {
            map = new HashMap();
            _valuesToMaps.put(value, map);
            Instance mapInstance = _projectKB.getInstance(value);
            if (mapInstance == null) {
                Log.getLogger().warning("Missing map instance: " + value);
            } else {
                load(map, mapInstance);
            }
        }
        return map;
    }

    private static Object getRectangle(String value) {
        Rectangle result;
        try {
            StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(value));
            tokenizer.nextToken();
            int x = (int) tokenizer.nval;
            tokenizer.nextToken();
            int y = (int) tokenizer.nval;
            tokenizer.nextToken();
            int w = (int) tokenizer.nval;
            tokenizer.nextToken();
            int h = (int) tokenizer.nval;
            result = new Rectangle(x, y, w, h);
        } catch (Exception e) {
            Log.getLogger().warning(e.getMessage());
            result = null;
        }
        return result;
    }

    private static Object getSlotValue(Instance instance, String slotName) {
        return ModelUtilities.getDirectOwnSlotValue(instance, slotName);
    }

    private static void setSlotValue(Instance instance, String slotName, Object value) {
        ModelUtilities.setOwnSlotValue(instance, slotName, value);
    }

    private static Collection getSlotValues(Instance instance, String slotName) {
        return ModelUtilities.getDirectOwnSlotValues(instance, slotName);
    }

    public static Map load(Instance instance, KnowledgeBase domainKB) {
        _domainKB = domainKB;
        _projectKB = instance.getKnowledgeBase();
        _valuesToMaps.clear();
        Map result = new HashMap();
        load(result, instance);
        return result;
    }

    private static void load(Map map, Instance instance) {
        Iterator i = new ArrayList(getSlotValues(instance, ENTRIES_SLOT)).iterator();
        while (i.hasNext()) {
            Instance entryInstance = (Instance) i.next();
            boolean succeeded = loadEntry(map, entryInstance);
            if (!succeeded) {
                removeSlotValue(instance, ENTRIES_SLOT, entryInstance);
            }
        }
    }

    private static boolean loadEntry(Map map, Instance entry) {
        boolean succeeded;
        Object key = loadObject(entry, KEY_SLOT, KEY_CLASS_SLOT);
        Object value = loadObject(entry, VALUE_SLOT, VALUE_CLASS_SLOT);
        if (key == null || value == null) {
            succeeded = false;
        } else {
            map.put(key, value);
            succeeded = true;
        }
        return succeeded;
    }

    private static Object loadObject(Instance entryInstance, String valueSlot, String valueClassSlot) {
        Object object;
        String value = (String) getSlotValue(entryInstance, valueSlot);
        String valueType = (String) getSlotValue(entryInstance, valueClassSlot);
        if (valueType.equals(Frame.class.getName())) {
            object = getFrame(value);
        } else if (valueType.equals(Map.class.getName())) {
            object = getMap(value);
        } else if (valueType.equals(Rectangle.class.getName())) {
            object = getRectangle(value);
        } else if (valueType.equals(Class.class.getName())) {
            object = getJavaClass(value);
        } else {
            object = getJavaClassObject(valueType, value);
        }
        return object;
    }

    private static String mapToString(Map map) {
        Instance mapInstance = createMapInstance();
        _valuesToMaps.put(mapInstance.getName(), map);
        storeMap(map, mapInstance);
        return mapInstance.getName();
    }

    private static String rectToString(Rectangle rect) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(rect.x);
        buffer.append(' ');
        buffer.append(rect.y);
        buffer.append(' ');
        buffer.append(rect.width);
        buffer.append(' ');
        buffer.append(rect.height);
        return buffer.toString();
    }

    private static void removeSlotValue(Instance instance, String slotName, Object value) {
        ModelUtilities.removeOwnSlotValue(instance, slotName, value);
    }

    private static void saveReferencedMaps(Instance mapInstance) {
        Iterator i = _valuesToMaps.keySet().iterator();
        while (i.hasNext()) {
            String instanceName = (String) i.next();
            Instance instance = _projectKB.getInstance(instanceName);
            ModelUtilities.addOwnSlotValue(mapInstance, REFERENCED_MAPS_SLOT, instance);
        }
    }

    public static void store(Map map, Instance mapInstance) {
        KnowledgeBase domainKB = null;

        _projectKB = mapInstance.getKnowledgeBase();
        _domainKB = domainKB;
        _valuesToMaps.clear();
        deleteMapEntries(mapInstance);
        storeMap(map, mapInstance);
        saveReferencedMaps(mapInstance);
    }

    private static void deleteMapEntries(Instance mapInstance) {
        Iterator i = getSlotValues(mapInstance, ENTRIES_SLOT).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            deleteMapEntry(instance);
        }
        setSlotValue(mapInstance, ENTRIES_SLOT, null);
    }

    private static void deleteMapEntry(Instance mapEntryInstance) {
        String valueClass = (String) getSlotValue(mapEntryInstance, VALUE_CLASS_SLOT);
        if (valueClass.equals(Map.class.getName())) {
            String value = (String) getSlotValue(mapEntryInstance, VALUE_SLOT);
            Instance instance = mapEntryInstance.getKnowledgeBase().getInstance(value);
            if (instance != null) {
                deleteMapEntries(instance);
                instance.delete();
            }
        }
        mapEntryInstance.delete();
    }

    private static void storeMap(Map map, Instance mapInstance) {
        Iterator i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null) {
                Log.getLogger().warning("Unable to store null");
            } else {
                storeMapEntry(key, value, mapInstance);
            }
        }
    }

    private static void storeMapEntry(Object key, Object value, Instance mapInstance) {
        Entry keyEntry = getEntry(key);
        Entry valueEntry = getEntry(value);
        if (keyEntry != null && valueEntry != null) {
            Instance entryInstance = createEntryInstance(mapInstance);
            ModelUtilities.setOwnSlotValue(entryInstance, KEY_SLOT, keyEntry.value);
            ModelUtilities.setOwnSlotValue(entryInstance, KEY_CLASS_SLOT, keyEntry.valueClass);
            ModelUtilities.setOwnSlotValue(entryInstance, VALUE_SLOT, valueEntry.value);
            ModelUtilities.setOwnSlotValue(entryInstance, VALUE_CLASS_SLOT, valueEntry.valueClass);
        }
    }
    
    public static void dispose() {
    	_valuesToMaps.clear();
    	_domainKB = null;
    	_projectKB = null;
    }
}