package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;

public interface PolicyControlledObject {

    String getName();

    void setName(String name);


    String getDescription();

    void setDescription(String description);


    boolean isPolicyModifiableByClient();

    void setPolicyModifiableByClient(boolean modifiable);


    Instance getProtegeInstance();

    MetaProject getMetaProject();

    /*
     * Allowed group operation methods
     */

    Set<GroupOperation> getAllowedGroupOperations();

    void setAllowedGroupOperations(Collection<GroupOperation> groupOperations);

    void addAllowedGroupOperations(GroupOperation groupOperation);

    /*
     * Name - value property methods
     */

    Collection<PropertyValue> getPropertyValues();

    Collection<String> getPropertyValuesAsString(String prop);

    String getPropertyValue(String prop);

    void setPropertyValues(Collection<PropertyValue> propertyValues);

    void addPropertyValue(PropertyValue propertyValue);

    void addPropertyValue(String prop, String value);

    void removePropertyValue(PropertyValue propertyValue);

    void removePropertyValue(String prop, String value);

    /*
     * inCollection methods
     */

    Set<PolicyControlledObjectCollection> getInCollection();

    void addInCollection(PolicyControlledObjectCollection collection);

    void removeInCollection(PolicyControlledObjectCollection collection);

    void setInCollection(Set<PolicyControlledObjectCollection> collections);

}
