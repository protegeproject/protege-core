package edu.stanford.smi.protege.server;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.SimpleTestCase;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.DeletionHook;
import edu.stanford.smi.protege.util.DeletionHookUtil;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * @author Ray Fergerson
 * 
 * Description of this class
 */
public class Server_Test extends SimpleTestCase {
    private static transient Logger log = Log.getLogger(Server_Test.class);
    
    public  static final String HOST = "localhost";
    public static final String USER1 = "Ray Fergerson";
    public static final String PASSWORD1 = "claudia";
    public static final String WRONG_PASSWORD = "elle";
    public static final String PROJECT_NAME = "Newspaper";
    public static final String USER2 = "Jennifer Vendetti";
    public static final String PASSWORD2 = "jenny";
    public static final String ADMIN_USER  = "Admin";
    public static final String ADMIN_PASSWORD = "admin";

    public static final String NAME = "//" + HOST + "/" + Server.getBoundName();
    public static final String JAR_PROPERTY="junit.server.protege.jar";
    public static String protegeJarLocation = "build/dist/protege.jar";
    public static String metaproject = "examples/server/metaproject.pprj";
    
    private static boolean serverRunning = false;
    
    private RemoteServer _server;
    private int updateCounter;
    
    @Override
    public void setUp() throws Exception {
      super.setUp();
      try {
        if (!startServer()) {
          return;
        }
        _server = (RemoteServer) Naming.lookup(NAME);
        _server.reinitialize();
      } catch (NotBoundException e) {
        fail("Could not bind to server (is rmiregistry running?)");
      }
    }
    
    private static boolean informedServerNotConfigured = false;
    
    public static boolean startServer() throws Exception {
      File appDir = ApplicationProperties.getApplicationDirectory();
      File jar = new File(appDir, protegeJarLocation);
      if (!jar.exists()) {
          jar = new File(protegeJarLocation);
          if (!jar.exists()) {
              System.out.println("Need to compile to a jar file before running server tests");
              System.out.println("System tests not configured");
              return false;
          }
      }
      System.setProperty("java.rmi.server.codebase", jar.toURL().toString());
      String [] serverArgs = {"", metaproject};
      if (!serverRunning) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("starting server");
        }
        Server.startServer(serverArgs);
        serverRunning = true;
      }
      return true;
    }
      
    public static boolean isServerRunning() {
      return serverRunning;
    }
    
    public static void setMetaProject(String metaproject) {
      Server_Test.metaproject = metaproject;
    }
 

    private static String getMachineIpAddress() {
        return SystemUtilities.getMachineIpAddress();
    } 

    public void testSession() throws RemoteException {
      if (!serverRunning) {
        return;
      }
      RemoteSession session = _server.openSession(USER1, getMachineIpAddress(), PASSWORD1);
      assertNotNull("session", session);
      RemoteSession noSession = _server.openSession(USER1, getMachineIpAddress(), WRONG_PASSWORD);
      assertNull("no session", noSession);
      _server.closeSession(session);
    }

    public void testConnection() throws RemoteException {
      if (!serverRunning) {
        return;
      }
      RemoteSession session1 = _server.openSession(USER1, getMachineIpAddress(), PASSWORD1);
      assertNotNull(session1);
      RemoteServerProject project1 = _server.openProject(PROJECT_NAME, session1);
      assertNotNull(project1);
      
      RemoteSession session2 = _server.openSession(USER2, getMachineIpAddress(), PASSWORD2);
      RemoteServerProject project2 = _server.openProject(PROJECT_NAME, session2);
      assertEquals("projects", project1, project2);
      
      Collection sessions = _server.getCurrentSessions(PROJECT_NAME, session2);
      assertEqualsSet(makeList(session1, session2), sessions);
      
      project2.close(session2);
      sessions = _server.getCurrentSessions(PROJECT_NAME, session1);
      assertEqualsSet(makeList(session1), sessions);
      
      project1.close(session1);
      _server.closeSession(session1);
      _server.closeSession(session2);
    }
    

    public void testGetProject() {
      if (!serverRunning) {
        return;
      }
      Project p = RemoteProjectManager.getInstance().getProject(HOST, USER1, PASSWORD1, PROJECT_NAME, true);
      assertNotNull(p);
      KnowledgeBase kb = p.getKnowledgeBase();
      Cls cls = kb.getCls("Editor");
      assertNotNull(cls);
      p.dispose();
    }
    
    public void testGetMetaProject() throws MalformedURLException, RemoteException, NotBoundException {
        if (!serverRunning) {
            return;
        } 
        RemoteServer server = (RemoteServer) Naming.lookup("//" + HOST + "/" + Server.getBoundName());
        RemoteSession session = server.openSession(ADMIN_USER, SystemUtilities.getMachineIpAddress(), ADMIN_PASSWORD);
        MetaProject metaproject = RemoteProjectManager.getInstance().connectToMetaProject(server, session);
        assertTrue(((MetaProjectImpl) metaproject).getKnowledgeBase().getProject().isMultiUserClient());
        assertTrue(metaproject.getProjects().size() == 7);
        assertTrue(metaproject.getProject("Wines") != null);
        metaproject.dispose();
    }
    
    public void testAccessTimes() throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        if (!serverRunning) {
            return;
        }
        RemoteServer server = (RemoteServer) Naming.lookup("//" + HOST + "/" + Server.getBoundName());
        RemoteSession session = server.openSession(ADMIN_USER, SystemUtilities.getMachineIpAddress(), ADMIN_PASSWORD);
        MetaProject metaproject = RemoteProjectManager.getInstance().connectToMetaProject(server, session);
        User u2 = metaproject.getUser(USER2);
        
        long start = System.currentTimeMillis();
        Thread.sleep(ServerProperties.getMetaProjectLastAccessTimeUpdateFrequency());
        ((MetaProjectImpl) metaproject).getKnowledgeBase().flushEvents();
        assertTrue(u2.getLastLogin() == null || u2.getLastLogin().getTime() < start);
        assertTrue(u2.getLastAccess() == null || u2.getLastAccess().getTime() < start);
        
        Project p = RemoteProjectManager.getInstance().getProject(HOST, USER2, PASSWORD2, PROJECT_NAME, true);
        KnowledgeBase kb = p.getKnowledgeBase();
    
        ((MetaProjectImpl) metaproject).getKnowledgeBase().flushEvents();
        Date loginTime = u2.getLastLogin();
        assertTrue(loginTime.getTime() > start);
        Thread.sleep(ServerProperties.getMetaProjectLastAccessTimeUpdateFrequency());
        ((MetaProjectImpl) metaproject).getKnowledgeBase().flushEvents();
        
        if (log.isLoggable(Level.FINE)) {
            log.fine("Last access time for user " + u2 + " is " + u2.getLastAccess());
        }
        assertTrue(u2.getLastAccess().getTime() >  start);
        
        start  = System.currentTimeMillis();
        Thread.sleep(ServerProperties.getMetaProjectLastAccessTimeUpdateFrequency());
        assertTrue(loginTime.getTime() < start);
        assertTrue(u2.getLastAccess().getTime() <= start);
         
        kb.createCls("garbage", Collections.singleton(kb.getRootCls()));
        Thread.sleep(ServerProperties.getMetaProjectLastAccessTimeUpdateFrequency());
        ((MetaProjectImpl) metaproject).getKnowledgeBase().flushEvents();
        
        assertTrue(loginTime.equals(u2.getLastLogin()));
        assertTrue(u2.getLastAccess().getTime() > start);
        assertTrue(u2.getLastAccess().getTime() <= System.currentTimeMillis());
        
        kb.getProject().dispose();
        metaproject.dispose();
    }

    public void testDeletionHook() {
        if (!serverRunning) {
            return;
        }
        Project p = RemoteProjectManager.getInstance().getProject(HOST, USER1, PASSWORD1, PROJECT_NAME, true);
        assertNotNull(p);
        KnowledgeBase kb = p.getKnowledgeBase();
        DeletionHookJob job = new DeletionHookJob(kb);
        job.execute();
        kb.deleteCls(kb.getCls("Content_Layout"));
        assertTrue((Boolean) job.execute());
        p.dispose();
    }
    
    public static void setProtegeJarLocation(String location) {
        protegeJarLocation = location;
    }
    
    public static class DeletionHookJob extends ProtegeJob {
        private static final long serialVersionUID = 1L;
        
        private static boolean installed = false;
        private static boolean deleteFound = false;
        
        public DeletionHookJob(KnowledgeBase kb) {
            super(kb);
        }
        
        @Override
        public Boolean run() {
            if (!installed) {
                DeletionHookUtil.addDeletionHook(getKnowledgeBase(), new DeletionHook() {
                    public void delete(Frame frame) {
                        if (frame.getKnowledgeBase() != null) {
                            deleteFound = true;
                        }
                    }
                });
                installed = true;
            }
            return deleteFound;
        }
    }
}