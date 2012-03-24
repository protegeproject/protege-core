package edu.stanford.smi.protege.model;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * This is the concrete subclass of DefaultInstance which handles everything but classes, slots, and facets.
 * This is also the class to be subclassed by users of the "java_packages" feature.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultSimpleInstance extends DefaultInstance implements SimpleInstance {

    private static final long serialVersionUID = 1587466135163556928L;

    public DefaultSimpleInstance() {

    }

    /**
     * This constructor should be used by classes which use the "java_packages" feature of
     * protege to load instances of user defined classes.  The User defined classes must
     * have a constructor whose signature (arguments) exactly match this constructor's signature.
     */
    public DefaultSimpleInstance(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }

    /**
     * This constructor may be called by applications that need to construct instances by calling a
     * constructor directly rather than by calling KnowledgeBase.createInstance (for example, JESS).
     * All other applications should use the KnowledgeBase.createInstance call to make instances.

     * @param name Pass null to cause the system to generate a name 
     */
    public DefaultSimpleInstance(KnowledgeBase kb, String name, Cls cls) {
        super(kb, null);
        getDefaultKnowledgeBase().addInstance(this, name, cls, true);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SimpleInstance(");
        buffer.append(getName());
        buffer.append(" of ");
        buffer.append(getDirectTypes());
        buffer.append(")");
        return buffer.toString();
    }

    public Icon getIcon() {
        return Icons.getInstanceIcon(!isEditable(), !isVisible());
    }
    
    public SimpleInstance rename(String name) {
        return (SimpleInstance) getKnowledgeBase().rename(this, name);
    }
}
