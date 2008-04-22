package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.event.*;

/**
 * An instance of a class.  The class is refered to as the direct type.
 * An instance can only have one direct type.  Note that, in Protege, all Frames are Instances so we could have
 * actually combined these two interfaces.  We choose not to.
 *
 * Note that classes and slots are also instances and implement this interface.
 * "Simple Instances" are instances which are not classes, slots, or facets. They
 * have their own interface SimpleInstance.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Instance extends Frame {

    void addInstanceListener(InstanceListener listener);

    /** 
     * return the "first" direct type 
     */
    Cls getDirectType();

    Collection getDirectTypes();

    Collection getReachableSimpleInstances();

    boolean hasDirectType(Cls cls);

    boolean hasType(Cls cls);

    void removeInstanceListener(InstanceListener listener);

    Instance setDirectType(Cls cls);

    Instance setDirectTypes(Collection types);

    void addDirectType(Cls cls);

    void removeDirectType(Cls cls);

    void moveDirectType(Cls cls, int index);
}
