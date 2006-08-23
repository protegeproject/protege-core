package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

public class UndoFrameStore_Test extends FrameStore_Test {
    private UndoFrameStore _frameStore;

    public FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        // _frameStore = new UndoFrameStore(true);
        _frameStore = new UndoFrameStore();
        _frameStore.setDelegate(new InMemoryFrameStore(kb));
        return _frameStore;
    }

    public void tearDown() throws Exception {
        super.tearDown();
        _frameStore = null;
    }

    public void testUndoCreateCls() {
        Cls cls = createCls();
        String name = cls.getName();
        assertNotNull("name", name);
        Frame frame = getFrame(name);
        assertEquals("created", cls, frame);
        assertTrue("can undo", _frameStore.canUndo());
        _frameStore.undo();
        frame = getFrame(name);
        assertNull("undone", frame);
        _frameStore.redo();
        frame = getFrame(name);
        assertEquals("recreated", frame.getName(), name);
    }

    public void testUndoCreateSimpleInstance() {
        Cls cls = createCls();
        Instance instance = createSimpleInstance(cls);
        String name = instance.getName();
        assertNotNull("name", name);
        Frame frame = getFrame(name);
        assertEquals("created", instance, frame);
        assertTrue("can undo", _frameStore.canUndo());
        _frameStore.undo();
        frame = getFrame(name);
        assertNull("undone", frame);
        _frameStore.redo();
        instance = (Instance) getFrame(name);
        assertEquals("recreated", instance.getName(), name);
    }

    public void testUndoCreateSlot() {
        Slot slot = createSlot();
        String name = slot.getName();
        assertNotNull("name", name);
        Frame frame = getFrame(name);
        assertEquals("created", slot, frame);
        assertTrue("can undo", _frameStore.canUndo());
        _frameStore.undo();
        frame = getFrame(name);
        assertNull("undone", frame);
        _frameStore.redo();
        frame = getFrame(name);
        assertEquals("recreated", frame.getName(), name);
    }

    public void testUndoCreateFacet() {
    }

    public void testUndoDeleteCls() {
        Cls clsA = createCls();
        Cls clsB = createCls();
        Cls clsA1 = createCls(clsA);
        Cls clsB1 = createCls(clsB);
        Cls clsAB2 = createCls(clsA1);
        _frameStore.addDirectSuperclass(clsAB2, clsB1);
        Cls clsAB3 = createCls(clsAB2);
        int count = _frameStore.getFrames().size();
        _frameStore.deleteCls(clsAB3);
        int newCount = _frameStore.getFrames().size();
        assertEquals("first count", count - 1, newCount);
        _frameStore.undo();
        newCount = _frameStore.getFrames().size();
        assertEquals("second count", count, newCount);
        _frameStore.redo();
        newCount = _frameStore.getFrames().size();
        assertEquals("third count", count - 1, newCount);
        _frameStore.undo();
        newCount = _frameStore.getFrames().size();
        assertEquals("forth count", count, newCount);

        String clsAName = _frameStore.getFrameName(clsA);
        _frameStore.deleteCls(clsA1);
        _frameStore.deleteCls(clsA);
        newCount = _frameStore.getFrames().size();
        assertEquals("fifth count", count - 2, newCount);
        _frameStore.undo();
        _frameStore.undo();
        newCount = _frameStore.getFrames().size();
        assertEquals("sixth count", count, newCount);
        Frame frame = _frameStore.getFrame(clsAName);
        assertNotNull("clsA", frame);

    }

    public void testUndoSimpleTransaction() {
        Cls cls = createCls();
        Slot slot1 = createSlot();
        Slot slot2 = createSlot();
        cls.addDirectTemplateSlot(slot1);
        cls.addDirectTemplateSlot(slot2);
        Instance instance = cls.createDirectInstance(null);
        _frameStore.beginTransaction("simple nonsense");
        _frameStore.setDirectOwnSlotValues(instance, slot1, makeList("foo1"));
        _frameStore.setDirectOwnSlotValues(instance, slot2, makeList("foo2"));
        _frameStore.commitTransaction();
        assertEqualsList(makeList("foo1"), _frameStore.getDirectOwnSlotValues(instance, slot1));
        assertEqualsList(makeList("foo2"), _frameStore.getDirectOwnSlotValues(instance, slot2));
        _frameStore.undo();
        assertEqualsList(makeList(), _frameStore.getDirectOwnSlotValues(instance, slot1));
        assertEqualsList(makeList(), _frameStore.getDirectOwnSlotValues(instance, slot2));
        _frameStore.redo();
        assertEqualsList(makeList("foo1"), _frameStore.getDirectOwnSlotValues(instance, slot1));
        assertEqualsList(makeList("foo2"), _frameStore.getDirectOwnSlotValues(instance, slot2));
    }

    public void testUndoDeleteSlot() {
    }

    public void testUndoDeleteFacet() {
    }

    public void testUndoDeleteSimpleInstance() {
    }

    public void testUndoSetDirectOwnSlotValues() {
    }

    public void testUndoSetFrameName() {
    }

    public void testUndoAddDirectTemplateSlot() {
    }

    public void testUndoRemoveDirectTemplateSlot() {
    }

    public void testUndoMoveDirectTemplateSlot() {
    }

    public void testUndoSetDirectTemplateSlotValues() {
    }

    public void testUndoRemoveDirectTemplateFacetOverrides() {
    }

    public void testUndoSetDirectTemplateFacetValues() {
    }

    public void testUndoAddDirectSuperclass() {
    }

    public void testUndoRemoveDirectSuperclass() {
    }

    public void testUndoMoveDirectSubclass() {
    }

    public void testUndoAddDirectSuperslot() {
    }

    public void testUndoRemoveDirectSuperslot() {
    }

    public void testUndoAddDirectType() {
    }

    public void testUndoMoveDirectType() {
    }

    public void testUndoCommitTransaction() {
    }

    public void testUndoRollbackTransaction() {
    }
}
