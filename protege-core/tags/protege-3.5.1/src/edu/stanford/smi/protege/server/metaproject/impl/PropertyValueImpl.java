package edu.stanford.smi.protege.server.metaproject.impl;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.PropertyValue;

public class PropertyValueImpl extends WrappedProtegeInstanceImpl implements
		PropertyValue {

	private static final long serialVersionUID = 7265423663403555058L;

	protected PropertyValueImpl(MetaProjectImpl mp, Instance pi) {
		super(mp, pi, MetaProjectImpl.ClsEnum.PropertyValue);
	}

	public String getPropertyName() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.propertyName));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.propertyName + " slot should take on string values");
		}
		return (String) value;
	}

	public String getPropertyValue() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.propertyValue));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.propertyValue + " slot should take on string values");
		}
		return (String) value;
	}

	public void setPropertyName(String name) {
		setSlotValue(MetaProjectImpl.SlotEnum.propertyName, name);
	}

	public void setPropertyValue(String value) {
		setSlotValue(MetaProjectImpl.SlotEnum.propertyValue, value);
	}

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof PropertyValue) || o == null) { return false; }
	    PropertyValue pv = (PropertyValue) o;
	    return getPropertyName().equals(pv.getPropertyName()) && getPropertyValue().equals(pv.getPropertyValue());
	}

	@Override
	public int hashCode() {
	    String propertyName = getPropertyName();
	    String propertyValue = getPropertyValue();
        return (propertyName == null ? 43 : propertyName.hashCode()) + (propertyValue == null ? 51 : propertyValue.hashCode());
	}

}
