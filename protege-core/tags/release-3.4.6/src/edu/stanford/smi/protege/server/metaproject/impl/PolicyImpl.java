package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.PolicyControlledObject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.ServerInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.CollectionUtilities;

public class PolicyImpl implements Policy, Localizable, Serializable {
    private static final long serialVersionUID = 2209527990426609790L;


    MetaProjectImpl mp;

	public PolicyImpl(MetaProjectImpl mp) {
		this.mp = mp;
	}

	/*
	 * The project is ignored in this implementation.
	 */
	public boolean isOperationAuthorized(User user,
	                                     Operation op,
	                                     PolicyControlledObject project) {
		if (!getKnownOperations().contains(op)) {
			return true;
		}
		Set<Group> userGroups = user.getGroups();
		for (GroupOperation ga : project.getAllowedGroupOperations()) {
			if (userGroups.contains(ga.getAllowedGroup()) && ga.getAllowedOperations().contains(op)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * The project is ignored in this implementation.
	 */
	public Set<Operation> getAllowedOperations(User user, ProjectInstance project) {
		Set<Operation> allowed = new HashSet<Operation>();
		if (project == null) {
		    return allowed;
		}
		Set<Group> userGroups = user.getGroups();
		for (GroupOperation ga : project.getAllowedGroupOperations()) {
			if (userGroups.contains(ga.getAllowedGroup())) {
				allowed.addAll(ga.getAllowedOperations());
			}
		}
		return allowed;
	}

	public Set<Operation> getKnownOperations() {
		return mp.getOperations();
	}

	public User getUserByName(String userName) {
	    if (userName == null) {
	        return null;
	    }
	    for (User user : mp.getUsers()) {
	        if (userName.equals(user.getName())) {
	            return user;
	        }
	    }
	    return null;
	}

	public ProjectInstance getProjectInstanceByName(String projectName) {
	    for (ProjectInstance projectInstance : mp.getProjects()) {
	        if (projectInstance.getName().equals(projectName)) {
	            return projectInstance;
	        }
	    }
	    return null;
	}


	public ServerInstance getServerInstanceByName(String serverName) {
	    for (ServerInstance serverInstance : mp.getServers()) {
	        if (serverInstance.getName().equals(serverName)) {
	            return serverInstance;
	        }
	    }
	    return null;
	}

	public ServerInstance getFirstServerInstance() {
		return CollectionUtilities.getFirstItem(mp.getServers());
	}

    public void localize(KnowledgeBase kb) {
        mp.localize(kb);
    }

}
