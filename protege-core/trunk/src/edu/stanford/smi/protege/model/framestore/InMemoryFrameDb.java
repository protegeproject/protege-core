package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

public class InMemoryFrameDb implements NarrowFrameStore {
    private static final int INITIAL_MAP_SIZE = 32771;
    private Map _valueToReferencesMap = new LinkedHashMap(INITIAL_MAP_SIZE);
    private Map _referenceToValuesMap = new LinkedHashMap(INITIAL_MAP_SIZE);
    private Map _frameToContainingReferencesMap = new HashMap(INITIAL_MAP_SIZE);
    private ReferenceImpl _lookupReference = new ReferenceImpl();
    private String frameDBName;

    private static int counter = FrameID.INITIAL_USER_FRAME_ID;

    public String getName() {
        return frameDBName;
    }

    public void setName(String name) {
        frameDBName = name;
    }

    public InMemoryFrameDb() {
        this("InMemoryFrameDb");
    }

    public InMemoryFrameDb(String name) {
        if (name != null) {
            frameDBName = name;
        }
    }

    public FrameID generateFrameID() {
        return FrameID.createLocal(counter++);
    }

    public List createList() {
        // return new HashList();
        return new ArrayList();
    }

    public Set createSet() {
        return new LinkedHashSet();
    }

    public void close() {
        _valueToReferencesMap = null;
        _referenceToValuesMap = null;
        _lookupReference = null;
    }

    private void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        insert(frame, slot, facet, isTemplate, value);
    }

    private List lookupValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        _lookupReference.set(frame, slot, facet, isTemplate);
        return lookupValues(_lookupReference);
    }

    private List lookupValues(Reference ref) {
        return (List) _referenceToValuesMap.get(ref);
    }

    private Set lookupReferences(Object value) {
        return (Set) _valueToReferencesMap.get(value);
    }

    private void insert(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Reference ref = createReference(frame, slot, facet, isTemplate);
        getOrCreateValues(ref).add(value);
        getOrCreateReferences(value).add(ref);
    }

    private Reference createReference(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Reference ref = new ReferenceImpl(frame, slot, facet, isTemplate);
        addReference(frame, ref);
        addReference(slot, ref);
        if (facet != null) {
            addReference(facet, ref);
        }
        return ref;
    }

    private void removeReference(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        _lookupReference.set(frame, slot, facet, isTemplate);
        removeReference(frame, _lookupReference);
        removeReference(slot, _lookupReference);
        if (facet != null) {
            removeReference(facet, _lookupReference);
        }
    }

    private void addReference(Frame frame, Reference ref) {
        Collection c = (Collection) _frameToContainingReferencesMap.get(frame);
        if (c == null) {
            c = createSet();
            _frameToContainingReferencesMap.put(frame, c);
        }
        c.add(ref);
    }

    private void removeReference(Frame frame, Reference ref) {
        Collection c = (Collection) _frameToContainingReferencesMap.get(frame);
        if (c != null) {
            c.remove(ref);
        }
    }

    private void insert(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Reference ref = createReference(frame, slot, facet, isTemplate);
        getOrCreateValues(ref).addAll(values);

        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            getOrCreateReferences(value).add(ref);
        }
    }

    private Set getOrCreateReferences(Object value) {
        Set references = lookupReferences(value);
        if (references == null) {
            references = createSet();
            _valueToReferencesMap.put(value, references);
        }
        return references;
    }

    private List getOrCreateValues(Reference ref) {
        List values = lookupValues(ref);
        if (values == null) {
            values = createList();
            _referenceToValuesMap.put(ref, values);
        }
        return values;
    }

    private void remove(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _lookupReference.set(frame, slot, facet, isTemplate);
        safeRemove(lookupReferences(value), _lookupReference);
        safeRemove(lookupValues(_lookupReference), value);
    }

    private void safeRemove(Collection c, Object v) {
        if (c != null) {
            if (c.size() > 100 && c instanceof List) {
                // Log.trace("Big collection: " + c.size() + " " + c.getClass(),
                // this, "safeRemove", v);
            }
            c.remove(v);
        }
    }

    private void remove(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        _lookupReference.set(frame, slot, facet, isTemplate);
        removeReference(_lookupReference);
    }

    private void removeReference(Reference ref) {
        List values = lookupValues(ref);
        if (values != null) {
            Iterator i = values.iterator();
            while (i.hasNext()) {
                Object value = i.next();
                lookupReferences(value).remove(ref);
            }
            _referenceToValuesMap.remove(ref);
        }
    }

    private void removeValue(Object value) {
        Collection references = lookupReferences(value);
        if (references != null) {
            Iterator i = references.iterator();
            while (i.hasNext()) {
                Reference ref = (Reference) i.next();
                lookupValues(ref).remove(value);
            }
            _valueToReferencesMap.remove(value);
        }
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        remove(frame, slot, facet, isTemplate, value);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        List values = lookupValues(frame, slot, facet, isTemplate);
        if (0 <= from && from < values.size() && 0 <= to && to < values.size()) {
            Object value = values.remove(from);
            values.add(to, value);
        } else {
            Log.getLogger().warning("Invalid index from=" + from + ", to=" + to);
        }
    }

    public Set getReferences(Object value) {
        Collection references = lookupReferences(value);
        return (references == null) ? Collections.EMPTY_SET : new HashSet(references);
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        if (maxMatches < 1) {
            maxMatches = Integer.MAX_VALUE;
        }
        SimpleStringMatcher matcher = new SimpleStringMatcher(value);
        Set matches = new HashSet();
        Iterator i = _valueToReferencesMap.entrySet().iterator();
        while (i.hasNext() && matches.size() < maxMatches) {
            Map.Entry entry = (Map.Entry) i.next();
            Object o = entry.getKey();
            if (o instanceof String && matcher.isMatch((String) o)) {
                Collection refs = (Collection) entry.getValue();
                matches.addAll(refs);
            }
        }
        return matches;
    }

    /** TODO implement executeQuery */
    public Set executeQuery(Query query) {
        return null;
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        remove(frame, slot, facet, isTemplate);
        if (values != null && !values.isEmpty()) {
            insert(frame, slot, facet, isTemplate, values);
        } else {
            removeReference(frame, slot, facet, isTemplate);
        }
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addValue(frame, slot, facet, isTemplate, value);
        }
    }

    private boolean _isInferred(Slot slot, Facet facet, boolean isTemplate) {
        boolean isInferred = facet == null && !isTemplate;
        if (isInferred) {
            FrameID id = slot.getFrameID();
            isInferred = id.equals(Model.SlotID.DIRECT_SUBCLASSES) || id.equals(Model.SlotID.DIRECT_SUBSLOTS)
                    || id.equals(Model.SlotID.DIRECT_INSTANCES);
        }
        return isInferred;
        // return false;
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values;
        if (false && _isInferred(slot, facet, isTemplate)) {
            Slot inverseSlot = getInverseSlot(slot);
            values = new ArrayList(getFrames(inverseSlot, null, false, frame));
        } else {
            values = lookupValues(frame, slot, facet, isTemplate);
        }
        return (values == null) ? Collections.EMPTY_LIST : Collections.unmodifiableList(values);
    }

    private Slot getInverseSlot(Slot slot) {
        Slot inverse = null;
        KnowledgeBase kb = slot.getKnowledgeBase();
        FrameID id = slot.getFrameID();
        if (id.equals(Model.SlotID.DIRECT_SUBCLASSES)) {
            inverse = new DefaultSlot(kb, Model.SlotID.DIRECT_SUPERCLASSES);
        } else if (id.equals(Model.SlotID.DIRECT_SUBSLOTS)) {
            inverse = new DefaultSlot(kb, Model.SlotID.DIRECT_SUPERSLOTS);
        } else if (id.equals(Model.SlotID.DIRECT_INSTANCES)) {
            inverse = new DefaultSlot(kb, Model.SlotID.DIRECT_TYPES);
        }
        return inverse;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Collection values = lookupValues(frame, slot, facet, isTemplate);
        return (values == null) ? 0 : values.size();
    }

    private void removeFrameAsReferenceElement(Frame frame) {
        /*
         * Iterator i = new ArrayList(_referenceToValuesMap.keySet()).iterator(); while (i.hasNext()) { Reference
         * reference = (Reference) i.next(); if (usesFrame(reference, frame)) { removeReference(reference); } }
         */
        Collection references = (Collection) _frameToContainingReferencesMap.remove(frame);
        Iterator i = references.iterator();
        while (i.hasNext()) {
            Reference reference = (Reference) i.next();
            removeReference(reference);
        }
    }

    public void deleteFrame(Frame frame) {
        // Log.enter(this, "deleteFrame", frame);
        removeFrameAsReferenceElement(frame);
        removeValue(frame);
    }

    /*
     * private static boolean usesFrame(Reference reference, Frame frame) { return equals(frame, reference.getFrame()) ||
     * equals(frame, reference.getSlot()) || equals(frame, reference.getFacet()); }
     */

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private static boolean matches(Reference reference, Slot slot, Facet facet, boolean isTemplate) {
        boolean matches = equals(slot, reference.getSlot()) && equals(facet, reference.getFacet())
                && isTemplate == reference.isTemplate();
        return matches;
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Set frames = new HashSet();
        Iterator i = getReferences(value).iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            if (matches(ref, slot, facet, isTemplate)) {
                frames.add(ref.getFrame());
            }
        }
        return frames;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        /*
         * Iterator i = _referenceToValuesMap.keySet().iterator(); while (i.hasNext()) { Reference ref = (Reference)
         * i.next(); if (matches(ref, slot, facet, isTemplate)) { frames.add(ref.getFrame()); } }
         */
        Collection slotReferences = (Collection) _frameToContainingReferencesMap.get(slot);
        if (slotReferences != null) {
            Iterator i = slotReferences.iterator();
            while (i.hasNext()) {
                Reference ref = (Reference) i.next();
                if (matches(ref, slot, facet, isTemplate)) {
                    frames.add(ref.getFrame());
                }
            }
        }
        return frames;
    }

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        if (maxMatches < 1) {
            maxMatches = Integer.MAX_VALUE;
        }
        Set frames = new HashSet();
        Iterator i = getMatchingReferences(value, FrameStore.UNLIMITED_MATCHES).iterator();
        while (i.hasNext() && frames.size() < maxMatches) {
            Reference ref = (Reference) i.next();
            if (matches(ref, slot, facet, isTemplate)) {
                frames.add(ref.getFrame());
            }
        }
        return frames;
    }

    public boolean beginTransaction(String transactionName) {
        return false;
    }

    public boolean commitTransaction() {
        return true;
    }

    public boolean rollbackTransaction() {
        return false;
    }

    public void replaceFrame(Frame frame) {
        replaceFrameInReferenceToValuesMap(frame);
        replaceFrameInValueToReferencesMap(frame);
    }

    private void replaceFrameInValueToReferencesMap(Frame frame) {
        replaceFrameKeyInValueToReferenceMap(frame);
        replaceFrameValueInValueToReferencesMap(frame);
    }

    private void replaceFrameValueInValueToReferencesMap(Frame frame) {
        Iterator i = _valueToReferencesMap.values().iterator();
        while (i.hasNext()) {
            Collection references = (Collection) i.next();
            Iterator j = references.iterator();
            while (j.hasNext()) {
                ReferenceImpl ref = (ReferenceImpl) j.next();
                replace(ref, frame);
            }
        }
    }

    private void replaceFrameKeyInValueToReferenceMap(Frame frame) {
        Collection frameReferences = (Collection) _valueToReferencesMap.remove(frame);
        if (frameReferences != null) {
            _valueToReferencesMap.put(frame, frameReferences);
        }
    }

    /*
     * private void replaceFrameInFrameToContainingReferencesMap(Frame frame) { Collection containingReferences =
     * (Collection) _frameToContainingReferencesMap.get(frame); Iterator i = containingReferences.iterator(); while
     * (i.hasNext()) { ReferenceImpl ref = (ReferenceImpl) i.next(); replace(ref, frame); } }
     */

    private void replaceFrameInReferenceToValuesMap(Frame frame) {
        Iterator i = _referenceToValuesMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            ReferenceImpl ref = (ReferenceImpl) entry.getKey();
            replace(ref, frame);
            List values = (List) entry.getValue();
            replace(values, frame);
        }
    }

    private void replace(List values, Frame frame) {
        ListIterator i = values.listIterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (equals(o, frame)) {
                i.set(frame);
            }
        }
    }

    private void replace(ReferenceImpl ref, Frame frame) {
        ref.replace(frame);
    }

    public int getClsCount() {
        return countUniqueValues(Cls.class);
    }

    public int getSlotCount() {
        return countUniqueValues(Slot.class);
    }

    public int getFacetCount() {
        return countUniqueValues(Facet.class);
    }

    public int getFrameCount() {
        return countUniqueValues(Frame.class);
    }

    public Set getFrames() {
        return uniqueValues(Frame.class);
    }

    public int getSimpleInstanceCount() {
        return countUniqueValues(SimpleInstance.class);
    }

    private int countUniqueValues(Class clas) {
        return uniqueValues(clas).size();
    }

    private Set uniqueValues(Class clas) {
        Slot nameSlot = null;
        Set uniqueValues = new HashSet();
        Iterator i = _valueToReferencesMap.keySet().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (clas.isInstance(o)) {
                Frame frame = (Frame) o;
                if (nameSlot == null) {
                    nameSlot = new DefaultSlot(frame.getKnowledgeBase(), Model.SlotID.NAME);
                }
                if (lookupValues(frame, nameSlot, null, false) != null) {
                    uniqueValues.add(o);
                }
            }
        }
        return uniqueValues;
    }

    // We can't mysteriously produce any frames down here. If we don't have one
    // by now we aren't going to get one.
    public Frame getFrame(FrameID id) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return frameDBName;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
    }

    public NarrowFrameStore getDelegate() {
        return null;
    }

}