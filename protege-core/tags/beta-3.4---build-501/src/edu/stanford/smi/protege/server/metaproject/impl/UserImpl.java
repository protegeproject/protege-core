package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class UserImpl extends WrappedProtegeInstanceImpl implements User, Serializable {
	private static final long serialVersionUID = -4416984896523630762L;

	private String name;
	private String password;

	@SuppressWarnings("unchecked")
	protected UserImpl(MetaProjectImpl mp, Instance ui) {
		super(mp, ui, ClsEnum.User);
		name = (String) ui.getOwnSlotValue(mp.getSlot(SlotEnum.name));
		password = (String) ui.getOwnSlotValue(mp.getSlot(SlotEnum.password));
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	@SuppressWarnings("unchecked")
    public Set<Group> getGroups() {
		return getSlotValues(SlotEnum.group, ClsEnum.Group);
	}

	@Override
    public boolean equals(Object o) { 
		if (!(o instanceof User)) {
			return false;
		}
		User other = (User) o;
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

	public void setName(String name) {
		this.name = name;
		setSlotValue(SlotEnum.name, name);
	}

	public void setPassword(String password) {
		setSlotValue(SlotEnum.password, password);
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
