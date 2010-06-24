package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;

public abstract class FrameStore_Test extends SimpleTestCase {
    private static transient Logger log = Log.getLogger(FrameStore_Test.class);
    
    private DefaultKnowledgeBase _kb;
    private FrameStore _testFrameStore;
    private FrameStore _modifiableFrameStore;
    private boolean _initialized = false;

    protected FrameStore_Test() {
    }

    protected void setUp() throws Exception {
        super.setUp();
        _kb = new DefaultKnowledgeBase();
        _testFrameStore = createFrameStore(_kb);
        _modifiableFrameStore = getModifiableFrameStore(_kb);
        if (_testFrameStore != null && _modifiableFrameStore != null) {
          _initialized = true;
        }
        if (_testFrameStore instanceof RemoteClientFrameStore) {
            _kb.setGenerateEventsEnabled(false);
        }
        _kb.setTerminalFrameStore(_testFrameStore);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        _kb.close();
        _kb = null;
        _testFrameStore = null;
        _modifiableFrameStore = null;
    }

    protected FrameStore getTestFrameStore() {
        return _testFrameStore;
    }

    protected abstract FrameStore createFrameStore(DefaultKnowledgeBase kb);

    protected FrameStore getModifiableFrameStore(DefaultKnowledgeBase kb) {
        return _testFrameStore;
    }

    protected static String createFrameName() {
        return "Frame_" + Math.random();
    }

    protected Cls createCls() {
        Cls parent = (Cls) getFrame(Model.Cls.THING);
        assertNotNull("thing", parent);
        return createCls(parent);
    }

    protected Slot createSlot(Slot slot) {
        return createSlot(null, makeList(slot));
    }

    protected Slot createSlotOnCls(Cls cls) {
        Slot slot = createSlot();
        addTemplateSlot(cls, slot);
        return slot;
    }

    protected Slot createSlotOnCls(Cls cls, ValueType type, boolean allowsMultipleValues) {
        Slot slot = createSlotOnCls(cls);
        setTypeAndCardinality(slot, type, allowsMultipleValues);
        return slot;
    }

    private void setTypeAndCardinality(Slot slot, ValueType type, boolean allowsMultipleValues) {
        Slot valueTypeSlot = (Slot) _testFrameStore.getFrame(Model.Slot.VALUE_TYPE);
        if (equals(type, ValueType.INSTANCE)) {
            Cls thing = (Cls) _testFrameStore.getFrame(Model.Cls.THING);
            _modifiableFrameStore.setDirectOwnSlotValues(slot, valueTypeSlot, makeList(type.toString(), thing));
        } else {
            _modifiableFrameStore.setDirectOwnSlotValues(slot, valueTypeSlot, makeList(type.toString()));
        }
        Slot maxCardinalitySlot = (Slot) _testFrameStore.getFrame(Model.Slot.MAXIMUM_CARDINALITY);
        Collection values = allowsMultipleValues ? Collections.EMPTY_SET : Collections.singleton(new Integer(1));
        _modifiableFrameStore.setDirectOwnSlotValues(slot, maxCardinalitySlot, values);
    }

    protected Slot createSlotOnCls(Cls cls, Cls directType) {
        Slot slot = createSlot(null, Collections.EMPTY_SET, directType);
        addTemplateSlot(cls, slot);
        return slot;
    }

    protected void addTemplateSlot(Cls cls, Slot slot) {
        _modifiableFrameStore.addDirectTemplateSlot(cls, slot);
    }

    protected SimpleInstance createSimpleInstance(Cls type) {
        return createSimpleInstance(null, type);
    }

    private static FrameID getID(String name) {
        if (name == null) {
          name = createFrameName();
        }
        return new FrameID(name);
    }

    protected SimpleInstance createSimpleInstance(String name, Cls type) {
        Collection types = makeList(type);
        return _modifiableFrameStore.createSimpleInstance(getID(name), types, true);
    }

    protected Cls createCls(Cls parent) {
        Cls directType = (Cls) _testFrameStore.getFrame(Model.Cls.STANDARD_CLASS);
        assertNotNull("directType", directType);
        Collection types = makeList(directType);
        Collection parents = makeList(parent);
        return _modifiableFrameStore.createCls(getID(createFrameName()), types, parents, true);
    }

    protected Slot createSlot() {
        return createSlot((String) null);
    }

    protected Slot createSlot(String name) {
        return createSlot(name, Collections.EMPTY_LIST);
    }

    protected Slot createSlot(String name, Collection superslots) {
        Cls type = (Cls) _testFrameStore.getFrame(Model.Cls.STANDARD_SLOT);
        return createSlot(name, superslots, type);
    }

    protected Slot createSlot(String name, Collection superslots, Cls type) {
        Collection types = makeList(type);
        return _modifiableFrameStore.createSlot(getID(name), types, superslots, true);
    }

    protected Facet createFacet() {
        return createFacet(ValueType.STRING);
    }

    protected Facet createFacet(ValueType valueType) {
        Cls type = (Cls) _testFrameStore.getFrame(Model.Cls.STANDARD_FACET);
        return createFacet(valueType, type);
    }

    protected Facet createFacet(ValueType valueType, Cls directType) {
        Collection types = makeList(directType);
        Facet facet = _modifiableFrameStore.createFacet(getID(null), types, true);
        Cls defaultMetaSlot = (Cls) _testFrameStore.getFrame(Model.Cls.STANDARD_SLOT);
        Cls metaCls = createCls(defaultMetaSlot);
        Slot associatedSlot = createSlotOnCls(metaCls);
        setTypeAndCardinality(associatedSlot, valueType, true);
        Slot associatedFacetSlot = (Slot) _testFrameStore.getFrame(Model.Slot.ASSOCIATED_FACET);
        _modifiableFrameStore.setDirectOwnSlotValues(associatedSlot, associatedFacetSlot, makeList(facet));
        return facet;
    }

    protected Frame getFrame(String s) {
        return _testFrameStore.getFrame(s);
    }

    protected int getFrameCount() {
        return _testFrameStore.getFrameCount();
    }

    protected String getName(Frame frame) {
        return _testFrameStore.getFrameName(frame);
    }

    protected void setOwnSlotValue(Frame frame, Slot slot, Object value) {
        Collection values = CollectionUtilities.createCollection(value);
        _modifiableFrameStore.setDirectOwnSlotValues(frame, slot, values);
    }

    // ------------------------ test cases -------------------------------------------
    
    public void testClosure() {
      if (!_initialized) {
        return;
      }
      Cls big = createCls();
      Slot slot = createSlotOnCls(big, ValueType.INSTANCE, true);

      Instance a = createSimpleInstance(big);
      Instance b = createSimpleInstance(big);
      Instance c = createSimpleInstance(big);
      Set<Frame> values = new HashSet<Frame>();
      values.add(b);
      values.add(c);
      _modifiableFrameStore.setDirectOwnSlotValues(a, slot, values);
      
      Cls d = createCls();
      Cls e = createCls();
      Instance f = createSimpleInstance(d);
      values = new HashSet<Frame>();
      values.add(d);
      values.add(e);
      values.add(f);
      _modifiableFrameStore.setDirectOwnSlotValues(b, slot, values);
      
      Instance g = createSimpleInstance(e);
      values = new HashSet<Frame>();
      values.add(g);
      _modifiableFrameStore.setDirectOwnSlotValues(c, slot, values);
      
      Set expected = new HashSet();
      expected.add(b);
      expected.add(c);
      expected.add(d);
      expected.add(e);
      expected.add(f);
      expected.add(g);
      
      Set result = _kb.getDirectOwnSlotValuesClosure(a, slot);
      assertTrue(result.equals(expected));

      if (_testFrameStore instanceof RemoteClientFrameStore) {
        int repeat = 10;
        _testFrameStore.getDirectOwnSlotValues(a,slot);
        ((RemoteClientFrameStore) _testFrameStore).flushCache();
        while (repeat-- > 0)  {
          result = _kb.getDirectOwnSlotValuesClosure(a, slot);
          assertTrue(result.equals(expected));
        }
      }
    }
    
    
    public void testCreateCls() {
        if (!_initialized) {
          return;
        }
        String name = createFrameName();
        Cls directType = (Cls) _testFrameStore.getFrame(Model.Cls.STANDARD_CLASS);
        Assert.assertNotNull("directType", directType);
        Cls parent = (Cls) _testFrameStore.getFrame(Model.Cls.THING);
        Collection parents = makeList(parent);
        Collection types = makeList(directType);
        Cls cls = _testFrameStore.createCls(getID(name), types, parents, true);
        assertEquals("name", name, getName(cls));
        assertEquals("direct type", directType, cls.getDirectType());
        assertEqualsList("parents", parents, cls.getDirectSuperclasses());
        Cls fsCls = (Cls) _testFrameStore.getFrame(name);
        assertEquals("pointer", cls, fsCls);
    }

    public void testGetFrame() {
        if (!_initialized) {
          return;
        }
        Frame frame = createCls();
        String name = _testFrameStore.getFrameName(frame);
        assertNotNull("name", name);
        assertEquals(frame, _testFrameStore.getFrame(name));
    }

    public void testCreateSlot() {
        if (!_initialized) {
          return;
        }
        Cls standardSlot = (Cls) getFrame(Model.Cls.STANDARD_SLOT);
        Collection standardSlots = makeList(standardSlot);
        Slot slota = _testFrameStore.createSlot(getID(null), standardSlots, Collections.EMPTY_LIST, true);
        assertNotNull(slota);
        assertEquals("slota", slota, getFrame(getName(slota)));
        List types = _testFrameStore.getDirectTypes(slota);
        assertEquals("types size", 1, types.size());
        assertEquals("standard slot", standardSlot, types.iterator().next());
        Slot slotb = _testFrameStore.createSlot(getID(null), standardSlots, Collections.EMPTY_LIST, true);
        List superslots = makeList(slota, slotb);
        String slotName = createFrameName();
        Slot slotc = _testFrameStore.createSlot(getID(slotName), standardSlots, superslots, true);
        assertNotNull(slotc);
        assertEquals("slotc", slotc, getFrame(getName(slotc)));
        assertEqualsSet("superslots", superslots, _testFrameStore.getDirectSuperslots(slotc));
    }

    public void testCreateSimpleInstance() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Collection types = makeList(cls);
        SimpleInstance simpleInstance = _testFrameStore.createSimpleInstance(getID(null), types, true);
        assertNotNull(simpleInstance);
        assertEquals(simpleInstance, getFrame(getName(simpleInstance)));
    }

    public void testCreateFacet() {
        if (!_initialized) {
          return;
        }
        Cls cls = (Cls) getFrame(Model.Cls.STANDARD_FACET);
        Collection types = makeList(cls);
        Facet facet = _testFrameStore.createFacet(getID(null), types, true);
        assertNotNull(facet);
        assertEquals(facet, getFrame(getName(facet)));
    }

    public void testDeleteCls() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        assertNotNull(cls);
        String name = getName(cls);
        assertNotNull(name);
        _testFrameStore.deleteCls(cls);
        assertNull("no class after delete", getFrame(name));
    }

    public void testDeleteSlot() {
        if (!_initialized) {
          return;
        }
        String name = "deleteSlot";
        Collection types = makeList(getFrame(Model.Cls.STANDARD_SLOT));
        Slot slot = _testFrameStore.createSlot(getID(name), types, Collections.EMPTY_LIST, true);
        assertNotNull(slot);
        _testFrameStore.deleteSlot(slot);
        assertNull("no slot after delete", getFrame(name));
    }

    public void testDeleteFacet() {
        if (!_initialized) {
          return;
        }
        String name = "deleteFacet";
        Collection types = makeList(getFrame(Model.Cls.STANDARD_FACET));
        Facet facet = _testFrameStore.createFacet(getID(name), types, true);
        assertNotNull(facet);
        _testFrameStore.deleteFacet(facet);
        assertNull("no facet after delete", getFrame(name));
    }

    public void testDeleteSimpleInstance() {
        if (!_initialized) {
          return;
        }
        Collection types = makeList(createCls());
        String name = "deleteSimpleInstance";
        SimpleInstance simpleInstance = _testFrameStore.createSimpleInstance(getID(name), types, true);
        assertNotNull(simpleInstance);
        _testFrameStore.deleteSimpleInstance(simpleInstance);
        assertNull("no simpleInstance after delete", getFrame(name));
    }

    public void testGetClses() {
        if (!_initialized) {
          return;
        }
        Collection clsesStart = new ArrayList(_testFrameStore.getClses());
        Cls thing = (Cls) getFrame(Model.Cls.THING);
        assertNotNull("thing not null", thing);
        assertTrue("thing", clsesStart.contains(thing));
        Cls cls = createCls();
        Collection clsesEnd = _testFrameStore.getClses();
        assertEquals("size", clsesStart.size() + 1, clsesEnd.size());
        assertTrue("new class", clsesEnd.contains(cls));
    }

    public void testGetInstances() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Collection<Instance> instances = _testFrameStore.getInstances(cls);
        assertEquals("none", 0, instances.size());
        Collection types = makeList(cls);
        _modifiableFrameStore.createSimpleInstance(getID(null), types, true);
        instances = _testFrameStore.getInstances(cls);
        assertEquals("after", 1, instances.size());
    }

    public void testAddDirectTemplateSlot() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlot();
        _testFrameStore.addDirectTemplateSlot(cls, slot);
        List slots = _testFrameStore.getDirectTemplateSlots(cls);
        assertEquals("size", 1, slots.size());
        assertEquals("value", slot, slots.get(0));
    }

    public void testGetOwnSlots() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        createSlotOnCls(cls);
        Cls subclass = createCls(cls);
        createSlotOnCls(subclass);
        Collection subclasses = makeList(subclass);
        SimpleInstance instance = _modifiableFrameStore.createSimpleInstance(getID(null), subclasses, true);
        Set ownSlots = _testFrameStore.getOwnSlots(instance);
        Set templateSlots = _testFrameStore.getTemplateSlots(subclass);
        assertEquals("size", 2, templateSlots.size());
        Set testOwnSlots = new HashSet(templateSlots);
        testOwnSlots.add(getFrame(Model.Slot.NAME));
        testOwnSlots.add(getFrame(Model.Slot.DIRECT_TYPES));
        assertEquals("sets", testOwnSlots, ownSlots);
    }

    public void testGetTemplateSlots() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls);
        Cls subclass = createCls(cls);
        Slot slotb = createSlotOnCls(subclass);
        Cls subsubclass = createCls(subclass);
        Collection slots = _modifiableFrameStore.getTemplateSlots(subsubclass);
        assertEquals("size", 2, slots.size());
        assertTrue("slot", slots.contains(slot));
        assertTrue("slotb", slots.contains(slotb));
    }

    public void testGetDirectOwnSlotValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Collection classes = makeList(cls);
        SimpleInstance instance = _modifiableFrameStore.createSimpleInstance(getID(null), classes, true);
        List values = makeList("foo", "bar");
        List oldValues = _testFrameStore.getDirectOwnSlotValues(instance, slot);
        _modifiableFrameStore.setDirectOwnSlotValues(instance, slot, values);
        assertEquals("oldsize", 0, oldValues.size());
        List newValues = _testFrameStore.getDirectOwnSlotValues(instance, slot);
        assertEquals("newsize", values.size(), newValues.size());
        assertEquals("lists", values, newValues);
    }

    public void testGetReferences() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slota = createSlotOnCls(cls, ValueType.INSTANCE, true);
        Slot slotb = createSlotOnCls(cls, ValueType.INSTANCE, true);
        Slot dislot = (Slot) _testFrameStore.getFrame(Model.Slot.DIRECT_INSTANCES);
        Facet facet = createFacet(ValueType.INSTANCE);
        Instance instanceA = createSimpleInstance(cls);
        Instance instanceB = createSimpleInstance(cls);
        Collection instances = makeList(instanceB);
        _modifiableFrameStore.setDirectOwnSlotValues(instanceA, slota, instances);
        _modifiableFrameStore.setDirectTemplateSlotValues(cls, slotb, instances);
        _modifiableFrameStore.setDirectTemplateFacetValues(cls, slota, facet, instances);
        Collection references = _testFrameStore.getReferences(instanceB);
        assertTrue("own slot value", references.contains(new ReferenceImpl(instanceA, slota, null, false)));
        // assertTrue("template slot value", references.contains(new ReferenceImpl(cls, slotb, null, true)));
        assertTrue("type", references.contains(new ReferenceImpl(cls, dislot, null, false)));
        assertTrue("facet value", references.contains(new ReferenceImpl(cls, slota, facet, true)));
        assertEquals("size", 4, references.size());
    }

    public void testGetFrames() {
        if (!_initialized) {
          return;
        }
        Frame thing = _testFrameStore.getFrame(Model.Cls.THING);
        Collection oldFrames = _testFrameStore.getFrames();
        int size = oldFrames.size();
        assertTrue("contains thing", oldFrames.contains(thing));
        Cls cls = createCls();
        createSlot();
        createSimpleInstance(cls);
        Collection frames = _testFrameStore.getFrames();
        assertEquals("size", size + 3, frames.size());
    }

    public void testGetOwnSlotValues() {
        if (!_initialized) {
          return;
        }
        if (log.isLoggable(Level.FINE)) {
          log.fine("Entering testGetOwnSlotValues");
        }
        try {
          Cls cls = createCls();
          Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
          List templateValues = makeList("foo", "bar");
          _modifiableFrameStore.setDirectTemplateSlotValues(cls, slot, templateValues);
          Instance instance = createSimpleInstance(cls);
          List ownValues = makeList("foob", "baz");
          _modifiableFrameStore.setDirectOwnSlotValues(instance, slot, ownValues);
          Collection values = _testFrameStore.getOwnSlotValues(instance, slot);
          Set originalValues = new HashSet(templateValues);
          originalValues.addAll(ownValues);
          assertEqualsSet("size", originalValues, values);
        } finally {
          if (log.isLoggable(Level.FINE)) {
            log.fine("testGetOwnSlotValues completed...");
          }
        }
    }

    public void testSetDirectOwnSlotValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Instance instance = createSimpleInstance(cls);
        Collection values = makeList("foo", "bar");
        Collection oldValues = _testFrameStore.getDirectOwnSlotValues(instance, slot);
        assertEquals("no old values", 0, oldValues.size());
        _testFrameStore.setDirectOwnSlotValues(instance, slot, values);
        Collection newValues = _testFrameStore.getDirectOwnSlotValues(instance, slot);
        assertEqualsList("values", values, newValues);
        _testFrameStore.setDirectOwnSlotValues(instance, slot, Collections.EMPTY_SET);
        Collection emptyValues = _testFrameStore.getDirectOwnSlotValues(instance, slot);
        assertEquals("empty values", 0, emptyValues.size());
    }

    public void testGetDirectTemplateSlots() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Collection oldSlots = _testFrameStore.getDirectTemplateSlots(cls);
        assertEquals("old values", 0, oldSlots.size());
        Slot slota = createSlotOnCls(cls);
        Slot slotb = createSlotOnCls(cls);
        Collection slots = _testFrameStore.getDirectTemplateSlots(cls);
        assertEquals("size", 2, slots.size());
        assertTrue("a", slots.contains(slota));
        assertTrue("b", slots.contains(slotb));
    }

    public void testRemoveDirectTemplateSlot() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slota = createSlotOnCls(cls);
        Slot slotb = createSlotOnCls(cls);
        Slot slotc = createSlotOnCls(cls);
        assertEquals("size1", 3, _testFrameStore.getDirectTemplateSlots(cls).size());
        _testFrameStore.removeDirectTemplateSlot(cls, slotb);
        Collection newValues = _testFrameStore.getDirectTemplateSlots(cls);
        assertEquals("size2", 2, newValues.size());
        assertTrue("contains a", newValues.contains(slota));
        assertTrue("contains c", newValues.contains(slotc));
        _testFrameStore.removeDirectTemplateSlot(cls, slota);
        _testFrameStore.removeDirectTemplateSlot(cls, slotc);
        assertEquals("size3", 0, _testFrameStore.getDirectTemplateSlots(cls).size());
    }

    public void testMoveDirectTemplateSlot() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slota = createSlotOnCls(cls);
        Slot slotb = createSlotOnCls(cls);
        Slot slotc = createSlotOnCls(cls);
        List slots = _testFrameStore.getDirectTemplateSlots(cls);
        assertEqualsList("start", makeList(slota, slotb, slotc), slots);
        _testFrameStore.moveDirectTemplateSlot(cls, slota, 2);
        _testFrameStore.moveDirectTemplateSlot(cls, slotc, 0);
        List movedSlots = _testFrameStore.getDirectTemplateSlots(cls);
        assertEqualsList("end", makeList(slotc, slotb, slota), movedSlots);
    }

    public void testGetTemplateSlotValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls subclass = createCls(cls);
        Slot slot = createSlotOnCls(cls);
        Collection clsValues = makeList("foo");
        Collection subclassValues = makeList("bar");
        Collection allValues = new ArrayList(clsValues);
        allValues.addAll(subclassValues);
        _modifiableFrameStore.setDirectTemplateSlotValues(cls, slot, clsValues);
        Collection clsValues1 = _testFrameStore.getTemplateSlotValues(cls, slot);
        assertEqualsSet("class values", clsValues, clsValues1);
        Collection subclassValues1 = _testFrameStore.getTemplateSlotValues(subclass, slot);
        assertEqualsSet("subclass values1", clsValues, subclassValues1);
        _modifiableFrameStore.setDirectTemplateSlotValues(subclass, slot, subclassValues);
        Collection subclassValues2 = _testFrameStore.getTemplateSlotValues(subclass, slot);
        assertEqualsSet("subclass values2", allValues, subclassValues2);
    }

    public void testGetDirectTemplateSlotValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Collection oldValues = makeList("foo", "bar");
        _modifiableFrameStore.setDirectTemplateSlotValues(cls, slot, oldValues);
        List newValues = _testFrameStore.getDirectTemplateSlotValues(cls, slot);
        assertEqualsList("values", oldValues, newValues);
    }

    public void testSetDirectTemplateSlotValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Collection oldValues = makeList("foo", "bar");
        _testFrameStore.setDirectTemplateSlotValues(cls, slot, oldValues);
        List newValues = _testFrameStore.getDirectTemplateSlotValues(cls, slot);
        assertEqualsList("values", oldValues, newValues);
    }

    public void testGetTemplateFacetValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Facet facet = createFacet();
        Cls subclass = createCls(cls);
        Cls subsubclass = createCls(subclass);
        Collection clsValues = makeList("foo");
        Collection subclassValues = makeList("bar");
        _modifiableFrameStore.setDirectTemplateFacetValues(cls, slot, facet, clsValues);
        _modifiableFrameStore.setDirectTemplateFacetValues(subclass, slot, facet, subclassValues);
        _testFrameStore.getDirectTemplateFacetValues(cls, slot, facet);
        _testFrameStore.getDirectTemplateFacetValues(subclass, slot, facet);
        _testFrameStore.getDirectTemplateFacetValues(subsubclass, slot, facet);
    }

    public void testGetDirectTemplateFacetValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls slotMetaCls = createCls(_kb.getDefaultSlotMetaCls());
        Slot metaSlot = createSlotOnCls(slotMetaCls, ValueType.STRING, true);
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        setDirectType(slot, slotMetaCls);
        Facet facet = createFacet();
        Slot associatedFacetSlot = (Slot) getFrame(Model.Slot.ASSOCIATED_FACET);
        Collection facets = CollectionUtilities.createCollection(facet);
        _modifiableFrameStore.setDirectOwnSlotValues(metaSlot, associatedFacetSlot, facets);
        Collection oldValues = makeList("foo", "bar");
        _modifiableFrameStore.setDirectTemplateFacetValues(cls, slot, facet, oldValues);
        Collection newValues = _testFrameStore.getDirectTemplateFacetValues(cls, slot, facet);
        assertEqualsList("values", oldValues, newValues);
    }

    private void setDirectType(Instance instance, Cls type) {
        Cls currentType = instance.getDirectType();
        _modifiableFrameStore.addDirectType(instance, type);
        _modifiableFrameStore.removeDirectType(instance, currentType);
    }

    public void testSetDirectTemplateFacetValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls slotMetaCls = createCls(_kb.getDefaultSlotMetaCls());
        Slot metaSlot = createSlotOnCls(slotMetaCls, ValueType.STRING, true);
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        slot.setDirectType(slotMetaCls);
        Facet facet = createFacet();
        metaSlot.setAssociatedFacet(facet);
        Collection oldValues = makeList("foo", "bar");
        _testFrameStore.setDirectTemplateFacetValues(cls, slot, facet, oldValues);
        Collection newValues = _testFrameStore.getDirectTemplateFacetValues(cls, slot, facet);
        assertEqualsList("values", oldValues, newValues);
    }

    public void testGetDirectSuperclasses() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls cls2 = createCls();
        Cls subclass = createCls(cls);
        _modifiableFrameStore.addDirectSuperclass(subclass, cls2);
        Collection clses = _testFrameStore.getDirectSuperclasses(subclass);
        assertEqualsList("clses", makeList(cls, cls2), clses);
    }

    public void testGetSuperclasses() {
        if (!_initialized) {
          return;
        }
        Cls thing = (Cls) getFrame(Model.Cls.THING);
        Cls cls = createCls(thing);
        Cls cls2 = createCls(thing);
        Cls subclass = createCls(cls);
        Cls subsubclass = createCls(subclass);
        _modifiableFrameStore.addDirectSuperclass(subclass, cls2);
        Collection clses = _testFrameStore.getSuperclasses(subsubclass);
        assertEqualsSet("clses", makeList(thing, cls, cls2, subclass), clses);
    }

    public void testGetDirectSubclasses() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls subclass1 = createCls(cls);
        Cls subclass2 = createCls(cls);
        Collection clses = _testFrameStore.getDirectSubclasses(cls);
        assertEqualsList("clses", makeList(subclass1, subclass2), clses);
    }

    public void testGetSubclasses() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls subclass = createCls(cls);
        Cls subsubclass1 = createCls(subclass);
        Cls subsubclass2 = createCls(subclass);
        Collection clses = _testFrameStore.getSubclasses(cls);
        assertEqualsSet("clses", makeList(subclass, subsubclass1, subsubclass2), clses);
    }

    public void testAddDirectSuperclass() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls cls2 = createCls(cls);
        Cls cls3 = createCls(cls);
        Cls cls4 = createCls(cls2);
        _testFrameStore.addDirectSuperclass(cls4, cls3);
        Collection clses = _testFrameStore.getDirectSuperclasses(cls4);
        assertEqualsSet("clses", makeList(cls2, cls3), clses);
    }

    public void testRemoveDirectSuperclass() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls cls2 = createCls(cls);
        Cls cls3 = createCls(cls);
        Cls cls4 = createCls(cls);
        Cls cls5 = createCls(cls2);
        _modifiableFrameStore.addDirectSuperclass(cls5, cls3);
        _modifiableFrameStore.addDirectSuperclass(cls5, cls4);
        Collection clses = _testFrameStore.getDirectSuperclasses(cls5);
        assertEqualsSet("clses start", makeList(cls2, cls3, cls4), clses);
        _testFrameStore.removeDirectSuperclass(cls5, cls2);
        clses = _testFrameStore.getDirectSuperclasses(cls5);
        assertEqualsSet("clses end", makeList(cls3, cls4), clses);
    }

    public void testMoveDirectSubclass() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls clsa = createCls(cls);
        Cls clsb = createCls(cls);
        Cls clsc = createCls(cls);
        List clses = _testFrameStore.getDirectSubclasses(cls);
        assertEqualsList("start", makeList(clsa, clsb, clsc), clses);
        _testFrameStore.moveDirectSubclass(cls, clsa, 1);
        clses = _testFrameStore.getDirectSubclasses(cls);
        assertEqualsList("1", makeList(clsb, clsa, clsc), clses);
        _testFrameStore.moveDirectSubclass(cls, clsa, 2);
        clses = _testFrameStore.getDirectSubclasses(cls);
        assertEqualsList("2", makeList(clsb, clsc, clsa), clses);
        _testFrameStore.moveDirectSubclass(cls, clsa, 0);
        clses = _testFrameStore.getDirectSubclasses(cls);
        assertEqualsList("3", makeList(clsa, clsb, clsc), clses);
    }

    public void testGetDirectSuperslots() {
        if (!_initialized) {
          return;
        }
        Slot slot = createSlot();
        Slot slota = createSlot(slot);
        Slot slotb = createSlot(slot);
        Slot slotc = createSlot(slota);
        _modifiableFrameStore.addDirectSuperslot(slotc, slotb);
        assertEquals("no superslots", 0, _testFrameStore.getDirectSuperslots(slot).size());
        assertEqualsList("1 superslot", makeList(slot), _testFrameStore.getDirectSuperslots(slota));
        assertEqualsList("2 superslots", makeList(slota, slotb), _testFrameStore.getDirectSuperslots(slotc));
    }

    public void testGetSuperslots() {
        if (!_initialized) {
          return;
        }
        Slot slot = createSlot();
        Slot slota = createSlot(slot);
        Slot slotb = createSlot(slot);
        Slot slotc = createSlot(slota);
        _modifiableFrameStore.addDirectSuperslot(slotc, slotb);
        assertEquals("no superslots", 0, _testFrameStore.getSuperslots(slot).size());
        assertEqualsSet("1 superslot", makeList(slot), _testFrameStore.getSuperslots(slota));
        assertEqualsSet("2 superslots", makeList(slot, slota, slotb), _testFrameStore.getSuperslots(slotc));
    }

    public void testGetDirectSubslots() {
        if (!_initialized) {
          return;
        }
        Slot slot = createSlot();
        Slot slota = createSlot(slot);
        Slot slotb = createSlot(slot);
        Slot slotc = createSlot(slota);
        _modifiableFrameStore.addDirectSuperslot(slotc, slotb);
        assertEqualsList("slot", makeList(slota, slotb), _testFrameStore.getDirectSubslots(slot));
        assertEqualsList("slota", makeList(slotc), _testFrameStore.getDirectSubslots(slota));
        assertEqualsList("slotc", makeList(), _testFrameStore.getDirectSubslots(slotc));
    }

    public void testGetSubslots() {
        if (!_initialized) {
          return;
        }
        Slot slot = createSlot();
        Slot slota = createSlot(slot);
        Slot slotb = createSlot(slot);
        Slot slotc = createSlot(slota);
        _modifiableFrameStore.addDirectSuperslot(slotc, slotb);
        assertEqualsSet("slot", makeList(slota, slotb, slotc), _testFrameStore.getSubslots(slot));
        assertEqualsSet("slota", makeList(slotc), _testFrameStore.getDirectSubslots(slota));
        assertEqualsSet("slotc", makeList(), _testFrameStore.getDirectSubslots(slotc));
    }

    public void testAddDirectSuperslot() {
        if (!_initialized) {
          return;
        }
        Slot slot = createSlot();
        Slot slotb = createSlot();
        Slot slotc = createSlot(slot);
        _testFrameStore.addDirectSuperslot(slotb, slot);
        assertEqualsList("b", makeList(slot), _testFrameStore.getDirectSuperslots(slotb));
        _testFrameStore.addDirectSuperslot(slotc, slotb);
        assertEqualsList("c", makeList(slot, slotb), _testFrameStore.getDirectSuperslots(slotc));
    }

    public void testRemoveDirectSuperslot() {
        if (!_initialized) {
          return;
        }
        Slot slota = createSlot();
        Slot slotb = createSlot();
        Slot slotc = createSlot(slota);
        _modifiableFrameStore.addDirectSuperslot(slotc, slotb);
        assertEqualsList("c", makeList(slota, slotb), _testFrameStore.getDirectSuperslots(slotc));
        _testFrameStore.removeDirectSuperslot(slotc, slota);
        assertEqualsList("c1", makeList(slotb), _testFrameStore.getDirectSuperslots(slotc));
        _testFrameStore.removeDirectSuperslot(slotc, slotb);
        assertEqualsList("c0", makeList(), _testFrameStore.getDirectSuperslots(slotc));
    }

    public void testGetDirectTypes() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls cls2 = createCls();
        Instance instance = createSimpleInstance(cls);
        assertEqualsList("1", makeList(cls), _testFrameStore.getDirectTypes(instance));
        _modifiableFrameStore.addDirectType(instance, cls2);
        assertEqualsList("2", makeList(cls, cls2), _testFrameStore.getDirectTypes(instance));
    }

    public void testGetTypes() {
        if (!_initialized) {
          return;
        }
        Cls thing = (Cls) getFrame(Model.Cls.THING);
        Cls cls = createCls();
        Cls cls2 = createCls();
        Instance instance = createSimpleInstance(cls);
        assertEqualsSet("1", makeList(cls, thing), _testFrameStore.getTypes(instance));
        _modifiableFrameStore.addDirectType(instance, cls2);
        assertEqualsSet("2", makeList(cls, cls2, thing), _testFrameStore.getTypes(instance));
    }

    public void testGetDirectInstances() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Instance instanceA = createSimpleInstance(null, cls);
        Instance instanceB = createSimpleInstance(null, cls);
        Instance instancec = createSimpleInstance(null, cls);
        assertEqualsList("1", makeList(instanceA, instanceB, instancec), _testFrameStore.getDirectInstances(cls));
    }

    public void testAddDirectType() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls clsb = createCls();
        Instance instance = createSimpleInstance(cls);
        _testFrameStore.addDirectType(instance, clsb);
        assertEqualsList("1", makeList(cls, clsb), _testFrameStore.getDirectTypes(instance));
    }

    public void testRemoveDirectType() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Cls clsb = createCls();
        Instance instance = createSimpleInstance(cls);
        _testFrameStore.addDirectType(instance, clsb);
        assertEqualsList("1", makeList(cls, clsb), _testFrameStore.getDirectTypes(instance));
        _testFrameStore.removeDirectType(instance, cls);
        assertEqualsList("2", makeList(clsb), _testFrameStore.getDirectTypes(instance));
    }

    public void testGetSlots() {
        if (!_initialized) {
          return;
        }
        Cls standardSlot = (Cls) getFrame(Model.Cls.STANDARD_SLOT);
        Cls slotMetaCls = createCls(standardSlot);
        Collection standardSlots = _testFrameStore.getSlots();
        Slot slot = createSlot();
        Slot slotb = createSlot(slot);
        Slot slotc = createSlotOnCls(slotMetaCls);
        Collection results = makeList(slot, slotb, slotc);
        results.addAll(standardSlots);
        assertEqualsSet("slots", results, _testFrameStore.getSlots());
    }

    public void testGetFacets() {
        if (!_initialized) {
          return;
        }
        Cls standardFacet = (Cls) getFrame(Model.Cls.STANDARD_FACET);
        Cls facetMetaCls = createCls(standardFacet);
        Collection standardFacets = _testFrameStore.getFacets();
        Facet facet = createFacet();
        Facet facetb = createFacet();
        Facet facetc = createFacet(ValueType.STRING, facetMetaCls);
        Collection results = makeList(facet, facetb, facetc);
        results.addAll(standardFacets);
        assertEqualsSet("slots", results, _testFrameStore.getFacets());
    }

    private Set<Reference> getMatchingReferences(String text) {
        return _testFrameStore.getMatchingReferences(text, FrameStore.UNLIMITED_MATCHES);
    }

    public void testGetMatchingReferences() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Instance instance = createSimpleInstance(cls);
        int exactStartSize = getMatchingReferences("refxxx").size();
        int startsWithStartSize = getMatchingReferences("refxxx*").size();
        int containsStartSize = getMatchingReferences("*EFx*").size();
        _modifiableFrameStore.setDirectOwnSlotValues(instance, slot, makeList("refxxx", "erexxx"));
        Instance instance2 = createSimpleInstance(cls);
        _modifiableFrameStore.setDirectOwnSlotValues(instance2, slot, makeList("refxxxerexxx"));
        Collection references = getMatchingReferences("refxxx");
        assertEquals("exact", exactStartSize + 1, references.size());
        Collection references2 = getMatchingReferences("refxxx*");
        assertEquals("startswith", startsWithStartSize + 2, references2.size());
        Collection references3 = getMatchingReferences("*EFx*");
        assertEquals("contains", containsStartSize + 2, references3.size());
    }

    public void testGetFramesWithOwnSlotValue() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Instance instanceA = createSimpleInstance(cls);
        _modifiableFrameStore.setDirectOwnSlotValues(instanceA, slot, makeList("own", "value"));
        Instance instanceB = createSimpleInstance(cls);
        _modifiableFrameStore.setDirectOwnSlotValues(instanceB, slot, makeList("foo", "own"));
        assertEqualsSet("values", makeList(instanceA, instanceB), _testFrameStore.getFramesWithDirectOwnSlotValue(slot,
                "own"));
    }

    public void testGetFramesWithMatchingOwnSlotValue() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, ValueType.STRING, true);
        Instance instanceA = createSimpleInstance(cls);
        _modifiableFrameStore.setDirectOwnSlotValues(instanceA, slot, makeList("own", "value"));
        Instance instanceB = createSimpleInstance(cls);
        _modifiableFrameStore.setDirectOwnSlotValues(instanceB, slot, makeList("foo", "own"));
        Instance instancec = createSimpleInstance(cls);
        _modifiableFrameStore.setDirectOwnSlotValues(instancec, slot, makeList("owx"));
        assertEqualsSet("exact", makeList(instanceA, instanceB), _testFrameStore
                .getFramesWithMatchingDirectOwnSlotValue(slot, "own", FrameStore.UNLIMITED_MATCHES));
        assertEqualsSet("starts with", makeList(instanceA, instanceB, instancec), _testFrameStore
                .getFramesWithMatchingDirectOwnSlotValue(slot, "OW*", FrameStore.UNLIMITED_MATCHES));
    }

    public void testGetTemplateFacets() {
        if (!_initialized) {
          return;
        }
        Cls standardSlot = (Cls) getFrame(Model.Cls.STANDARD_SLOT);
        Slot associatedFacetSlot = (Slot) getFrame(Model.Slot.ASSOCIATED_FACET);
        Cls slotMetaCls = createCls(standardSlot);
        Slot metaClsSlot = createSlotOnCls(slotMetaCls);
        Facet facet = createFacet();
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls, slotMetaCls);
        setOwnSlotValue(metaClsSlot, associatedFacetSlot, facet);
        Collection facets = _testFrameStore.getTemplateFacets(cls, slot);
        assertTrue("new facet", facets.contains(facet));
    }

    public void testSetInverseSlotValues() {
        if (!_initialized) {
          return;
        }
        Cls cls = createCls();
        Slot slota = createSlotOnCls(cls, ValueType.INSTANCE, true);
        Slot slotb = createSlotOnCls(cls, ValueType.INSTANCE, true);
        Slot inverseSlotSlot = (Slot) getFrame(Model.Slot.INVERSE);
        setOwnSlotValue(slota, inverseSlotSlot, slotb);
        Instance instance1 = createSimpleInstance(cls);
        Instance instance2 = createSimpleInstance(cls);
        Cls clsc = createCls();
        Instance instance3 = createSimpleInstance(clsc);
        assertEquals(0, _testFrameStore.getDirectOwnSlotValues(instance1, slota).size());
        _testFrameStore.setDirectOwnSlotValues(instance1, slota, makeList(instance2));
        assertEqualsList("1", makeList(instance2), _testFrameStore.getDirectOwnSlotValues(instance1, slota));
        assertEqualsList("2", makeList(instance1), _testFrameStore.getDirectOwnSlotValues(instance2, slotb));
        _testFrameStore.setDirectOwnSlotValues(instance2, slotb, makeList(instance3));
        assertEquals(0, _testFrameStore.getDirectOwnSlotValues(instance1, slota).size());
        assertEqualsList("3", makeList(instance3), _testFrameStore.getDirectOwnSlotValues(instance2, slotb));
        assertEquals(0, _testFrameStore.getDirectOwnSlotValues(instance3, slota).size());
    }
    


}
