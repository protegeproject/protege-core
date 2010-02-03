package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

public interface User {

	String getName();

	Set<Group> getGroups();

	String getDescription();

	Date getLastAccess();

	Date getLastLogin();

	void setDescription(String description);

	void setName(String name);

	void setPassword(String password);

	void setLastAccess(Date time);

	void setLastLogin(Date time);

	boolean verifyPassword(String password);

	void setEmail(String email);

	String getEmail();

	Collection<PropertyValue> getPropertyValues();

	void setPropertyValues(Collection<PropertyValue> propertyValues);

	void addPropertyValue(PropertyValue propertyValue);

}
