package edu.stanford.smi.protege.model;

//ESCA*JAVA0037


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
public abstract class Transaction<X> {
    public static final String APPLY_TO_TRAILER_STRING = " -- Apply to: ";

    private final KnowledgeBase _knowledgeBase;
    
    private String applyTo;
    
    private String transactionName = "transaction";

    protected Transaction(KnowledgeBase kb) {
        _knowledgeBase = kb;
    }
    
    protected Transaction(KnowledgeBase kb, String transactionName) {
        _knowledgeBase = kb;
        this.transactionName = transactionName;
    }
    
    protected Transaction(KnowledgeBase kb, String transactionName, String applyTo) {
        _knowledgeBase = kb;
        this.transactionName = transactionName;
        this.applyTo = applyTo;
    }
    
    public KnowledgeBase getKnowledgeBase() {
        return _knowledgeBase;
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
                _knowledgeBase.beginTransaction(transactionName, applyTo);
                boolean doCommit = doOperations();
                if (doCommit) {
                    commited = _knowledgeBase.commitTransaction();
                } else {
                    /* how to handle an error here? */
                    _knowledgeBase.rollbackTransaction();
                    commited = false;
                }
                transactionComplete = true;
            } finally {
                if (!transactionComplete) {
                    _knowledgeBase.rollbackTransaction();
                }
            }
        }
        return commited;
    }
    
    public X getResult() {
        return null;
    }
    
    public static String getApplyTo(String beginString) {
        int index = beginString.indexOf(Transaction.APPLY_TO_TRAILER_STRING);
        if (index < 0) return null;
        index += Transaction.APPLY_TO_TRAILER_STRING.length();
        return beginString.substring(index);
    }

    public static String removeApplyTo(String beginString) {
        if (beginString == null){
            return null;
        }
        int index = beginString.indexOf(Transaction.APPLY_TO_TRAILER_STRING);
        if (index < 0) return beginString;
        return beginString.substring(0, index);
    }
}
