package edu.stanford.smi.protege.model;

import edu.stanford.smi.protege.test.*;

/**
 * Unit tests for Transaction class
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Transaction_Test extends APITestCase {

    public void setUp() throws Exception {
        super.setUp();
        chooseDBType();
        if (getDBType() != null) {
          setDatabaseProject();
        }
    }

    public void testCommit() {
        if (getDBType() == null) {
          System.out.println("Transaction Test not configuured");
          return;
        }

        int initialFrameCount = getFrameCount();
        Transaction t = new Transaction(getDomainKB()) {
            public boolean doOperations() {
                KnowledgeBase kb = getKnowledgeBase();
                Cls cls = kb.createCls(null, kb.getRootClses());
                kb.createInstance(null, cls);
                // commit the transaction
                return true;
            }
        };

        boolean succeeded = t.execute();
        assertTrue("transaction succeeded", succeeded);
        assertEquals("frame count in cache", initialFrameCount + 2, getFrameCount());
        saveAndReload();
        assertEquals("frame count in database", initialFrameCount + 2, getFrameCount());
    }

    public void testRollback() {
        if (getDBType() == null) {
          System.out.println("Transaction Test not configured");
          return;
        }
        String clsName = createCls().getName();

        int initialFrameCount = getFrameCount();

        Transaction t = new Transaction(getDomainKB()) {
            public boolean doOperations() {
                KnowledgeBase kb = getKnowledgeBase();
                Cls cls = kb.createCls(null, kb.getRootClses());
                kb.createInstance(null, cls);
                // rollback this transaction
                return false;
            }
        };

        boolean committed = t.execute();
        assertTrue("transaction rolled back", !committed);
        assertEquals("frame count in cache", initialFrameCount, getFrameCount());
        saveAndReload();
        assertEquals("frame count in database", initialFrameCount, getFrameCount());
        Cls cls = getCls(clsName);
        assertNotNull("class", cls);
        createInstance(cls);
    }
}
