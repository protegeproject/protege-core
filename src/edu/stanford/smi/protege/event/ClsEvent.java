package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Event generated when a class is changed.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsEvent extends AbstractEvent {
    private static final long serialVersionUID = -6942618784435546270L;
    private static final int BASE = 300;
    public static final int DIRECT_SUPERCLASS_ADDED = BASE + 1;
    public static final int DIRECT_SUPERCLASS_REMOVED = BASE + 2;
    public static final int DIRECT_SUBCLASS_ADDED = BASE + 3;
    public static final int DIRECT_SUBCLASS_REMOVED = BASE + 4;
    public static final int DIRECT_INSTANCE_ADDED = BASE + 5;
    public static final int DIRECT_INSTANCE_REMOVED = BASE + 6;
    public static final int DIRECT_SUBCLASS_MOVED = BASE + 7;

    public static final int TEMPLATE_SLOT_ADDED = BASE + 8;
    public static final int TEMPLATE_SLOT_REMOVED = BASE + 9;
    public static final int TEMPLATE_SLOT_VALUE_CHANGED = BASE + 10;
    public static final int TEMPLATE_FACET_ADDED = BASE + 11;
    public static final int TEMPLATE_FACET_REMOVED = BASE + 12;
    public static final int TEMPLATE_FACET_VALUE_CHANGED = BASE + 13;

    public ClsEvent(Cls cls, int type, Object argument) {
        super(cls, type, argument);
    }

    public ClsEvent(Cls cls, int type, Object argument1, Object argument2) {
        super(cls, type, argument1, argument2);
    }

    public Cls getCls() {
        return (Cls) getSource();
    }

    public Facet getFacet() {
        return (Facet) getArgument2();
    }

    public Instance getInstance() {
        return (Instance) getArgument1();
    }

    public Slot getSlot() {
        return (Slot) getArgument1();
    }

    public Cls getSubclass() {
        Cls cls;
        Object o = getArgument1();
        if (o instanceof Cls) {
            cls = (Cls) o;
        } else {
            cls = null;
            Log.getLogger().severe("invalid cls: " + o);
        }
        return cls;
    }

    public Cls getSuperclass() {
        return (Cls) getArgument1();
    }
    
    public boolean isDeletingClsEvent() {
        return getCls().isBeingDeleted();
    }
}
