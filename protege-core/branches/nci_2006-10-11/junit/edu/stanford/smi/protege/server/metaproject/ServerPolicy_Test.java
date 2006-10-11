package edu.stanford.smi.protege.server.metaproject;

import java.rmi.NotBoundException;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.Server_Test;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.impl.OperationImpl;
import edu.stanford.smi.protege.test.APITestCase;

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
    Project p = RemoteProjectManager.getInstance().getProject(Server_Test.HOST, USER1, PASSWORD1, PROJECT_NAME, true);
    DefaultKnowledgeBase kb = (DefaultKnowledgeBase) p.getKnowledgeBase();
    
    assertTrue(RemoteClientFrameStore.isOperationAllowed( kb, new OperationImpl("RestartServer")));
    assertTrue(RemoteClientFrameStore.isOperationAllowed( kb, OperationImpl.READ));
    assertFalse(RemoteClientFrameStore.isOperationAllowed(kb, OperationImpl.WRITE));
    assertFalse(RemoteClientFrameStore.isOperationAllowed(kb, Policy_Test.SELF_DESTRUCT));
    assertTrue(RemoteClientFrameStore.isOperationAllowed( kb, new OperationImpl("someWeirdNotInOntology")));
  }


}
