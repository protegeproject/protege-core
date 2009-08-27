package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.ServerInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;

public class MetaProjectImpl implements MetaProject, Localizable, Serializable {
    
    public static enum ClsEnum {
    	Project, User, Group, Operation, GroupOperation, PolicyControlledObject, Server;
    }


    public static enum SlotEnum {
        name, password, location, 
        lastLogin, lastRead, lastModification,
        group, member, allowedGroup, allowedOperation, 
        allowedGroupOperation, owner, description, annotationProject, hostName;
    }


    private static final long serialVersionUID = -7866511885264147967L;
    
    private transient KnowledgeBase kb;
	private transient Policy policy;
	
	public MetaProjectImpl(URI metaProjectURI) {
	    this(metaProjectURI, false);
	}

	public MetaProjectImpl(URI metaprojectURI, boolean isMultiUserServer) {
		Collection errors = new ArrayList();
		Project project = Project.loadProjectFromURI(metaprojectURI, errors, isMultiUserServer);
		if (!errors.isEmpty()) {
			throw new RuntimeException(errors.iterator().next().toString());
		}
		kb = project.getKnowledgeBase();
	}
	
	public MetaProjectImpl(Project p) {
	    kb = p.getKnowledgeBase();
	}


	public Cls getCls(MetaProjectImpl.ClsEnum cls) throws OntologyException {
		Cls frameCls = kb.getCls(cls.toString());
		if (frameCls == null) {
			throw new OntologyException("Metaproject Ontology should contain a class " + cls);
		}
		return frameCls;
	}

	public Slot getSlot(MetaProjectImpl.SlotEnum slot) throws OntologyException {
		Slot frameSlot = kb.getSlot(slot.toString());
		if (frameSlot == null) {
			throw new OntologyException("Metaproject Ontology should contain a slot " + slot);
		}
		return frameSlot;
	}

	protected WrappedProtegeInstanceImpl wrapInstance(MetaProjectImpl.ClsEnum cls, Instance i) {
		if (!i.hasType(getCls(cls))) {
			throw new IllegalArgumentException("" + i + " should be a " + cls + " instance");
		}
		switch (cls) {
		case GroupOperation:
			return new GroupOperationImpl(this, i);
		case Group:
			return new GroupImpl(this, i);
		case Project:
			return new ProjectInstanceImpl(this, i);
		case Operation:
			return new OperationImpl(this, i);
		case User:
			return new UserImpl(this, i);
		case Server:
			return new ServerInstanceImpl(this, i);
		case PolicyControlledObject:
			return new PolicyControlledObjectImpl(this, i);
		default:
			throw new UnsupportedOperationException("Unexpected cls " + cls);
		}
	}

	@SuppressWarnings("unchecked")
	protected Set getWrappedInstances(MetaProjectImpl.ClsEnum cls) {
		Set instances = new HashSet();
		for (Instance i : kb.getInstances(getCls(cls))) {
			instances.add(wrapInstance(cls, i));
		}
		return instances;
	}

	@SuppressWarnings("unchecked")
	public Set<ProjectInstance> getProjects() {
		return getWrappedInstances(MetaProjectImpl.ClsEnum.Project);
	}

	@SuppressWarnings("unchecked")
    public ProjectInstance getProject(String name) {
		Collection frames = kb.getFramesWithValue(getSlot(MetaProjectImpl.SlotEnum.name), null, false, name);
		if (frames == null || frames.isEmpty()) {
			return null;
		}
		Frame frame = (Frame) frames.iterator().next();
		if (!(frame instanceof Frame)) {
			return null;
		}
		return new ProjectInstanceImpl(this, (Instance) frame);
	}

	@SuppressWarnings("unchecked")
	public Set<User> getUsers() {
		return getWrappedInstances(MetaProjectImpl.ClsEnum.User);
	}

	public User getUser(String name) {
		Collection frames = kb.getFramesWithValue(getSlot(MetaProjectImpl.SlotEnum.name), null, false, name);
		if (frames == null || frames.isEmpty()) {
			return null;
		}
		Frame frame = (Frame) frames.iterator().next();
		if (!(frame instanceof Frame)) {
			return null;
		}
		return new UserImpl(this, (Instance) frame);
	}
	
	public Operation getOperation(String name) {
		Collection frames = kb.getFramesWithValue(getSlot(MetaProjectImpl.SlotEnum.name), null, false, name);
		if (frames == null || frames.isEmpty()) {
			return null;
		}
		Frame frame = (Frame) frames.iterator().next();
		if (!(frame instanceof Frame)) {
			return null;
		}
		return new OperationImpl(this, (Instance) frame);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Operation> getOperations() {
		return getWrappedInstances(MetaProjectImpl.ClsEnum.Operation);
	}

	
	public Group getGroup(String name) {
		Collection frames = kb.getFramesWithValue(getSlot(MetaProjectImpl.SlotEnum.name), null, false, name);
		if (frames == null || frames.isEmpty()) {
			return null;
		}
		Frame frame = (Frame) frames.iterator().next();
		if (!(frame instanceof Frame)) {
			return null;
		}
		return new GroupImpl(this, (Instance) frame);
	}


	@SuppressWarnings("unchecked")
	public Set<Group> getGroups() {
		return getWrappedInstances(MetaProjectImpl.ClsEnum.Group);
	}
	
	@SuppressWarnings("unchecked")
	public Set<GroupOperation> getGroupOperations() {
	    return getWrappedInstances(MetaProjectImpl.ClsEnum.GroupOperation);
	}
	
	@SuppressWarnings("unchecked")
	public Set<ServerInstance> getServers() {
		return getWrappedInstances(MetaProjectImpl.ClsEnum.Server);
	}
	
	
	public Policy getPolicy() {
		if (policy == null) {
			policy = new  PolicyImpl(this);
		}
		return policy;
	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	public boolean save(Collection errors) {
		ArrayList saveErrors = new ArrayList();

		try {			
			kb.getProject().save(saveErrors);

			if (saveErrors.size() > 0) {
				Log.getLogger().warning("Server: Errors at saving metaproject. Error messages: " + saveErrors);
				errors.addAll(saveErrors);
				return false;
			}

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Server: Errors at saving metaproject. Error message: " + e.getMessage(), e);
			errors.add(e);
			return false;
		}

		Log.getLogger().info("SERVER: Saved metaproject.");
		return true;
	}

	public ProjectInstance createProject(String name) {
		Instance pi = kb.createInstance(name, getCls(MetaProjectImpl.ClsEnum.Project));
		
		ProjectInstance project = new ProjectInstanceImpl(this, pi);
		
		project.setName(name);
		return project;
	}

	public User createUser(String name, String password) {
		Instance ui = kb.createInstance(null, getCls(MetaProjectImpl.ClsEnum.User));

		User userInstance = new UserImpl(this, ui);
		userInstance.setName(name);
		userInstance.setPassword(password);

		return userInstance;
	}
	
	public Group createGroup(String name) {
		Instance pi = kb.createInstance(name, getCls(MetaProjectImpl.ClsEnum.Group));
		
		Group group = new GroupImpl(this, pi);
		
		group.setName(name);
		return group;
	}
	

	public Operation createOperation(String name) {
		Instance pi = kb.createInstance(name, getCls(MetaProjectImpl.ClsEnum.Operation));
		
		Operation op = new OperationImpl(this, pi);
		
		op.setName(name);
		return op;
	}
	
	
	public GroupOperation createGroupOperation() {
		Instance pi = kb.createInstance(null, getCls(MetaProjectImpl.ClsEnum.GroupOperation));
		
		GroupOperation groupOp = new GroupOperationImpl(this, pi);
		
		return groupOp;
	}


    public void localize(KnowledgeBase kb) {
        this.kb = kb;
        policy = null;
    }

}
