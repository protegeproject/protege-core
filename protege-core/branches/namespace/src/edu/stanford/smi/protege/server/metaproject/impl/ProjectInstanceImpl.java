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
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;
import edu.stanford.smi.protege.util.CollectionUtilities;

public class ProjectInstanceImpl extends WrappedProtegeInstanceImpl implements ProjectInstance, Serializable {
	private static final long serialVersionUID = 8666270295698053695L;

	String name;

	String location;

	@SuppressWarnings("unchecked")
	protected ProjectInstanceImpl(MetaProjectImpl mp, Instance pi) {
		super(mp, pi, ClsEnum.Project);
		location = (String) pi.getOwnSlotValue(mp.getSlot(SlotEnum.location));
		localizeLocation(location);
		name = (String) pi.getOwnSlotValue(mp.getSlot(SlotEnum.name));
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
		return getSlotValues(SlotEnum.allowedGroupOperation, ClsEnum.GroupOperation);
	}

	public ProjectInstance getAnnotationProject() {
		return (ProjectInstance) getSlotValue(SlotEnum.annotationProject, ClsEnum.Project);
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
		setSlotValue(SlotEnum.location, location);
	}

	public void setName(String name) {
		this.name = name;
		setSlotValue(SlotEnum.name, name);
	}

	public User getOwner() {
		return (User) getSlotValue(SlotEnum.owner, ClsEnum.User);
	}

	public void setAllowedGroupOperations(Collection<GroupOperation> groupOperations) {
		setSlotValuesAsProtegeInstances(SlotEnum.allowedGroupOperation, groupOperations);
	}

	public void addAllowedGroupOperations(GroupOperation groupOperation) {
		addSlotValue(SlotEnum.allowedGroupOperation, groupOperation);		
	}

	public void setAnnotationProject(ProjectInstance annotationProject) {
		
		//make sure that the annotationProject slot is there
		Project prj = getProtegeInstance().getProject();
		Slot annotationProjectSlot = prj.getKnowledgeBase().getSlot(SlotEnum.annotationProject.name());
		if (annotationProjectSlot == null) {
			annotationProjectSlot = prj.getKnowledgeBase().createSlot(SlotEnum.annotationProject.name());
			annotationProjectSlot.setValueType(ValueType.INSTANCE);
			annotationProjectSlot.setAllowedClses(CollectionUtilities.createCollection(prj.getKnowledgeBase().getCls(ClsEnum.Project.name())));
		}
		
		setSlotValue(SlotEnum.annotationProject, annotationProject);
	}

	public void setOwner(User owner) {
		setSlotValue(SlotEnum.owner, owner);
	}
	
	public String getDescription() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(SlotEnum.description));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + SlotEnum.name + " slot should take on string values");
		}
		return (String) value;		
	}
	
	public void setDescription(String description) {
		setSlotValue(SlotEnum.description, description);
	}

}
