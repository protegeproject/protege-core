package edu.stanford.smi.protege.event;

import junit.framework.*;

/**
 * Test Suite including all unit tests in this package
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _EventPackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("event");
        suite.addTestSuite(ProjectListener_Test.class);
        suite.addTestSuite(FrameListener_Test.class);
        suite.addTestSuite(TransactionListener_Test.class);
        return suite;
    }
}
