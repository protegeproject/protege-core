package edu.stanford.smi.protege.util;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;

public interface DeletionHook {
    void delete(Frame frame);

}
