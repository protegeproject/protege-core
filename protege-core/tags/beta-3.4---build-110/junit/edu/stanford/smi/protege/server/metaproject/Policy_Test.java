package edu.stanford.smi.protege.server.metaproject;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.framestore.SimpleTestCase;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectInstanceImpl;
import edu.stanford.smi.protege.server.metaproject.impl.OperationImpl;
import edu.stanford.smi.protege.server.metaproject.impl.UserInstanceImpl;
import edu.stanford.smi.protege.util.URIUtilities;

public class Policy_Test extends SimpleTestCase {
  
  public static final String METAPROJECT = "junit/pprj/policy/metaproject.pprj";
  
  public static final MetaProjectInstance PROJECT1 = new MetaProjectInstanceImpl("Newspaper");
  
  public static final UserInstance        NATASHA  = new UserInstanceImpl("Natasha Noy");
  public static final UserInstance        BOB      = new UserInstanceImpl("Bob");
  public static final UserInstance        PAUL     = new UserInstanceImpl("Paul");
  public static final UserInstance        DOUBLEO  = new UserInstanceImpl("Bond, James Bond");
  
  public static final Operation           RESTART  = new OperationImpl("RestartServer"); 
  public static final Operation     SELF_DESTRUCT  = new OperationImpl("DestroyInstallationToProtectAgents");
  public static final Operation           KILL     = new OperationImpl("LicensedKill");
  
  public static void checkAuthorization(Policy p,
                                        UserInstance user,
                                        Operation op,
                                        MetaProjectInstance project,
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

    checkAuthorization(p, NATASHA, OperationImpl.WRITE, PROJECT1, true);
    checkAuthorization(p, BOB,     OperationImpl.WRITE, PROJECT1, false);
    checkAuthorization(p, PAUL,    OperationImpl.WRITE, PROJECT1, false);
    checkAuthorization(p, PAUL,    KILL,                PROJECT1, false);
    checkAuthorization(p, PAUL,    RESTART,             PROJECT1, true);
    checkAuthorization(p, DOUBLEO, KILL,                PROJECT1, true);
    checkAuthorization(p, DOUBLEO, SELF_DESTRUCT,       PROJECT1, true);
    checkAuthorization(p, DOUBLEO, OperationImpl.WRITE, PROJECT1, false);
  }
}
