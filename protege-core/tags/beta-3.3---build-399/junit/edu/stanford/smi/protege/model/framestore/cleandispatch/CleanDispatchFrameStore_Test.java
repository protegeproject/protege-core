package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

public class CleanDispatchFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs = new CleanDispatchFrameStore();
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }

    public void testCleanAddSuperclassDispatch() {
        Cls cls = createCls();
        Cls otherclass = createCls();
        Collection superclasses = new ArrayList(cls.getDirectSuperclasses());
        superclasses.add(otherclass);
        Slot slot = (Slot) getFrame(Model.Slot.DIRECT_SUPERCLASSES);
        getTestFrameStore().setDirectOwnSlotValues(cls, slot, superclasses);
        Collection currentSuperclasses = getTestFrameStore().getDirectSuperclasses(cls);
        assertEqualsSet(superclasses, currentSuperclasses);
    }
}
