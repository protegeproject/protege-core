package edu.stanford.smi.protege.model;

import edu.stanford.smi.protege.util.*;

/**
 * Encapsulation of method for executing a tranaction.
 * <br> <br>
 *
 * Example:
 * <pre> <code>
 * void foo(KnowledgeBase kb) {
 * 		Transaction t = new Transaction(kb) {
 *			public boolean doOperations() {
 *				// kb calls go here
 *				// return true for commit, false for rollback
 *			}
 * 		};
 *		boolean committed = t.execute();
 *		if (committed) {
 *			System.out("transaction committed");
 *		} else {
 * 			System.out("transaction rolled back");
 *		}
 * }
 * </code> </pre>
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class Transaction {
    private KnowledgeBase _knowledgeBase;

    public Transaction(KnowledgeBase kb) {
        _knowledgeBase = kb;
    }

    /** returns true if the the results of this method should be committed */
    public abstract boolean doOperations();

    /** returns true if the transaction was committed
     *
     * This method handles the logic of executing a transaction, including the
     * necessary synchronization.  In addition, it handles correctly both
     * runtime exceptions and errors.
     */
    public boolean execute() {
        boolean commited = false;
        synchronized (_knowledgeBase) {
            boolean transactionComplete = false;
            try {
                boolean inTransaction = _knowledgeBase.beginTransaction("transaction");
                boolean doCommit = doOperations();
                if (inTransaction) {
                    commited = _knowledgeBase.endTransaction(doCommit);
                } else if (!doCommit) {
                    Log.warning("Unable to rollback, transaction committed", this, "execute");
                    commited = true;
                }
                transactionComplete = true;
            } finally {
                if (!transactionComplete) {
                    _knowledgeBase.endTransaction(false);
                }
            }
        }
        return commited;
    }

    public KnowledgeBase getKnowledgeBase() {
        return _knowledgeBase;
    }
}
