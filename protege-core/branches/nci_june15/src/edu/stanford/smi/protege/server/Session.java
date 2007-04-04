package edu.stanford.smi.protege.server;

import java.io.Serializable;


public class Session implements RemoteSession, Serializable {
    private static int nextId = 100;
    private int id;
    private String userName;
    private String userIpAddress;
    private long startTime;
    private long lastAccessTime;

    public Session(String userName, String userIpAddress) {
        this.id = nextId++;
        this.userName = userName;
        this.userIpAddress = userIpAddress;
        this.startTime = currentTime();
        this.lastAccessTime = startTime;
    }

    public Session() {

    }
    
    /* from Externalizable interface
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeInt(id);
        output.writeUTF(userName);
        output.writeUTF(userIpAddress);
        output.writeLong(startTime);
        output.writeLong(lastAccessTime);
    }

    public void readExternal(ObjectInput input) throws IOException {
        id = input.readInt();
        userName = input.readUTF();
        userIpAddress = input.readUTF();
        startTime = input.readLong();
        lastAccessTime = input.readLong();

    }
    */

    public String getUserName() {
        return userName;
    }

    public String getUserIpAddress() {
        return userIpAddress;
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