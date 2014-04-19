package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

class DefaultEntry {
    private boolean cardinalityMultiple;
    private String typeName;
    private String allowedClsName;
    private String slotname;

    DefaultEntry(String cardinality, String typeName, String allowedClsName, String slotname) {
        this.cardinalityMultiple = "multiple".equalsIgnoreCase(cardinality);
        this.typeName = typeName;
        this.allowedClsName = allowedClsName;
        this.slotname = slotname;
    }

    DefaultEntry(boolean cardinality, ValueType type, Cls allowedCls) {
        this.cardinalityMultiple = cardinality;
        this.typeName = type.toString();
        this.allowedClsName = (allowedCls == null) ? null : allowedCls.getName();
    }

    DefaultEntry(Slot slot) {
        this.slotname = slot.getName();
    }

    public boolean equals(Object o) {
        DefaultEntry rhs = (DefaultEntry) o;
        boolean isEquals = (cardinalityMultiple == rhs.cardinalityMultiple)
                && SystemUtilities.equals(typeName, rhs.typeName)
                && SystemUtilities.equals(allowedClsName, rhs.allowedClsName)
                && SystemUtilities.equals(slotname, rhs.slotname);
        return isEquals;
    }

    public int hashCode() {
        return HashUtils.getHash(typeName, allowedClsName, slotname, cardinalityMultiple);
    }

    public String toString() {
        return "DefaultEntry(" + cardinalityMultiple + ", " + typeName + ", " + allowedClsName + ")";
    }
}
