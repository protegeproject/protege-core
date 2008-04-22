package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

class MethodCall {
    private Method _method;
    private Object[] _args;
    private int _hashCode;

    private static final Object[] NULL_ARGS = new Object[0];

    MethodCall() {
    }

    MethodCall(Method m, Object[] args) {
        Object[] safeArgs = null;
        if (args != null) {
            safeArgs = new Object[args.length];
            for (int i = 0; i < args.length; ++i) {
                Object o = args[i];
                if (o instanceof Collection) {
                    o = new ArrayList((Collection) o);
                    // Log.getLogger().info("allocating collection");
                }
                safeArgs[i] = o;
            }
        }
        set(m, safeArgs);
    }

    public void set(Method m, Object[] args) {
        _method = m;
        _args = (args == null) ? NULL_ARGS : args;
        _hashCode = HashUtils.getHash(m, _args);
    }

    public int hashCode() {
        return _hashCode;
    }

    public boolean equals(Object o) {
        MethodCall rhs = (MethodCall) o;
        boolean equals = _method.equals(rhs._method);
        equals &= _args.length == rhs._args.length;
        for (int i = 0; equals && i < _args.length; ++i) {
            //ESCA-JAVA0119 
            equals = _args[i] == rhs._args[i];
        }
        return equals;
    }

    public String toString() {
        return "MethodCall";
    }
}