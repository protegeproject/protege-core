package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;

public class MergingNarrowFrameStore_Test extends FrameStore_Test {

    @Override
    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        return new InMemoryFrameStore(kb);
    }

    private MergingNarrowFrameStore getMergingFrameStore() {
        InMemoryFrameStore fs = (InMemoryFrameStore) getModifiableFrameStore(null);
        NarrowFrameStore nfs = fs.getHelper().getDelegate();
        while (!(nfs instanceof MergingNarrowFrameStore)) {
            nfs = nfs.getDelegate();
        }
        return (MergingNarrowFrameStore) nfs;
    }
    

    public void testMergingGetOwnSlotValues() {
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls);
        Instance instance = createSimpleInstance(cls);
        Collection values = makeList("foo", "bar");
        instance.setOwnSlotValues(slot, values);
        assertEqualsList(values, instance.getDirectOwnSlotValues(slot));
        MergingNarrowFrameStore fs = getMergingFrameStore();

        String childName = fs.getActiveFrameStore().getName();
        fs.addActiveFrameStore(new InMemoryFrameDb("parent"));
        fs.addRelation("parent", childName);
        assertEqualsList(values, instance.getDirectOwnSlotValues(slot));
        Collection newValues = makeList("foo", "bar", "baz");
        instance.setOwnSlotValues(slot, newValues);
        assertEqualsSet(newValues, instance.getDirectOwnSlotValues(slot));
    }

    public void testGetCounts() {
        int count = getFrameCount();
        createCls();
        assertEquals(count + 1, getFrameCount());

        MergingNarrowFrameStore fs = getMergingFrameStore();
        String parentName = fs.getActiveFrameStore().getName();
        fs.addActiveFrameStore(new InMemoryFrameDb("child"));
        fs.addRelation(parentName, "child");
        createCls();
        fs.setActiveFrameStore(parentName);

        assertEquals(count + 2, getFrameCount());
        createCls();
        assertEquals(count + 3, getFrameCount());
    }

    public void testInterleavedValues() {
        Cls cls = createCls();
        Slot slot = createSlotOnCls(cls);
        SimpleInstance instance = createSimpleInstance(cls);
        List values = makeList("a", "b", "c");
        instance.setOwnSlotValues(slot, values);
        MergingNarrowFrameStore mnfs = getMergingFrameStore();
        NarrowFrameStore child = mnfs.getActiveFrameStore();
        mnfs.addActiveFrameStore(new InMemoryFrameDb("parent"));
        mnfs.addRelation("parent", child.getName());
        assertEqualsList(values, instance.getOwnSlotValues(slot));
        instance.setOwnSlotValues(slot, makeList("c", "d", "e", "f"));
        List allValues = makeList("a", "b", "c", "d", "e", "f");
        assertEqualsList(allValues, instance.getOwnSlotValues(slot));

        instance.moveDirectOwnSlotValue(slot, 1, 3); // should do nothing
        assertEqualsList(allValues, instance.getOwnSlotValues(slot));

        instance.moveDirectOwnSlotValue(slot, 4, 1); // should do nothing
        assertEqualsList(allValues, instance.getOwnSlotValues(slot));

        instance.moveDirectOwnSlotValue(slot, 3, 4);
        assertEqualsList(makeList("a", "b", "c", "e", "d", "f"), instance.getOwnSlotValues(slot));
    }
}