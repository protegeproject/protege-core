package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
import edu.stanford.smi.protege.util.CollectionUtilities;

public class ProjectInstanceImpl extends WrappedProtegeInstanceImpl implements ProjectInstance, Serializable {
	private static final long serialVersionUID = 8666270295698053695L;

	String name;

	String location;

	@SuppressWarnings("unchecked")
	protected ProjectInstanceImpl(MetaProjectImpl mp, Instance pi) {
		super(mp, pi, MetaProjectImpl.ClsEnum.Project);
		location = (String) pi.getOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.location));
		localizeLocation(location);
		name = (String) pi.getOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.name));
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	private static String localizeLocation(String location) {
		if (location == null) {
			return null;
		}
		
		if (File.separatorChar != '\\') {
			location = location.replace('\\', File.separatorChar);
		}
		return location;
	}

	@SuppressWarnings("unchecked")
    public Set<GroupOperation> getAllowedGroupOperations() {
		return getSlotValues(MetaProjectImpl.SlotEnum.allowedGroupOperation, MetaProjectImpl.ClsEnum.GroupOperation);
	}

	public ProjectInstance getAnnotationProject() {
		return (ProjectInstance) getSlotValue(MetaProjectImpl.SlotEnum.annotationProject, MetaProjectImpl.ClsEnum.Project);
	}
	
	@Override
    public boolean equals(Object o) {
		if (!(o instanceof ProjectInstance)) {
			return false;
		}
		ProjectInstance other = (ProjectInstance) o;
		return name.equals(other.getName());
	}

	@Override
    public int hashCode() {
		return name.hashCode();
	}

	@Override
    public String toString() {
		return name;
	}

	public void setLocation(String location) {
		this.location = location;
		setSlotValue(MetaProjectImpl.SlotEnum.location, location);
	}

	public void setName(String name) {
		this.name = name;
		setSlotValue(MetaProjectImpl.SlotEnum.name, name);
	}

	public User getOwner() {
		return (User) getSlotValue(MetaProjectImpl.SlotEnum.owner, MetaProjectImpl.ClsEnum.User);
	}

	public void setAllowedGroupOperations(Collection<GroupOperation> groupOperations) {
		setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.allowedGroupOperation, groupOperations);
	}

	public void addAllowedGroupOperations(GroupOperation groupOperation) {
		addSlotValue(MetaProjectImpl.SlotEnum.allowedGroupOperation, groupOperation);		
	}

	public void setAnnotationProject(ProjectInstance annotationProject) {
		
		//make sure that the annotationProject slot is there
		Project prj = getProtegeInstance().getProject();
		Slot annotationProjectSlot = prj.getKnowledgeBase().getSlot(MetaProjectImpl.SlotEnum.annotationProject.name());
		if (annotationProjectSlot == null) {
			annotationProjectSlot = prj.getKnowledgeBase().createSlot(MetaProjectImpl.SlotEnum.annotationProject.name());
			annotationProjectSlot.setValueType(ValueType.INSTANCE);
			annotationProjectSlot.setAllowedClses(CollectionUtilities.createCollection(prj.getKnowledgeBase().getCls(MetaProjectImpl.ClsEnum.Project.name())));
		}
		
		setSlotValue(MetaProjectImpl.SlotEnum.annotationProject, annotationProject);
	}

	public void setOwner(User owner) {
		setSlotValue(MetaProjectImpl.SlotEnum.owner, owner);
	}
	
	public String getDescription() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.description));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.name + " slot should take on string values");
		}
		return (String) value;		
	}
	
	public void setDescription(String description) {
		setSlotValue(MetaProjectImpl.SlotEnum.description, description);
	}

}
