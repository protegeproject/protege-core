package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * All queries go to all frame stores.  Writes go to the primary (delegate) frame store.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _MergingFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private Collection secondaryFrameStores = new ArrayList();

    public static FrameStore newInstance() {
        return newInstance(_MergingFrameStoreHandler.class);
    }

    public void addSecondaryFrameStore(FrameStore fs) {
        secondaryFrameStores.add(fs);
    }

    public void removeSecondaryFrameStore(FrameStore fs) {
        secondaryFrameStores.remove(fs);
    }

    protected Object handleInvoke(Method method, Object[] arguments) {
        Object returnValue;
        if (isQuery(method)) {
            returnValue = handleQuery(method, arguments);
        } else {
            returnValue = handleModification(method, arguments);
        }
        return returnValue;
    }

    protected Object handleQuery(Method method, Object[] arguments) {
        Object returnValue = invoke(method, arguments);
        Iterator i = secondaryFrameStores.iterator();
        while (i.hasNext()) {
            FrameStore frameStore = (FrameStore) i.next();
            Object secondaryReturnValue = invoke(method, arguments, frameStore);
            returnValue = merge(returnValue, secondaryReturnValue);
        }
        return returnValue;
    }

    protected Object merge(Object primary, Object secondary) {
        Object returnValue = null;
        if (primary == null) {
            returnValue = secondary;
        } else if (secondary == null) {
            returnValue = primary;
        } else if (primary instanceof String) {
            returnValue = mergeStrings((String) primary, (String) secondary);
        } else if (primary instanceof Frame) {
            returnValue = mergeFrames((Frame) primary, (Frame) secondary);
        } else if (primary instanceof Integer) {
            returnValue = mergeIntegers((Integer) primary, (Integer) secondary);
        } else if (primary instanceof List) {
            returnValue = mergeLists((List) primary, (List) secondary);
        } else if (primary instanceof Set) {
            returnValue = mergeSets((Set) primary, (Set) secondary);
        } else if (primary instanceof Collection) {
            returnValue = mergeCollections((Collection) primary, (Collection) secondary);
        } else {
            throw new UnsupportedOperationException(primary.toString());
        }
        return returnValue;
    }

    private String mergeStrings(String s1, String s2) {
        Log.getLogger().warning("Unable to merge: " + s1 + " " + s2);
        return s1;
    }

    private Frame mergeFrames(Frame f1, Frame f2) {
        Log.getLogger().warning("Unable to merge: " + f1 + " " + f2);
        return f1;
    }

    private Integer mergeIntegers(Integer i1, Integer i2) {
        return new Integer(i1.intValue() + i2.intValue());
    }

    private List mergeLists(List list1, List list2) {
        List merged;
        if (list1.isEmpty()) {
            merged = list2;
        } else if (list2.isEmpty()) {
            merged = list1;
        } else {
            merged = new ArrayList();
            merged.addAll(list1);
            merged.addAll(list2);
        }
        return merged;
    }

    private Set mergeSets(Set set1, Set set2) {
        Set merged;
        if (set1.isEmpty()) {
            merged = set2;
        } else if (set2.isEmpty()) {
            merged = set1;
        } else {
            merged = new LinkedHashSet();
            merged.addAll(set1);
            merged.addAll(set2);
        }
        return merged;
    }

    private Collection mergeCollections(Collection collection1, Collection collection2) {
        Collection merged;
        if (collection1.isEmpty()) {
            merged = collection2;
        } else if (collection2.isEmpty()) {
            merged = collection1;
        } else {
            merged = new ArrayList();
            merged.addAll(collection1);
            merged.addAll(collection2);
        }
        return merged;
    }

    protected Object handleModification(Method method, Object[] arguments) {
        return invoke(method, arguments);
    }

}