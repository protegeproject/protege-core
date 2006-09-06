package edu.stanford.smi.protege.server.metaproject;

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
  
  public static final Operation           RESTART  = new OperationImpl("RestartServer");   
  
  public void testPolicy01() {
    MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));
    
    
    Policy p = mp.getPolicy();

    assertTrue( p.isOperationAuthorized(NATASHA, OperationImpl.EDIT, PROJECT1));
    assertFalse(p.isOperationAuthorized(BOB,     OperationImpl.EDIT, PROJECT1));
    assertFalse(p.isOperationAuthorized(PAUL,    OperationImpl.EDIT, PROJECT1));
    assertTrue( p.isOperationAuthorized(PAUL,    RESTART,            PROJECT1));
  }
}
