package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class ArgumentCheckingFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs = new ArgumentCheckingFrameStore();
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }
}
