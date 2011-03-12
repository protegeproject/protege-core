package edu.stanford.smi.protege.plugin;

import junit.framework.*;

public class _PluginPackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("plugin");
        suite.addTestSuite(PluginUtilities_Test.class);
        return suite;
    }

}
