package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;

/**
 * All queries go to all frame stores.  Writes go to the primary (delegate) frame store.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MergingBasicFrameStore implements NarrowFrameStore {
    private List frameStores = new ArrayList();

    public void setDelegate(NarrowFrameStore fs) {
        if (frameStores.isEmpty()) {
            frameStores.add(fs);
        } else {
            frameStores.set(0, fs);
        }
    }

    public NarrowFrameStore getDelegate() {
        return (NarrowFrameStore) frameStores.get(0);
    }

    public void addFrameStore(NarrowFrameStore fs) {
        frameStores.add(fs);
    }

    public void removeFrameStore(NarrowFrameStore fs) {
        frameStores.remove(fs);
    }

    public FrameID generateFrameID() {
        return getDelegate().generateFrameID();
    }

    public int getFrameCount() {
        int count = 0;
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getFrameCount();
        }
        return count;
    }
    
    public Set getFrames() {
        throw new UnsupportedOperationException();
    }

    public int getClsCount() {
        int count = 0;
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getClsCount();
        }
        return count;
    }

    public int getSlotCount() {
        int count = 0;
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getSlotCount();
        }
        return count;
    }

    public int getFacetCount() {
        int count = 0;
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getFacetCount();
        }
        return count;
    }

    public int getSimpleInstanceCount() {
        int count = 0;
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getSimpleInstanceCount();
        }
        return count;
    }

    public Frame getFrame(FrameID id) {
        Frame frame = null;
        Iterator i = frameStores.iterator();
        while (i.hasNext() && frame == null) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frame = fs.getFrame(id);
        }
        return frame;
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = new ArrayList();
        Iterator i = frameStores.iterator();
        while (i.hasNext() && frame == null) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            values.addAll(fs.getValues(frame, slot, facet, isTemplate));
        }
        return values;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        int count = 0;
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getValuesCount(frame, slot, facet, isTemplate);
        }
        return count;
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        getDelegate().addValues(frame, slot, facet, isTemplate, values);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        getDelegate().moveValue(frame, slot, facet, isTemplate, from, to);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        getDelegate().removeValue(frame, slot, facet, isTemplate, value);
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        getDelegate().setValues(frame, slot, facet, isTemplate, values);
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Set frames = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getFrames(slot, facet, isTemplate, value));
        }
        return frames;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getFramesWithAnyValue(slot, facet, isTemplate));
        }
        return frames;
    }
    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        Set frames = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext() && frames.size() < maxMatches) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getMatchingFrames(slot, facet, isTemplate, value, maxMatches - frames.size()));
        }
        return frames;
    }
    public Set getReferences(Object value) {
        Set references = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            references.addAll(fs.getReferences(value));
        }
        return references;
    }
    public Set getMatchingReferences(String value, int maxMatches) {
        Set references = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext() && references.size() < maxMatches) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            references.addAll(fs.getMatchingReferences(value, maxMatches - references.size()));
        }
        return references;
    }

    public Set executeQuery(Query query) {
        Set results = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            results.addAll(fs.executeQuery(query));
        }
        return results;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getClosure(frame, slot, facet, isTemplate));
        }
        return frames;
    }

    public void deleteFrame(Frame frame) {
        getDelegate().deleteFrame(frame);
    }
    public void close() {
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            fs.close();
        }
        frameStores.clear();
    }

    public void replaceFrame(Frame frame) {
        getDelegate().replaceFrame(frame);
    }
    public boolean beginTransaction(String name) {
        return getDelegate().beginTransaction(name);
    }
    public boolean commitTransaction() {
        return getDelegate().commitTransaction();
    }
    public boolean rollbackTransaction() {
        return getDelegate().rollbackTransaction();
    }
}