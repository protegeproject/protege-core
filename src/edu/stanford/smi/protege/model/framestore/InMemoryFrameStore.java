package edu.stanford.smi.protege.model.framestore;

import java.net.*;

import edu.stanford.smi.protege.model.*;

public class InMemoryFrameStore extends SimpleFrameStore {
    public InMemoryFrameStore(KnowledgeBase kb) {
        // super(kb, new ClosureCachingBasicFrameStore(new InMemoryFrameDb()));
        super(kb, new ClosureCachingBasicFrameStore(new MergingNarrowFrameStore(
                new InMemoryFrameDb(getName(kb)))));
        MergingNarrowFrameStore mfs = (MergingNarrowFrameStore) getHelper().getDelegate();
        NarrowFrameStore parent = mfs.activateFrameStore(MergingNarrowFrameStore.SYSTEM_NAME);
        addSystemFrames();
        mfs.activateFrameStore(parent);
    }

    private static String getName(KnowledgeBase kb) {
        String name = null;
        Project p = kb.getProject();
        if (p != null) {
            URI uri = p.getProjectURI();
            if (uri != null) {
                name = uri.toString();
            }
        }
        if (name == null) {
            name = kb.getName();
        }
        return name;
    }

}