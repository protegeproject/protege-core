package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class JournalingFrameStoreHandler_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs = AbstractFrameStoreInvocationHandler.newInstance(JournalingFrameStoreHandler.class);
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }
}
