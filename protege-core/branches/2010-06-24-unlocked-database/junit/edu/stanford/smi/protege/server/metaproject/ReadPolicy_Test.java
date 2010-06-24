package edu.stanford.smi.protege.server.metaproject;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.Server_Test;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class ReadPolicy_Test extends APITestCase {
    private Logger log = Log.getLogger(ReadPolicy_Test.class);

    public static final String USER1 = "Natasha Noy";
    public static final String PASSWORD1 = "natalia";
    public static final String USER2 = "Tania Tudorache";
    public static final String PASSWORD2 = "tania";
    public static final String USER3 = "Timothy Redmond";
    public static final String PASSWORD3 = "troglodyte";

    public static final String META_PROJECT = "Metaproject";
    public static final String NEWSPAPER_PROJECT = "Newspaper";
    public static final String WINES_PROJECT = "Wines";

    private RemoteServer clientsRemoteServer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            Server_Test.setMetaProject("junit/pprj/policy/metaproject01.pprj");
            Server_Test.startServer();
            clientsRemoteServer = (RemoteServer) Naming.lookup("//" + Server_Test.HOST + "/" + Server.getBoundName());
        } catch (NotBoundException e) {
            fail("Could not bind to server (is rmiregistry running?)");
        }
    }

    public void testNatashaAccess() throws Exception {
        RemoteSession session = clientsRemoteServer.openSession(USER1, 
                                                                SystemUtilities.getMachineIpAddress(), 
                                                                PASSWORD1);
        Collection<String> projects = clientsRemoteServer.getAvailableProjectNames(session);
        assertEquals(0, projects.size());
        checkReadAccess(session, META_PROJECT, false);
        checkReadAccess(session, NEWSPAPER_PROJECT, true);
        checkReadAccess(session, WINES_PROJECT, false);
    }
    
    public void testTaniaAccess() throws Exception {
        RemoteSession session = clientsRemoteServer.openSession(USER2, 
                                                                SystemUtilities.getMachineIpAddress(), 
                                                                PASSWORD2);
        Collection<String> projects = clientsRemoteServer.getAvailableProjectNames(session);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(NEWSPAPER_PROJECT));
        assertTrue(projects.contains(WINES_PROJECT));
        checkReadAccess(session, META_PROJECT, false);
        checkReadAccess(session, NEWSPAPER_PROJECT, true);
        checkReadAccess(session, WINES_PROJECT, true);
    }
    
    public void testTimothyAccess() throws Exception {
        RemoteSession session = clientsRemoteServer.openSession(USER3, 
                                                                SystemUtilities.getMachineIpAddress(), 
                                                                PASSWORD3);
        Collection<String> projects = clientsRemoteServer.getAvailableProjectNames(session);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(NEWSPAPER_PROJECT));
        assertTrue(projects.contains(WINES_PROJECT));
        checkReadAccess(session, META_PROJECT, true);
        checkReadAccess(session, NEWSPAPER_PROJECT, true);
        checkReadAccess(session, WINES_PROJECT, true);
    }

    private void checkReadAccess(RemoteSession session, String project, boolean allowed) throws RemoteException {
        session = clientsRemoteServer.cloneSession(session);
        Project p = RemoteProjectManager.getInstance().connectToProject(clientsRemoteServer, session, project);
        if (!allowed) {
            assertTrue(p == null);
            return;
        }
        assertTrue(p != null);
        KnowledgeBase kb = p.getKnowledgeBase();
        if (project.equals(META_PROJECT)) {
            assertNotNull(kb.getCls("Group"));
        }
        else if (project.equals(NEWSPAPER_PROJECT)) {
            assertNotNull(kb.getCls("Article"));
        }
        else if (project.equals(WINES_PROJECT)) {
            assertNotNull(kb.getCls("Wine"));
        }
        p.dispose();
    }


}
