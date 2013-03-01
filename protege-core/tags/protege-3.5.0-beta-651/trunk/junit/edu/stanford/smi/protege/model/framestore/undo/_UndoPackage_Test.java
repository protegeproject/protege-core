package edu.stanford.smi.protege.model.framestore.undo;

import junit.framework.*;
/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class _UndoPackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("model.framestore.undo");
        suite.addTestSuite(UndoFrameStore_Test.class);
        return suite;
    }
}
