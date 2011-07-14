package edu.stanford.smi.protege.server;

import java.io.Serializable;
import java.util.UUID;


public class Session implements RemoteSession, Serializable {
    private static final long serialVersionUID = -7027997614420432217L;

    private static int nextPrettyId = 0;
    
    private int prettyId;
    private UUID id;
    private boolean allowDelegation;
    
    private String userName;
    private String delegateUserName;
    
    private String userIpAddress;
    private long startTime;
    
    public Session(String userName, String userIpAddress) {
        this(userName, userIpAddress, false);
    }

    public Session(String userName, String userIpAddress, boolean allowDelegation) {
        this.prettyId  = nextPrettyId++;
        this.id = UUID.randomUUID();
        this.allowDelegation = allowDelegation;
        
        this.userName = userName;
        this.delegateUserName = null;
        
        this.userIpAddress = userIpAddress;
        this.startTime = currentTime();
    }

    public Session() {

    }

    public int getId() {
    	return prettyId;
    }
    
    public boolean allowDelegation() {
        return allowDelegation;
    }

    public void setDelegate(String delegateUserName) {
        if (!allowDelegation) {
            throw new IllegalAccessError("Not allowed to delegate");
        }
        this.delegateUserName = delegateUserName;
    }

    public String getUserName() {
        if (allowDelegation && delegateUserName != null) {
            return delegateUserName;
        }
        else {
            return userName;
        }
    }
    
    public String getRealUserName() {
        return userName;
    }

    public String getUserIpAddress() {
        return userIpAddress;
    }

	public long getStartTime() {
		return startTime;
	}

	private static long currentTime() {
        return System.currentTimeMillis();
    }

    @Override
	public boolean equals(Object o) {
        if (o == null || !(o instanceof Session)) {
            return false;
        }
        Session other = (Session) o;
        return id.equals(other.id) && userName.equals(other.userName) && allowDelegation == other.allowDelegation;
    }

    @Override
	public int hashCode() {
        return id.hashCode();
    }
    
    @Override
	public String toString() {
        return "Session(id=" + prettyId + ", user=" + userName + ")";
    }
}