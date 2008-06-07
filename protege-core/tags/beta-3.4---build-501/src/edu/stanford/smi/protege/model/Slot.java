package edu.stanford.smi.protege.model;

import java.util.Collection;

import edu.stanford.smi.protege.event.SlotListener;

/**
 * A top level slot object.  Note that this object does not have "facet overrides" by itself.  If you want to get facet
 * values at a class you need to call {@link Cls#getTemplateFacetValues(Slot, Facet)}
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Slot extends Instance {

    void addDirectSuperslot(Slot slot);

    void addSlotListener(SlotListener listener);

    /**
     * @return Collection of {@link Cls Clses}
     *
     * The behavior of this method is undefined if the slot is not of type Instance
     */
    Collection getAllowedClses();

    /**
     * Returns the classes which restrict the values that this slot can take.  This slot will accept as a value any
     * Cls which has one of these values as its parent (or is itself one of these values).
     * 
     * @return Collection of {@link Cls Clses}
     *
     * The behavior of this method is undefined if the slot is not of type Class
     */
    Collection getAllowedParents();

    /**
     * @return Collection of Strings which are the allowed values of this slot.
     *
     * The behavior of this method is undefined if the slot is not of type Symbol
     */
    Collection getAllowedValues();

    /**
     * @return whether or not this slot allows more than one value
     */
    boolean getAllowsMultipleValues();

    Facet getAssociatedFacet();

    Collection getDefaultValues();

    int getDirectSubslotCount();

    Collection getDirectSubslots();

    int getDirectSuperslotCount();

    Collection getDirectSuperslots();

    boolean hasDirectSuperslot(Slot slot);

    boolean hasSuperslot(Slot slot);

    void moveDirectSubslot(Slot movedCls, Slot afterCls);

    Collection getDocumentation();

    Slot getInverseSlot();

    int getMaximumCardinality();

    Number getMaximumValue();

    int getMinimumCardinality();

    Number getMinimumValue();

    Collection getSubslots();

    Collection getSuperslots();

    /**
     * @deprecated  Use #getDirectDomain()
     */
    Collection getTemplateSlotClses();

    Collection getDirectDomain();

    Collection getDomain();

    /**
     * Returns the "template slot values" for a top level slot.  Usually this is empty.
     * Beware: this method probably doesn't do what you think!  It does NOT return own slot values at a particular frame.
     * To get own slot values at a class see {@link Frame#getOwnSlotValues(Slot)}
     * What it does do is return the values which will become template slot values when this slot is attached to a class.
     */
    Collection getValues();

    ValueType getValueType();

    boolean hasValueAtSomeFrame();

    void removeDirectSuperslot(Slot slot);

    void removeSlotListener(SlotListener listener);

    void setAllowedClses(Collection clses);

    void setAllowedParents(Collection parents);

    void setAllowedValues(Collection values);

    void setAllowsMultipleValues(boolean b);

    void setAssociatedFacet(Facet facet);

    void setDefaultValues(Collection values);

    void setDirectTypeOfSubslots(Cls cls);

    void setDocumentation(String doc);

    void setInverseSlot(Slot slot);

    void setMaximumCardinality(int max);

    void setMaximumValue(Number n);

    void setMinimumCardinality(int min);

    void setMinimumValue(Number n);

    void setValues(Collection values);

    void setValueType(ValueType type);
    
    Slot rename(String name);
}
