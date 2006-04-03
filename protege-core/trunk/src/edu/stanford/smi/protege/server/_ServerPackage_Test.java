package edu.stanford.smi.protege.server;

import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore_Test;
import junit.framework.*;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class _ServerPackage_Test {
    public static Test suite() {
        TestSuite suite = new TestSuite("server");
        suite.addTestSuite(Server_Test.class);
        suite.addTestSuite(RemoteClientFrameStore_Test.class);
        return suite;
    }

}
