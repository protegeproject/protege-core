// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package edu.stanford.smi.protege.server;

public final class Server_Stub
    extends java.rmi.server.RemoteStub
    implements edu.stanford.smi.protege.server.RemoteServer, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_closeSession_0;
    private static java.lang.reflect.Method $method_getAvailableProjectNames_1;
    private static java.lang.reflect.Method $method_getCurrentSessions_2;
    private static java.lang.reflect.Method $method_openProject_3;
    private static java.lang.reflect.Method $method_openSession_4;
    private static java.lang.reflect.Method $method_reinitialize_5;
    
    static {
	try {
	    $method_closeSession_0 = edu.stanford.smi.protege.server.RemoteServer.class.getMethod("closeSession", new java.lang.Class[] {edu.stanford.smi.protege.server.RemoteSession.class});
	    $method_getAvailableProjectNames_1 = edu.stanford.smi.protege.server.RemoteServer.class.getMethod("getAvailableProjectNames", new java.lang.Class[] {edu.stanford.smi.protege.server.RemoteSession.class});
	    $method_getCurrentSessions_2 = edu.stanford.smi.protege.server.RemoteServer.class.getMethod("getCurrentSessions", new java.lang.Class[] {java.lang.String.class, edu.stanford.smi.protege.server.RemoteSession.class});
	    $method_openProject_3 = edu.stanford.smi.protege.server.RemoteServer.class.getMethod("openProject", new java.lang.Class[] {java.lang.String.class, edu.stanford.smi.protege.server.RemoteSession.class});
	    $method_openSession_4 = edu.stanford.smi.protege.server.RemoteServer.class.getMethod("openSession", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class});
	    $method_reinitialize_5 = edu.stanford.smi.protege.server.RemoteServer.class.getMethod("reinitialize", new java.lang.Class[] {});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public Server_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of closeSession(RemoteSession)
    public void closeSession(edu.stanford.smi.protege.server.RemoteSession $param_RemoteSession_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_closeSession_0, new java.lang.Object[] {$param_RemoteSession_1}, 6594239815482672801L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getAvailableProjectNames(RemoteSession)
    public java.util.Collection getAvailableProjectNames(edu.stanford.smi.protege.server.RemoteSession $param_RemoteSession_1)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getAvailableProjectNames_1, new java.lang.Object[] {$param_RemoteSession_1}, 2201225262091498790L);
	    return ((java.util.Collection) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getCurrentSessions(String, RemoteSession)
    public java.util.Collection getCurrentSessions(java.lang.String $param_String_1, edu.stanford.smi.protege.server.RemoteSession $param_RemoteSession_2)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getCurrentSessions_2, new java.lang.Object[] {$param_String_1, $param_RemoteSession_2}, 3703387393377370884L);
	    return ((java.util.Collection) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of openProject(String, RemoteSession)
    public edu.stanford.smi.protege.server.RemoteServerProject openProject(java.lang.String $param_String_1, edu.stanford.smi.protege.server.RemoteSession $param_RemoteSession_2)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_openProject_3, new java.lang.Object[] {$param_String_1, $param_RemoteSession_2}, 6216658583787717573L);
	    return ((edu.stanford.smi.protege.server.RemoteServerProject) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of openSession(String, String, String)
    public edu.stanford.smi.protege.server.RemoteSession openSession(java.lang.String $param_String_1, java.lang.String $param_String_2, java.lang.String $param_String_3)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_openSession_4, new java.lang.Object[] {$param_String_1, $param_String_2, $param_String_3}, -7882112180587072835L);
	    return ((edu.stanford.smi.protege.server.RemoteSession) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of reinitialize()
    public void reinitialize()
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_reinitialize_5, null, 3978709805145571485L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
