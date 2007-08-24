package edu.stanford.smi.protege.util;

import junit.framework.*;

/**
 * Unit test suite for all classes in this package.
 */

public class _UtilPackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("util");
        suite.addTestSuite(SystemUtilities_Test.class);
        suite.addTestSuite(Log_Test.class);
        suite.addTestSuite(FileUtilities_Test.class);
        suite.addTestSuite(Tree_Test.class);
        return suite;
    }
}
