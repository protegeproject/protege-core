package edu.stanford.smi.protege.code.generator.wrapping;

import edu.stanford.smi.protege.model.Slot;

/**
 * An object representing metadata about a property
 * useful for Java code generation.
 *
 * @author Csongor Nyulas
 */
public class SlotCode implements Comparable {

    private Slot property;

    private boolean usePrefix;

    public SlotCode(Slot property, boolean usePrefixInNames) {
        this.property = property;
        this.usePrefix = usePrefixInNames;
    }


    public int compareTo(Object o) {
        if (o instanceof SlotCode) {
            SlotCode other = (SlotCode) o;
            return getJavaName().compareTo(other.getJavaName());
        }
        return 0;
    }


    public String getJavaName() {
        return ClsCode.getValidJavaName(property.getName());
    }


    //commented parts may be useful in the future ....

/*
    public String getJavaType() {
        RDFResource range = property.getRange();
        if (range instanceof RDFSDatatype) {
            OWLModel owlModel = property.getOWLModel();
            if (owlModel.getXSDboolean().equals(range)) {
                return "boolean";
            }
            else if (owlModel.getXSDfloat().equals(range)) {
                return "float";
            }
            else if (owlModel.getXSDint().equals(range)) {
                return "int";
            }
            else if (owlModel.getXSDstring().equals(range)) {
                return "String";
            }
            else {
                return "RDFSLiteral";
            }
        }
        else if (range instanceof RDFSNamedClass) {
            return new RDFSClassCode((RDFSNamedClass) range).getJavaName();
        } else if (range instanceof OWLAnonymousClass) {
        	RDFResource propRange = property.getRange();

        	if (propRange != null && propRange instanceof RDFSNamedClass) {
        		return new RDFSClassCode((RDFSNamedClass) propRange).getJavaName();
        	} else {
        		return "Object";
        	}
        }
        else {
            return "Object";
        }
    }
*/

    public Slot getRDFProperty() {
        return property;
    }


    public String getUpperCaseJavaName() {
        String name = getJavaName();
        if (name.length() > 1) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        else {
            return name.toUpperCase();
        }
    }


    //commented parts may be useful in the future ....

/*
    public boolean isCustomType() {
        RDFResource range = property.getRange();
        return range instanceof RDFSNamedClass;
    }


    public boolean isMultiple() {
        return !property.isFunctional();
    }


    public boolean isPrimitive() {
        RDFResource range = property.getRange();
        if (range instanceof RDFSDatatype) {
            OWLModel owlModel = property.getOWLModel();
            if (owlModel.getXSDboolean().equals(range) ||
                    owlModel.getXSDfloat().equals(range) ||
                    owlModel.getXSDint().equals(range)) {
                return true;
            }
        }
        return false;
    }
*/
}
