package edu.stanford.smi.protege.model;

import java.io.Serializable;
import java.util.WeakHashMap;


/**
 
 */
public class FrameID implements Serializable, Localizable {
    private static final long serialVersionUID = -3804918126573053937L;
    private String name;
    private int hashCode;
    private static WeakHashMap<String, String> canonicalString = new WeakHashMap<String, String>();

    public FrameID(String name) {
        this.name = name;
        if (name != null) {
            intern();
            hashCode = name.hashCode() + 42;
        }
        else {
        	hashCode = 0;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FrameID)) {
            return false;
        }
        FrameID other = (FrameID) o;
        return name == other.name;
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "FrameID(" + name + ")";
    }

    public void localize(KnowledgeBase kb) {
        if (name != null) {
            intern();
        }
    }
    
    private void intern() {
        /*
        String canonical = canonicalString.get(name);
        if (canonical != null) {
            name = canonical;
        }
        else {
            canonicalString.put(name, name);
        }
        */
        name = name.intern();
    }
}