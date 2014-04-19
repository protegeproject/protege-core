package edu.stanford.smi.protege.model;

//ESCA*JAVA0054

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.InstanceAdapter;
import edu.stanford.smi.protege.event.InstanceEvent;
import edu.stanford.smi.protege.event.InstanceListener;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.SlotAdapter;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.event.SlotListener;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreAdapter;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.StandardDateFormat;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Unit tests for the DefaultKnowledgeBase implementation.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public class DefaultKnowledgeBase_Test extends APITestCase {

    public void testFilteredInverseSlotValueNotifications() {
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls);
        Slot slotInverse = createSlotOnCls(cls);
        slot.setInverseSlot(slotInverse);
        slot.setAllowedClses(getDomainKB().getRootClses());
        Instance instance1 = createInstance(cls);
        Instance instance2 = createInstance(cls);
        Instance instance3 = createInstance(cls);
        Instance instance4 = createInstance(cls);
        instance1.setOwnSlotValues(slot, makeList(instance2, instance3));
        final Integer[] counter = new Integer[] { new Integer(0) };
        FrameListener listener = new FrameAdapter() {
            public void ownSlotValueChanged(FrameEvent event) {
                counter[0] = new Integer(counter[0].intValue() + 1);
            }
        };
        getDomainKB().addFrameListener(listener);
        instance1.addOwnSlotValue(slot, instance4);
        assertEquals(2, counter[0].intValue());
    }

    public void testGetOwnSlotValuesWithSubslotInCoreProtege() {
         Slot hasMemberSlot = createSlot();
         Slot hasLeaderSlot = createSlot();
         hasLeaderSlot.addDirectSuperslot(hasMemberSlot);
         Instance person = getDomainKB().getRootCls().createDirectInstance("Person");
         Instance team = getDomainKB().getRootCls().createDirectInstance("Team");
         team.addOwnSlotValue(hasLeaderSlot, person);
         assertEquals(1, team.getOwnSlotValues(hasMemberSlot).size());
         team.addOwnSlotValue(hasMemberSlot, person);
         assertEquals(1, team.getDirectOwnSlotValues(hasMemberSlot).size());
         assertEquals(1, team.getDirectOwnSlotValues(hasLeaderSlot).size());
         assertEquals(2, team.getOwnSlotValues(hasMemberSlot).size());
         assertEquals(1, team.getOwnSlotValues(hasLeaderSlot).size());
    }

    public void testInferredInverseSlotValues() {
        Cls cls1 = createCls();
        Slot slot1 = createSlotOnCls(cls1);
        Cls cls2 = createCls();
        Slot slot2 = createSlotOnCls(cls2);
        Instance instance2 = createInstance(cls2);
        slot2.setInverseSlot(slot1);
        slot1.setAllowedClses(makeList(cls2));
        cls1.setTemplateSlotValues(slot1, makeList(instance2));
        assertEqualsSet(makeList(), instance2.getOwnSlotValues(slot2));
        Instance instance1 = createInstance(cls1);
        assertEqualsSet(makeList(instance1), instance2.getOwnSlotValues(slot2));
    }

    public void testAddDirectTypeSave() {
        Cls cls1 = createCls();
        String cls1Name = cls1.getName();
        Cls cls2 = createCls();
        String cls2Name = cls2.getName();
        Instance instance = createInstance(cls1);
        String instanceName = instance.getName();
        instance.addDirectType(cls2);
        assertEqualsSet(makeList(cls1, cls2), instance.getDirectTypes());
        saveAndReload();
        cls1 = getCls(cls1Name);
        cls2 = getCls(cls2Name);
        instance = getInstance(instanceName);
        assertEqualsSet(makeList(cls1, cls2), instance.getDirectTypes());
    }

    public void testGetValueTypeByOwnSlotValue() {
        Slot slot = createSlot();
        ValueType type = slot.getValueType();
        assertEquals(type, ValueType.STRING);
        Slot valueTypeSlot = getSlot(Model.Slot.VALUE_TYPE);
        slot.setDirectOwnSlotValues(valueTypeSlot, Collections.EMPTY_LIST);
        Collection values = slot.getDirectOwnSlotValues(valueTypeSlot);
        assertEqualsList(values, Collections.EMPTY_LIST);
    }

    public void testDeleteFromCache() {
        KnowledgeBase kb = getDomainKB();
        Cls cls = null; // kb.createCls(null, kb.getRootClses());
        String name = "foo";
        Instance instance = kb.createInstance(name, cls);
        String instanceName = instance.getName();
        instance = kb.getInstance(instanceName);
        instance.delete();
        Instance instance2 = createInstance(instanceName, cls);
        //      instance2.markDeleted(false); /*have to have this uncommented, otherwise I get "<<deleted>>" as the name
        // below*/
        instance2 = getInstance(instanceName);
        assertEquals(name, instance2.getName());
        // System.out.println(instance.getName()); /* returns "<<deleted>>"*/
    }

    public void testOwnSlotValueChangedEventOnInverseSlot() {
        final int[] eventCount = new int[1];
        Slot slotA = createSlot();
        slotA.setValueType(ValueType.INSTANCE);
        Slot slotB = createSlot();
        slotB.setValueType(ValueType.INSTANCE);
        slotA.setInverseSlot(slotB);
        Cls clsA = createCls();
        Cls clsB = createCls();
        clsA.addDirectTemplateSlot(slotA);
        clsB.addDirectTemplateSlot(slotB);
        Instance a = clsA.createDirectInstance(null);
        Instance b = clsB.createDirectInstance(null);
        a.setDirectOwnSlotValue(slotA, b);

        assertEquals(1, a.getDirectOwnSlotValues(slotA).size());
        assertEquals(1, b.getDirectOwnSlotValues(slotB).size());

        getDomainKB().addFrameListener(new FrameAdapter() {
            public void ownSlotValueChanged(FrameEvent event) {
                eventCount[0]++;
            }
        });

        a.setDirectOwnSlotValue(slotA, null);

        assertEquals(0, a.getDirectOwnSlotValues(slotA).size());
        assertEquals(0, b.getDirectOwnSlotValues(slotB).size());
        assertEquals(2, eventCount[0]);
    }

    public void testRemoveFrameStore() {
        final boolean[] called = new boolean[] { false };
        FrameStore fs = new FrameStoreAdapter() {
            public Frame getFrame(String name) {
                called[0] = true;
                return super.getFrame(name);
            }
        };
        getDomainKB().insertFrameStore(fs);
        getDomainKB().getCls("foo");
        assertTrue(called[0]);
        called[0] = false;
        getDomainKB().removeFrameStore(fs);
        getDomainKB().getCls("foo");
        assertFalse(called[0]);
    }

    public void testRemoveFrameStoreWithPosition() {
        final boolean[] called = new boolean[] { false };
        FrameStore fs = new FrameStoreAdapter() {
            public Frame getFrame(String name) {
                called[0] = true;
                return super.getFrame(name);
            }
        };
        getDomainKB().insertFrameStore(fs, 3);
        getDomainKB().getCls("foo");
        assertTrue(called[0]);
        called[0] = false;
        getDomainKB().removeFrameStore(fs);
        getDomainKB().getCls("foo");
        assertFalse(called[0]);
    }

    public void testGetOverriddenDefaultValues() {
        Cls cls = createCls();
        Slot slot = createSlot();
        slot.setDefaultValues(makeList("foo"));
        cls.addDirectTemplateSlot(slot);
        cls.setTemplateSlotDefaultValues(slot, makeList("bar"));
        Cls subcls = createSubCls(cls);
        subcls.setTemplateSlotDefaultValues(slot, makeList("baz"));
        assertEqualsList(makeList("bar"), cls.getTemplateSlotDefaultValues(slot));
        assertEqualsList(makeList("baz"), subcls.getTemplateSlotDefaultValues(slot));
    }

    public void testGetOwnSlotDefaultValues() {
        Cls cls = createCls();
        Slot slot = createSlot();
        slot.setDefaultValues(makeList("foo"));
        cls.addDirectTemplateSlot(slot);
        Instance instance = createInstance(cls);
        assertEqualsList(makeList("foo"), instance.getOwnSlotDefaultValues(slot));
    }

    public void testGetDomain() {
        Cls clsA = createCls();
        Cls clsB = createCls();
        Cls clsB1 = createSubCls(clsB);
        Cls clsB11 = createSubCls(clsB1);
        Slot slot = createSlotOnCls(clsB);
        Collection domain = slot.getDomain();
        assertFalse(domain.contains(clsA));
        assertTrue(domain.contains(clsB));
        assertTrue(domain.contains(clsB1));
        assertTrue(domain.contains(clsB11));
        assertFalse(domain.contains(getDomainKB().getRootCls()));
    }

    public void testUndoCreateCls() {
        enableUndo();
        Cls cls = createCls();
        Cls standardCls = getCls(Model.Cls.STANDARD_CLASS);
        Cls metaclass = createSubCls(standardCls);
        Cls clsA = createSubClsWithType(cls, metaclass);
        String clsAName = clsA.getName();
        Cls clsB = createCls();
        String clsBName = clsB.getName();
        undo();
        assertNull(getFrame(clsBName));
        assertNotNull(getFrame(clsAName));
        undo();
        assertNull(getFrame(clsAName));
        redo();
        Cls clsAPrime = (Cls) getFrame(clsAName);
        assertNotNull(clsAPrime);
        assertEqualsList(makeList(cls), clsAPrime.getDirectSuperclasses());
        assertEqualsList(makeList(metaclass), clsAPrime.getDirectTypes());
        assertNull(getFrame(clsBName));
        redo();
        assertNotNull(getFrame(clsBName));
    }

    private void enableUndo() {
        getDomainKB().setUndoEnabled(true);
    }

    private void undo() {
        assertTrue(getDomainKB().getCommandManager().canUndo());
        getDomainKB().getCommandManager().undo();
    }

    private void redo() {
        assertTrue(getDomainKB().getCommandManager().canRedo());
        getDomainKB().getCommandManager().redo();
    }

    public void testUndoDeleteCls() {
        Cls cls = createCls();
        Cls standardCls = getCls(Model.Cls.STANDARD_CLASS);
        Cls metaCls = createSubCls(standardCls);
        Cls clsA = createSubClsWithType(cls, metaCls);
        String clsAName = clsA.getName();
        enableUndo();
        getDomainKB().deleteCls(clsA);
        assertNull(getFrame(clsAName));
        undo();
        Cls clsAPrime = getCls(clsAName);
        assertNotNull(clsAPrime);
        assertEqualsList(makeList(cls), clsAPrime.getDirectSuperclasses());
        assertEqualsList(makeList(metaCls), clsAPrime.getDirectTypes());
        redo();
        assertNull(getFrame(clsAName));
    }

    public void testUndoDeleteInstanceWithReferences() {
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls);
        Instance instanceA = cls.createDirectInstance(null);
        Instance instanceB = cls.createDirectInstance(null);
        instanceA.setOwnSlotValue(slot, instanceB);
        instanceB.setOwnSlotValue(slot, instanceA);
        enableUndo();
        delete(instanceB);
        assertNull(instanceA.getOwnSlotValue(slot));
        assertTrue(instanceB.isDeleted());
        undo();
        assertEquals(instanceB, instanceA.getOwnSlotValue(slot));
        assertEquals(instanceA, instanceB.getOwnSlotValue(slot));
        redo();
        assertNull(instanceA.getOwnSlotValue(slot));
        assertTrue(instanceB.isDeleted());
    }

    public void testDeleteClsWithSlots() {
        Cls cls = createCls();
        Slot slotA = createSlotOnCls(cls);
        Slot slotB = createSlotOnCls(cls);
        enableUndo();
        delete(cls);
        undo();
        assertEqualsSet(makeList(slotA, slotB), cls.getDirectTemplateSlots());
        redo();
        assertTrue(cls.isDeleted());
        undo();
        assertEqualsSet(makeList(slotA, slotB), cls.getDirectTemplateSlots());
    }

    public void testUndoDeleteWithFacetOverrides() {
        Cls cls = createCls();
        Cls clsA = createCls();
        Slot slot = createSlotOnCls(cls);
        slot.setAllowedClses(getDomainKB().getRootClses());
        cls.setTemplateSlotAllowedClses(slot, makeList(clsA));
        enableUndo();
        delete(clsA); // clsA deleted
        assertEquals(makeList(), cls.getTemplateSlotAllowedClses(slot));
        undo(); // clsA exists
        assertEquals(makeList(clsA), cls.getTemplateSlotAllowedClses(slot));
        redo(); // clsA deleted
        assertEquals(makeList(), cls.getTemplateSlotAllowedClses(slot));
        undo(); // clsA exists
        String clsName = cls.getName();
        delete(cls); // cls deleted
        assertNull(getCls(clsName));
        undo(); // cls exists
        cls = getCls(clsName);
        assertNotNull(cls);
        assertFalse(cls.isDeleted());
        assertEqualsList(makeList(clsA), cls.getTemplateSlotAllowedClses(slot));
        redo();
        assertNull(getCls(clsName));
    }

    public void testFoo() {
        Cls clsA = createCls("A");
        Cls clsB = createCls("B");
        enableUndo();
        delete(clsA); // clsA deleted
        undo(); // clsA exists
        delete(clsB); // cls deleted
        undo(); // cls exists
    }

    public void testDeleteAfterUndo() {
        Cls clsA = createCls();
        String clsAName = clsA.getName();
        Cls clsB = createCls();
        String clsBName = clsB.getName();
        enableUndo();
        delete(clsA);
        delete(clsB);
        assertNull(getFrame(clsAName));
        assertNull(getFrame(clsBName));
        undo();
        assertNull(getFrame(clsAName));
        assertNotNull(getFrame(clsBName));
        undo();
        assertNotNull(getFrame(clsAName));
        assertNotNull(getFrame(clsBName));
        redo();
        redo();
        assertNull(getFrame(clsAName));
        assertNull(getFrame(clsBName));
        undo();
        assertNull(getFrame(clsAName));
        assertNotNull(getFrame(clsBName));
        delete(clsB);
        undo();
        assertNotNull(getFrame(clsBName));
    }

    public void testGetClsCount() {
        KnowledgeBase kb = getDomainKB();
        int count = kb.getClsCount();
        Cls cls = createCls();
        assertEquals(count + 1, kb.getClsCount());
        kb.deleteCls(cls);
        assertEquals(count, kb.getClsCount());
    }

    public void testGetSlotCount() {
        KnowledgeBase kb = getDomainKB();
        int count = kb.getSlotCount();
        Slot slot = createSlot();
        assertEquals(count + 1, kb.getSlotCount());
        kb.deleteSlot(slot);
        assertEquals(count, kb.getSlotCount());
    }

    public void testGetFacetCount() {

    }

    public void testGetSimpleInstanceCount() {
    }

    public void testGetFrameCount() {
    }

    public void testGetClses() {
    }

    public void testGetSlots() {
    }

    public void testGetFacets() {
    }

    public void testGetFrames() {
    }

    public void testGetFrameByID() {
    }

    public void testGetFrameByName() {
    }

    public void testGetFrameName() {
    }

    public void testCreateCls() {
    }

    public void testCreateSlot() {
    }

    public void testCreateFacet() {
    }

    public void testCreateSimpleInstance() {
    }

    public void testDeleteSlot() {
    }

    public void testDeleteFacet() {
    }

    public void testDeleteSimpleInstance() {
    }

    public void testAddInverseSlotValue() {
        Cls c = createCls();
        Slot s1 = createMultiValuedSlot(ValueType.INSTANCE);
        Slot s2 = createMultiValuedSlot(ValueType.INSTANCE);
        s1.setInverseSlot(s2);
        c.addDirectTemplateSlot(s1);
        c.addDirectTemplateSlot(s2);
        Instance instanceA = createInstance(c);
        Instance instanceB = createInstance(c);
        instanceA.addOwnSlotValue(s1, instanceB);
        assertSame("forward", instanceB, instanceA.getOwnSlotValue(s1));
        assertSame("backward", instanceA, instanceB.getOwnSlotValue(s2));
    }

    public void testAddOwnSlotValue() {
        Cls c = createCls();
        Slot s = createMultiValuedSlot(ValueType.INSTANCE);
        Instance instance = createInstance(c);
        Instance value1 = createInstance(c);
        Instance value2 = createInstance(c);
        instance.setOwnSlotValue(s, value1);
        instance.addOwnSlotValue(s, value2);
        assertEquals("pass 1", 2, instance.getOwnSlotValues(s).size());

        instance.setOwnSlotValue(s, value1);
        instance.addOwnSlotValue(s, value2);
        assertEquals("pass 2", 2, instance.getOwnSlotValues(s).size());

        assertSame("add doesn't replace", value1, instance.getOwnSlotValue(s));
    }

    public void testAreValidOwnSlotValues() {
        Cls cls = createCls();
        Slot slot = createSingleValuedSlot(ValueType.INTEGER);
        slot.setMinimumValue(new Integer(2));
        cls.addDirectTemplateSlot(slot);
        Instance instance = createInstance(cls);
        assertFalse("1", instance.areValidOwnSlotValues(slot, Collections.singleton(new Integer(1))));
        assertTrue("2", instance.areValidOwnSlotValues(slot, Collections.singleton(new Integer(2))));
        assertTrue("3", instance.areValidOwnSlotValues(slot, Collections.singleton(new Integer(3))));
    }

    public void testCreateSubslot() {
        Cls c = createCls();
        Slot a = createMultiValuedSlot(ValueType.INSTANCE, c);
        Slot b = createSubSlot(a);
        assertSame("a type", ValueType.INSTANCE, a.getValueType());
        assertSame("b type", ValueType.INSTANCE, b.getValueType());
        assertTrue("a cardinality", a.getAllowsMultipleValues());
        assertTrue("b cardinality", b.getAllowsMultipleValues());
    }

    public void testDeleteCls() {
        Cls cls = createCls();
        Cls subCls1 = createSubCls(cls);
        Cls subCls2 = createSubCls(cls);
        Instance instance1 = createInstance(subCls1);
        Instance instance2 = createInstance(subCls2);

        int nFrames = getFrameCount();
        try {
            deleteFrame(subCls1);
            fail();
        } catch (RuntimeException e) {
            // do nothing
        }
        assertEquals("delete with direct instance", nFrames, getFrameCount());
        try {
            deleteFrame(cls);
        } catch (RuntimeException e) {
            // do nothing
        }
        assertEquals("delete with indirect instance", nFrames, getFrameCount());

        deleteFrame(instance1);
        deleteFrame(subCls1);
        nFrames -= 2;
        assertEquals("delete with no direct instance", nFrames, getFrameCount());
        deleteFrame(instance2);
        deleteFrame(cls);
        nFrames -= 3;
        assertEquals("delete with no instances", nFrames, getFrameCount());
    }

    public void testDeleteClsEvent() {
        final KnowledgeBaseEvent[] firedEvent = new KnowledgeBaseEvent[1];
        Cls cls = createCls();
        String name = cls.getName();
        KnowledgeBaseListener listener = new KnowledgeBaseAdapter() {
            public void clsDeleted(KnowledgeBaseEvent event) {
                firedEvent[0] = event;
            }
        };
        cls.getKnowledgeBase().addKnowledgeBaseListener(listener);
        clearEvents();
        deleteFrame(cls);
        cls.getKnowledgeBase().removeKnowledgeBaseListener(listener);
        assertNotNull("fired", firedEvent[0]);
        assertEquals("name available", name, firedEvent[0].getOldName());
    }

    public void testFacetOverrides() {
        Cls a = createCls();
        Cls b = createSubCls(a);
        Slot s = createSingleValuedSlot(ValueType.ANY);
        // s.setAllowedClses(Collections.singleton(c));
        a.addDirectTemplateSlot(s);
        Facet valueTypeFacet = (Facet) getFrame(Model.Facet.VALUE_TYPE);
        // Facet maxCardFacet = (Facet) getFrame(Model.Facet.MAXIMUM_CARDINALITY);
        assertEquals(ValueType.ANY, s.getValueType());
        assertFalse("a not overridden", a.hasOverriddenTemplateSlot(s));
        assertFalse("b not overridden", b.hasOverriddenTemplateSlot(s));
        assertFalse("a not directly overridden", a.hasDirectlyOverriddenTemplateSlot(s));
        assertFalse("b not directly overridden", b.hasDirectlyOverriddenTemplateSlot(s));

        a.setTemplateSlotValueType(s, ValueType.FLOAT);
        assertEquals(ValueType.ANY, s.getValueType());
        assertEquals(ValueType.FLOAT, a.getTemplateSlotValueType(s));

        assertTrue("a overridden", a.hasOverriddenTemplateSlot(s));
        assertTrue("b overridden", b.hasOverriddenTemplateSlot(s));
        assertTrue("a directly overridden", a.hasDirectlyOverriddenTemplateSlot(s));
        assertFalse("b not directly overridden", b.hasDirectlyOverriddenTemplateSlot(s));
        assertTrue("a valueType overridden", a.hasOverriddenTemplateFacet(s, valueTypeFacet));
        assertTrue("a valueType directly overridden", a.hasDirectlyOverriddenTemplateFacet(s, valueTypeFacet));
        assertTrue("b valueType overridden", b.hasOverriddenTemplateFacet(s, valueTypeFacet));
        assertFalse("b valueType direct overridden", b.hasDirectlyOverriddenTemplateFacet(s, valueTypeFacet));
    }

    public void testIsDirectlyOverriddenFacet() {
        Cls a = createCls();
        Cls b = createSubCls(a);
        Slot s = createSingleValuedSlot(ValueType.INSTANCE);
        a.addDirectTemplateSlot(s);
        b.setTemplateSlotDefaultValues(s, Collections.singleton(a));
        Iterator i = b.getTemplateFacets(s).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            boolean overridden = b.hasOverriddenTemplateFacet(s, facet);
            boolean shouldBeOverridden = facet.getName().equals(Model.Facet.DEFAULTS);
            assertEquals("facet: " + facet, shouldBeOverridden, overridden);
        }
    }

    public void testGetMatchingReferences() {
        Slot s = createMultiValuedSlot(ValueType.STRING);
        Cls a = createCls();
        a.addDirectTemplateSlot(s);
        Instance inst1 = createInstance(a);
        Instance inst2 = createInstance(a);
        Instance inst3 = createInstance(a);

        inst1.addOwnSlotValue(s, "zabcy");
        inst2.addOwnSlotValue(s, "abcz");
        inst3.addOwnSlotValue(s, "qqq");
        assertEquals("exact", 0, getDomainKB().getMatchingReferences("z", 0).size());
        assertEquals("starts", 1, getDomainKB().getMatchingReferences("z*", KnowledgeBase.UNLIMITED_MATCHES).size());
        assertEquals("contains", 2, getDomainKB().getMatchingReferences("*z*", KnowledgeBase.UNLIMITED_MATCHES).size());
        assertEquals("contains insensitive", 2, getDomainKB().getMatchingReferences("*Z*",
                KnowledgeBase.UNLIMITED_MATCHES).size());
        assertEquals("contains 2", 2, getDomainKB().getMatchingReferences("*abc*", KnowledgeBase.UNLIMITED_MATCHES)
                .size());
    }

    public void testGetFramesWithValue() {
        Slot s1 = createMultiValuedSlot(ValueType.STRING);
        Slot s2 = createMultiValuedSlot(ValueType.INSTANCE);
        Cls a = createCls();
        a.addDirectTemplateSlot(s1);
        a.addDirectTemplateSlot(s2);
        Instance inst1 = createInstance(a);
        Instance inst2 = createInstance(a);
        Instance inst3 = createInstance(a);

        inst1.addOwnSlotValue(s1, "abc");
        inst2.addOwnSlotValue(s1, "abc");
        inst3.addOwnSlotValue(s1, "abc");

        inst2.addOwnSlotValue(s2, inst1);

        assertEquals("string match", 3, getDomainKB().getFramesWithValue(s1, null, false, "abc").size());
        assertEquals("frame match", 1, getDomainKB().getFramesWithValue(s2, null, false, inst1).size());
    }

    public void testGetInstances() {
        List c = new ArrayList(getProject().getSources().getKnowledgeBase().getInstances());
        Collections.sort(c, new edu.stanford.smi.protege.ui.FrameNameComparator());
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            assertNotNull(instance.getName());
        }

    }

    public void testGetMatchingFrameNames() {
    }

    public void testInverseSlotRelationship() {
        Slot s1 = createSingleValuedSlot(ValueType.INSTANCE);
        Slot s2 = createSingleValuedSlot(ValueType.INSTANCE);
        s1.setInverseSlot(s2);
        assertSame("created", s1, s2.getInverseSlot());
        s1.setInverseSlot(null);
        assertNull("null", s2.getInverseSlot());
    }

    public void testJavaPackagesOnInstance() {
        Class testJavaClass = TestSimpleInstance.class;
        String fullClsName = testJavaClass.getName();
        int index = fullClsName.lastIndexOf('.');
        String packageName = fullClsName.substring(0, index);
        String clsName = fullClsName.substring(index + 1);

        getProject().addJavaPackageName(packageName);
        Cls testCls = getCls(clsName);
        if (testCls == null) {
            testCls = createCls(clsName);
        }
        Instance instance = createInstance(testCls);
        assertEquals(testJavaClass, instance.getClass());

        saveAndReload();
        testCls = getCls(clsName);
        assertNotNull("after reload", testCls);
        Iterator<Instance> i = testCls.getDirectInstances().iterator();
        while (i.hasNext()) {
            Instance inst = i.next();
            assertTrue("correct class: " + inst, testJavaClass.isInstance(instance));
        }
        getProject().removeJavaPackageName(packageName);
    }

    public void testJavaPackagesOnCls() {
        Class testClass = TestCls.class;
        String fullClsName = testClass.getName();
        int index = fullClsName.lastIndexOf('.');
        String packageName = fullClsName.substring(0, index);
        String clsName = fullClsName.substring(index + 1);

        getProject().addJavaPackageName(packageName);
        Cls standardCls = getCls(Model.Cls.STANDARD_CLASS);
        Cls testCls = getCls(clsName);
        if (testCls == null) {
            testCls = createCls(clsName, standardCls);
        }
        Cls cls = createClsWithType(testCls);
        assertTrue("correct class: " + cls, testClass.isInstance(cls));

        saveAndReload();
        testCls = getCls(clsName);
        assertNotNull("after reload", testCls);
        Iterator i = testCls.getDirectInstances().iterator();
        while (i.hasNext()) {
            Cls clsA = (Cls) i.next();
            assertTrue("correct class: " + clsA.getClass(), testClass.isInstance(clsA));
        }

        getProject().removeJavaPackageName(packageName);
    }

    public void testJavaPackagesCreateClsEvent() {
        final Class testClass = TestCls.class;
        String fullClsName = testClass.getName();
        int index = fullClsName.lastIndexOf('.');
        String packageName = fullClsName.substring(0, index);
        String clsName = fullClsName.substring(index + 1);

        getProject().addJavaPackageName(packageName);
        Cls standardCls = getCls(Model.Cls.STANDARD_CLASS);
        Cls testCls = getCls(clsName);
        if (testCls == null) {
            testCls = createCls(clsName, standardCls);
        }
        KnowledgeBaseListener listener = new KnowledgeBaseAdapter() {
            public void clsCreated(KnowledgeBaseEvent event) {
                assertTrue(testClass.isInstance(event.getCls()));
                recordEventFired(event);
            }
        };
        getDomainKB().addKnowledgeBaseListener(listener);
        clearEvents();
        Cls cls = createClsWithType(testCls);
        assertTrue(testClass.isInstance(cls));
        assertEventFired(KnowledgeBaseEvent.CLS_CREATED);
    }

    public void testCreateClsEvent() {
        KnowledgeBaseListener listener = new KnowledgeBaseAdapter() {
            public void clsCreated(KnowledgeBaseEvent event) {
                assertNotNull(event.getCls());
                recordEventFired(event);
            }
        };
        getDomainKB().addKnowledgeBaseListener(listener);
        clearEvents();
        Cls cls = createCls();
        assertNotNull(cls);
        assertEventFired(KnowledgeBaseEvent.CLS_CREATED);
    }

    public void testFrameFactoryCreateClsEvent() {
        getDomainKB().setFrameFactory(new TestFrameFactory(getDomainKB()));
        KnowledgeBaseListener listener = new KnowledgeBaseAdapter() {
            public void clsCreated(KnowledgeBaseEvent event) {
                assertTrue(event.getCls() instanceof TestCls);
                recordEventFired(event);
            }
        };
        getDomainKB().addKnowledgeBaseListener(listener);
        clearEvents();
        Cls cls = createCls();
        assertTrue(cls instanceof TestCls);
        assertEventFired(KnowledgeBaseEvent.CLS_CREATED);
    }

    public void testModificationSlots() throws java.text.ParseException {
        final String user_name = ApplicationProperties.getUserName();
        Date start = new Date();
        SystemUtilities.sleepMsec(100);
        KnowledgeBase kb = getDomainKB();
        kb.setModificationRecordUpdatingEnabled(true);

        Cls metaCls = createSubCls(getCls(Model.Cls.STANDARD_CLASS));
        metaCls.addDirectTemplateSlot(getSlot(Model.Slot.CREATOR));
        metaCls.addDirectTemplateSlot(getSlot(Model.Slot.CREATION_TIMESTAMP));
        metaCls.addDirectTemplateSlot(getSlot(Model.Slot.MODIFIER));
        metaCls.addDirectTemplateSlot(getSlot(Model.Slot.MODIFICATION_TIMESTAMP));
        Slot slot = createSingleValuedSlot(ValueType.INSTANCE);
        Cls a = (Cls) createInstance(metaCls);
        assertEquals("creator", user_name, kb.getFrameCreator(a));
        String creationString = kb.getFrameCreationTimestamp(a);
        assertNotNull("creation stamp", creationString);
        Date createDate = new StandardDateFormat().parse(creationString);
        SystemUtilities.sleepMsec(100);
        Date endDate = new Date();
        assertTrue("create timestamp after begin: " + createDate + " - " + start, createDate.after(start));
        assertTrue("create timestamp before end: " + createDate + " - " + endDate, createDate.before(endDate));

        a.addDirectTemplateSlot(slot);
        SystemUtilities.sleepMsec(100);

        String modifier = kb.getFrameLastModifier(a);
        assertEquals("name", user_name, modifier);
        String modStamp = kb.getFrameLastModificationTimestamp(a);
        assertNotNull("timestamp", modStamp);
        Date modDate = new StandardDateFormat().parse(modStamp);
        SystemUtilities.sleepMsec(100);
        Date end = new Date();
        assertTrue("mod timestamp after begin: " + modDate + " - " + start, modDate.after(start));
        assertTrue("mod timestamp before end: " + modDate + " - " + end, modDate.before(end));
        kb.setModificationRecordUpdatingEnabled(false);
    }

    public void testRecursiveDelete() {
        Cls clsA = createCls();
        Cls clsB = createSubCls(clsA);
        createSubCls(clsB);
        int count = getFrameCount();
        deleteFrame(clsB);
        int newCount = count - 2;
        assertEquals(newCount, getFrameCount());
        saveAndReload();
        assertEquals(newCount, getFrameCount());
    }

    public void testRemoveInverseSlotValue() {
        Cls c = createCls();
        Slot s1 = createSingleValuedSlot(ValueType.INSTANCE);
        Slot s2 = createSingleValuedSlot(ValueType.INSTANCE);
        s1.setInverseSlot(s2);
        c.addDirectTemplateSlot(s1);
        c.addDirectTemplateSlot(s2);
        Instance instanceA = createInstance(c);
        Instance instanceB = createInstance(c);
        instanceA.setOwnSlotValues(s1, Collections.singleton(instanceB));
        instanceA.setOwnSlotValue(s1, null);
        assertNull("forward", instanceA.getOwnSlotValue(s1));
        assertNull("backward", instanceB.getOwnSlotValue(s2));
    }

    public void testRemoveOwnSlotValue() {
        Cls c = createCls();
        Slot s = createSingleValuedSlot(ValueType.INSTANCE);
        c.addDirectTemplateSlot(s);
        Instance instanceA = createInstance(c);
        Instance instanceB = createInstance(c);
        Instance instanceC = createInstance(c);

        Collection values = new ArrayList();
        values.add(instanceB);
        values.add(instanceC);

        instanceA.setOwnSlotValues(s, values);
        instanceA.removeOwnSlotValue(s, instanceC);
        assertEquals("size", 1, instanceA.getOwnSlotValueCount(s));
        assertEquals("1st value", instanceB, instanceA.getOwnSlotValue(s));

        instanceA.setOwnSlotValues(s, values);
        instanceA.removeOwnSlotValue(s, instanceB);
        assertEquals("size", 1, instanceA.getOwnSlotValueCount(s));
        assertEquals("2nd value", instanceC, instanceA.getOwnSlotValue(s));
    }

    public void testSaveLoadTemplateSlotValues() {
        Cls c1 = createCls();
        Cls c2 = createCls();
        String c2Name = c2.getName();
        Slot s = createSingleValuedSlot(ValueType.INSTANCE);
        String sName = s.getName();
        c2.addDirectTemplateSlot(s);
        Collection values = Collections.singleton(c1);
        s.setValues(values);
        c2.setTemplateSlotValues(s, values);
        saveAndReload();
        s = getSlot(sName);
        c2 = getCls(c2Name);
        assertEquals(1, s.getValues().size());
        assertEquals(1, c2.getTemplateSlotValues(s).size());
    }

    public void testSetInverseSlotValue() {
        Cls c = createCls();
        Slot s1 = createMultiValuedSlot(ValueType.INSTANCE);
        Slot s2 = createMultiValuedSlot(ValueType.INSTANCE);
        s1.setInverseSlot(s2);
        c.addDirectTemplateSlot(s1);
        c.addDirectTemplateSlot(s2);
        Instance instanceA = createInstance(c);
        Instance instanceB = createInstance(c);
        String s1Name = s1.getName();
        String s2Name = s2.getName();
        String instanceAName = instanceA.getName();
        String instanceBName = instanceB.getName();

        instanceA.setOwnSlotValue(s1, instanceB);
        assertSame("set with value", instanceA, instanceB.getOwnSlotValue(s2));
        assertEquals(1, instanceB.getOwnSlotValues(s2).size());
        assertEquals(1, instanceA.getOwnSlotValues(s1).size());
        instanceA.setOwnSlotValue(s1, null);
        assertNull(instanceB.getOwnSlotValue(s2));

        instanceA.setOwnSlotValue(s1, instanceB);
        saveAndReload();
        s1 = getSlot(s1Name);
        s2 = getSlot(s2Name);
        instanceA = getInstance(instanceAName);
        instanceB = getInstance(instanceBName);
        assertEquals(instanceA, instanceB.getOwnSlotValue(s2));
        assertEquals(1, instanceB.getOwnSlotValues(s2).size());
        assertEquals(1, instanceA.getOwnSlotValues(s1).size());
    }

    public void testSetInverseSlotValues1N() {
        Cls cls = createCls();
        Slot s1 = createSingleValuedSlot(ValueType.INSTANCE);
        Slot sN = createMultiValuedSlot(ValueType.INSTANCE);
        assertEquals(1, s1.getMaximumCardinality());
        s1.setInverseSlot(sN);
        cls.addDirectTemplateSlot(s1);
        cls.addDirectTemplateSlot(sN);
        Instance instanceA = createInstance(cls);
        Instance instanceB = createInstance(cls);
        Instance instanceC = createInstance(cls);
        String s1Name = s1.getName();
        String sNName = sN.getName();
        String instanceAName = instanceA.getName();
        String instanceBName = instanceB.getName();
        String instanceCName = instanceC.getName();

        instanceB.setOwnSlotValue(s1, instanceC);
        assertEquals("b connected to c", instanceC, instanceB.getOwnSlotValue(s1));
        assertEquals("c connected to b", instanceB, instanceC.getOwnSlotValue(sN));

        instanceA.setOwnSlotValues(sN, Collections.singleton(instanceB));
        assertEquals("size b", 1, instanceB.getOwnSlotValues(s1).size());
        assertEquals("b connected to a", instanceA, instanceB.getOwnSlotValue(s1));
        assertNull("c disconnected", instanceC.getOwnSlotValue(sN));

        saveAndReload();
        s1 = getSlot(s1Name);
        sN = getSlot(sNName);
        instanceA = getInstance(instanceAName);
        instanceB = getInstance(instanceBName);
        instanceC = getInstance(instanceCName);
        assertEquals("size b", 1, instanceB.getOwnSlotValues(s1).size());
        assertEquals("b connected to a", instanceA, instanceB.getOwnSlotValue(s1));
        assertNull("c disconnected", instanceC.getOwnSlotValue(sN));
    }

    public void testSetInverseSlotValuesNN() {
        Cls cls = createCls();
        Slot s1 = createMultiValuedSlot(ValueType.INSTANCE);
        Slot s2 = createMultiValuedSlot(ValueType.INSTANCE);
        s1.setInverseSlot(s2);
        cls.addDirectTemplateSlot(s1);
        cls.addDirectTemplateSlot(s2);
        Instance instanceA = createInstance(cls);
        Instance instanceB = createInstance(cls);
        String s2Name = s2.getName();
        String instanceAName = instanceA.getName();
        String instanceBName = instanceB.getName();

        instanceA.setOwnSlotValues(s1, Collections.singleton(instanceB));
        assertEquals("set with values", instanceA, instanceB.getOwnSlotValue(s2));
        instanceA.setOwnSlotValues(s1, Collections.EMPTY_LIST);
        assertNull("set with empty collection", instanceB.getOwnSlotValue(s2));

        instanceA.setOwnSlotValues(s1, Collections.singleton(instanceB));
        saveAndReload();
        s2 = getSlot(s2Name);
        instanceA = getInstance(instanceAName);
        instanceB = getInstance(instanceBName);
        assertEquals("set with values", instanceA, instanceB.getOwnSlotValue(s2));
    }

    public void testSetOwnSlotValue() {
        Cls c = createCls();
        Slot s = createMultiValuedSlot(ValueType.INSTANCE);
        c.addDirectTemplateSlot(s);
        Instance instanceA = createInstance(c);
        Instance instanceB = createInstance(c);

        instanceA.setOwnSlotValue(s, instanceB);
        Collection values = instanceA.getOwnSlotValues(s);
        assertEquals(1, values.size());
        assertEquals(instanceB, CollectionUtilities.getFirstItem(values));

        instanceA.setOwnSlotValue(s, instanceA);
        values = instanceA.getOwnSlotValues(s);
        assertEquals(1, values.size());
        assertEquals(instanceA, CollectionUtilities.getFirstItem(values));
    }

    public void testSetOwnSlotValues() {
        getDomainKB().setValueChecking(true);
        Cls cls = createCls();
        Slot slot = createSingleValuedSlot(ValueType.STRING);
        cls.addDirectTemplateSlot(slot);
        Instance i = createInstance(cls);
        Collection values = new ArrayList();
        values.add("");
        i.setOwnSlotValues(slot, values);
        values.add("");
        try {
            i.setOwnSlotValues(slot, values);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // do nothing
        } finally {
            getDomainKB().setValueChecking(false);
        }
    }

    public void testSubslots() {
        Cls cls = createCls();
        Instance instanceA = createInstance(cls);
        String instanceAName = instanceA.getName();
        Slot a = createMultiValuedSlot(ValueType.STRING);
        Slot b = createSubSlot(a);
        String obj1 = "a";
        String obj2 = "b";
        String aSlotName = a.getName();

        cls.addDirectTemplateSlot(a);
        cls.addDirectTemplateSlot(b);
        instanceA.setOwnSlotValue(a, obj1);
        instanceA.setOwnSlotValue(b, obj2);
        Collection aValues = instanceA.getOwnSlotValues(a);
        assertEquals("a", 2, aValues.size());
        assertTrue("a contains", aValues.contains(obj1));
        Collection bValues = instanceA.getOwnSlotValues(b);
        assertEquals("b", 1, bValues.size());
        assertTrue("b contains", bValues.contains(obj2));
        Collection allAValues = instanceA.getOwnSlotAndSubslotValues(a);
        assertEquals("a and b", 2, allAValues.size());
        assertTrue("a and b contains 1", allAValues.contains(obj1));
        assertTrue("a and b contains 2", allAValues.contains(obj2));
        Collection allBValues = instanceA.getOwnSlotAndSubslotValues(b);
        assertEquals("b but not a", 1, allBValues.size());
        assertTrue("b but not a contains 2", allBValues.contains(obj2));

        saveAndReload();
        a = getSlot(aSlotName);
        instanceA = getInstance(instanceAName);
        assertNotNull("slot a", a);
        allAValues = instanceA.getOwnSlotAndSubslotValues(a);
        assertEquals("save", 2, allAValues.size());
        assertTrue("save value 1", allAValues.contains(obj1));
        assertTrue("save value 2", allAValues.contains(obj2));
    }

    public void testGetSuperslots() {
        Slot a = createSlot();
        Slot b = createSubSlot(a);
        Slot c = createSubSlot(b);
        Collection superslots = c.getSuperslots();
        assertEqualsSet(makeList(a, b), superslots);
    }

    public void testTopLevelGetTemplateSlotValues() {
        Cls cls = createCls();
        Slot slot = createSlot();
        Collection initialValues = makeList("foo", "bar");
        slot.setValues(initialValues);
        Collection directValues = slot.getValues();
        assertEqualsList(initialValues, directValues);
        cls.addDirectTemplateSlot(slot);
        Collection indirectValues = cls.getTemplateSlotValues(slot);
        assertEqualsList(initialValues, indirectValues);
    }

    public void testGetTemplateSlotValues() {
        Cls cls = createCls();
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        Collection initialValues = makeList("foo", "bar");
        cls.setTemplateSlotValues(slot, initialValues);
        Collection directValues = cls.getTemplateSlotValues(slot);
        assertEqualsList(initialValues, directValues);
        // directValues.add("baz");
        Collection directValues2 = cls.getTemplateSlotValues(slot);
        assertEquals(2, directValues2.size());
        assertEqualsList(initialValues, directValues2);
    }

    public void testAddTemplateSlotValue() {
        String VALUE = "foo";
        Cls cls = createCls();
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        cls.addTemplateSlotValue(slot, VALUE);
        Collection values = cls.getTemplateSlotValues(slot);
        assertEquals(1, values.size());
        assertEquals(VALUE, CollectionUtilities.getFirstItem(values));
    }

    public void testValueTypeOverride() {
        Cls cls = createCls();
        Slot slot = createSlot();
        String clsName = cls.getName();
        String slotName = slot.getName();
        cls.addDirectTemplateSlot(slot);
        Facet facet = (Facet) getFrame(Model.Facet.VALUE_TYPE);
        assertFalse(cls.hasDirectlyOverriddenTemplateFacet(slot, facet));
        assertFalse(cls.hasOverriddenTemplateFacet(slot, facet));
        assertFalse(cls.hasOverriddenTemplateSlot(slot));
        assertFalse(cls.hasDirectlyOverriddenTemplateSlot(slot));

        saveAndReload();

        cls = (Cls) getFrame(clsName);
        slot = (Slot) getFrame(slotName);
        facet = (Facet) getFrame(Model.Facet.VALUE_TYPE);
        assertFalse("facet direct override 2", cls.hasDirectlyOverriddenTemplateFacet(slot, facet));
        assertFalse("facet override 2", cls.hasOverriddenTemplateFacet(slot, facet));
        assertFalse("slot override 2", cls.hasOverriddenTemplateSlot(slot));
        assertFalse("slot direct override 2", cls.hasDirectlyOverriddenTemplateSlot(slot));
    }

    public void testTemplateSlotValue() {
        final Collection values = new ArrayList();
        values.add("foo");
        Cls cls = createCls();
        Slot slot = createSlot();
        String clsName = cls.getName();
        String slotName = slot.getName();
        cls.addDirectTemplateSlot(slot);
        cls.setTemplateSlotValues(slot, values);
        assertEqualsList(values, cls.getDirectTemplateSlotValues(slot));
        assertEqualsList(values, cls.getTemplateSlotValues(slot));

        saveAndReload();
        cls = getCls(clsName);
        slot = getSlot(slotName);
        assertEqualsList(values, cls.getDirectTemplateSlotValues(slot));
        assertEqualsList(values, cls.getTemplateSlotValues(slot));
    }

    public void testTemplateSlotValue2() {
        final Collection values = new ArrayList();
        values.add("foo");
        Cls cls = createCls();
        Slot slot = createSlot();
        Facet facet = getFacet(Model.Facet.VALUES);
        String clsName = cls.getName();
        String slotName = slot.getName();
        cls.addDirectTemplateSlot(slot);
        cls.setTemplateFacetValues(slot, facet, values);
        assertEqualsList(values, cls.getDirectTemplateFacetValues(slot, facet));
        assertEqualsList(values, cls.getTemplateFacetValues(slot, facet));

        saveAndReload();
        cls = getCls(clsName);
        slot = getSlot(slotName);
        facet = getFacet(Model.Facet.VALUES);
        assertEqualsList(values, cls.getDirectTemplateFacetValues(slot, facet));
        assertEqualsList(values, cls.getTemplateFacetValues(slot, facet));
    }

    public void testSetDirectTypeEvent() {
        Cls cls1 = createCls();
        Instance instance = createInstance(cls1);
        Cls cls2 = createCls();
        ClsListener clsListener1 = new ClsAdapter() {
            public void directInstanceRemoved(ClsEvent event) {
                super.directInstanceRemoved(event);
                recordEventFired(event);
            }
        };
        cls1.addClsListener(clsListener1);
        ClsListener clsListener2 = new ClsAdapter() {
            public void directInstanceAdded(ClsEvent event) {
                super.directInstanceAdded(event);
                recordEventFired(event);
            }
        };
        cls2.addClsListener(clsListener2);
        InstanceListener instanceListener = new InstanceAdapter() {
            public void directTypeAdded(InstanceEvent event) {
                super.directTypeAdded(event);
                recordEventFired(event);
            }

            public void directTypeRemoved(InstanceEvent event) {
                super.directTypeRemoved(event);
                recordEventFired(event);
            }
        };
        instance.addInstanceListener(instanceListener);
        clearEvents();
        instance.setDirectType(cls2);
        assertEventFired(ClsEvent.DIRECT_INSTANCE_REMOVED);
        assertEventFired(ClsEvent.DIRECT_INSTANCE_ADDED);
        assertEventFired(InstanceEvent.DIRECT_TYPE_ADDED);
        assertEventFired(InstanceEvent.DIRECT_TYPE_REMOVED);
    }

    public void testAddFrameListener() {
        FrameListener listener = new FrameAdapter() {
            public void ownSlotValueChanged(FrameEvent event) {
                recordEventFired(event);
            }
        };
        Slot slot = createSlot();
        Frame frame = createFrame();
        frame.addFrameListener(listener);
        clearEvents();
        frame.setOwnSlotValue(slot, "foo");
        assertEventFired(FrameEvent.OWN_SLOT_VALUE_CHANGED);
        frame.removeFrameListener(listener);
        clearEvents();
        frame.setOwnSlotValue(slot, "bar");
        assertEquals(0, getEventCount());

        getDomainKB().addFrameListener(listener);
        frame.setOwnSlotValue(slot, "baz");
        assertEventFired(FrameEvent.OWN_SLOT_VALUE_CHANGED);
    }

    public void testAddSlotListener() {
        SlotListener listener = new SlotAdapter() {
            public void templateSlotClsAdded(SlotEvent event) {
                recordEventFired(event);
            }

            public void templateSlotClsRemoved(SlotEvent event) {
                recordEventFired(event);
            }
        };
        Slot slot = createSlot();
        Cls cls = createCls();
        slot.addSlotListener(listener);
        clearEvents();
        cls.addDirectTemplateSlot(slot);
        assertEventFired(SlotEvent.TEMPLATE_SLOT_CLS_ADDED);
        slot.removeSlotListener(listener);
        clearEvents();
        cls.removeDirectTemplateSlot(slot);
        assertEquals(0, getEventCount());

        getDomainKB().addSlotListener(listener);
        cls.addDirectTemplateSlot(slot);
        assertEventFired(SlotEvent.TEMPLATE_SLOT_CLS_ADDED);
    }

    public void testAddClsListener() {
        ClsListener listener = new ClsAdapter() {
            public void directSuperclassAdded(ClsEvent event) {
                recordEventFired(event);
            }

            public void directSuperclassRemoved(ClsEvent event) {
                recordEventFired(event);
            }
        };
        Cls cls = createCls();
        Cls clsA = createCls();
        cls.addClsListener(listener);
        clearEvents();
        cls.addDirectSuperclass(clsA);
        assertEventFired(ClsEvent.DIRECT_SUPERCLASS_ADDED);
        cls.removeClsListener(listener);
        clearEvents();
        cls.removeDirectSuperclass(clsA);
        assertEquals(0, getEventCount());

        getDomainKB().addClsListener(listener);
        cls.addDirectSuperclass(clsA);
        assertEventFired(ClsEvent.DIRECT_SUPERCLASS_ADDED);
    }

    public void testDeleteInstanceEvent() {
        Cls cls = createCls();
        Instance instance = createInstance(cls);
        KnowledgeBaseListener kbListener = new KnowledgeBaseAdapter() {
            public void instanceDeleted(KnowledgeBaseEvent event) {
                super.instanceDeleted(event);
                recordEventFired(event);
            }
        };
        getDomainKB().addKnowledgeBaseListener(kbListener);
        ClsListener clsListener = new ClsAdapter() {
            public void directInstanceRemoved(ClsEvent event) {
                recordEventFired(event);
            }
        };
        getDomainKB().addClsListener(cls, clsListener);
        clearEvents();
        getDomainKB().deleteInstance(instance);
        assertEventFired(KnowledgeBaseEvent.INSTANCE_DELETED);
        assertEventFired(ClsEvent.DIRECT_INSTANCE_REMOVED);
    }

    public void testAddSuperclassEvents() {
        Cls cls = createCls();
        Cls supercls = createCls();
        getDomainKB().addClsListener(new ClsAdapter() {
            public void directSuperclassAdded(ClsEvent event) {
                recordEventFired(event);
            }

            public void directSubclassAdded(ClsEvent event) {
                recordEventFired(event);
            }
        });
        clearEvents();
        cls.addDirectSuperclass(supercls);
        assertEquals(2, getEventCount());
        assertEventFired(ClsEvent.DIRECT_SUPERCLASS_ADDED);
        assertEventFired(ClsEvent.DIRECT_SUBCLASS_ADDED);
    }

    public void testAddSuperslotEvents() {
        Slot slot = createSlot();
        Slot superSlot = createSlot();
        getDomainKB().addSlotListener(new SlotAdapter() {
            public void directSuperslotAdded(SlotEvent event) {
                super.directSuperslotAdded(event);
                recordEventFired(event);
            }

            public void directSubslotAdded(SlotEvent event) {
                super.directSubslotAdded(event);
                recordEventFired(event);
            }
        });
        clearEvents();
        slot.addDirectSuperslot(superSlot);
        assertEquals(2, getEventCount());
        assertEventFired(SlotEvent.DIRECT_SUPERSLOT_ADDED);
        assertEventFired(SlotEvent.DIRECT_SUBSLOT_ADDED);
    }

    public void testSetBadOwnSlotValue() {
        getDomainKB().setValueChecking(true);
        Frame frame = createFrame();
        Slot slot = createSlot();
        Object o = new Object();
        try {
            frame.setOwnSlotValue(slot, o);
            fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    public void testSetBadFrameName() {
        String GOOD_NAME = "foo";
        String BAD_NAME = "foo * foo";
        KnowledgeBase kb = getDomainKB();
        Frame cls = createCls();
        assertTrue(kb.isValidFrameName(GOOD_NAME, cls));
        assertNull(kb.getInvalidFrameNameDescription(GOOD_NAME, cls));
        assertTrue(kb.isValidFrameName(BAD_NAME, cls));
        assertNull(kb.getInvalidFrameNameDescription(BAD_NAME, cls));

        kb.setFrameNameValidator(new FrameNameValidator() {
            public String getErrorMessage(String name, Frame frame) {
                return isValid(name, frame) ? null : "Contains a *";
            }

            public boolean isValid(String name, Frame frame) {
                return name.indexOf("*") == -1;
            }
        });
        assertTrue(kb.isValidFrameName(GOOD_NAME, cls));
        assertNull(kb.getInvalidFrameNameDescription(GOOD_NAME, cls));
        assertFalse(kb.isValidFrameName(BAD_NAME, cls));
        assertNotNull(kb.getInvalidFrameNameDescription(BAD_NAME, cls));
    }

    public void testDefaultClsMetaCls() {
        Cls cls = createCls();
        Cls directType = cls.getDirectType();
        Cls standardCls = (Cls) getFrame(Model.Cls.STANDARD_CLASS);
        assertEquals(directType, standardCls);
        Cls metaCls = createCls(null, standardCls);
        getDomainKB().setDefaultClsMetaCls(metaCls);
        Cls cls1 = createCls(null, cls);
        assertEquals(cls1.getDirectType(), standardCls);
        Cls cls2 = createCls();
        assertEquals(cls2.getDirectType(), metaCls);
    }

    public void testRecursiveEquivalentClassesDelete() {
        Cls clsA = createCls();
        Cls clsB = createCls();
        Cls clsC = createSubCls(clsB);
        int frameCount = getFrameCount();
        Cls clsD = createSubCls(clsB);
        clsC.addDirectSuperclass(clsD);
        clsD.addDirectSuperclass(clsC);
        clsD.addDirectSuperclass(clsA);
        clsD.delete();
        assertEquals(frameCount, getFrameCount());
    }

    public void testDirectDomainForSubslots() {
        Cls cls = createCls();
        Slot slot = createSlot();
        Slot subslot1 = createSubSlot(slot);
        cls.addDirectTemplateSlot(slot);
        cls.addDirectTemplateSlot(subslot1);
        Collection c = subslot1.getDirectDomain();
        assertEqualsSet(makeList(cls), c);
    }

    public void testRemoveTemplateSlotEvent() {
        final Boolean[] fired = new Boolean[] { Boolean.FALSE };
        final Cls cls = createCls();
        final Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        cls.addClsListener(new ClsAdapter() {
            public void templateSlotRemoved(ClsEvent event) {
                super.templateSlotRemoved(event);
                Collection clses = slot.getDirectDomain();
                assertFalse(clses.contains(cls));
                fired[0] = Boolean.TRUE;
            }
        });
        cls.removeDirectTemplateSlot(slot);
        assertTrue(fired[0].booleanValue());
    }

    public void testModifyReturnedCollection() {
        Cls cls = createCls();
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        Instance instance = createInstance(cls);
        checkUnmodifiable(instance, slot, 0);
        instance.setOwnSlotValue(slot, "foo");
        checkUnmodifiable(instance, slot, 1);
    }

    private static void checkUnmodifiable(Instance instance, Slot slot, int startSize) {
        Collection c = instance.getDirectOwnSlotValues(slot);
        checkUnmodifiable(c);
        c = instance.getDirectOwnSlotValues(slot);
        assertEquals(startSize, c.size());
        c = instance.getOwnSlotValues(slot);
        assertEquals(startSize, c.size());
        checkUnmodifiable(c);
        assertEquals(startSize, c.size());
    }

    private static void checkUnmodifiable(Collection c) {
        try {
            c.add(new Object());
            fail();
        } catch (Exception e) {
            // do nothing
        }
    }

    public void testFrameStoreInsertion() {
        final Boolean[] fired = new Boolean[1];
        fired[0] = Boolean.FALSE;
        FrameStore originalHead = ((DefaultKnowledgeBase) getDomainKB()).getHeadFrameStore();
        FrameStore testFs = new FrameStoreAdapter() {
            public Slot createSlot(FrameID id, Collection superslots, Collection types, boolean init) {
                fired[0] = Boolean.TRUE;
                return getDelegate().createSlot(id, superslots, types, init);
            }
        };
        getDomainKB().insertFrameStore(testFs);
        assertEquals(originalHead, testFs.getDelegate());
        getDomainKB().createSlot(null);
        assertTrue(fired[0].booleanValue());
    }

    public void testStrangeOverride() {
        Slot slot = createSlot();
        slot.setValueType(ValueType.STRING);
        Cls cls = createCls();
        cls.addDirectTemplateSlot(slot);
        cls.setTemplateSlotValueType(slot, ValueType.INSTANCE);
        Cls allowedCls = createCls();
        Collection allowedClses = CollectionUtilities.createCollection(allowedCls);
        cls.setTemplateSlotAllowedClses(slot, allowedClses);
        Collection returnedAllowedClses = cls.getTemplateSlotAllowedClses(slot);
        assertEqualsList(allowedClses, returnedAllowedClses);
    }

    public void testSetValueType() {
        Cls cls = createCls();
        Collection allowedClses = CollectionUtilities.createCollection(createCls());
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        cls.setTemplateSlotAllowedClses(slot, allowedClses);
        cls.setTemplateSlotValueType(slot, ValueType.INSTANCE);
        Collection returnedAllowedClses = cls.getTemplateSlotAllowedClses(slot);
        assertEqualsList(allowedClses, returnedAllowedClses);
    }

    public void testSaveAndLoadAllowedValues() {
        Cls cls = createCls();
        String clsName = cls.getName();
        Slot slot = createSlot();
        String slotName = slot.getName();
        cls.addDirectTemplateSlot(slot);
        Collection values = new ArrayList();
        values.add("a");
        values.add("b");
        cls.setTemplateSlotAllowedValues(slot, values);
        assertEqualsList(values, cls.getTemplateSlotAllowedValues(slot));

        saveAndReload();
        slot = getSlot(slotName);
        cls = getCls(clsName);
        assertEqualsList(values, cls.getTemplateSlotAllowedValues(slot));
    }

    public void testHasSuperclass() {
        Cls a = createCls();
        Cls a_b = createSubCls(a);
        Cls a_c = createSubCls(a);
        Cls bc_d = createSubCls(a_b);
        bc_d.addDirectSuperclass(a_c);
        Cls d_e = createSubCls(bc_d);
        Cls f = createCls();
        Cls ef_g = createSubCls(d_e);
        ef_g.addDirectSuperclass(f);
        Cls h = createCls();
        assertTrue(bc_d.hasSuperclass(a));
        assertTrue(d_e.hasSuperclass(a_b));
        assertTrue(d_e.hasSuperclass(a_c));
        assertTrue(d_e.hasSuperclass(bc_d));
        assertTrue(ef_g.hasSuperclass(a));
        assertTrue(ef_g.hasSuperclass(a_c));
        assertTrue(ef_g.hasSuperclass(f));
        assertFalse(ef_g.hasSuperclass(h));
    }

    public void testTopLevelDefaultValueClear() {
        Integer DEFAULT = new Integer(1);
        Collection defaults = makeList(DEFAULT);
        Slot slot = createSlot();
        slot.setValueType(ValueType.INTEGER);
        slot.setDefaultValues(defaults);
        assertEqualsList(defaults, slot.getDefaultValues());
        slot.setValueType(ValueType.FLOAT);
        assertEqualsList(makeList(), slot.getDefaultValues());
    }

    public void testTemplateSlotDefaultValueClear() {
        Integer DEFAULT = new Integer(1);
        Collection defaults = makeList(DEFAULT);
        Cls cls = createCls();
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        cls.setTemplateSlotValueType(slot, ValueType.INTEGER);
        cls.setTemplateSlotDefaultValues(slot, defaults);
        assertEqualsList(defaults, cls.getTemplateSlotDefaultValues(slot));
        cls.setTemplateSlotValueType(slot, ValueType.FLOAT);
        assertEqualsList(makeList(), cls.getTemplateSlotDefaultValues(slot));
    }

    public void testDefaultAndTemplateInstanceClear() {
        Cls cls = createCls();
        Instance instance = createInstance(cls);
        Cls clsB = createCls();
        Slot slot = createSlot();
        clsB.addDirectTemplateSlot(slot);
        clsB.setTemplateSlotAllowedClses(slot, makeList(cls));
        clsB.setTemplateSlotValues(slot, makeList(instance));
        clsB.setTemplateSlotDefaultValues(slot, makeList(instance));
        assertEqualsList(makeList(instance), clsB.getTemplateSlotValues(slot));
        assertEqualsList(makeList(instance), clsB.getTemplateSlotDefaultValues(slot));
        clsB.setTemplateSlotValueType(slot, ValueType.STRING);
        assertEqualsList(makeList(), clsB.getTemplateSlotValues(slot));
        assertEqualsList(makeList(), clsB.getTemplateSlotDefaultValues(slot));
    }

    public void testClearSlotValuesOnValueTypeChange() {
        Cls cls = createCls();
        Slot slot = createSlot();
        slot.setValueType(ValueType.STRING);
        cls.addDirectTemplateSlot(slot);
        Instance instance = createInstance(cls);
        String stringValue = "foo";
        instance.setOwnSlotValue(slot, stringValue);
        assertEquals(stringValue, instance.getOwnSlotValue(slot));
        slot.setValueType(ValueType.INTEGER);
        assertNull(instance.getOwnSlotValue(slot));
        Integer intValue = new Integer(1);
        instance.setOwnSlotValue(slot, intValue);
        assertEquals(intValue, instance.getOwnSlotValue(slot));
        cls.setTemplateSlotValueType(slot, ValueType.STRING);
        assertNull(instance.getOwnSlotValue(slot));
    }

    public void testFacetOverride() {
        Cls cls = createCls();
        Cls otherCls = createCls();
        Slot slot = createSlot();
        slot.setValueType(ValueType.INSTANCE);
        slot.setAllowedClses(Collections.singleton(cls));
        cls.setTemplateSlotAllowedClses(slot, Collections.singleton(otherCls));
        Facet valueTypeFacet = getProject().getKnowledgeBase().getFacet(Model.Facet.VALUE_TYPE);
        assertTrue(cls.hasDirectlyOverriddenTemplateFacet(slot, valueTypeFacet));
        cls.setTemplateSlotAllowedClses(slot, Collections.EMPTY_LIST);
        assertFalse(cls.hasDirectlyOverriddenTemplateFacet(slot, valueTypeFacet));
    }

    public void testSubSubslot() {
        Cls cls = createCls();
        Collection clses = makeList(cls);
        Slot slot = createSlot();
        slot.setAllowedClses(clses);
        Slot subslot = createSubSlot(slot);
        Slot subsubslot = createSubSlot(subslot);
        Collection subclses = subslot.getAllowedClses();
        assertEqualsList(clses, subclses);
        Collection subsubclses = subsubslot.getAllowedClses();
        assertEqualsList(clses, subsubclses);
    }

    public void testCreateEvent() {
        Cls cls = createCls();
        cls.addFrameListener(new FrameAdapter() {
            public void ownSlotValueChanged(FrameEvent event) {
                if (event.getSlot().equals(getSlot(Model.Slot.DIRECT_INSTANCES))) {
                    recordEventFired(event);
                }
            }
        });
        clearEvents();
        cls.createDirectInstance(null);
        assertEventFired(FrameEvent.OWN_SLOT_VALUE_CHANGED);
    }


    public void testSetAllowedClsesOverride() {
        Cls cls = createCls();
        Cls subclass = createSubCls(cls);
        Collection clses1 = makeList(cls);
        Collection clses2 = makeList(subclass);
        Slot slot = createSlotOnCls(cls);
        slot.setAllowedClses(clses1);
        assertEqualsSet(clses1, slot.getAllowedClses());
        cls.setTemplateSlotAllowedClses(slot, clses1);
        assertEqualsSet(clses1, cls.getTemplateSlotAllowedClses(slot));
        assertFalse(cls.hasOverriddenTemplateSlot(slot));
        cls.setTemplateSlotAllowedClses(slot, clses2);
        assertTrue(cls.hasOverriddenTemplateSlot(slot));
        cls.setTemplateSlotAllowedClses(slot, clses1);
        assertFalse(cls.hasOverriddenTemplateSlot(slot));
    }

    public void testAllowedClsesAsSet() {
        Cls clsA = createCls();
        Slot slot = createSlotOnCls(clsA);
        Collection clses = makeList(clsA, clsA);
        slot.setAllowedClses(clses);
        assertEquals(makeList(clsA), slot.getAllowedClses());
        assertEquals(makeList(clsA), clsA.getTemplateSlotAllowedClses(slot));
        clsA.setTemplateSlotAllowedClses(slot, clses);
        assertEquals(makeList(clsA), clsA.getTemplateSlotAllowedClses(slot));
    }

    static class TestFrameFactory implements FrameFactory {
        private KnowledgeBase kb;

        TestFrameFactory(KnowledgeBase kb) {
            this.kb = kb;
        }

        public void addJavaPackage(String packageName) {
            throw new UnsupportedOperationException();
        }

        public Cls createCls(FrameID id, Collection directTypes) {
            return new TestCls(kb, id);
        }

        public Facet createFacet(FrameID id, Collection directTypes) {
            return new TestFacet(kb, id);
        }

        public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes) {
            return new TestSimpleInstance(kb, id);
        }

        public Slot createSlot(FrameID id, Collection directTypes) {
            return new TestSlot(kb, id);
        }

        public boolean isCorrectJavaImplementationClass(FrameID id, Collection types, Class clas) {
            return clas.isAssignableFrom(TestInterface.class);
        }

        public void removeJavaPackage(String packageName) {
            throw new UnsupportedOperationException();
        }

        public int getJavaClassId(Frame frame) {
            throw new UnsupportedOperationException();
        }

        public Frame createFrameFromClassId(int javaClassId, FrameID id) {
            throw new UnsupportedOperationException();
        }

        public Collection getClsJavaClassIds() {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection getSlotJavaClassIds() {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection getFacetJavaClassIds() {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection getSimpleInstanceJavaClassIds() {
            // TODO Auto-generated method stub
            return null;
        }

        public Frame replaceFrameWithFrameNamed(Frame original, String name) {
          throw new UnsupportedOperationException();
        }

        public Frame rename(Frame original, String name) {
          throw new UnsupportedOperationException();
        }
    }

    static interface TestInterface {
    }

    public static class TestCls extends DefaultCls implements TestInterface {
        private static final long serialVersionUID = -1423984723632122772L;

        public TestCls(KnowledgeBase kb, FrameID id) {
            super(kb, id);
        }
    }

    public static class TestSlot extends DefaultSlot implements TestInterface {
        private static final long serialVersionUID = -1815103988108933212L;

        public TestSlot(KnowledgeBase kb, FrameID id) {
            super(kb, id);
        }
    }

    public static class TestFacet extends DefaultFacet implements TestInterface {
        private static final long serialVersionUID = -1492240588930520569L;

        public TestFacet(KnowledgeBase kb, FrameID id) {
            super(kb, id);
        }
    }

    public static class TestSimpleInstance extends DefaultSimpleInstance implements TestInterface {
        private static final long serialVersionUID = 1300758264519841291L;

        public TestSimpleInstance(KnowledgeBase kb, FrameID id) {
            super(kb, id);
        }
    }
}