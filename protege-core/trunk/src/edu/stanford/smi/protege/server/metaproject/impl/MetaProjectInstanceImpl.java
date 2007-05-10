package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupAndOperation;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class MetaProjectInstanceImpl extends WrappedProtegeInstanceImpl
		implements MetaProjectInstance, Serializable {
	private static final long serialVersionUID = 8666270295698053695L;

	String name;

	String location;

	Set<GroupAndOperation> operations;

	@SuppressWarnings("unchecked")
	protected MetaProjectInstanceImpl(MetaProjectImpl mp, Instance pi) {
		super(mp, pi, ClsEnum.Project);
		location = (String) pi.getOwnSlotValue(mp.getSlot(SlotEnum.location));
		localizeLocation(location);
		name = (String) pi.getOwnSlotValue(mp.getSlot(SlotEnum.name));
		operations = (Set<GroupAndOperation>) getSlotValues(
				SlotEnum.allowedGroupOperation, ClsEnum.GroupOperation);
	}

	public MetaProjectInstanceImpl(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	private static String localizeLocation(String location) {
		if (File.separatorChar != '\\') {
			location = location.replace('\\', File.separatorChar);
		}
		return location;
	}

	public Set<GroupAndOperation> getAllowedGroupOperations() {
		return operations;
	}

	public boolean equals(Object o) {
		if (!(o instanceof MetaProjectInstance)) {
			return false;
		}
		MetaProjectInstance other = (MetaProjectInstance) o;
		return name.equals(other.getName());
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return name;
	}

	//TT: Tim, can you please implement this method? Thanks!
	public void setLocation(String location) {
		// TODO Auto-generated method stub
		
	}
	
	//TT: Tim, can you please implement this method? Thanks!
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

}
