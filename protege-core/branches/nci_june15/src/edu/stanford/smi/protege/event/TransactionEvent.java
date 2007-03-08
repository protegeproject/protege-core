package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

public class TransactionEvent extends AbstractEvent {
    private static final int BASE = 700;
    public static final int TRANSACTION_BEGIN = BASE + 1;
    public static final int TRANSACTION_END = BASE + 2;
    
    public TransactionEvent(KnowledgeBase kb, int type, String name, Frame applyTo) {
        super(kb, type, name, applyTo);
    }
    
    public KnowledgeBase getKnowledgeBase() {
        return (KnowledgeBase) getSource();
    }
    
    public String getBeginString() {
        return (String) getArgument();
    }
    
    public Frame getApplyTo() {
        return (Frame) getArgument2();
    }
}
