package edu.stanford.smi.protege.server;

import java.io.*;

public class Session implements RemoteSession, Externalizable {
    private static int nextId = 100;
    private int id;
    private String userName;
    private String userMachine;
    private long startTime;
    private long lastAccessTime;

    public Session(String userName, String userMachine) {
        this.id = nextId++;
        this.userName = userName;
        this.userMachine = userMachine;
        this.startTime = currentTime();
        this.lastAccessTime = startTime;
    }

    public Session() {

    }

    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeInt(id);
        output.writeUTF(userName);
        output.writeUTF(userMachine);
        output.writeLong(startTime);
        output.writeLong(lastAccessTime);
    }

    public void readExternal(ObjectInput input) throws IOException {
        id = input.readInt();
        userName = input.readUTF();
        userMachine = input.readUTF();
        startTime = input.readLong();
        lastAccessTime = input.readLong();

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