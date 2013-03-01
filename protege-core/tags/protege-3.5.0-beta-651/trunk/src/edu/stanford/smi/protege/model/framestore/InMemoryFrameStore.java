package edu.stanford.smi.protege.model.framestore;

import java.net.URI;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;

public class InMemoryFrameStore extends SimpleFrameStore {
    public InMemoryFrameStore(KnowledgeBase kb) {
        super(kb, buildNarrowFrameStore(kb));
        addSystemFrames();
        NarrowFrameStore mfs;
        for (mfs = getHelper(); !(mfs instanceof MergingNarrowFrameStore); mfs = mfs.getDelegate()) {
        	;
        }
        ((MergingNarrowFrameStore) mfs).addActiveFrameStore(new InMemoryFrameDb(getName(kb)));
    }
    
    private static NarrowFrameStore buildNarrowFrameStore(KnowledgeBase kb) {
    	MergingNarrowFrameStore merging = new MergingNarrowFrameStore(kb);
    	ClosureCachingBasicFrameStore closureCaching = new ClosureCachingBasicFrameStore(merging);
    	ImmutableNamesNarrowFrameStore immutableNames = new ImmutableNamesNarrowFrameStore(kb, closureCaching);
    	return immutableNames;
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