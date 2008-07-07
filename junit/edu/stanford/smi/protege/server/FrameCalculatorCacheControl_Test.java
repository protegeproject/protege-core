package edu.stanford.smi.protege.server;

import java.rmi.Naming;
import java.rmi.NotBoundException;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.ControlFrameCalculatorCachingJob;

public class FrameCalculatorCacheControl_Test extends APITestCase {

    private static final String SERVER_PROJECT = "examples/server/metaproject.pprj";
    
    private static final String HOST = "localhost";
    private static final String NAME = "//" + HOST + "/" + Server.getBoundName();
    private static final String USER = "Ray Fergerson";
    private static final String PASSWORD = "claudia";
    private static final String CLIENT_PROJECT = "Newspaper";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty(ServerProperties.SKIP_PRELOAD, "TRUE");
        try {
            Server_Test.setMetaProject(SERVER_PROJECT);
            Server_Test.startServer();
            Naming.lookup(NAME);
        } catch (NotBoundException e) {
            fail("Could not bind to server (is rmiregistry running?)");
        }
    }

    public DefaultKnowledgeBase getKb() {
        RemoteProjectManager rpm = RemoteProjectManager.getInstance();
        Project p = rpm.getProject(HOST, USER, PASSWORD, CLIENT_PROJECT, true);
        return (DefaultKnowledgeBase) p.getKnowledgeBase();
    }
    
    public void flushCache(KnowledgeBase  kb) {
        RemoteClientFrameStore remoteClientFrameStore =  (RemoteClientFrameStore) ((DefaultKnowledgeBase) kb).getTerminalFrameStore();
        remoteClientFrameStore.flushCache();
    }

    public void testFrameCalculatorCacheControl() {
        KnowledgeBase kb = getKb();
        assertTrue((Boolean) new ControlFrameCalculatorCachingJob(kb, false).execute());
        flushCache(kb);
        Cls editor = kb.getCls("Editor");
        assertEquals(4, editor.getInstances().size());
        assertFalse((Boolean) new ControlFrameCalculatorCachingJob(kb, true).execute());
        flushCache(kb);
        Cls reporter = kb.getCls("Reporter");
        assertEquals(3, reporter.getInstances().size());
    }

}
