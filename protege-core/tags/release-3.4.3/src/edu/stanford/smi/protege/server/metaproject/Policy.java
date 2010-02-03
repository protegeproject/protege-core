package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface Policy {
	
	public boolean isOperationAuthorized(User user, Operation op, PolicyControlledObject project);

	Set<Operation> getAllowedOperations(User user, ProjectInstance project);

	Set<Operation> getKnownOperations();

	User getUserByName(String user);

	ProjectInstance getProjectInstanceByName(String projectName);
	
	ServerInstance getServerInstanceByName(String serverName);
	
	ServerInstance getFirstServerInstance();	

}
