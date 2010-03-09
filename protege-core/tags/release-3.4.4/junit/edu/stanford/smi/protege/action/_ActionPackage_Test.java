package edu.stanford.smi.protege.action;

import junit.framework.*;

/**
 *  Test suite for all classes in this package
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _ActionPackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("action");
        // suite.addTestSuite(AutosynchronizeTrees_Test.class);
        suite.addTestSuite(ArchiveProject_Test.class);
        return suite;
    }
}
