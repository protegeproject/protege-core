package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.AbstractEvent;

public class TransactionEvent extends AbstractEvent {
    private static final int BASE = 700;
    public static final int TRANSACTION_BEGIN = BASE + 1;
    public static final int TRANSACTION_END = BASE + 2;
    
    public TransactionEvent(KnowledgeBase kb, int type, String name) {
        super(kb, type, name);
    }
    
    public KnowledgeBase getKnowledgeBase() {
        return (KnowledgeBase) getSource();
    }
    
    public String getBeginString() {
        return (String) getArgument();
    }

}
