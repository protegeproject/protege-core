package edu.stanford.smi.protege.server;

import java.rmi.*;
import java.util.*;

import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author Ray Fergerson
 * 
 * Description of this class
 */
public class Server_Test extends SimpleTestCase {
    private static final String HOST = "localhost";
    private static final String USER1 = "Ray Fergerson";
    private static final String PASSWORD1 = "claudia";
    private static final String WRONG_PASSWORD = "elle";
    private static final String PROJECT_NAME = "Newspaper";
    private static final String USER2 = "Jennifer Vendetti";
    private static final String PASSWORD2 = "jenny";

    private static final String NAME = "//" + HOST + "/" + Server.getBoundName();

    private RemoteServer _server;

    public void setUp() throws Exception {
        try {
            _server = (RemoteServer) Naming.lookup(NAME);
            _server.reinitialize();
        } catch (NotBoundException e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }

    private static String getMachineName() {
        return SystemUtilities.getMachineName();
    }

    public void testSession() throws RemoteException {
        RemoteSession session = _server.openSession(USER1, getMachineName(), PASSWORD1);
        assertNotNull("session", session);
        RemoteSession noSession = _server.openSession(USER1, getMachineName(), WRONG_PASSWORD);
        assertNull("no session", noSession);
        _server.closeSession(session);
    }

    public void testConnection() throws RemoteException {
        RemoteSession session1 = _server.openSession(USER1, getMachineName(), PASSWORD1);
        assertNotNull(session1);
        RemoteServerProject project1 = _server.openProject(PROJECT_NAME, session1);
        assertNotNull(project1);

        RemoteSession session2 = _server.openSession(USER2, getMachineName(), PASSWORD2);
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

}