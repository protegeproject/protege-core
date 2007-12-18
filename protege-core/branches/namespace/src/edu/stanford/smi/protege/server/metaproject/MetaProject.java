package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public interface MetaProject {
	public enum ClsEnum {
		Project, User, Group, Operation, GroupOperation;
	}

	public enum SlotEnum {
		name, password, location, group, member, allowedGroup, allowedOperation, allowedGroupOperation, owner, description, annotationProject;
	}

	Cls getCls(ClsEnum cls);

	Slot getSlot(SlotEnum slot);

	
	Set<ProjectInstance> getProjects();

	ProjectInstance getProject(String name);

	
	Set<User> getUsers();
	
	User getUser(String name);

	
	Set<Operation> getOperations();
	
	Operation getOperation(String name);
	
	
	Set<Group> getGroups();
	
	Group getGroup(String name);

	
	Policy getPolicy();

	KnowledgeBase getKnowledgeBase();

	
	ProjectInstance createProject(String name);

	User createUser(String name, String password);
	
	Group createGroup(String name);
	
	Operation createOperation(String name);
	
	GroupOperation createGroupOperation();

	
	boolean save(Collection errors);

}


