package edu.stanford.smi.protege.server;

import java.io.Serializable;


public class Session implements RemoteSession, Serializable {
    private static int nextId = 100;
    private static int nextSessionGroup = 100;
    private int id;
    private int sessionGroup;
    private String userName;
    private String userIpAddress;
    private long startTime;
    private long lastAccessTime;
    
    public Session(String userName, String userIpAddress) {
        this(userName, userIpAddress, nextSessionGroup++);
    }

    public Session(String userName, String userIpAddress, int sessionGroup) {
        this.id = nextId++;
        this.sessionGroup = sessionGroup;
        this.userName = userName;
        this.userIpAddress = userIpAddress;
        
        this.startTime = currentTime();
        this.lastAccessTime = startTime;
    }

    public Session() {

    }

    public String getUserName() {
        return userName;
    }

    public String getUserIpAddress() {
        return userIpAddress;
    }
    
    public int getSessionGroup() {
        return sessionGroup;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateAccessTime() {
        lastAccessTime = currentTime();
    }

    private static long currentTime() {
        return System.currentTimeMillis();
    }

    public boolean equals(Object o) {
        return id == ((Session) o).id;
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        return "Session(id=" + id + ", user=" + userName + ")";
    }
}