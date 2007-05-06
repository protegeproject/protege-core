package edu.stanford.smi.protege.model;

import java.util.*;

/**
 * Value which determines whether a class can have direct instances.  Abstract classes can have no direct instances.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class RoleConstraint extends AbstractFacetConstraint {
    public final static String ABSTRACT = "Abstract";
    public final static String CONCRETE = "Concrete";
    private final static Collection _values;

    static {
        _values = new ArrayList();
        _values.add(ABSTRACT);
        _values.add(CONCRETE);
    }

    public static Collection getValues() {
        return _values;
    }

    public static boolean isAbstract(String s) {
        return ABSTRACT.equals(s);
    }
}
