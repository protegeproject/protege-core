package edu.stanford.smi.protege.model.framestore;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.job.CacheControlJob;

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
        KnowledgeBase kb = frame.getKnowledgeBase();
        boolean isInitializing = (kb.getFrameStoreManager() == null);
        try {
            if (!isInitializing) {
                new CacheControlJob(kb, false, true).execute();  // TODO - breaks layering - we really should have a generic NarrowFrameStore method
            }
            return calculateClosure(store, frame, slot, facet, isTemplate, new LinkedHashSet());
        }
        finally {
            if (!isInitializing) {
                new CacheControlJob(kb, false, false).execute();
            }
        }
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
