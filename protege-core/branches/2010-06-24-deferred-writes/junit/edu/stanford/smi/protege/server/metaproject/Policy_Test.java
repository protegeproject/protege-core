package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

import edu.stanford.smi.protege.model.framestore.SimpleTestCase;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.util.URIUtilities;

public class Policy_Test extends SimpleTestCase {
  
  public static final String METAPROJECT = "junit/pprj/policy/metaproject.pprj";
  
  public static final String PROJECT1 ="Newspaper";
  
  public static final String        NATASHA  = "Natasha Noy";
  public static final String        BOB      = "Bob";
  public static final String        PAUL     = "Paul";
  public static final String        DOUBLEO  = "Bond, James Bond";
  
  public static final Operation           RESTART  = new UnbackedOperationImpl("RestartServer", null); 
  public static final Operation     SELF_DESTRUCT  = new UnbackedOperationImpl("DestroyInstallationToProtectAgents",null);
  public static final Operation           KILL     = new UnbackedOperationImpl("LicensedKill",null);
  
  public static void checkAuthorization(Policy p,
                                        String userName,
                                        Operation op,
                                        String projectName,
                                        boolean allowed) {
    User user = p.getUserByName(userName);
    ProjectInstance project = p.getProjectInstanceByName(projectName);
    checkAuthorization(p, user, op, project, allowed);
  }
  
  public static void checkAuthorization(Policy p,
                                        User user,
                                        Operation op,
                                        ProjectInstance project,
                                        boolean allowed) {
    Set<Operation> operations = p.getAllowedOperations(user, project);
    if (allowed) {
      assertTrue(p.isOperationAuthorized(user, op, project));
      assertTrue(operations.contains(op));
    } else {
      assertFalse(p.isOperationAuthorized(user, op, project));
      assertFalse(operations.contains(op));
    } 
  }
  
  public void testPolicy01() {
    MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));
        
    Policy p = mp.getPolicy();

    checkAuthorization(p, NATASHA, MetaProjectConstants.OPERATION_WRITE, PROJECT1, true);
    checkAuthorization(p, BOB,     MetaProjectConstants.OPERATION_WRITE, PROJECT1, false);
    checkAuthorization(p, PAUL,    MetaProjectConstants.OPERATION_WRITE, PROJECT1, false);
    checkAuthorization(p, PAUL,    KILL,                PROJECT1, false);
    checkAuthorization(p, PAUL,    RESTART,             PROJECT1, true);
    checkAuthorization(p, DOUBLEO, KILL,                PROJECT1, true);
    checkAuthorization(p, DOUBLEO, SELF_DESTRUCT,       PROJECT1, true);
    checkAuthorization(p, DOUBLEO, MetaProjectConstants.OPERATION_WRITE, PROJECT1, false);
  }
}
