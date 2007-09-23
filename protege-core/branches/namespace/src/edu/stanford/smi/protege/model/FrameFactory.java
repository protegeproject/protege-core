package edu.stanford.smi.protege.model;

import java.util.Collection;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameFactory {
    void addJavaPackage(String packageName);

    void removeJavaPackage(String packageName);

    Cls createCls(FrameID id, Collection directTypes);

    Slot createSlot(FrameID id, Collection directTypes);

    Facet createFacet(FrameID id, Collection directTypes);

    SimpleInstance createSimpleInstance(FrameID id, Collection directTypes);

    boolean isCorrectJavaImplementationClass(FrameID id, Collection directTypes, Class clas);

    /**
     * @return integer to map this frame to a Java class. This integer may then be passed in later
     * to the createFrameFromClassId method
     */
    int getJavaClassId(Frame value);

    /**
     * @return frame appropriate for this java class id.
     */
    Frame createFrameFromClassId(int javaClassId, FrameID id);

    /**
     * @return all java class ids which correspond to classes.  
     * The collection contains java.lang.Integer objects.
     */
    Collection getClsJavaClassIds();

    /**
     * @return all java class ids which correspond to slots
     */
    Collection getSlotJavaClassIds();

    /**
     * @return all java class ids which correspond to facets
     */
    Collection getFacetJavaClassIds();

    /**
     * @return all java class ids which correspond to simple instances
     */
    Collection getSimpleInstanceJavaClassIds();
    
    Frame rename(Frame original, String name);
}