package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

public class ArgumentCheckingFrameStoreHandler extends AbstractFrameStoreInvocationHandler {

    public Object handleInvoke(Method m, Object[] args) {
        checkArguments(m, args);
        return invoke(m, args);
    }

    private void checkArguments(Method m, Object[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                Object arg = args[i];
                checkArgument(m, i, arg);
            }
        }
    }

    private void checkArgument(Method m, int position, Object arg) {
        if (arg == null && !allowedNull(m, position)) {
            throw new IllegalArgumentException("Null argument for method " + m.getName() + " at position: " + position);
        } else if (arg instanceof Frame) {
            checkFrame(m, position, (Frame) arg);
        } else if (arg instanceof Collection) {
            checkCollection(m, position, (Collection) arg);
        }
    }

    private void checkFrame(Method m, int position, Frame arg) {
    }

    private void checkValue(Method m, int position, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Null collection element for method " + m.getName() + " at position: " + position);
        } else if (!isValidClass(value)) {
            throw new IllegalArgumentException(
                "Invalid collection element " + value + " for method " + m.getName() + " at position: " + position);
        }
    }

    private boolean isValidClass(Object value) {
        return value instanceof Boolean
            || value instanceof Float
            || value instanceof Integer
            || value instanceof Frame
            || value instanceof String;
    }

    private void checkCollection(Method m, int position, Collection arg) {
        Iterator i = arg.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            checkValue(m, position, o);
        }
    }

    private boolean allowedNull(Method m, int position) {
        String name = m.getName();
        return (name.startsWith("create") && (position == 0 || position == 1))
            || (name.startsWith("setAssociated") && position == 1)
            || (name.startsWith("setInverseSlot") && position == 1);
    }
}

