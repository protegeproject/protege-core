package edu.stanford.smi.protege.util;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * Base class for all events.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractEvent extends EventObject implements Localizable {
    private int _eventType;
    private Object _source;
    private Object _argument1;
    private Object _argument2;
    private Object _argument3;

    protected AbstractEvent(Object source, int type) {
        this(source, type, null, null, null);
    }

    protected AbstractEvent(Object source, int type, Object arg1) {
        this(source, type, arg1, null, null);
    }

    protected AbstractEvent(Object source, int type, Object arg1, Object arg2) {
        this(source, type, arg1, arg2, null);
    }

    protected AbstractEvent(Object source, int type, Object arg1, Object arg2, Object arg3) {
        super(source);
        if (source instanceof Serializable) {
            _source = source;
        }
        _eventType = type;
        _argument1 = arg1;
        _argument2 = arg2;
        _argument3 = arg3;
    }

    public void localize(KnowledgeBase kb) {
        LocalizeUtils.localize(_source, kb);
        LocalizeUtils.localize(_argument1, kb);
        LocalizeUtils.localize(_argument2, kb);
        LocalizeUtils.localize(_argument3, kb);
    }

    protected void setSource(Object o) {
        _source = o;
        source = o;
    }

    public Object getArgument() {
        return getArgument1();
    }

    public Object getArgument1() {
        return _argument1;
    }

    public Object getArgument2() {
        return _argument2;
    }

    public Object getArgument3() {
        return _argument3;
    }

    public int getEventType() {
        return _eventType;
    }

    public Object getSource() {
        return (_source == null) ? super.getSource() : _source;
    }

    public String toString() {
        String text = StringUtilities.getClassName(this);
        return text + "(" + getSource() + ", " + _eventType + ", " + getArgument1() + ", " + getArgument2() + ")";
    }

    public int hashCode() {
        return HashUtils.getHash(source, _argument1, _argument2);
    }

    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof AbstractEvent) {
            AbstractEvent rhs = (AbstractEvent) o;
            equals = equals(source, rhs.source) && equals(_argument1, rhs._argument1)
                    && equals(_argument2, rhs._argument2) && equals(_argument3, rhs._argument3);
        }
        return equals;
    }

    public static boolean equals(Object o1, Object o2) {
        boolean equals;
        if (o1 instanceof Collection && o2 instanceof Collection) {
            equals = CollectionUtilities.equalsSet((Collection) o1, (Collection) o2);
        } else {
            equals = SystemUtilities.equals(o1, o2);
        }
        return equals;
    }
}