package edu.stanford.smi.protege.ui;

import junit.framework.*;

/**
 * Suite of unit tests for all classes in this package.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _UiPackage_Test {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("ui");
        suite.addTestSuite(InstanceDisplay_Test.class);
        return suite;
    }
}
