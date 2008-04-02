package edu.stanford.smi.protege;

import junit.framework.*;

/**
 * Unit-test suite for all of Protege
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AllUnitTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("All Unit Tests");
        suite.addTest(edu.stanford.smi.protege._ProtegePackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.server._ServerPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.action._ActionPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.event._EventPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.model._ModelPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.model.framestore._FrameStorePackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.model.framestore.undo._UndoPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.model.framestore.cleandispatch._CleanDispatchPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.plugin._PluginPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.resource._ResourcePackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.storage.clips.AllTests.suite());
        suite.addTest(edu.stanford.smi.protege.ui._UiPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.util._UtilPackage_Test.suite());
        suite.addTest(edu.stanford.smi.protege.widget._WidgetPackage_Test.suite());
        return suite;
    }
}