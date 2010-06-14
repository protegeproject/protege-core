package edu.stanford.smi.protege.server;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.event.ServerProjectListener;
import edu.stanford.smi.protege.event.ServerProjectNotificationEvent;
import edu.stanford.smi.protege.event.ServerProjectSessionClosedEvent;
import edu.stanford.smi.protege.event.ServerProjectStatusChangeEvent;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.LockStepper;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class ServerAdmin_Test extends APITestCase {
    private static Logger log = Log.getLogger(ServerAdmin_Test.class);
    
    private RemoteServer server;
    
    public  static final String HOST = "localhost";
    
    public static final String NAME = "//" + HOST + "/" + Server.getBoundName();
    
    public static final String PROJECT_NAME = "Newspaper";
    
    public static final String USER1 = "Ray Fergerson";
    public static final String PASSWORD1 = "claudia";

    public static final String USER2 = "Jennifer Vendetti";
    public static final String PASSWORD2 = "jenny";
    
    public static final String USER3 = "Admin";
    public static final String PASSWORD3 = "admin";

    @Override
	public void setUp() throws Exception {
        super.setUp();
        try {
            if (!Server_Test.startServer()) {
                return;
            }
            server = (RemoteServer) Naming.lookup(NAME);
            server.reinitialize();
        } catch (NotBoundException e) {
            fail("Could not bind to server (is rmiregistry running?)");
        }
    }
    
    public DefaultKnowledgeBase getKb() {
        RemoteProjectManager rpm = RemoteProjectManager.getInstance();
        Project p = rpm.getProject(HOST, USER1, PASSWORD1, PROJECT_NAME, true);
        return (DefaultKnowledgeBase) p.getKnowledgeBase();
    }
    
    public RemoteSession openSession() throws RemoteException {
        return server.openSession(USER2, SystemUtilities.getMachineIpAddress(), PASSWORD2);
    }
    
    public RemoteSession openAdminSession() throws RemoteException {
        return server.openSession(USER3, SystemUtilities.getMachineIpAddress(), PASSWORD3);
    }
    
    public enum TestSetProjectStatusStages {
        STARTED, LISTENING, STATUS_CHANGED, DONE;
    }
    public void testSetProjectStatus() throws RemoteException {
        final LockStepper<TestSetProjectStatusStages> ls = new LockStepper<TestSetProjectStatusStages>(TestSetProjectStatusStages.STARTED);
        new Thread("Listening for project status changes thread") {
            @Override
            public void run() {
                try {
                    KnowledgeBase kb = getKb();
                    TestSetProjectStatusListener listener = new TestSetProjectStatusListener();
                    kb.addServerProjectListener(listener);
                    kb.flushEvents();
                    assertFalse(listener.isStatusChanged());
                    ls.stageAchieved(TestSetProjectStatusStages.LISTENING, null);
                    ls.waitForStage(TestSetProjectStatusStages.STATUS_CHANGED);
                    kb.flushEvents();
                    assertTrue(listener.isStatusChanged());
                    assertTrue(listener.getNewStatus() == ProjectStatus.SHUTTING_DOWN);
                    kb.dispose();
                    ls.stageAchieved(TestSetProjectStatusStages.DONE, null);
                }
                catch (Throwable t) {
                    ls.exceptionOffMainThread(TestSetProjectStatusStages.DONE, t);
                }
            }
        }.start();
        ls.waitForStage(TestSetProjectStatusStages.LISTENING);
        RemoteSession session = openSession();
        server.setProjectStatus(PROJECT_NAME, ProjectStatus.SHUTTING_DOWN, session);
        ls.stageAchieved(TestSetProjectStatusStages.STATUS_CHANGED, null);
        ls.waitForStage(TestSetProjectStatusStages.DONE);
    }
    
    private class TestSetProjectStatusListener implements ServerProjectListener {
        private boolean statusChanged = false;
        private ProjectStatus oldStatus;
        private ProjectStatus newStatus;

        public void projectNotificationReceived(ServerProjectNotificationEvent event) {
            ;
        }

        public void projectStatusChanged(ServerProjectStatusChangeEvent event) {
            statusChanged = true;
            oldStatus = event.getOldStatus();
            newStatus = event.getNewStatus();
        }
        
        public void beforeProjectSessionClosed(ServerProjectSessionClosedEvent event) {
                	
        }

        public boolean isStatusChanged() {
            return statusChanged;
        }

        public ProjectStatus getOldStatus() {
            return oldStatus;
        }

        public ProjectStatus getNewStatus() {
            return newStatus;
        }
        
    }
    
    public enum TestNotificationStages {
        STARTED, LISTENING, NOTIFIED, DONE;
    }
    public void testNotification() throws RemoteException {
        final LockStepper<TestNotificationStages> ls = new LockStepper<TestNotificationStages>(TestNotificationStages.STARTED);
        final String message = "hello world";
        new Thread("Listening for project notification thread") {
            @Override
            public void run() {
                try {
                    KnowledgeBase kb = getKb();
                    TestNotificationListener listener = new TestNotificationListener();
                    kb.addServerProjectListener(listener);
                    assertFalse(listener.isNotified());
                    ls.stageAchieved(TestNotificationStages.LISTENING, null);
                    ls.waitForStage(TestNotificationStages.NOTIFIED);
                    kb.flushEvents();
                    assertTrue(listener.isNotified());
                    assertTrue(listener.getMessage().equals(message));
                    kb.dispose();
                    ls.stageAchieved(TestNotificationStages.DONE, null);
                }
                catch (Throwable t) {
                    ls.exceptionOffMainThread(TestNotificationStages.DONE, t);
                }
            }
        }.start();
        ls.waitForStage(TestNotificationStages.LISTENING);
        RemoteSession session = openSession();
        server.notifyProject(PROJECT_NAME, message, session);
        ls.stageAchieved(TestNotificationStages.NOTIFIED, null);
        ls.waitForStage(TestNotificationStages.DONE);
    }
    
    private class TestNotificationListener implements ServerProjectListener {
        private boolean notified = false;
        private String message;

        public void projectNotificationReceived(ServerProjectNotificationEvent event) {
            notified = true;
            message = event.getMessage();
        }

        public void projectStatusChanged(ServerProjectStatusChangeEvent event) {
            ;
        }
        
        public void beforeProjectSessionClosed(ServerProjectSessionClosedEvent event) {
        	
        }

        public boolean isNotified() {
            return notified;
        }

        public String getMessage() {
            return message;
        }
    }
    
    public enum TestSingleProjectShutdownStages {
        START, OPENED, KILLED_PROJECT, OK_DEAD, REVIVED, DONE;
    }
    public void testSingleProjectShutdown() throws RemoteException {
        final LockStepper<TestSingleProjectShutdownStages> ls = new LockStepper<TestSingleProjectShutdownStages>(TestSingleProjectShutdownStages.START);
        
        new Thread("Is the Project Alive Thread") {
            @Override
            public void run() {
                try {
                    KnowledgeBase kb = getKb();
                    int frameCount = kb.getFrames().size();
                    kb.flushCache();
                    ls.stageAchieved(TestSingleProjectShutdownStages.OPENED, null);
                    ls.waitForStage(TestSingleProjectShutdownStages.KILLED_PROJECT);
                    boolean failed = false;
                    try {
                        kb.getFrame("Article");
                    }
                    catch (Throwable t) {
                        failed = true;
                    }
                    assertTrue(failed);
                    failed = false;
                    try {
                        kb = getKb();
                        kb.getFrames();
                    }
                    catch (Throwable t) {
                        failed = true;
                    }
                    assertTrue(failed);
                    ls.stageAchieved(TestSingleProjectShutdownStages.OK_DEAD, null);
                    ls.waitForStage(TestSingleProjectShutdownStages.REVIVED);
                    kb = getKb();
                    assertTrue(kb.getFrames().size() == frameCount);
                    ls.stageAchieved(TestSingleProjectShutdownStages.DONE, null);
                }
                catch (Throwable t) {
                    log.log(Level.WARNING, "Exception will cause junit to fail", t);
                    ls.exceptionOffMainThread(TestSingleProjectShutdownStages.DONE, t);
                }
            }
        }.start();
        ls.waitForStage(TestSingleProjectShutdownStages.OPENED);
        RemoteSession session = openAdminSession();
        server.shutdown(PROJECT_NAME, session);
        SystemUtilities.sleepMsec(200);
        ls.stageAchieved(TestSingleProjectShutdownStages.KILLED_PROJECT, null);
        ls.waitForStage(TestSingleProjectShutdownStages.OK_DEAD);
        server.setProjectStatus(PROJECT_NAME, ProjectStatus.READY, session);
        ls.stageAchieved(TestSingleProjectShutdownStages.REVIVED, null);
        ls.waitForStage(TestSingleProjectShutdownStages.DONE);
    }
    
    public enum TestKillOtherUser {
        START, OPEN, MURDER, CONFIRMED;
    }
    public void testKillOtherUser() throws RemoteException {
        final LockStepper<TestKillOtherUser> ls = new LockStepper<TestKillOtherUser>(TestKillOtherUser.START);
        new Thread() {
            @Override
            public void run() {
                try {
                    RemoteSession session = server.openSession(USER1, SystemUtilities.getMachineIpAddress(), PASSWORD1);
                    RemoteServerProject serverProject = server.openProject(PROJECT_NAME, session);
                    Project p = RemoteClientProject.createProject(server, serverProject, session, false);
                    KnowledgeBase kb = p.getKnowledgeBase();
                    kb.flushCache();
                    kb.getFrames();
                    kb.flushCache();
                    ls.stageAchieved(TestKillOtherUser.OPEN, session);
                    ls.waitForStage(TestKillOtherUser.MURDER);
                    boolean failed = false;
                    Thread.sleep(6000);
                    try {
                        kb.getFrames();
                    }
                    catch (Throwable t) {
                        failed = true;
                    }
                    assertTrue(failed);
                    serverProject = server.openProject(PROJECT_NAME, session);
                    assertTrue(serverProject == null);
                    ls.stageAchieved(TestKillOtherUser.CONFIRMED, null);
                }
                catch (Throwable t) {
                	log.log(Level.SEVERE, t.getMessage(), t);
                    ls.exceptionOffMainThread(TestKillOtherUser.CONFIRMED, t);                    
                }
            }
        }.start();
        RemoteSession session = openAdminSession();
        RemoteSession sessionToKill = (RemoteSession) ls.waitForStage(TestKillOtherUser.OPEN);
        server.killOtherUserSession(sessionToKill, session);
        ls.stageAchieved(TestKillOtherUser.MURDER, null);
        ls.waitForStage(TestKillOtherUser.CONFIRMED);
    }
    
    public void testKillOtherUserPreventedByPolicy() throws RemoteException {
        final LockStepper<TestKillOtherUser> ls = new LockStepper<TestKillOtherUser>(TestKillOtherUser.START);
        new Thread() {
            @Override
            public void run() {
                try {
                    RemoteSession session = server.openSession(USER1, SystemUtilities.getMachineIpAddress(), PASSWORD1);
                    RemoteServerProject serverProject = server.openProject(PROJECT_NAME, session);
                    Project p = RemoteClientProject.createProject(server, serverProject, session, false);
                    KnowledgeBase kb = p.getKnowledgeBase();
                    kb.flushCache();
                    kb.getFrames();
                    kb.flushCache();
                    ls.stageAchieved(TestKillOtherUser.OPEN, session);
                    ls.waitForStage(TestKillOtherUser.MURDER);
                    boolean failed = false;
                    Thread.sleep(6000);
                    try {
                        kb.getFrames();
                    }
                    catch (Throwable t) {
                        failed = true;
                    }
                    assertTrue(!failed);
                    ls.stageAchieved(TestKillOtherUser.CONFIRMED, null);
                }
                catch (Throwable t) {
                    ls.exceptionOffMainThread(TestKillOtherUser.CONFIRMED, t);
                }
            }
        }.start();
        RemoteSession session = openSession();
        RemoteSession sessionToKill = (RemoteSession) ls.waitForStage(TestKillOtherUser.OPEN);
        server.killOtherUserSession(sessionToKill, session);
        ls.stageAchieved(TestKillOtherUser.MURDER, null);
        ls.waitForStage(TestKillOtherUser.CONFIRMED);
    }
}

