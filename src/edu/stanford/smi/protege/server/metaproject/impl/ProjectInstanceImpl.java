package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.CollectionUtilities;

public class ProjectInstanceImpl extends PolicyControlledObjectImpl implements ProjectInstance, Serializable {
    private static final long serialVersionUID = 8666270295698053695L;

    String location;

    @SuppressWarnings("unchecked")
    protected ProjectInstanceImpl(MetaProjectImpl mp, Instance pi) {
        super(mp, pi, MetaProjectImpl.ClsEnum.Project);
        if (pi != null) {
            location = (String) pi.getOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.location));
        }
        localizeLocation(location);
    }

    public String getLocation() {
        return location;
    }

    private static String localizeLocation(String location) {
        if (location == null) {	return null; }
        if (File.separatorChar != '\\') {
            location = location.replace('\\', File.separatorChar);
        }
        return location;
    }

    public ProjectInstance getAnnotationProject() {
        return (ProjectInstance) getSlotValue(MetaProjectImpl.SlotEnum.annotationProject, MetaProjectImpl.ClsEnum.Project);
    }

    public void setLocation(String location) {
        this.location = location;
        setSlotValue(MetaProjectImpl.SlotEnum.location, location);
    }


    public User getOwner() {
        return (User) getSlotValue(MetaProjectImpl.SlotEnum.owner, MetaProjectImpl.ClsEnum.User);
    }

    @SuppressWarnings("unchecked")
    public Set<User> getOwners() {
        return getSlotValues(MetaProjectImpl.SlotEnum.owner, MetaProjectImpl.ClsEnum.User);
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

    public void setOwners(Set<User> owners) {
        setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.owner, owners);
    }

}
