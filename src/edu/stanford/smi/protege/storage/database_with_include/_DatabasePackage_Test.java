package edu.stanford.smi.protege.storage.database_with_include;

import junit.framework.*;

/**
 * Test suite for unit tests of classes in this package.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _DatabasePackage_Test {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("storage.database");
        suite.addTestSuite(DatabaseFrameStore_Test.class);
        return suite;
    }
}
