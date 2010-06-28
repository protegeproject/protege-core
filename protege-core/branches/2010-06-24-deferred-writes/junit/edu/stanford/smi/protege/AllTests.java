package edu.stanford.smi.protege;

import junit.extensions.*;
import junit.framework.*;

import edu.stanford.smi.protege.test.*;

/**
 * Test Suite for all of Protege
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("All Tests");
        suite.addTest(AllUnitTests.suite());
        suite.addTestSuite(AllFunctionalTests.class);

        TestSetup fileTests = new FactoryTestSetup(suite, new ClipsProjectFactory());
        // TestSetup mySQLTests = new FactoryTestSetup(suite, new MySQLProjectFactory());

        TestSuite backendSuite = new TestSuite("All Backend Tests");
        backendSuite.addTest(fileTests);
        // backendSuite.addTest(mySQLTests);
        return backendSuite;
    }
}
