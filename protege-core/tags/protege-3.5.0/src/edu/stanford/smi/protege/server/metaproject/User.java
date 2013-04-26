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

	String getSalt();

	String getDigestedPassword();

	void setDigestedPassword(String hashedPassword, String salt);

	Collection<PropertyValue> getPropertyValues();

	Collection<String> getPropertyValuesAsString(String prop);

    String getPropertyValue(String prop);

    void setPropertyValue(String propertyName, String propertyValue);

	void setPropertyValues(Collection<PropertyValue> propertyValues);

	void addPropertyValue(PropertyValue propertyValue);

	void addPropertyValue(String prop, String value);

	void removePropertyValue(PropertyValue propertyValue);

	void removePropertyValue(String prop, String value);

}
