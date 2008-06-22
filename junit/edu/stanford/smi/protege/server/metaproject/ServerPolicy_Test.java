package edu.stanford.smi.protege.server.metaproject;

import java.rmi.NotBoundException;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.Server_Test;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.test.APITestCase;

public class ServerPolicy_Test extends APITestCase {
  private static final String USER1 = "Paul";
  private static final String PASSWORD1 = "paul";
  private static final String PROJECT_NAME = "Newspaper";

  @Override
public void setUp() throws Exception {
    super.setUp();
    try {
      Server_Test.setMetaProject("junit/pprj/policy/metaproject.pprj");
      Server_Test.startServer();
    } catch (NotBoundException e) {
      fail("Could not bind to server (is rmiregistry running?)");
    }
  }
  
  public void testServerPolicy01() throws Exception {
    Project p = RemoteProjectManager.getInstance().getProject(Server_Test.HOST, USER1, PASSWORD1, PROJECT_NAME, true);
    DefaultKnowledgeBase kb = (DefaultKnowledgeBase) p.getKnowledgeBase();
    
    assertTrue(RemoteClientFrameStore.isOperationAllowed( kb, new UnbackedOperationImpl("RestartServer", null)));
    assertTrue(RemoteClientFrameStore.isOperationAllowed( kb, MetaProject_Test.ALT_READ));
    assertFalse(RemoteClientFrameStore.isOperationAllowed(kb, MetaProjectConstants.OPERATION_WRITE));
    assertFalse(RemoteClientFrameStore.isOperationAllowed(kb, Policy_Test.SELF_DESTRUCT));
    assertTrue(RemoteClientFrameStore.isOperationAllowed( kb, new UnbackedOperationImpl("someWeirdNotInOntology", null)));
  }


}
