package edu.stanford.smi.protege.storage.jdbc.framedb;

import junit.framework.*;

/**
 * The suite of unit tests for the classes in this package 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _FramedbPackage_Test {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("model.framedb");
        suite.addTestSuite(FrameDB_Test.class);
        suite.addTestSuite(FrameDBStorage_Test.class);
        return suite;
    }
}
