package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ModificationRecordFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        FrameStore fs = new ModificationRecordFrameStore();
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }
}
