package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TransactionListener_Test extends APITestCase {
    private static final String TEST_STRING = "test transaction";

    public void testTransactionBegin() {
        KnowledgeBase kb = getDomainKB();
        kb.addTransactionListener(new TransactionAdapter() {
            public void transactionBegin(TransactionEvent event) {
                recordEventFired(event);
            }
        });
        kb.beginTransaction(TEST_STRING);
        assertEventFired(TransactionEvent.TRANSACTION_BEGIN);
    }
    
    @SuppressWarnings("deprecation")
    public void testTransactionEnd() {
        KnowledgeBase kb = getDomainKB();
        kb.addTransactionListener(new TransactionAdapter() {
            public void transactionEnded(TransactionEvent event) {
                recordEventFired(event);
            }
        });
        kb.beginTransaction(TEST_STRING);
        kb.endTransaction(true);
        assertEventFired(TransactionEvent.TRANSACTION_END);
    }
}
