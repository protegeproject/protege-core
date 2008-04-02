package edu.stanford.smi.protege.server;

import java.io.Serializable;

import edu.stanford.smi.protege.server.auth.LoginToken;
import edu.stanford.smi.protege.server.auth.UserId;


public class Session extends UserId implements RemoteSession, Serializable {
    private static int nextId = 100;
    private int id;

    private long startTime;
    private long lastAccessTime;

    public Session(LoginToken token) {
        super(token.getUserName(), token.getUserIpAddress());
        this.id = nextId++;
        this.startTime = currentTime();
        this.lastAccessTime = startTime;
    }

    public Session() {
        super(null, null);
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