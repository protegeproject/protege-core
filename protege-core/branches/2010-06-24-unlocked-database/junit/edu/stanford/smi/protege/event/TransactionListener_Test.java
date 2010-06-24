package edu.stanford.smi.protege.event;

import java.util.Collections;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

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
    
    public void testNestedTransactionsHidden() {
        for (DBType dbt : DBType.values()) {
            setDBType(dbt);
            if (!dbConfigured()) {
                continue;
              }
              setDatabaseProject();
              KnowledgeBase kb = getDomainKB();
              Cls thing = kb.getSystemFrames().getRootCls();
              TransactionMonitor monitor = kb.getFrameStoreManager().getHeadFrameStore().getTransactionStatusMonitor();
              TransactionIsolationLevel level = monitor == null  ? TransactionIsolationLevel.NONE : monitor.getTransationIsolationLevel();
              if (level.compareTo(TransactionIsolationLevel.READ_COMMITTED) < 0) {
                  continue;
              }
              clearEvents();
              kb.addTransactionListener(new TransactionAdapter() {
                  @Override
                public void transactionBegin(TransactionEvent event) {
                      recordEventFired(event);
                  }
              });
              kb.addKnowledgeBaseListener(new KnowledgeBaseAdapter() {
                 @Override
                 public void clsCreated(KnowledgeBaseEvent event) {
                     recordEventFired(event);
                 }
                 
                 @Override
                public void slotCreated(KnowledgeBaseEvent event) {
                     recordEventFired(event);
                }
              });
              kb.beginTransaction("Starting outer transaction");
              kb.createCls("A", Collections.singleton(thing));
              assertEventFired(TransactionEvent.TRANSACTION_BEGIN);
              assert(getEventFired(KnowledgeBaseEvent.CLS_CREATED) == null);
              
              
              kb.beginTransaction("Starting inner transaction");
              kb.createSlot("f");
              assert(getEventFired(KnowledgeBaseEvent.SLOT_CREATED) == null);
              kb.commitTransaction();
              
              assert(getEventFired(KnowledgeBaseEvent.SLOT_CREATED) == null);
              assert(getEventFired(KnowledgeBaseEvent.CLS_CREATED) == null);
              
              kb.commitTransaction();
              assertEventFired(KnowledgeBaseEvent.CLS_CREATED);
              assertEventFired(KnowledgeBaseEvent.SLOT_CREATED);
        }
    }
}
