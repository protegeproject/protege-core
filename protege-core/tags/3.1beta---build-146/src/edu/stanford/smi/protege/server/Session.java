package edu.stanford.smi.protege.server;

import java.io.*;

public class Session implements RemoteSession, Serializable {
    private static int nextId = 100;
    private final int id;
    private final String userName;
    private final String userMachine;
    private final long startTime;
    private long lastAccessTime;

    public Session(String userName, String userMachine) {
        this.id = nextId++;
        this.userName = userName;
        this.userMachine = userMachine;
        this.startTime = currentTime();
        this.lastAccessTime = startTime;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserMachine() {
        return userMachine;
    }
    
    public long getLastAccessTime() {
        return lastAccessTime; 
    }
    
    public void updateAccessTime() {
        lastAccessTime = currentTime();
    }
    
    private long currentTime() {
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