package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class InMemoryFrameStore extends SimpleFrameStore {
    public InMemoryFrameStore(KnowledgeBase kb) {
        super(kb, new ClosureCachingBasicFrameStore(new InMemoryFrameDb()));
        // kb.getSystemFrames().addSystemFrames(this);
        addSystemFrames();
    }

}
