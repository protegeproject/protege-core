package edu.stanford.smi.protege;

import junit.framework.*;

/**
 * Test suite for all classes in this package
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _ProtegePackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("app"); //$NON-NLS-1$
        suite.addTestSuite(Application_Test.class);
        return suite;
    }
}
