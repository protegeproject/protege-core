package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;

public interface Operation {

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	Collection<PropertyValue> getPropertyValues();

	void setPropertyValues(Collection<PropertyValue> propertyValues);

	void addPropertyValue(PropertyValue propertyValue);

}
