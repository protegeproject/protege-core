package edu.stanford.smi.protege.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import junit.framework.TestCase;

public class ServerDelegationTest extends TestCase {
    public static final String ADMIN_USER = "Admin";
    public static final String ADMIN_PASSWORD = "admin";
    
    static {
        Server_Test.setMetaProject("junit/pprj/policy/metaproject02.pprj");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Server_Test.startServer();
    }
    
    public void testSuccessfulDelegation() throws MalformedURLException, RemoteException, NotBoundException {
        if (!Server_Test.isServerRunning()) {
            return;
        }
        RemoteServer server = (RemoteServer) Naming.lookup(Server_Test.NAME);
        RemoteSession session = server.openSession(ADMIN_USER, "localhost", ADMIN_PASSWORD);
        assertTrue(session.allowDelegation());
        assertTrue(session.getUserName().equals(ADMIN_USER));
        assertTrue(session.getRealUserName().equals(ADMIN_USER));
        RemoteSession delegate = session.makeDelegate(Server_Test.USER1);
        assertTrue(delegate.getUserName().equals(Server_Test.USER1));
        assertTrue(delegate.getRealUserName().equals(ADMIN_USER));
    }
    
    public void testFailedDelegation() throws MalformedURLException, RemoteException, NotBoundException {
        if (!Server_Test.isServerRunning()) {
            return;
        }
        RemoteServer server = (RemoteServer) Naming.lookup(Server_Test.NAME);
        RemoteSession session = server.openSession(Server_Test.USER1, "localhost", Server_Test.PASSWORD1);
        assertTrue(!session.allowDelegation());
        assertTrue(session.getUserName().equals(Server_Test.USER1));
        assertTrue(session.getRealUserName().equals(Server_Test.USER1));
        boolean failed = false;
        try  {
            session.makeDelegate(Server_Test.USER2);
        }
        catch (IllegalAccessError iae) {
            failed = true;
        }
        assertTrue(failed);
    }
    
}
