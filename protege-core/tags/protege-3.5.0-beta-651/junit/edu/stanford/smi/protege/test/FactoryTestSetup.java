package edu.stanford.smi.protege.test;

import junit.extensions.*;
import junit.framework.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FactoryTestSetup extends TestSetup {
    private ProjectFactory factory;

    public FactoryTestSetup(TestSuite test, ProjectFactory factory) {
        super(getTest(test, factory));
        this.factory = factory;
    }

    private static Test getTest(TestSuite suite, ProjectFactory factory) {
        TestSuite test = new TestSuite(StringUtilities.getClassName(factory.getClass()) + " Tests");
        test.addTest(suite);
        return test;
    }

    public void setUp() {
        APITestCase.setProjectFactory(factory);
    }

    public void tearDown() {
        APITestCase.setProjectFactory(null);
    }
}
