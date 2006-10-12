package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

public class EventDispatchFrameStore_Test extends FrameStore_Test {

    protected FrameStore     createFrameStore(DefaultKnowledgeBase kb){
        FrameStore fs = new EventDispatchFrameStore(kb);
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }
}
