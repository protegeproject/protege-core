package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class FrameStoreAdapter_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs = new FrameStoreAdapter() {
        };
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }
}
