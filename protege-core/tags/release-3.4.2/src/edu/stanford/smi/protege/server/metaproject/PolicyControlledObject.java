package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;

public interface PolicyControlledObject {

	String getName();
	
	String getDescription();
	
	Set<GroupOperation> getAllowedGroupOperations();
	
	Instance getProtegeInstance();
	
	MetaProject getMetaProject();
	
	void setDescription(String description);
	  
	void setName(String name);
	  
	void setAllowedGroupOperations(Collection<GroupOperation> groupOperations);
	  
	void addAllowedGroupOperations(GroupOperation groupOperation);
	
	Collection<PropertyValue> getPropertyValues();
	
	void setPropertyValues(Collection<PropertyValue> propertyValues);
	
	void addPropertyValue(PropertyValue propertyValue);

}
