package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

public class MergingNarrowFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        return new InMemoryFrameStore(kb);
    }

    private MergingNarrowFrameStore getMergingFrameStore() {
        InMemoryFrameStore fs = (InMemoryFrameStore) getModifiableFrameStore(null);
        return (MergingNarrowFrameStore) fs.getHelper().getDelegate();
    }

    public void testMergingGetOwnSlotValues() {
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls);
        Instance instance = createSimpleInstance(cls);
        Collection values = makeList("foo", "bar");
        instance.setOwnSlotValues(slot, values);
        assertEqualsList(values, instance.getDirectOwnSlotValues(slot));
        MergingNarrowFrameStore fs = getMergingFrameStore();
        fs.addActiveFrameStoreChild(new InMemoryFrameDb("child"));
        assertEqualsList(values, instance.getDirectOwnSlotValues(slot));
        Collection newValues = makeList("foo", "bar", "baz");
        instance.setOwnSlotValues(slot, newValues);
        assertEqualsSet(newValues, instance.getDirectOwnSlotValues(slot));
    }

    public void testGetCounts() {
        createCls();
        int count = getTestFrameStore().getFrameCount();
        MergingNarrowFrameStore fs = getMergingFrameStore();
        fs.addActiveFrameStoreChild(new InMemoryFrameDb("child"));
        assertEquals(count, getTestFrameStore().getFrameCount());
        NarrowFrameStore parent = fs.activateFrameStore("child");
        assertEquals(count - 1, getTestFrameStore().getFrameCount());
        Cls cls = createCls();
        createSlotOnCls(cls);
        assertEquals(count + 1, getTestFrameStore().getFrameCount());
        fs.activateFrameStore(parent);
        assertEquals(count + 2, getTestFrameStore().getFrameCount());
    }
}