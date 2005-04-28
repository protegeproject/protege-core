package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Constraint that checks a set of values to determine if they are of the right type for a slot.  This class also 
 * contains a bunch of helper methods for handling the bizarre storage format of the VALUE-TYPE slot.  
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ValueTypeConstraint extends AbstractFacetConstraint {
    private static final Collection _values;

    static {
        _values = new ArrayList();
        _values.add(ValueType.ANY.toString());
        _values.add(ValueType.BOOLEAN.toString());
        _values.add(ValueType.CLS.toString());
        _values.add(ValueType.FLOAT.toString());
        _values.add(ValueType.INSTANCE.toString());
        _values.add(ValueType.INTEGER.toString());
        _values.add(ValueType.STRING.toString());
        _values.add(ValueType.SYMBOL.toString());
    }

    private static Collection getAllButFirst(Collection c) {
        Collection values;
        if (c == null || c.size() < 1) {
            // Log.stack("empty collection", ValueTypeConstraint.class, "getAllButFirst", c);
            values = Collections.EMPTY_LIST;
        } else {
            ArrayList list = new ArrayList(c);
            list.remove(0);
            values = list;
        }
        return values;
    }

    public static Collection getAllowedClses(Collection bindingValue) {
        Collection allowedClses;
        ValueType type = getType(bindingValue);
        if (type.equals(ValueType.INSTANCE)) {
            allowedClses = getAllButFirst(bindingValue);
        } else if (type.equals(ValueType.CLS)) {
            KnowledgeBase kb = getKb(bindingValue);
            if (kb == null) {
                allowedClses = Collections.EMPTY_LIST;
            } else {
                allowedClses = CollectionUtilities.createCollection(kb.getRootClsMetaCls());
            }
        } else {
            allowedClses = Collections.EMPTY_LIST;
        }
        ensureClses(allowedClses);
        return allowedClses;
    }

    private static void ensureClses(Collection clses) {
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof Cls)) {
                Log.getLogger().severe("Invalid Class: " + o);
                clses.remove(o);
            }
        }
    }

    private static KnowledgeBase getKb(Collection bindingValue) {
        KnowledgeBase kb = null;
        Iterator i = bindingValue.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Frame) {
                kb = ((Frame) o).getKnowledgeBase();
            }
        }
        return kb;
    }

    public static Collection getAllowedParents(Collection bindingValue) {
        Collection allowedParents;
        ValueType type = getType(bindingValue);
        if (type.equals(ValueType.CLS)) {
            allowedParents = getAllButFirst(bindingValue);
        } else if (type.equals(ValueType.INSTANCE)) {
            KnowledgeBase kb = getKb(bindingValue);
            if (kb == null) {
                allowedParents = Collections.EMPTY_LIST;
            } else {
                allowedParents = kb.getRootClses();
            }
        } else {
            allowedParents = Collections.EMPTY_LIST;
        }
        ensureClses(allowedParents);
        return allowedParents;
    }

    public static Collection getAllowedValues(Collection bindingValue) {
        ValueType type = getType(bindingValue);
        return type.equals(ValueType.SYMBOL) ? getAllButFirst(bindingValue) : Collections.EMPTY_LIST;
    }

    public String getInvalidAnyValueText(Object value) {
        boolean isValid = value instanceof Boolean || value instanceof Frame || value instanceof String
                || value instanceof Number;
        return isValid ? (String) null : "Value must by one of the allowed types";
    }

    public String getInvalidBooleanValueText(Object value) {
        if (value instanceof String) {
            Log.getLogger().warning("String value in boolean slot: " + value);
        }
        return (value instanceof Boolean) ? (String) null : "Value must be a boolean";
    }

    public String getInvalidClsValueText(Object value, Collection allowedParents) {
        String result = null;
        if (value instanceof Cls) {
            Cls cls = (Cls) value;
            if (!allowedParents.contains(cls)) {
                boolean foundParent = false;
                Iterator i = allowedParents.iterator();
                while (!foundParent && i.hasNext()) {
                    Cls parent = (Cls) i.next();
                    if (cls.hasSuperclass(parent)) {
                        foundParent = true;
                    }
                }
                if (!foundParent) {
                    result = "Value must be a subclass of one of the allowed parents";
                }
            }
        } else {
            result = "Value must be a class";
        }
        return result;
    }

    public String getInvalidFloatValueText(Object value) {
        return (value instanceof Float) ? (String) null : "Value must be a floating point number";
    }

    public String getInvalidInstanceValueText(Object value, Collection allowedClses) {
        String result = null;
        if (value instanceof Instance) {
            if (!allowedClses.isEmpty()) {
                Instance instance = (Instance) value;
                boolean foundType = false;
                Iterator i = allowedClses.iterator();
                while (!foundType && i.hasNext()) {
                    Cls type = (Cls) i.next();
                    if (instance.hasType(type)) {
                        foundType = true;
                    }
                }
                if (!foundType) {
                    result = "Value must be an instance of one of the allowed classes";
                }
            }
        } else {
            result = "Value must be an instance";
        }
        return result;
    }

    public String getInvalidIntegerValueText(Object value) {
        return (value instanceof Integer) ? (String) null : "Value must be an integer";
    }

    public String getInvalidStringValueText(Object value) {
        return (value instanceof String) ? (String) null : "Value must be a string";
    }

    public String getInvalidSymbolValueText(Object value, Collection allowedValues) {
        return allowedValues.contains(value) ? (String) null : "'" + value + "' is not one of the allowed values";
    }

    public String getInvalidValuesText(Frame frame, Slot slot, Collection slotValues, Collection facetValues) {
        String result = null;
        ValueType type = getType(facetValues);
        Iterator i = slotValues.iterator();
        while (result == null && i.hasNext()) {
            Object value = i.next();
            result = getInvalidValueText(facetValues, type, value);
        }
        return result;
    }

    public String getInvalidValueText(Frame frame, Slot slot, Object value, Collection facetValues) {
        ValueType type = getType(facetValues);
        return getInvalidValueText(facetValues, type, value);
    }

    private String getInvalidValueText(Collection facetValues, ValueType type, Object value) {
        String result = null;
        if (equals(type, ValueType.BOOLEAN)) {
            result = getInvalidBooleanValueText(value);
        } else if (equals(type, ValueType.CLS)) {
            result = getInvalidClsValueText(value, getAllowedParents(facetValues));
        } else if (equals(type, ValueType.FLOAT)) {
            result = getInvalidFloatValueText(value);
        } else if (equals(type, ValueType.INSTANCE)) {
            result = getInvalidInstanceValueText(value, getAllowedClses(facetValues));
        } else if (equals(type, ValueType.INTEGER)) {
            result = getInvalidIntegerValueText(value);
        } else if (equals(type, ValueType.STRING)) {
            result = getInvalidStringValueText(value);
        } else if (equals(type, ValueType.SYMBOL)) {
            result = getInvalidSymbolValueText(value, getAllowedValues(facetValues));
        } else if (equals(type, ValueType.ANY)) {
            result = getInvalidAnyValueText(value);
        } else {
            Assert.fail("Invalid type: " + type);
            result = "failed";
        }
        return result;
    }

    public static ValueType getType(Collection bindingValue) {
        Object first = CollectionUtilities.getFirstItem(bindingValue);
        ValueType type;
        if (first == null) {
            // Log.warning("empty collection", ValueTypeConstraint.class, "getType", bindingValue);
            type = ValueType.ANY;
        } else if (first instanceof String) {
            type = ValueType.valueOf((String) first);
        } else {
            Log.getLogger().severe("invalid value type: " + bindingValue);
            type = ValueType.ANY;
        }
        return type;
    }

    public static Collection getValues() {
        return _values;
    }

    public static Collection getValues(ValueType type) {
        return getValues(type, Collections.EMPTY_LIST);
    }

    public static List getValues(ValueType type, Collection values) {
        List value = new ArrayList();
        if (!type.equals(ValueType.ANY)) {
            value.add(type.toString());
            if (values != null) {
                // remove duplicates by putting into a set first
                value.addAll(new LinkedHashSet(values));
            }
        }
        return value;
    }

    public Collection resolve(Collection existingValues, Collection newValues) {
        Collection values;
        ValueType t1 = getType(existingValues);
        ValueType t2 = getType(newValues);
        if (t1.equals(ValueType.ANY)) {
            values = newValues;
        } else if (t2.equals(ValueType.ANY)) {
            values = existingValues;
        } else if (t1.equals(t2)) {
            if (t1.equals(ValueType.CLS)) {
                values = resolveClsValues(existingValues, newValues);
            } else if (t1.equals(ValueType.INSTANCE)) {
                values = resolveInstanceValues(existingValues, newValues);
            } else if (t1.equals(ValueType.SYMBOL)) {
                values = resolveSymbolValues(existingValues, newValues);
            } else {
                values = existingValues;
            }
        } else {
            // Log.warning("incompatible types", this, "resolve", existingValues, newValues);
            values = existingValues;
        }
        return values;
    }

    private Collection resolveClsValues(Collection existingValues, Collection newValues) {
        return (existingValues.isEmpty()) ? newValues : existingValues;
    }

    private Collection resolveInstanceValues(Collection existingValues, Collection newValues) {
        return (existingValues.isEmpty()) ? newValues : existingValues;
    }

    private Collection resolveSymbolValues(Collection existingValues, Collection newValues) {
        return (existingValues.isEmpty()) ? newValues : existingValues;
    }
}
