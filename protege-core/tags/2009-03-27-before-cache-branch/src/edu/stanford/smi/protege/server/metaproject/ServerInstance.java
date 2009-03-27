package edu.stanford.smi.protege.server.metaproject;

public interface ServerInstance extends PolicyControlledObject {
	
	String getHostName();
	
	void setHostName(String hostName);
}
