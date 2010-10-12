package edu.stanford.smi.protege.model.framestore;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;

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
        RemoteSession session = ServerFrameStore.getCurrentSession();
        try {
            store.setCaching(session, false);
            return calculateClosure(store, frame, slot, facet, isTemplate, new LinkedHashSet());
        }
        finally {
            store.setCaching(session, true);
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
