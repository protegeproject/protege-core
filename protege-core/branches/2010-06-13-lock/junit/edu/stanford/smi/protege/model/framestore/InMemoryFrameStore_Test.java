package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class InMemoryFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        return new InMemoryFrameStore(kb);
    }
}
