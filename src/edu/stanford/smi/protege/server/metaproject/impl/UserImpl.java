package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
import edu.stanford.smi.protege.util.DigestAndSalt;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

public class UserImpl extends WrappedProtegeInstanceImpl implements User, Serializable {
	private static final long serialVersionUID = -4416984896523630762L;
	private static final Logger log = Log.getLogger(UserImpl.class);

	private static Random random;
	private String name;

	@SuppressWarnings("unchecked")
	protected UserImpl(MetaProjectImpl mp, Instance ui) {
		super(mp, ui, MetaProjectImpl.ClsEnum.User);
		name = (String) ui.getOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.name));
	}

	public String getDescription() {
    	Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.description));
    	if (!(value instanceof String)) {
    		throw new OntologyException("The " + MetaProjectImpl.SlotEnum.name + " slot should take on string values");
    	}
    	return (String) value;		
    }

    @SuppressWarnings("unchecked")
    public Set<Group> getGroups() {
    	return getSlotValues(MetaProjectImpl.SlotEnum.group, MetaProjectImpl.ClsEnum.Group);
    }
    
    public Date getLastAccess() {
        Object unparsed = (String) getSlotValue(SlotEnum.lastAccess, null);
        if (unparsed != null && !(unparsed instanceof String)) {
            throw new OntologyException("The " + MetaProjectImpl.SlotEnum.lastAccess + " slot should take on string values");
        }
        return parseDate((String) unparsed);
    }
    
    public Date getLastLogin() {
        Object unparsed = (String) getSlotValue(SlotEnum.lastLogin, null);
        if (unparsed != null && !(unparsed instanceof String)) {
            throw new OntologyException("The " + MetaProjectImpl.SlotEnum.lastLogin + " slot should take on string values");
        }
        return parseDate((String) unparsed);
    }

    public String getName() {
		return name;
	}

	public void setDescription(String description) {
    	setSlotValue(MetaProjectImpl.SlotEnum.description, description);
    }
	
	public void setLastAccess(Date time) {
	    setSlotValue(SlotEnum.lastAccess, new Long(time.getTime()).toString());
	}
	
	public void setLastLogin(Date time) {
        setSlotValue(SlotEnum.lastLogin, new Long(time.getTime()).toString());
	}

    public void setName(String name) {
		this.name = name;
		setSlotValue(MetaProjectImpl.SlotEnum.name, name);
	}

	public void setPassword(String password) {
	    if (password == null) {
	        password = "";
	    }
	    if (useDigest()) {
	        DigestAndSalt encrypted = StringUtilities.makeDigest(password);
	        setSlotValue(SlotEnum.salt, encrypted.getSalt());
	        setSlotValue(SlotEnum.digest, encrypted.getDigest());
	    }
	    else {
	        setSlotValue(MetaProjectImpl.SlotEnum.password, password);
	    }
	}
	
	public boolean verifyPassword(String password) {
	    if (password == null) {
	        password = "";
	    }
	    if (useDigest()) {
	        DigestAndSalt encodedPassword = StringUtilities.makeDigest(password, (String) getSlotValue(SlotEnum.salt, null));
	        return encodedPassword.getDigest().equals(getSlotValue(SlotEnum.digest, null));
	    }
	    else {
	        String realPassword  = (String) getSlotValue(MetaProjectImpl.SlotEnum.password, null);
	        if (realPassword == null) {
	            realPassword = "";
	        }
	        return realPassword.equals(password);
	    }
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
    	return name == null ? 42 : name.hashCode();
    }

    @Override
    public String toString() {
    	return name;
    }

    private static Date parseDate(String dateAsLong) {
	    if (dateAsLong == null) {
	        return null;
	    }
	    try {
	        return new Date(Long.parseLong(dateAsLong));
	    }
	    catch (NumberFormatException nfe) {
	        return null;
	    }
	}
    
    private boolean useDigest() {
        return mp.getKnowledgeBase().getSlot(SlotEnum.digest.toString()) != null &&
                    mp.getKnowledgeBase().getSlot(SlotEnum.salt.toString()) != null;
    }
}
