package edu.stanford.smi.protege.server;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.SimpleTestCase;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * @author Ray Fergerson
 * 
 * Description of this class
 */
public class Server_Test extends SimpleTestCase {
    private static transient Logger log = Log.getLogger(Server_Test.class);
    
    private static final String HOST = "localhost";
    private static final String USER1 = "Ray Fergerson";
    private static final String PASSWORD1 = "claudia";
    private static final String WRONG_PASSWORD = "elle";
    private static final String PROJECT_NAME = "Newspaper";
    private static final String USER2 = "Jennifer Vendetti";
    private static final String PASSWORD2 = "jenny";

    private static final String NAME = "//" + HOST + "/" + Server.getBoundName();
    
    private static  final String JAR_PROPERTY="junit.server.protege.jar";
    
    private static final int RETRY = 5;
    private static final long SLEEP = 3000;
    
    private static boolean serverRunning = false;

    private RemoteServer _server;

    public void setUp() throws Exception {
      super.setUp();
      try {
        startServer();
        _server = (RemoteServer) Naming.lookup(NAME);
        _server.reinitialize();
      } catch (NotBoundException e) {
        fail("Could not bind to server (is rmiregistry running?)");
      }
    }
    
    public static void startServer() throws Exception {
      Properties jup = APITestCase.getJunitProperties();
      String jar_uri = jup.getProperty(JAR_PROPERTY);
      if (jar_uri == null) {
        return;
      }
      System.setProperty("java.rmi.server.codebase", jar_uri);
      String [] serverArgs = {"", "examples/server/metaproject.pprj"};
      if (!serverRunning) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("starting server");
        }
        Server.startServer(serverArgs);
        serverRunning = true;
      }
    }
      
    public static boolean isServerRunning() {
      return serverRunning;
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

}