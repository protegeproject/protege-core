package edu.stanford.smi.protege.server.metaproject;

import java.rmi.NotBoundException;
import java.util.Set;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.Server_Test;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.Log;

public class ServerPolicy_Test extends APITestCase {
  private static final String USER1 = "Paul";
  private static final String PASSWORD1 = "paul";
  private static final String PROJECT_NAME = "Newspaper";


  
  

  public void setUp() throws Exception {
    super.setUp();
    try {
      Server_Test.startServer("junit/pprj/policy/metaproject.pprj");
    } catch (NotBoundException e) {
      fail("Could not bind to server (is rmiregistry running?)");
    }
  }
  
  public void testServerPolicy01() throws Exception {
    Server server = Server.getInstance();
    RemoteSession session = server.openSession(USER1, Server_Test.getMachineIpAddress(), PASSWORD1);
    RemoteServerProject project = server.openProject(PROJECT_NAME, session);
    RemoteServerFrameStore serverFrameStore = project.getProjectKbFrameStore(session);
    Set<Operation> operations = serverFrameStore.getAllowedOperations(session);
    assertFalse(operations.isEmpty());
    
  }


}
