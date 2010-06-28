package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.model.framestore.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface KnowledgeBaseFactory2 extends KnowledgeBaseFactory {

    void prepareToSaveInFormat(KnowledgeBase kb, KnowledgeBaseFactory factory, Collection errors);

    NarrowFrameStore createNarrowFrameStore(String name);
}