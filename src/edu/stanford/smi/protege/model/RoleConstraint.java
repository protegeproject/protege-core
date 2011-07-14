package edu.stanford.smi.protege.model;

import java.util.*;

/**
 * Value which determines whether a class can have direct instances.  Abstract classes can have no direct instances.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class RoleConstraint extends AbstractFacetConstraint {
    private static final long serialVersionUID = -8969445861845694716L;
    public static final String ABSTRACT = "Abstract";
    public static final String CONCRETE = "Concrete";
    private static final Collection values;

    static {
        values = new ArrayList();
        values.add(ABSTRACT);
        values.add(CONCRETE);
    }

    public static Collection getValues() {
        return Collections.unmodifiableCollection(values);
    }

    public static boolean isAbstract(String s) {
        return ABSTRACT.equals(s);
    }
}
