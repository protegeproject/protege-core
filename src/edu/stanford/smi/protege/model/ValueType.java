package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * An enumeration class for the types of slots.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ValueType implements Comparable {
    private static int _nValues;
    private static Map _values = CollectionUtilities.createSmallMap(); // <string, ValueType>
    private String _string;
    private int _intValue;
    private Class _javaType;

    public final static ValueType ANY = new ValueType("Any", Object.class);
    public final static ValueType BOOLEAN = new ValueType("Boolean", Boolean.class);
    public final static ValueType CLS = new ValueType("Class", Cls.class);
    public final static ValueType FLOAT = new ValueType("Float", Float.class);
    public final static ValueType INSTANCE = new ValueType("Instance", Instance.class);
    public final static ValueType INTEGER = new ValueType("Integer", Integer.class);
    public final static ValueType STRING = new ValueType("String", String.class);
    public final static ValueType SYMBOL = new ValueType("Symbol", String.class);
    
    private ValueType(String s, Class javaType) {
        _values.put(s, this);
        _string = s;
        _javaType = javaType;
        _intValue = _nValues++;
    }

    public int getIntValue() {
        return _intValue;
    }

    public Class getJavaType() {
        return _javaType;
    }

    /** @return String representations of all of the allowed value-types */
    public static Collection getValues() {
        return Collections.unmodifiableCollection(_values.values());
    }

    /** @return a String representation of this values-type (e.g. "String") */
    public String toString() {
        return _string;
    }

    /** @param s a String representation of a value-type.  Normally this will have
     *  been obtained from a previous call to "toString()" on a ValueType object.
     *  @return the corresponding value-type
     */
    public static ValueType valueOf(String s) {
        ValueType type = (ValueType) _values.get(s);
        Assert.assertNotNull("type", type);
        return type;
    }

    public int compareTo(Object o) {
        return _string.compareTo(o.toString());
    }
}
