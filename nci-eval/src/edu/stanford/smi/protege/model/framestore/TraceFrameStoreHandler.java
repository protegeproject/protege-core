package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

public class TraceFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private Map methodCounts = new HashMap();

    public Object handleInvoke(Method m, Object[] args) {
        Object o = invoke(m, args);
        trace(m, args);
        return o;
    }

    private void trace(Method method, Object[] args) {
        Integer current = (Integer) methodCounts.get(method);
        Integer next = (current == null) ? new Integer(1) : new Integer(current.intValue() + 1);
        methodCounts.put(method, next);
        print(next.intValue(), method, args);
    }
    
    public static void print(int next, Method method, Object[] args) {
        System.out.println(next + " " + method.getName() + " " + argString(args));
    }
    private static String argString(Object[] args) {
        StringBuffer buffer = new StringBuffer();
        if (args != null) {
	        for (int i = 0; i < args.length; ++i) {
	            Object o = args[i];
	            if (o instanceof Frame) {
	                o = ((Frame)o).getFrameID();
	            }
	            buffer.append(o);
	            buffer.append(" ");
	        }
        }
        return buffer.toString();
    }
}

