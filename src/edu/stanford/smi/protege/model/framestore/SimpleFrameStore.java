package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

public class SimpleFrameStore implements FrameStore {
    private KnowledgeBase _kb;
    private SystemFrames _systemFrames;
    private NarrowFrameStore _helper;
    private Set _inheritedSuperslotSlots = new HashSet();
    private CacheMap nameToFrameMap = new CacheMap();

    // private Map frameIdToFrameMap = new HashMap();

    public String getName() {
        return getClass().getName();
    }

    public SimpleFrameStore(KnowledgeBase kb, NarrowFrameStore helper) {
        setHelper(helper);
        if (kb != null) {
            _kb = kb;
            _systemFrames = kb.getSystemFrames();
            loadInheritedSuperslotSlots();
        }
    }

    private void loadInheritedSuperslotSlots() {
        _inheritedSuperslotSlots.add(_systemFrames.getDirectDomainSlot());
        _inheritedSuperslotSlots.add(_systemFrames.getValueTypeSlot());
        _inheritedSuperslotSlots.add(_systemFrames.getMaximumCardinalitySlot());
        _inheritedSuperslotSlots.add(_systemFrames.getMinimumValueSlot());
        _inheritedSuperslotSlots.add(_systemFrames.getMaximumValueSlot());
    }

    public void reinitialize() {
        nameToFrameMap.clear();
    }

    protected void setHelper(NarrowFrameStore helper) {
        _helper = helper;
    }

    public NarrowFrameStore getHelper() {
        return _helper;
    }

    public Set executeQuery(Query query) {
        return _helper.executeQuery(query);
    }

    public void deleteCls(Cls cls) {
        deleteFrame(cls);
    }

    /**
     * This method should not be down here in the frame store chain.
     * 
     * @return Collection of Clses
     */
    public static Collection getClsesToBeDeleted(Cls cls, FrameStore fs) {
        Collection subclasses = fs.getSubclasses(cls);
        Collection clsesToBeDeleted = new HashSet(subclasses);
        clsesToBeDeleted.add(cls);

        Iterator i = subclasses.iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            // take care with recursive inheritance situations!
            if (!subclass.equals(cls) && reachableByAnotherRoute(subclass, clsesToBeDeleted, fs)) {
                clsesToBeDeleted.remove(subclass);
                Collection subsubclasses = new HashSet(fs.getSubclasses(subclass));
                subsubclasses.remove(cls);
                clsesToBeDeleted.removeAll(subsubclasses);
            }
        }
        return clsesToBeDeleted;
    }

    private static boolean reachableByAnotherRoute(Cls subclass, Collection classesToBeDeleted, FrameStore fs) {
        boolean reachable = false;
        Collection superclasses = fs.getDirectSuperclasses(subclass);
        if (superclasses.size() > 1) {
            Iterator j = superclasses.iterator();
            while (j.hasNext()) {
                Cls superclass = (Cls) j.next();
                if (!classesToBeDeleted.contains(superclass)) {
                    reachable = true;
                    break;
                }
            }
        }
        return reachable;
    }

    public void deleteSlot(Slot slot) {
        deleteFrame(slot);
    }

    public void deleteFacet(Facet facet) {
        deleteFrame(facet);
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        deleteFrame(simpleInstance);
    }

    private void deleteFrame(Frame frame) {
        nameToFrameMap.remove(getFrameName(frame));
        _helper.deleteFrame(frame);
    }

    public Set getReferences(Object value) {
        Set references = _helper.getReferences(value);
        return unmodifiableSet(references);
    }

    public void close() {
        _kb = null;
        _systemFrames = null;
        _helper.close();
        _helper = null;
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        return _helper.getMatchingReferences(value, maxMatches);
    }

    public Set getClsesWithMatchingBrowserText(String value, Collection superclasses, int maxMatches) {
        StringMatcher matcher = new SimpleStringMatcher(value);
        Set clses = new HashSet();
        Set references = _helper.getMatchingReferences(value, FrameStore.UNLIMITED_MATCHES);
        Iterator i = references.iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            Frame frame = ref.getFrame();
            if (frame instanceof Cls) {
                Cls cls = (Cls) frame;
                if (matcher.isMatch(cls.getBrowserText()) && isSubclassMatch(cls, superclasses)) {
                    clses.add(cls);
                    if (maxMatches != UNLIMITED_MATCHES && clses.size() == maxMatches) {
                        break;
                    }
                }
            }
        }
        return clses;
    }

    private static boolean isSubclassMatch(Cls cls, Collection superclasses) {
        boolean isMatch = true;
        if (!superclasses.isEmpty()) {
            Collection clsSuperclasses = new HashSet(cls.getSuperclasses());
            isMatch = clsSuperclasses.removeAll(superclasses);
        }
        return isMatch;
    }

    private FrameFactory getFrameFactory() {
        return _kb.getFrameFactory();
    }

    public int getClsCount() {
        return _helper.getClsCount();
    }

    public int getSlotCount() {
        return _helper.getSlotCount();
    }

    public int getFacetCount() {
        return _helper.getFacetCount();
    }

    public int getSimpleInstanceCount() {
        return _helper.getSimpleInstanceCount();
    }

    public int getFrameCount() {
        return _helper.getFrameCount();
    }

    public Set getClses() {
        return getInstances(_systemFrames.getRootClsMetaCls());
    }

    public Set getSlots() {
        return getInstances(_systemFrames.getRootSlotMetaCls());
    }

    public Set getFacets() {
        return getInstances(_systemFrames.getRootFacetMetaCls());
    }

    public Set getFrames() {
        // return getInstances(_systemFrames.getRootCls());
        return _helper.getFrames();
    }

    public List getDirectTemplateSlots(Cls cls) {
        return getDirectOwnSlotValues(cls, _systemFrames.getDirectTemplateSlotsSlot());
    }

    public List getDirectSuperclasses(Cls cls) {
        return getDirectOwnSlotValues(cls, _systemFrames.getDirectSuperclassesSlot());
    }

    public List getDirectSuperslots(Slot slot) {
        return getDirectOwnSlotValues(slot, _systemFrames.getDirectSuperslotsSlot());
    }

    public List getDirectSubslots(Slot slot) {
        return getDirectOwnSlotValues(slot, _systemFrames.getDirectSubslotsSlot());
    }

    public Set getSuperslots(Slot slot) {
        return getDirectOwnSlotValuesClosure(slot, _systemFrames.getDirectSuperslotsSlot());
    }

    public Set getSubslots(Slot slot) {
        return getDirectOwnSlotValuesClosure(slot, _systemFrames.getDirectSubslotsSlot());
    }

    public Set getSuperclasses(Cls cls) {
        return getDirectOwnSlotValuesClosure(cls, _systemFrames.getDirectSuperclassesSlot());
    }

    public List getDirectSubclasses(Cls cls) {
        return getDirectOwnSlotValues(cls, _systemFrames.getDirectSubclassesSlot());
    }

    public Set getSubclasses(Cls cls) {
        return getDirectOwnSlotValuesClosure(cls, _systemFrames.getDirectSubclassesSlot());
    }

    public Set getTemplateFacets(Cls cls, Slot slot) {
        Collection slots = getOwnSlots(slot);
        Set facets = collectOwnSlotValues(slots, _systemFrames.getAssociatedFacetSlot());
        return unmodifiableSet(facets);
    }

    public Collection getTemplateFacetValues(Cls localCls, Slot slot, Facet facet) {
        Collection values = new ArrayList(getDirectTemplateFacetValues(localCls, slot, facet));
        Iterator i = getSuperclasses(localCls).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            Collection superclassValues = getDirectTemplateFacetValues(cls, slot, facet);
            values = resolveValues(values, superclassValues, facet);
        }
        Slot associatedSlot = (Slot) getDirectOwnSlotValue(facet, _systemFrames.getAssociatedSlotSlot());
        if (associatedSlot != null) {
            // Collection topLevelValues = getDirectOwnSlotValues(slot, associatedSlot);
            Collection topLevelValues = getOwnSlotValues(slot, associatedSlot);
            values = resolveValues(values, topLevelValues, facet);
        }
        return unmodifiableCollection(values);
    }

    private static Collection resolveValues(Collection values, Collection newValues, Facet facet) {
        if (!newValues.isEmpty()) {
            if (values.isEmpty()) {
                values.addAll(newValues);
            } else {
                values = facet.resolveValues(values, newValues);
                if (values == newValues) {
                    values = new ArrayList(values);
                }
            }
        }
        return values;
    }

    public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        /*
         * Collection values = getValuesClosure(cls, slot, null, true, _systemFrames.getDirectSuperclassesSlot());
         * values.addAll(getDirectOwnSlotValues(slot, _systemFrames.getValuesSlot())); return values;
         */
        return getTemplateFacetValues(cls, slot, _systemFrames.getValuesFacet());
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        Set closure = getValuesClosure(frame, slot, null, false);
        return unmodifiableSet(closure);
    }

    private Set getValuesClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return getValuesClosure(frame, slot, facet, isTemplate, slot);
    }

    private Set getValuesClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate, Slot traversalSlot) {
        Set values;
        Set closure = _helper.getClosure(frame, traversalSlot, null, false);
        if (equals(slot, traversalSlot) && facet == null && !isTemplate) {
            values = closure;
        } else {
            values = new HashSet(getValues(frame, slot, facet, isTemplate));
            Iterator i = closure.iterator();
            while (i.hasNext()) {
                Frame traversedFrame = (Frame) i.next();
                values.addAll(getValues(traversedFrame, slot, facet, isTemplate));
            }
        }
        return values;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public List getDirectTypes(Instance instance) {
        return getDirectOwnSlotValues(instance, _systemFrames.getDirectTypesSlot());
    }

    public List getDirectInstances(Cls cls) {
        return getDirectOwnSlotValues(cls, _systemFrames.getDirectInstancesSlot());
    }

    public Set getInstances(Cls cls) {
        Collection clses = new LinkedHashSet(getSubclasses(cls));
        clses.add(cls);
        return collectOwnSlotValues(clses, _systemFrames.getDirectInstancesSlot());
        /*
         * return getOwnSlotValuesClosure( cls, _systemFrames.getDirectSubclassesSlot(),
         * _systemFrames.getDirectInstancesSlot());
         */
    }

    public Set getTypes(Instance instance) {
        List directTypes = getDirectTypes(instance);
        Set types = new LinkedHashSet(directTypes);

        // avoid creating an iterator in this inner loop method
        int nTypes = directTypes.size();
        for (int i = 0; i < nTypes; ++i) {
            Cls type = (Cls) directTypes.get(i);
            types.addAll(getSuperclasses(type));
        }
        return unmodifiableSet(types);
    }

    private static Set unmodifiableSet(Set set) {
        return set == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(set);
        // return set == null ? Collections.EMPTY_SET : set;
    }

    private static List unmodifiableList(List list) {
        return list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(list);
    }

    private static Collection unmodifiableCollection(Collection collection) {
        return collection == null ? Collections.EMPTY_LIST : Collections.unmodifiableCollection(collection);
    }

    // This inner loop has to be optimized
    private Set collectOwnSlotValues(Collection frames, Slot slot) {
        Set values = new LinkedHashSet();
        Object[] frameArray = frames.toArray();
        for (int i = 0; i < frameArray.length; ++i) {
            Frame frame = (Frame) frameArray[i];
            values.addAll(getDirectOwnSlotValues(frame, slot));
        }
        return values;
    }

    public Frame getFrame(String name) {
        Frame frame = (Frame) nameToFrameMap.get(name);
        if (frame == null) {
            Collection c = getFramesWithDirectOwnSlotValue(_systemFrames.getNameSlot(), name);
            frame = (Frame) CollectionUtilities.getFirstItem(c);
            if (frame != null) {
                nameToFrameMap.put(name, frame);
            }
        }
        return frame;
    }

    public Frame getFrame(FrameID id) {
        /*
         * Frame frame = (Frame) frameIdToFrameMap.get(id); if (frame == null) { frame = _helper.getFrame(id);
         * frameIdToFrameMap.put(id, frame); } return frame;
         */
        return _helper.getFrame(id);
    }

    public Set getOwnSlots(Frame frame) {
        Collection types = getTypes((Instance) frame);
        Set ownSlots = collectOwnSlotValues(types, _systemFrames.getDirectTemplateSlotsSlot());
        ownSlots.add(_systemFrames.getNameSlot());
        ownSlots.add(_systemFrames.getDirectTypesSlot());
        return ownSlots;
    }

    public Set getTemplateSlots(Cls cls) {
        Set clses = new LinkedHashSet(getSuperclasses(cls));
        clses.add(cls);
        Set values = collectOwnSlotValues(clses, _systemFrames.getDirectTemplateSlotsSlot());
        addSubslotsInDomain(values, cls);
        return unmodifiableSet(values);
    }

    private void addSubslotsInDomain(Collection slots, Cls domain) {
        Iterator i = new HashSet(slots).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            addSubslotsInDomain(slot, domain, slots);
        }
    }

    private void addSubslotsInDomain(Slot slot, Cls domain, Collection slots) {
        Iterator i = getSubslots(slot).iterator();
        while (i.hasNext()) {
            Slot subslot = (Slot) i.next();
            if (isInDomain(subslot, domain)) {
                slots.add(subslot);
            }
        }
    }

    private boolean isInDomain(Slot subslot, Cls cls) {
        Collection directDomain = getDirectDomain(subslot);
        boolean isInDomain = directDomain.isEmpty();
        Iterator i = directDomain.iterator();
        while (i.hasNext()) {
            Cls directDomainCls = (Cls) i.next();
            if (getSuperclasses(cls).contains(directDomainCls)) {
                isInDomain = true;
                break;
            }
        }
        return isInDomain;
    }

    public Set getDomain(Slot slot) {
        Set domain = getClosure(getDirectDomain(slot), _systemFrames.getDirectSubclassesSlot());
        if (domain.isEmpty()) {
            addSuperslotsDomain(slot, domain);
        }
        return domain;
    }

    private void addSuperslotsDomain(Slot slot, Set domain) {
        Iterator i = getDirectSuperslots(slot).iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            domain.addAll(getDomain(superslot));
        }
    }

    private Set getClosure(Collection startValues, Slot propagationSlot) {
        Set closure = new HashSet(startValues);
        Iterator i = startValues.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Instance) {
                Instance instance = (Instance) o;
                closure.addAll(getDirectOwnSlotValuesClosure(instance, propagationSlot));
            }
        }
        return closure;
    }

    public List getDirectDomain(Slot slot) {
        return getDirectOwnSlotValues(slot, _systemFrames.getDirectDomainSlot());
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        addDirectOwnSlotValuePair(cls, _systemFrames.getDirectSuperclassesSlot(), _systemFrames
                .getDirectSubclassesSlot(), superclass);
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        removeDirectOwnSlotValuePair(cls, _systemFrames.getDirectSuperclassesSlot(), _systemFrames
                .getDirectSubclassesSlot(), superclass);
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        addDirectOwnSlotValuePair(slot, _systemFrames.getDirectSuperslotsSlot(), _systemFrames.getDirectSubslotsSlot(),
                superslot);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        removeDirectOwnSlotValuePair(slot, _systemFrames.getDirectSuperslotsSlot(), _systemFrames
                .getDirectSubslotsSlot(), superslot);
    }

    public void addDirectType(Instance instance, Cls type) {
        addDirectOwnSlotValuePair(instance, _systemFrames.getDirectTypesSlot(), _systemFrames.getDirectInstancesSlot(),
                type);
        swizzleInstance(instance);
    }

    private void swizzleInstance(Instance instance) {
        // Log.enter(this, "swizzleInstance", instance);
        FrameFactory factory = getFrameFactory();
        Collection types = getDirectTypes(instance);
        FrameID id = instance.getFrameID();
        Class interfac = getFrameInterface(types);
        boolean isCorrectClass = interfac == null || interfac.isInstance(instance);
        isCorrectClass &= factory.isCorrectJavaImplementationClass(id, types, instance.getClass());
        if (!isCorrectClass) {
            Instance newInstance;
            if (interfac.equals(Cls.class)) {
                newInstance = factory.createCls(id, types);
            } else if (interfac.equals(Slot.class)) {
                newInstance = factory.createSlot(id, types);
            } else if (interfac.equals(Facet.class)) {
                newInstance = factory.createFacet(id, types);
            } else {
                newInstance = factory.createSimpleInstance(id, types);
            }
            updateNewInstance(newInstance, instance);
            _helper.replaceFrame(newInstance);
            nameToFrameMap.put(getFrameName(newInstance), newInstance);
        }
    }

    private Class getFrameInterface(Collection directTypes) {
        Class interfac;
        Set types = getTypes(directTypes);
        if (types.isEmpty()) {
            interfac = null;
        } else if (types.contains(_systemFrames.getRootClsMetaCls())) {
            interfac = Cls.class;
        } else if (types.contains(_systemFrames.getRootSlotMetaCls())) {
            interfac = Slot.class;
        } else if (types.contains(_systemFrames.getRootFacetMetaCls())) {
            interfac = Facet.class;
        } else {
            interfac = SimpleInstance.class;
        }
        return interfac;
    }

    private Set getTypes(Collection directTypes) {
        Set types = new HashSet(directTypes);
        Iterator i = directTypes.iterator();
        while (i.hasNext()) {
            Cls directType = (Cls) i.next();
            types.addAll(getSuperclasses(directType));
        }
        return types;
    }

    private static void updateNewInstance(Instance newInstance, Instance oldInstance) {
        newInstance.setEditable(oldInstance.isEditable());
        newInstance.setIncluded(oldInstance.isIncluded());
    }

    private void addDirectOwnSlotValuePair(Frame source, Slot sourceSlot, Slot targetSlot, Frame target) {
        addDirectOwnSlotValue(source, sourceSlot, target);
        addDirectOwnSlotValue(target, targetSlot, source);
    }

    private void removeDirectOwnSlotValuePair(Frame source, Slot sourceSlot, Slot targetSlot, Frame target) {
        removeDirectOwnSlotValue(source, sourceSlot, target);
        removeDirectOwnSlotValue(target, targetSlot, source);
    }

    public void removeDirectType(Instance instance, Cls type) {
        removeDirectOwnSlotValuePair(instance, _systemFrames.getDirectTypesSlot(), _systemFrames
                .getDirectInstancesSlot(), type);
        swizzleInstance(instance);
    }

    public void moveDirectType(Instance instance, Cls type, int index) {
        int from = getDirectTypes(instance).indexOf(type);
        moveDirectOwnSlotValue(instance, _systemFrames.getDirectTypesSlot(), from, index);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        int from = getDirectSubclasses(cls).indexOf(subclass);
        moveDirectOwnSlotValue(cls, _systemFrames.getDirectSubclassesSlot(), from, index);
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        int from = getDirectSubslots(slot).indexOf(subslot);
        moveDirectOwnSlotValue(slot, _systemFrames.getDirectSubslotsSlot(), from, index);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        addDirectOwnSlotValuePair(cls, _systemFrames.getDirectTemplateSlotsSlot(), _systemFrames.getDirectDomainSlot(),
                slot);
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        int from = getDirectTemplateSlots(cls).indexOf(slot);
        moveDirectOwnSlotValue(cls, _systemFrames.getDirectTemplateSlotsSlot(), from, index);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        removeDirectOwnSlotValuePair(cls, _systemFrames.getDirectTemplateSlotsSlot(), _systemFrames
                .getDirectDomainSlot(), slot);
    }

    public void addDirectOwnSlotValue(Frame frame, Slot slot, Object value) {
        addDirectOwnSlotValues(frame, slot, Collections.singleton(value));
    }

    public void removeDirectOwnSlotValue(Frame frame, Slot slot, Object value) {
        _helper.removeValue(frame, slot, null, false, value);
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        _helper.moveValue(frame, slot, null, false, from, to);
    }

    public void addDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        addValues(frame, slot, null, false, values);
    }

    private void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _helper.addValues(frame, slot, facet, isTemplate, values);
    }

    public Cls createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaults) {
        Cls cls = createCls(id, name, directTypes);
        addCls(cls, name, directTypes, directSuperclasses, loadDefaults);
        return cls;
    }

    public SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes, boolean loadDefaults) {
        SimpleInstance simpleInstance = createSimpleInstance(id, name, directTypes);
        addSimpleInstance(simpleInstance, name, directTypes, loadDefaults);
        return simpleInstance;
    }

    public Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaults) {
        Facet facet = createFacet(id, name, directTypes);
        addFacet(facet, name, directTypes, loadDefaults);
        return facet;
    }

    private void addSimpleInstance(SimpleInstance simpleInstance, String name, Collection directTypes,
            boolean loadDefaults) {
        name = uniqueName(name, "Instance_");
        addInstance(simpleInstance, name, directTypes, loadDefaults);
    }

    protected void addCls(Cls cls, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaults) {
        name = uniqueName(name, "Class_");
        addInstance(cls, name, directTypes, loadDefaults);
        addDirectOwnSlotValuePairs(cls, _systemFrames.getDirectSuperclassesSlot(), _systemFrames
                .getDirectSubclassesSlot(), directSuperclasses);
    }

    protected void addSlot(Slot slot, String name, Collection directTypes, Collection directSuperslots,
            boolean loadDefaults) {
        // Default values interfere with inherited values from superslots. Thus
        // we disable defaults if any
        // superslot is specified.
        if (!directSuperslots.isEmpty()) {
            loadDefaults = false;
        }
        name = uniqueName(name, "Slot_");
        addInstance(slot, name, directTypes, loadDefaults);
        if (!directSuperslots.isEmpty()) {
            addDirectOwnSlotValuePairs(slot, _systemFrames.getDirectSuperslotsSlot(), _systemFrames
                    .getDirectSubslotsSlot(), directSuperslots);
        }
    }

    private void addDirectOwnSlotValuePairs(Frame source, Slot sourceSlot, Slot targetSlot, Collection targets) {
        addDirectOwnSlotValues(source, sourceSlot, targets);
        Iterator i = targets.iterator();
        while (i.hasNext()) {
            Frame target = (Frame) i.next();
            addDirectOwnSlotValue(target, targetSlot, source);
        }
    }

    private void addFacet(Facet facet, String name, Collection directTypes, boolean loadDefaults) {
        name = uniqueName(name, "Facet_");
        addInstance(facet, name, directTypes, loadDefaults);
    }

    public Slot createSlot(FrameID id, String name, Collection directTypes, Collection directSuperslots,
            boolean loadDefaults) {
        Slot slot = createSlot(id, name, directTypes);
        addSlot(slot, name, directTypes, directSuperslots, loadDefaults);
        return slot;
    }

    private void setDirectOwnSlotValue(Frame frame, Slot slot, Object value) {
        Collection values = CollectionUtilities.createCollection(value);
        setDirectOwnSlotValues(frame, slot, values);
    }

    private void addInstance(Instance instance, String name, Collection directTypes, boolean loadDefaults) {
        addFrame(instance, name);
        addDirectOwnSlotValuePairs(instance, _systemFrames.getDirectTypesSlot(),
                _systemFrames.getDirectInstancesSlot(), directTypes);
        if (loadDefaults) {
            addDefaults(instance, directTypes);
        }
    }

    protected String uniqueName(String name, String baseName) {
        String uniqueName;
        if (name == null) {
            uniqueName = generateUniqueName(_kb.getName() + "_" + baseName);
        } else {
            Frame frame = getFrame(name);
            if (frame != null) {
                throw new IllegalArgumentException(name + " not unique: " + frame);
            }
            uniqueName = name;
        }
        return uniqueName;
    }

    private int nextName;

    protected String generateUniqueName(String baseName) {
        String uniqueName = null;

        while (uniqueName == null) {
            String s = baseName + nextName;
            if (getFrame(s) == null) {
                uniqueName = s;
                ++nextName;
            } else {
                nextName += 10000;
            }
        }
        return uniqueName;
    }

    private Slot getInverseSlot(Slot slot) {
        return (Slot) getDirectOwnSlotValue(slot, _systemFrames.getInverseSlotSlot());
    }

    public Collection getOwnSlotValues(Frame frame, Slot slot) {
        Collection values = new ArrayList();
        addOwnSlotValues(frame, slot, values);
        return unmodifiableCollection(values);
    }

    private void addOwnSlotValues(Frame frame, Slot slot, Collection values) {
        values.addAll(getDirectOwnSlotValues(frame, slot));
        addInheritedTemplateSlotValues(frame, slot, values);
        addSubslotValues(frame, slot, values);
        addInferredInverseSlotValues(frame, slot, values);
        if (frame instanceof Slot && values.isEmpty() && isInheritedSuperslotSlot(slot)) {
            addInheritedSuperslotValues((Slot) frame, slot, values);
        }
    }

    private void addInferredInverseSlotValues(Frame frame, Slot slot, Collection values) {
        Slot inverseSlot = getInverseSlot(slot);
        if (inverseSlot != null) {
            Collection referencingClasses = getTemplateSlotValuesReferences(frame, inverseSlot);
            Iterator i = referencingClasses.iterator();
            while (i.hasNext()) {
                Cls cls = (Cls) i.next();
                values.addAll(getInstances(cls));
            }
        }
    }

    private Collection getTemplateSlotValuesReferences(Frame frame, Slot slot) {
        Collection references = new HashSet();
        references.addAll(_helper.getFrames(slot, _systemFrames.getValuesFacet(), true, frame));
        // Probably should add "top level" values here
        return references;
    }

    private boolean isInheritedSuperslotSlot(Slot slot) {
        return _inheritedSuperslotSlots.contains(slot);
    }

    private void addInheritedSuperslotValues(Slot slotFrame, Slot slot, Collection values) {
        Facet facet = (Facet) getDirectOwnSlotValue(slot, _systemFrames.getAssociatedFacetSlot());
        Iterator i = getSuperslots(slotFrame).iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            Collection superslotValues = getDirectOwnSlotValues(superslot, slot);
            if (facet == null) {
                values.addAll(superslotValues);
            } else {
                Collection resolvedValues = facet.resolveValues(values, superslotValues);
                if (!resolvedValues.equals(values)) {
                    values.clear();
                    values.addAll(resolvedValues);
                }
            }
        }
    }

    private void addInheritedTemplateSlotValues(Frame frame, Slot slot, Collection values) {
        if (frame instanceof Instance) {
            Set templateSlotValues = new LinkedHashSet();
            Instance instance = (Instance) frame;
            Iterator i = getTypes(instance).iterator();
            while (i.hasNext()) {
                Cls type = (Cls) i.next();
                templateSlotValues.addAll(getDirectTemplateSlotValues(type, slot));
            }
            templateSlotValues.addAll(getDirectOwnSlotValues(slot, _systemFrames.getValuesSlot()));
            values.addAll(templateSlotValues);
        }
    }

    private void addSubslotValues(Frame frame, Slot slot, Collection values) {
        Iterator i = getSubslots(slot).iterator();
        while (i.hasNext()) {
            Slot subslot = (Slot) i.next();
            values.addAll(getDirectOwnSlotValues(frame, subslot));
        }
    }

    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        List values = getValues(frame, slot, null, false);
        return unmodifiableList(values);
    }

    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        return _helper.getValuesCount(frame, slot, null, false);
    }

    private List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return _helper.getValues(frame, slot, facet, isTemplate);
    }

    private Object getValue(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = _helper.getValues(frame, slot, facet, isTemplate);
        return (values.isEmpty()) ? null : values.get(0);
    }

    private void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _helper.setValues(frame, slot, facet, isTemplate, values);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection newValues) {
        Slot inverseSlot = getInverseSlot(slot);
        if (inverseSlot != null) {
            removeInverseLinksFromCurrentValues(frame, slot, inverseSlot);
            if (newValues != null && !newValues.isEmpty()) {
                addLinksFromNewValues(frame, slot, newValues, inverseSlot);
            }
        }
        _helper.setValues(frame, slot, null, false, newValues);
    }

    private boolean hasOwnSlot(Frame frame, Slot slot) {
        return getOwnSlots(frame).contains(slot);
    }

    private void addLinksFromNewValues(Frame frame, Slot slot, Collection newValues, Slot inverseSlot) {
        boolean isInverseSlotSingleCardinality = isCardinalitySingle(inverseSlot);
        Iterator i = newValues.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Frame) {
                Frame target = (Frame) o;
                if (hasOwnSlot(target, inverseSlot)) {
                    if (isInverseSlotSingleCardinality) {
                        Object o2 = getDirectOwnSlotValue(target, inverseSlot);
                        if (o2 instanceof Frame) {
                            Frame targetTarget = (Frame) o2;
                            if (targetTarget != null && !targetTarget.equals(frame)) {
                                removeDirectOwnSlotValue(targetTarget, slot, target);
                                removeDirectOwnSlotValue(target, inverseSlot, targetTarget);
                            }
                        }
                    }
                    if (!getDirectOwnSlotValues(target, inverseSlot).contains(frame)) {
                        addDirectOwnSlotValue(target, inverseSlot, frame);
                    }
                }
            }
        }
    }

    private boolean isCardinalitySingle(Slot slot) {
        Number n = (Number) getDirectOwnSlotValue(slot, _systemFrames.getMaximumCardinalitySlot());
        return n != null && n.intValue() == 1;
    }

    private void removeInverseLinksFromCurrentValues(Frame frame, Slot slot, Slot inverseSlot) {
        Collection currentValues = getDirectOwnSlotValues(frame, slot);
        if (!currentValues.isEmpty()) {
            Iterator i = new ArrayList(currentValues).iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof Frame) {
                    Frame target = (Frame) o;
                    removeDirectOwnSlotValue(target, inverseSlot, frame);
                }
            }
        }
    }

    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        // return getValues(cls, slot, null, true);
        return getValues(cls, slot, _systemFrames.getValuesFacet(), true);
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        // setValues(cls, slot, null, true, values);
        setValues(cls, slot, _systemFrames.getValuesFacet(), true, values);
    }

    public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getValues(cls, slot, facet, true);
    }

    private Slot getAssociatedSlot(Facet facet) {
        Collection slots = getDirectOwnSlotValues(facet, _systemFrames.getAssociatedSlotSlot());
        return (Slot) CollectionUtilities.getFirstItem(slots);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        // Log.enter(this, "setDirectTemplateFacetValues", cls, slot, facet,
        // values);
        Slot associatedSlot = getAssociatedSlot(facet);
        if (associatedSlot != null) {
            Collection topLevelValues = getDirectOwnSlotValues(slot, associatedSlot);
            if (CollectionUtilities.equalsList(topLevelValues, values)) {
                values = Collections.EMPTY_LIST;
            }
        }
        setValues(cls, slot, facet, true, values);
    }

    public Set getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        return _helper.getFrames(slot, null, false, value);
    }

    public Set getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        return _helper.getFramesWithAnyValue(slot, null, false);
    }

    public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        return _helper.getFrames(slot, null, true, value);
    }

    public Set getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        return _helper.getFramesWithAnyValue(slot, null, true);
    }

    public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value) {
        return _helper.getFrames(slot, facet, true, value);
    }

    public Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        return _helper.getMatchingFrames(slot, null, false, value, maxMatches);
    }

    public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches) {
        return _helper.getMatchingFrames(slot, null, true, value, maxMatches);
    }

    public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches) {
        return _helper.getMatchingFrames(slot, facet, true, value, maxMatches);
    }

    private Object getDirectOwnSlotValue(Frame frame, Slot slot) {
        return getValue(frame, slot, null, false);
    }

    public String getFrameName(Frame frame) {
        String name = (String) getDirectOwnSlotValue(frame, _systemFrames.getNameSlot());
        if (name == null) {
            name = "<<missing frame name for " + frame.getFrameID() + ">>";
        }
        return name;
    }

    public void setFrameName(Frame frame, String name) {
        Frame frameWithName = getFrame(name);
        if (name == null || (frameWithName != null && !frameWithName.equals(frame))) {
            throw new IllegalArgumentException("Duplicate frame name: " + name);
        }
        setDirectOwnSlotValue(frame, _systemFrames.getNameSlot(), name);
        nameToFrameMap.remove(getFrameName(frame));
        nameToFrameMap.put(name, frame);
    }

    protected void addSystemFrames() {
        _systemFrames.addSystemFrames(this);
    }

    private FrameID ensureValid(FrameID id, String name) {
        if (id == null) {
            id = _helper.generateFrameID();
        }
        return id;
    }

    protected Cls createCls(FrameID id, String name, Collection directTypes) {
        id = ensureValid(id, name);
        return getFrameFactory().createCls(id, directTypes);
    }

    protected Slot createSlot(FrameID id, String name, Collection directTypes) {
        id = ensureValid(id, name);
        return getFrameFactory().createSlot(id, directTypes);
    }

    protected Facet createFacet(FrameID id, String name, Collection directTypes) {
        id = ensureValid(id, name);
        return getFrameFactory().createFacet(id, directTypes);
    }

    protected SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes) {
        id = ensureValid(id, name);
        return getFrameFactory().createSimpleInstance(id, directTypes);
    }

    private void addFrame(Frame frame, String name) {
        addDirectOwnSlotValue(frame, _systemFrames.getNameSlot(), name);
        // frameIdToFrameMap.put(frame.getFrameID(), frame);
    }

    private void addDefaults(Instance instance, Collection directTypes) {
        Iterator i = directTypes.iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            addDefaults(instance, type);
        }
    }

    private void addDefaults(Instance instance, Cls type) {
        Iterator i = getTemplateSlots(type).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            addDefault(instance, type, slot);
        }
    }

    private void addDefault(Instance instance, Cls type, Slot slot) {
        Collection values = getTemplateFacetValues(type, slot, _systemFrames.getDefaultValuesFacet());
        if (!values.isEmpty()) {
            // shouldn't use add here in case slot in case same slot comes from
            // more than one type
            Slot inverseSlot = slot.getInverseSlot();
            if (inverseSlot == null) {
                addDirectOwnSlotValues(instance, slot, values);
            } else {
                Iterator i = values.iterator();
                while (i.hasNext()) {
                    Frame value = (Frame) i.next();
                    addDirectOwnSlotValuePair(instance, slot, inverseSlot, value);
                }
            }
        }
    }

    public Set getOwnFacets(Frame frame, Slot slot) {
        Set facets = new HashSet();
        Iterator i = getOwnSlots(slot).iterator();
        while (i.hasNext()) {
            Slot ownSlot = (Slot) i.next();
            Facet facet = (Facet) getDirectOwnSlotValue(ownSlot, _systemFrames.getAssociatedFacetSlot());
            if (facet != null) {
                facets.add(facet);
            }
        }
        return facets;
    }

    /*
     * private Set getOwnSlotValuesClosure(Frame frame, Slot traversalSlot, Slot valueSlot) { return getClosure(frame,
     * traversalSlot, null, false, valueSlot, null, false); }
     * 
     * private Set getClosure( Frame frame, Slot traversalSlot, Facet traversalFacet, boolean traversalIsTemplate, Slot
     * valueSlot, Facet valueFacet, boolean valueIsTemplate) { Set closure = new LinkedHashSet(); Iterator i =
     * _helper.getClosure(frame, traversalSlot, traversalFacet, traversalIsTemplate).iterator(); while (i.hasNext()) {
     * Frame valueFrame = (Frame) i.next(); Collection values = _helper.getValues(valueFrame, valueSlot, valueFacet,
     * valueIsTemplate); closure.addAll(values); } return closure; }
     */

    public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        Collection values = new ArrayList();
        Iterator i = getDirectTypes((Instance) frame).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            Collection typeValues = getTemplateFacetValues(cls, slot, facet);
            values = resolveValues(values, typeValues, facet);
        }
        return values;
    }

    public Set getOverriddenTemplateSlots(Cls cls) {
        Set overriddenSlots = new LinkedHashSet(getTemplateSlots(cls));
        Iterator i = overriddenSlots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (!isOverridden(cls, slot)) {
                i.remove();
            }
        }
        return overriddenSlots;
    }

    public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        Set overriddenSlots = new LinkedHashSet(getTemplateSlots(cls));
        Iterator i = overriddenSlots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (!isDirectlyOverridden(cls, slot)) {
                i.remove();
            }
        }
        return overriddenSlots;
    }

    public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        Set overriddenFacets = new LinkedHashSet(getTemplateFacets(cls, slot));
        Iterator i = overriddenFacets.iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            if (!isOverridden(cls, slot, facet)) {
                i.remove();
            }
        }
        return overriddenFacets;
    }

    public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        Set overriddenFacets = new LinkedHashSet(getTemplateFacets(cls, slot));
        Iterator i = overriddenFacets.iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            if (!isDirectlyOverridden(cls, slot, facet)) {
                i.remove();
            }
        }
        return overriddenFacets;
    }

    private boolean isDirectlyOverridden(Cls cls, Slot slot) {
        return !getDirectlyOverriddenTemplateFacets(cls, slot).isEmpty();
    }

    private boolean isOverridden(Cls cls, Slot slot) {
        return !getOverriddenTemplateFacets(cls, slot).isEmpty();
    }

    private boolean isDirectlyOverridden(Cls cls, Slot slot, Facet facet) {
        return !getDirectTemplateFacetValues(cls, slot, facet).isEmpty();
    }

    private boolean isOverridden(Cls cls, Slot slot, Facet facet) {
        Collection facetValues = new HashSet(getTemplateFacetValues(cls, slot, facet));
        Collection topLevelValues = getDirectOwnSlotValues(slot, facet.getAssociatedSlot());
        return !CollectionUtilities.equalsSet(facetValues, topLevelValues);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        Set facets = getTemplateFacets(cls, slot);
        Iterator i = facets.iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            setDirectTemplateFacetValues(cls, slot, facet, Collections.EMPTY_LIST);
        }
    }

    public boolean beginTransaction(String name) {
        return _helper.beginTransaction(name);
    }

    public boolean commitTransaction() {
        return _helper.commitTransaction();
    }

    public boolean rollbackTransaction() {
        return _helper.rollbackTransaction();
    }

    public List getEvents() {
        return Collections.EMPTY_LIST;
    }

    public void setDelegate(FrameStore fs) {
        Log.getLogger().severe("Unable to set delegate: " + fs.getName());
    }

    public FrameStore getDelegate() {
        return null;
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}