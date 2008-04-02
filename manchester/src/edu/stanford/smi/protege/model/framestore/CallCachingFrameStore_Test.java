package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class CallCachingFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs = AbstractFrameStoreInvocationHandler.newInstance(CallCachingFrameStore.class);
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }
}
