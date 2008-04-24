package edu.stanford.smi.protege.model;

import junit.framework.*;

/**
 * Suite of unit tests for this package
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _ModelPackage_Test {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("model");
        suite.addTestSuite(Project_Test.class);
        suite.addTestSuite(PropertyMapUtil_Test.class);
        suite.addTestSuite(DefaultKnowledgeBase_Test.class);
        suite.addTestSuite(DefaultKnowledgeBase_SimpleTest.class);
        suite.addTestSuite(Transaction_Test.class);
        suite.addTestSuite(DefaultCls_Test.class);
        suite.addTestSuite(DefaultFrame_Test.class);
        suite.addTestSuite(BrowserSlotPattern_Test.class);
        suite.addTestSuite(ModelUtilities_Test.class);
        return suite;
    }
}
