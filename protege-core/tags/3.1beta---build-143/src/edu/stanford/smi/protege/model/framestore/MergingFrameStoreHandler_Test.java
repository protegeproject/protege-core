package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;

import edu.stanford.smi.protege.model.*;

public class MergingFrameStoreHandler_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs1 = new InMemoryFrameStore(kb);
        FrameStore fs2 = new InMemoryFrameStore(kb);
        FrameStore merging = AbstractFrameStoreInvocationHandler.newInstance(MergingFrameStoreHandler.class);
        merging.setDelegate(fs1);
        MergingFrameStoreHandler handler = (MergingFrameStoreHandler) Proxy.getInvocationHandler(merging);
        handler.addSecondaryFrameStore(fs2);
        return merging;
    }

}
