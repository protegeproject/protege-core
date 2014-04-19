package edu.stanford.smi.protege.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import edu.stanford.smi.protege.event.FrameListener;

/**
 * A container for slot and facet values. This definition of a Frame is roughly consistent with the OKBC notion.
 * 
 * OKBC introduces a notion of a slot being attached to a frame as either an "own-slot" or as a "template-slot". A
 * "template-slot" is a slot on a class which is automatically attached to subclasses and is automatically attached at
 * all instances of the class. An "own-slot" is a slot on any frame (either a class or not) which is not automatically
 * attached to subclasses and is not automatically attached to instances of the frame. Note that the difference between
 * these two things is only relavent for class frames and we only support template slots on class frames. OKBC ascribes
 * no meaning to template slots on non-classes.
 * 
 * Own-slot values, even on a class, are not inherited to subclasses and do not occur on instances of the class. If a
 * template slot has a value, this value is necessarily inherited to subclasses and appears on instances of the class.
 * 
 * get/set slot and facet value methods return/take a collection of objects. The type of object returned or required
 * depends on the value-type of the slot. The mapping between Protege value-types and Java object type is given below:
 * 
 * <pre>
 * 
 *  Boolean    --&gt; java.lang.Boolean
 *  Class      --&gt; edu.stanford.smi.protege.model.Cls
 *  Float      --&gt; java.lang.Float
 *  Instance   --&gt; edu.stanford.smi.protege.model.Instance
 *  Integer    --&gt; java.lang.Integer
 *  Symbol     --&gt; java.lang.String
 *  String     --&gt; java.lang.String
 *  
 * </pre>
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Frame extends Comparable<Frame> {

    void addFrameListener(FrameListener listener);

    boolean addOwnFacetValue(Slot slot, Facet facet, Object value);

    void addOwnSlotValue(Slot slot, Object value);

    boolean areValidOwnSlotValues(Slot slot, Collection values);

    /**
     * Create a "deep copy" of the frame. If a frame has slot values that refer to SimpleInstances then these instances
     * are duplicated as well. References to classes, slots, and facets are not duplicated. The copy of the frame is
     * placed in the "kb" KnowledgeBase. To duplicate an instance into its own knowledgeBase you can pass in null for
     * the kb. Always pass in null for the valueMap argument.
     */
    Frame deepCopy(KnowledgeBase kb, Map valueMap);

    Frame shallowCopy(KnowledgeBase kb, Map valueMap);

    Frame copy(KnowledgeBase kb, Map valueMap, boolean isDeep);

    void delete();

    String getBrowserText();

    Icon getIcon();

    Collection getDocumentation();

    FrameID getFrameID();

    String getInvalidOwnSlotValuesText(Slot slot, Collection values);

    String getInvalidOwnSlotValueText(Slot slot, Object value);

    KnowledgeBase getKnowledgeBase();

    String getName();

    /** see {@link Frame}for a description of the return type */
    Object getOwnFacetValue(Slot slot, Facet facet);

    /** see {@link Frame}for a description of the return type */
    Collection getOwnFacetValues(Slot slot, Facet facet);

    boolean getOwnSlotAllowsMultipleValues(Slot slot);

    /**
     * Returns the own slot values for this slot and all of its subslots. See {@link Frame} for a description of the
     * return type.
     */
    Collection getOwnSlotAndSubslotValues(Slot slot);

    /** see {@link Frame}for a description of the return type */
    Collection getOwnSlotDefaultValues(Slot slot);

    Collection getOwnSlotFacets(Slot slot);

    Collection getOwnSlotFacetValues(Slot slot, Facet facet);

    /**
     * Gets the collection of own slots for a frame. These consist of the template slots of all of its direct types as
     * well as the slots :NAME and :DIRECT-TYPE. See the Protege documentation (or the OKBC spec) for additional
     * information about "own" and "template" slots.
     * 
     * @return a collection of #Slot for this frame
     */
    Collection<Slot> getOwnSlots();

    /** see {@link Frame} for a description of the return type */
    Object getDirectOwnSlotValue(Slot slot);

    /** see {@link Frame} for a description of the return type */
    Object getOwnSlotValue(Slot slot);

    int getOwnSlotValueCount(Slot slot);

    /** see {@link Frame} for a description of the return type */
    Collection getOwnSlotValues(Slot slot);

    /**
     * See {@link Frame} for a description of the return type.
     */
    List getDirectOwnSlotValues(Slot slot);

    ValueType getOwnSlotValueType(Slot slot);

    Project getProject();

    Collection<Reference> getReferences();

    Collection getReferences(int maxReferences);

    boolean hasOwnSlot(Slot slot);

    boolean isEditable();

    boolean isIncluded();

    boolean isSystem();

    /**
     * @deprecated Use #isDeleted()
     */

    boolean isValid();

    void markDeleting();

    void markDeleted(boolean deleted);

    boolean isDeleted();

    boolean isBeingDeleted();

    boolean isValidOwnSlotValue(Slot slot, Object item);

    boolean isVisible();

    void moveDirectOwnSlotValue(Slot slot, int fromIndex, int toIndex);

    void removeFrameListener(FrameListener listener);

    /** Remove _all_ occurrences of this value. */
    void removeOwnSlotValue(Slot slot, Object value);

    void setDocumentation(String documentation);

    void setDocumentation(Collection documentation);

    void setEditable(boolean b);

    void setIncluded(boolean b);

    /** See {@link Frame}for a description of the value type. */
    void setOwnFacetValue(Slot slot, Facet facet, Object value);

    /** See {@link Frame}for a description of the value type. */
    void setOwnFacetValues(Slot slot, Facet facet, Collection values);

    /**
     * See {@link Frame}for a description of the value type.
     * 
     * This method sets the value of an own slot at a frame to a single value. The value passed in cannot be a
     * Collection. To pass in a collection instead use {@link #setOwnSlotValues(Slot, Collection)}(note the final "s").
     * This method can be called for either cardinality-single slots or cardinality-multiple slots.
     */
    void setOwnSlotValue(Slot slot, Object value);

    /**
     * Same as #setOwnSlotValue(Slot, Object)
     */
    void setDirectOwnSlotValue(Slot slot, Object value);

    /**
     * See {@link Frame}for a description of the value type.
     * 
     * This method sets the value of an own slot at a frame to a collection of values. This method can be called for
     * either cardinality-single slots or cardinality-multiple slots. For a cardinality single slot the collection
     * passed in must have no more than one element.
     */
    void setOwnSlotValues(Slot slot, Collection values);

    /**
     * Same as #setOwnSlotValues(Slot, Collection)
     */
    void setDirectOwnSlotValues(Slot slot, Collection values);

    void setVisible(boolean b);
    
    /**
     * This is as close as we come to renaming a frame.  Creates a clone of this with the new
     * name and deletes this.
     *
     */
    Frame rename(String name);
    
    /**
     * This call ensures that the name slot for the frame has the frame name as its value.
     * This can be important when constructing a frame from scratch and then trying to ensure that the frame
     * actually appears in the knowledge base.  The frame will not appear in the knowledge base until the knowledge
     * base has an assertion about the frame.  Sometimes frames are created without any type information and then later the
     * needed assertions are added.
     */
    void assertFrameName();
}