package edu.stanford.smi.protege.server.metaproject;

public interface PropertyValue {

	String getPropertyName();
	
	String getPropertyValue();
	
	void setPropertyName(String name);
	
	void setPropertyValue(String value);
	
}
