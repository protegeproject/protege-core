package edu.stanford.smi.protege.server;

public interface RemoteSession {

    int getId();
    boolean allowDelegation();
    void setDelegate(String delegateUserName);    
    
    String getUserName();
    String getRealUserName();


    String getUserIpAddress();
    long getStartTime();
}