package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.*;

public class TraceFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private Map<Method, Integer> methodCounts = new HashMap<Method, Integer>();
    private static Method executeQueryMethod;
    static {
      try {
        executeQueryMethod 
          = FrameStore.class.getMethod("executeQuery", 
                                       new Class[] {Query.class, QueryCallback.class});
      } catch (Exception e) {
        Log.getLogger().warning("Non-fatal Problem encountered finding executeQuery method - contact developers");
      }
    }
    
    public Object handleInvoke(Method m, Object[] args) {
        Object o = invoke(m, args);
        trace(m, args);
        return o;
    }
    
    @Override
    protected void executeQuery(Query q, QueryCallback qc) {
      getDelegate().executeQuery(q, qc);
      Integer next = updateMethodCount(executeQueryMethod);
      print(next, executeQueryMethod, new Object[] {q, qc});
    }

    private void trace(Method method, Object[] args) {
        Integer next = updateMethodCount(method);
        print(next.intValue(), method, args);
    }
    
    private Integer updateMethodCount(Method method) {
      Integer current = (Integer) methodCounts.get(method);
      Integer next = (current == null) ? new Integer(1) : new Integer(current.intValue() + 1);
      methodCounts.put(method, next);
      return next;
    }

    public static void print(int next, Method method, Object[] args) {
        Log.getLogger().info(next + " " + method.getName() + " " + argString(args));
    }

    private static String argString(Object[] args) {
        StringBuffer buffer = new StringBuffer();
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                Object o = args[i];
                if (o instanceof Frame) {
                    o = ((Frame) o).getFrameID();
                }
                buffer.append(o);
                buffer.append(" ");
            }
        }
        return buffer.toString();
    }


}
