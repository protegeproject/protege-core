package edu.stanford.smi.protege.model.framestore;

import junit.framework.*;

public class _FrameStorePackage_Test {

    public static Test suite() {
        TestSuite suite = new TestSuite("model.framestore");
        suite.addTestSuite(InMemoryFrameStore_Test.class);
        // suite.addTestSuite(ClientFrameStore_Test.class);

        suite.addTestSuite(EventGeneratorFrameStore_Test.class);
        suite.addTestSuite(JournalingFrameStoreHandler_Test.class);
        suite.addTestSuite(ReadOnlyFrameStoreHandler_Test.class);
        suite.addTestSuite(ArgumentCheckingFrameStore_Test.class);
        suite.addTestSuite(CallCachingFrameStore_Test.class);
        suite.addTestSuite(FrameStoreAdapter_Test.class);
        suite.addTestSuite(EventDispatchFrameStore_Test.class);
        // suite.addTestSuite(FacetCheckingFrameStore_Test.class);
        suite.addTestSuite(ModificationRecordFrameStore_Test.class);
        
        suite.addTestSuite(MergingNarrowFrameStore_Test.class);
        return suite;
    }
}
