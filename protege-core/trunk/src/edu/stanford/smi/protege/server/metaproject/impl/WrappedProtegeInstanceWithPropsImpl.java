package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.PropertyValue;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;

public abstract class WrappedProtegeInstanceWithPropsImpl extends WrappedProtegeInstanceImpl {

    private static final long serialVersionUID = -6687720331317552706L;

    public WrappedProtegeInstanceWithPropsImpl(MetaProjectImpl mp, Instance i, ClsEnum cls) {
        super(mp, i, cls);
    }

    @SuppressWarnings("unchecked")
    public Set<PropertyValue> getPropertyValues() {
        return getSlotValues(MetaProjectImpl.SlotEnum.properties, MetaProjectImpl.ClsEnum.PropertyValue);
    }

    public void setPropertyValues(Collection<PropertyValue> propertyValues) {
        setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.properties, propertyValues);
    }

    /**
     * Will replace any existing PropertyValue sharing the same propertyName with the PropertyValue passed in.
     * @param propertyValue
     */
    public void setPropertyValue(String propertyName, String propertyValue) {
        final Set<PropertyValue> propertyValues = getPropertyValues();
        final Set<PropertyValue> propertyValuesToSet = new HashSet<PropertyValue>();
        for (PropertyValue value : propertyValues) {
            if (!value.getPropertyName().equals(propertyValue)){
                propertyValuesToSet.add(value);
            }
        }
        PropertyValue propValue = getMetaProject().createPropertyValue();
        propValue.setPropertyName(propertyName);
        propValue.setPropertyValue(propertyValue);
        propertyValuesToSet.add(propValue);

        setPropertyValues(propertyValuesToSet);
    }

    public void addPropertyValue(PropertyValue propertyValue) {
        addSlotValue(MetaProjectImpl.SlotEnum.properties, propertyValue);
    }

    public void addPropertyValue(String prop, String value) {
        PropertyValue pv = getMetaProject().createPropertyValue();
        pv.setPropertyName(prop);
        pv.setPropertyValue(value);
        addPropertyValue(pv);
    }

    public void removePropertyValue(PropertyValue propertyValue) {
        removeSlotValue(MetaProjectImpl.SlotEnum.properties, propertyValue);
    }

    public void removePropertyValue(String prop, String value) {
        Collection<PropertyValue> propVals = getPropertyValues();
        for (PropertyValue propVal : propVals) {
            String propertyName = propVal.getPropertyName();
            String propertyValue = propVal.getPropertyValue();
            if (
                ((prop == null && propertyName == null) || (prop != null && prop.equals(propertyName))) &&
                ((value == null && propertyValue == null) || (value != null &&  value.equals(propertyValue)))
               ) {
                    ((WrappedProtegeInstanceImpl)propVal).getProtegeInstance().delete();
            }
        }
    }

    public Collection<String> getPropertyValuesAsString(String prop) {
        Collection<String> values = new ArrayList<String>();
        for (PropertyValue pv : getPropertyValues()) {
            if (prop.equals(pv.getPropertyName())) {
                values.add(pv.getPropertyValue());
            }
        }
        return values;
    }

    public String getPropertyValue(String prop) {
        for (PropertyValue pv : getPropertyValues()) {
            if (prop.equals(pv.getPropertyName())) {
                return pv.getPropertyValue();
            }
        }
        return null;
    }


}
