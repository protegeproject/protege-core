package edu.stanford.smi.protege.widget;

import junit.framework.*;

/**
 * Unit test suite for all of the classes in this package. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _WidgetPackage_Test {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("widget");
        suite.addTestSuite(SlotsTab_Test.class);
        return suite;
    }
}
