package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.ArrayList;
import java.util.Collection;
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
        PropertyValue pv = getMetaProject().createPropertyValue();
        pv.setPropertyName(prop);
        pv.setPropertyValue(value);
        removePropertyValue(pv); //TODO: check if this works
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
