package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClosureUtils {

    public static Set calculateClosure(
        NarrowFrameStore store,
        Frame frame,
        Slot slot,
        Facet facet,
        boolean isTemplate) {
        return calculateClosure(store, frame, slot, facet, isTemplate, new LinkedHashSet());
    }

    // TODO It would be preferable if this method returned a breadth first closure 
    private static Set calculateClosure(
        NarrowFrameStore store,
        Frame frame,
        Slot slot,
        Facet facet,
        boolean isTemplate,
        Set values) {
        Iterator i = store.getValues(frame, slot, facet, isTemplate).iterator();
        while (i.hasNext()) {
            Object o = i.next();
            boolean changed = values.add(o);
            if (changed && o instanceof Frame) {
                calculateClosure(store, (Frame) o, slot, facet, isTemplate, values);
            }
        }
        return values;
    }

}
