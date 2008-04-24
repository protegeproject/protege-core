package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.User;

public class PolicyImpl implements Policy {
	MetaProjectImpl mp;

	public PolicyImpl(MetaProjectImpl mp) {
		this.mp = mp;
	}

	private User fillFields(User user) {
		for (User realUser : mp.getUsers()) {
			if (user.getName().equals(realUser.getName())) {
				return realUser;
			}
		}
		throw new IllegalArgumentException("Unknown user " + user);
	}

	private ProjectInstance fillFields(ProjectInstance project) {
		for (ProjectInstance realProject : mp.getProjects()) {
			if (realProject.getName().equals(project.getName())) {
				return realProject;
			}
		}
		throw new IllegalArgumentException("Unknown project " + project);
	}


	/*
	 * The project is ignored in this implementation.
	 */
	public boolean isOperationAuthorized(User user, 
			Operation op, 
			ProjectInstance project) {
		if (!getKnownOperations().contains(op)) {
			return true;
		}
		user = fillFields(user);
		project = fillFields(project);
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
		user = fillFields(user);
		project = fillFields(project);
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

}
