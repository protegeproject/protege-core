package edu.stanford.smi.protege.server;

public interface RemoteSession {

    int getId();
    boolean allowDelegation();
    RemoteSession makeDelegate(String delegateUserName);    
    
    String getUserName();
    String getRealUserName();


    String getUserIpAddress();
    long getStartTime();
}