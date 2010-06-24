package edu.stanford.smi.protege.server;

import java.lang.reflect.Proxy;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.ControlFrameCalculatorCachingJob;
import edu.stanford.smi.protege.util.Log;

public class FrameCalculatorCacheControl_Test extends TestCase {
    private static transient Logger log = Log.getLogger(FrameCalculatorCacheControl_Test.class);

    private static final String SERVER_PROJECT = "examples/server/metaproject.pprj";
    
    private static final String HOST = "localhost";
    private static final String NAME = "//" + HOST + "/" + Server.getBoundName();
    private static final String USER = "Ray Fergerson";
    private static final String PASSWORD = "claudia";
    private static final String CLIENT_PROJECT = "Newspaper";
    
    private ServerActivityFrameStore activityDetector;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty(ServerProperties.SKIP_PRELOAD, "TRUE");
        try {
            Server_Test.setMetaProject(SERVER_PROJECT);
            Server_Test.startServer();
            Naming.lookup(NAME);
            KnowledgeBase serverKb = Server.getInstance().getProject(CLIENT_PROJECT).getKnowledgeBase();
            activityDetector = new ServerActivityFrameStore(serverKb);
            serverKb.getFrameStoreManager().insertFrameStore((FrameStore) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                                                                 new Class<?>[] { FrameStore.class }, 
                                                                                                 activityDetector),
                                                              1);
        } catch (NotBoundException e) {
            fail("Could not bind to server (is rmiregistry running?)");
        }
    }
    
    public void tearDown() {
        try {
            Server.getInstance().reinitialize();
        } catch (RemoteException e) {
            fail();
        }
    }
   

    public DefaultKnowledgeBase getKb() {
        RemoteProjectManager rpm = RemoteProjectManager.getInstance();
        Project p = rpm.getProject(HOST, USER, PASSWORD, CLIENT_PROJECT, true);
        return (DefaultKnowledgeBase) p.getKnowledgeBase();
    }
    
    public void waitForCache(KnowledgeBase kb) {
        try {
            Thread.sleep(10000);  // an interminably long time
        } catch (InterruptedException e) {
            fail();
        }
        kb.flushEvents();
    }
    
    public void flushCache(KnowledgeBase  kb) {
        RemoteClientFrameStore remoteClientFrameStore =  (RemoteClientFrameStore) ((DefaultKnowledgeBase) kb).getTerminalFrameStore();
        remoteClientFrameStore.flushCache();
    }

    public void testFrameCalculatorCacheControl() { 
        Log.setLoggingLevel(FrameCalculatorCacheControl_Test.class, Level.FINE);
        KnowledgeBase kb = getKb();
        
        checkDisabledCacheWorks(kb, true);
        checkEnabledCacheWorks(kb, false);

    }
    
    private void checkEnabledCacheWorks(KnowledgeBase kb, boolean expectedPreviousCachingStatus) {
        Slot documentation = kb.getSystemFrames().getDocumentationSlot();
        boolean previousCachingStatus = (Boolean) new ControlFrameCalculatorCachingJob(kb, true).execute();
        assertTrue(previousCachingStatus == expectedPreviousCachingStatus);
        waitForCache(kb);
        flushCache(kb);
        
        if (log.isLoggable(Level.FINE)) {
            Log.setLoggingLevel(ServerFrameStore.class, Level.FINE);
            Log.setLoggingLevel(ServerActivityFrameStore.class, Level.FINE);
        }
        
        activityDetector.clearServerActivity();
        Cls reporter = kb.getCls("Reporter");
        assertEquals(3, reporter.getInstances().size());
        assertTrue(activityDetector.serverInvoked());
        waitForCache(kb);
        activityDetector.clearServerActivity();
        assertTrue(((String) reporter.getDirectOwnSlotValue(documentation)).startsWith("A reporter is"));
        assertFalse(activityDetector.serverInvoked());
        
        if (log.isLoggable(Level.FINE)) {
            Log.setLoggingLevel(ServerFrameStore.class, Level.WARNING);
            Log.setLoggingLevel(ServerActivityFrameStore.class, Level.WARNING);
        }
    }
    
    private void checkDisabledCacheWorks(KnowledgeBase kb, boolean expectedPreviousCachingStatus) {
        Slot documentation = kb.getSystemFrames().getDocumentationSlot();
        boolean previousCachingStatus = (Boolean) new ControlFrameCalculatorCachingJob(kb, false).execute();
        assertTrue(expectedPreviousCachingStatus == previousCachingStatus);
        waitForCache(kb);
        flushCache(kb);

        if (log.isLoggable(Level.FINE)) {
            Log.setLoggingLevel(ServerFrameStore.class, Level.FINE);
            Log.setLoggingLevel(ServerActivityFrameStore.class, Level.FINE);
        }
        
        activityDetector.clearServerActivity();
        Cls editor = kb.getCls("Editor");
        assertEquals(4, editor.getInstances().size());
        assertTrue(activityDetector.serverInvoked());
        waitForCache(kb);
        activityDetector.clearServerActivity();
        assertTrue(((String) editor.getDirectOwnSlotValue(documentation)).startsWith("Editors are responsible"));
        assertTrue(activityDetector.serverInvoked());
        
        if (log.isLoggable(Level.FINE)) {
            Log.setLoggingLevel(ServerFrameStore.class, Level.WARNING);
            Log.setLoggingLevel(ServerActivityFrameStore.class, Level.WARNING);
        }
    }

}
