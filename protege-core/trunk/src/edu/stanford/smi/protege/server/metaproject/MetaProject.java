package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

public interface MetaProject {

    Set<ProjectInstance> getProjects();

    ProjectInstance getProject(String name);

    ProjectInstance createProject(String name);


    Set<User> getUsers();

    User getUser(String name);

    User createUser(String name, String password);


    Set<Operation> getOperations();

    Operation getOperation(String name);

    Operation createOperation(String name);


    Set<Group> getGroups();

    Group getGroup(String name);

    Group createGroup(String name);


    Set<GroupOperation> getGroupOperations();

    GroupOperation createGroupOperation();

    Policy getPolicy();

    PropertyValue createPropertyValue();

    @SuppressWarnings("unchecked")
    boolean save(Collection errors);

    void dispose();

}


