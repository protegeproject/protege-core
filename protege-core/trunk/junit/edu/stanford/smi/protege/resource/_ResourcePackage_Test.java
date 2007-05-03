package edu.stanford.smi.protege.resource;

import junit.framework.*;

/**
 * Unit tests for the classes in this packages.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _ResourcePackage_Test {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("resource");
        suite.addTestSuite(LocalizedText_Test.class);
        suite.addTestSuite(ResourceKey_Test.class);
        return suite;
    }
}
