package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.event.*;

/**
 * A frame which has one or more superclasses, may have subclasses, and which exhibits
 * inheritance behavior (such as inheritance of slots). The specific inheritance behavior
 * (such as propagation of slots and facets) are left unspecified by the
 * interface.
 * 
 * Note that Class is uniformly shortened to Cls in order to avoid conflicts with java.lang.Class and Object.getClass()
 *
 * See {@link Frame} for a discussion of "own" and "template" slots.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Cls extends Instance {

    void addClsListener(ClsListener listener);

    void addDirectSuperclass(Cls cls);

    void addDirectTemplateSlot(Slot slot);

    void addTemplateFacetValue(Slot slot, Facet facet, Object value);

    void addTemplateSlotValue(Slot slot, Object value);

    Instance createDirectInstance(String name);

    Collection getConcreteSubclasses();

    /**
     * @deprecated
     */
    Slot getBrowserSlot();

    BrowserSlotPattern getBrowserSlotPattern();

    /**
     * @deprecated
     */
    Slot getDirectBrowserSlot();

    BrowserSlotPattern getDirectBrowserSlotPattern();

    /**
     * @deprecated
     */
    Slot getInheritedBrowserSlot();

    BrowserSlotPattern getInheritedBrowserSlotPattern();

    int getDirectInstanceCount();

    /** 
     * @return a Collection of {@link Instance} 
     * 
     * The values returned are instances of this class but not instances of its subclasses.  If you want instances of
     * both a class and its subclasses see {@link #getInstances()}
     */
    Collection<Instance> getDirectInstances();

    int getDirectSubclassCount();

    Collection getDirectSubclasses();

    int getDirectSuperclassCount();

    Collection<Cls> getDirectSuperclasses();

    /** see {@link Frame} for a description of the returned objects */
    List getDirectTemplateFacetValues(Slot slot, Facet facet);

    Collection getOverriddenTemplateFacets(Slot slot);

    Collection getDirectlyOverriddenTemplateFacets(Slot slot);

    Collection<Slot> getDirectTemplateSlots();

    /** see {@link Frame} for a description of the returned objects */
    List getDirectTemplateSlotValues(Slot slot);

    int getInstanceCount();

    /** 
     * @return a Collection of {@link Instance} 
     * 
     * The values returned are instances of this class and all of its subclasses.  If you want instances of
     * just this class see {@link #getDirectInstances()}
     */
    Collection<Instance> getInstances();

    /** 
     * Get all descendent classes for this class.  This includes children, grandchildren, etc.  If you want only
     * the children who directly inherit from this class then see {@link #getDirectSubclasses}
     * 
     * @return a Collection of {@link Cls}
     */
    Collection getSubclasses();

    /** 
     * Get all ancestors classes for this class.  This includes children, grandchildren, etc.  If you want only
     * the children who directly inherit from this class then see {@link #getDirectSubclasses}
     * 
     * @return a Collection of {@link Cls}
     */
    Collection getSuperclasses();

    Collection<Facet> getTemplateFacets(Slot slot);

    /** see {@link Frame} for a description of the return type */
    Object getTemplateFacetValue(Slot slot, Facet facet);

    /** see {@link Frame} for a description of the return type */
    Collection getTemplateFacetValues(Slot slot, Facet facet);

    /** 
     * @return a Collection of {@link Cls} objects if the slot is of type INSTANCE.  Undefined otherwise 
     * 
     */
    Collection getTemplateSlotAllowedClses(Slot slot);

    /** 
     * @return a Collection of {@link Cls} objects if the slot is of type CLASS.  Undefined otherwise
     */
    Collection getTemplateSlotAllowedParents(Slot slot);

    /** 
     * @return a Collection of Strings if the slot is of type SYMBOL.  Undefined otherwise 
     */
    Collection getTemplateSlotAllowedValues(Slot slot);

    boolean getTemplateSlotAllowsMultipleValues(Slot slot);

    /** 
     * @return a Collection of java.lang.Objects.  See {@link Frame} for a description of the object types. 
     */
    Collection getTemplateSlotDefaultValues(Slot slot);

    /** 
     * Get free text documentation for this template slot.  Documentation is defined to return a collection only because
     * it is so defined by the OKBC specification.  Only the first value is used by Protege.
     * 
     * @return a Collection of Strings.  
     */
    Collection getTemplateSlotDocumentation(Slot slot);

    /** returns 0 if there is no maximum cardinality */
    int getTemplateSlotMaximumCardinality(Slot slot);

    Number getTemplateSlotMaximumValue(Slot slot);

    int getTemplateSlotMinimumCardinality(Slot slot);

    Number getTemplateSlotMinimumValue(Slot slot);

    Collection<Slot> getTemplateSlots();

    /** see {@link Frame} for a description of the return type */
    Object getTemplateSlotValue(Slot slot);

    /** see {@link Frame} for a description of the return type */
    Collection getTemplateSlotValues(Slot slot);

    ValueType getTemplateSlotValueType(Slot slot);

    int getVisibleDirectSubclassCount();

    Collection getVisibleDirectSubclasses();

    Collection getVisibleTemplateSlots();

    /**
     * returns true if the facet is overridden on this slot relative to the value
     * at this classes direct parent.
     */
    boolean hasDirectlyOverriddenTemplateFacet(Slot slot, Facet facet);

    /**
     * returns true if any facet of this slot at this class is
     * directly overridden.  "directly overridden" means that the slot is
     * overridden at this class relative to the direct parent class or classes.
     */
    boolean hasDirectlyOverriddenTemplateSlot(Slot slot);

    boolean hasDirectSuperclass(Cls cls);

    boolean hasDirectTemplateSlot(Slot slot);

    boolean hasInheritedTemplateSlot(Slot slot);

    /**
     * returns true if the facet is overridden on this slot relative to the value of
     * the facets associated slot on the top-level slot.
     */
    boolean hasOverriddenTemplateFacet(Slot slot, Facet facet);

    /**
     * returns true if any facet of this slot at this class is
     * overridden.  "overridden" refers to an override relative to the
     * top-level slot.
     */
    boolean hasOverriddenTemplateSlot(Slot slot);

    boolean hasSuperclass(Cls cls);

    boolean hasTemplateFacet(Slot slot, Facet facet);

    boolean hasTemplateSlot(Slot slot);

    boolean isAbstract();

    boolean isClsMetaCls();

    boolean isConcrete();

    boolean isDefaultClsMetaCls();

    boolean isDefaultFacetMetaCls();

    boolean isDefaultSlotMetaCls();

    boolean isFacetMetaCls();

    boolean isMetaCls();

    boolean isRoot();

    boolean isSlotMetaCls();

    /**
     * Reorder the subclasses, moving <code>movedSubclass</code> so that it appears
     * after <code>afterCls</code> .  If aftetCls is null then the movedSubclass appears
     * first
     */
    void moveDirectSubclass(Cls movedSubclass, Cls afterCls);

    void moveDirectTemplateSlot(Slot slot, int toIndex);

    void removeClsListener(ClsListener listener);

    void removeDirectSuperclass(Cls cls);

    void removeDirectTemplateSlot(Slot slot);

    void removeTemplateFacetOverrides(Slot slot);

    void setAbstract(boolean v);

    void setDirectBrowserSlot(Slot slot);

    void setDirectBrowserSlotPattern(BrowserSlotPattern pattern);

    void setDirectTypeOfSubclasses(Cls metaCls);

    /** see {@link Frame} for a description of the value type */
    void setTemplateFacetValue(Slot slot, Facet facet, Object value);

    /** see {@link Frame} for a description of the value type */
    void setTemplateFacetValues(Slot slot, Facet facet, Collection c);

    void setTemplateSlotAllowedClses(Slot slot, Collection clses);

    void setTemplateSlotAllowedParents(Slot slot, Collection clses);

    void setTemplateSlotAllowedValues(Slot slot, Collection values);

    void setTemplateSlotAllowsMultipleValues(Slot slot, boolean b);

    /** see {@link Frame} for a description of the value type */
    void setTemplateSlotDefaultValues(Slot slot, Collection values);

    void setTemplateSlotDocumentation(Slot slot, String documentation);

    void setTemplateSlotDocumentation(Slot slot, Collection documentation);

    /** sets the maximum cardinality.  max = 0 => no maximum */
    void setTemplateSlotMaximumCardinality(Slot slot, int max);

    /** sets the maximum slot value.  max = null => no maximum */
    void setTemplateSlotMaximumValue(Slot slot, Number max);

    /** sets the minimum cardinality */
    void setTemplateSlotMinimumCardinality(Slot slot, int min);

    void setTemplateSlotMinimumValue(Slot slot, Number min);

    /** see {@link Frame} for a description of the value type */
    void setTemplateSlotValue(Slot slot, Object value);

    /** see {@link Frame} for a description of the value type */
    void setTemplateSlotValues(Slot slot, Collection c);

    void setTemplateSlotValueType(Slot slot, ValueType valueType);
    
    Cls rename(String name);
}
