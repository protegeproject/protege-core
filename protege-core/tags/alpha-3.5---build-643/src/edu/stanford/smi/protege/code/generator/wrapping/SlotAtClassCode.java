package edu.stanford.smi.protege.code.generator.wrapping;

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;


public class SlotAtClassCode implements Comparable<SlotAtClassCode> {

    private Cls cls;
    private Slot slot;

    public SlotAtClassCode(Cls cls, Slot property) {
        this.cls = cls;
        this.slot = property;
    }


    public String getJavaName() {
        return ClsCode.getValidJavaName(slot.getName());
    }

    public String getJavaType() {
    	return getJavaType(false);
    }

    public String getJavaType(boolean onlyNonPrimitive) {
    	ValueType valueType = slot.getValueType();

    	if (valueType == ValueType.STRING || valueType == ValueType.SYMBOL) {
    		return "String";
    	} else if (valueType == ValueType.BOOLEAN) {
    		return onlyNonPrimitive ? "java.lang.Boolean" : "boolean";
    	} else if (valueType == ValueType.FLOAT) {
    		return onlyNonPrimitive ? "java.lang.Float" : "float";
    	} else if (valueType == ValueType.INTEGER) {
    		return onlyNonPrimitive ? "java.lang.Integer" : "int";
    	} else if (valueType == ValueType.CLS) {
    		Log.getLogger().warning("Value type CLS for " + slot + " is not supported by the code generator.");
    		return "Object";
    	} else if (valueType == ValueType.INSTANCE) {
    		return getInstanceValueJavaTypeName();
    	} else if (valueType == ValueType.ANY) {
    		return "Object";
    	}

    	Log.getLogger().warning("Unrecognized value type for " + slot);

    	return "Object";
    }

    public String getPrimitiveMethod() {
    	ValueType valueType = slot.getValueType();

    	if (valueType == ValueType.INTEGER) {
    		return "intValue()";
    	} else if (valueType == ValueType.FLOAT) {
    		return "floatValue()";
    	} else if (valueType == ValueType.BOOLEAN) {
    		return "booleanValue()";
    	}

    	return "";
    }

    public boolean needsPrimitiveMethod() {
    	ValueType valueType = slot.getValueType();
    	return valueType == ValueType.INTEGER || valueType == ValueType.FLOAT || valueType == ValueType.BOOLEAN;
    }


    @SuppressWarnings("unchecked")
	protected String getInstanceValueJavaTypeName() {
    	Collection allowedClses = cls.getTemplateSlotAllowedClses(slot);
    	if (allowedClses.size() > 1) { //can't decide which one it is..
    		return "Object";
    	}

    	Cls allowedCls = (Cls) CollectionUtilities.getSoleItem(allowedClses);
		return ClsCode.getValidJavaName(allowedCls.getName());
	}

    @SuppressWarnings("unchecked")
	public String getRangeClsName() { //TODO: check for ANY
		return getRangeCls().getName();
	}

    public Cls getRangeCls() {
    	Collection allowedClses = cls.getTemplateSlotAllowedClses(slot);
    	if (allowedClses.size() == 0) { //bad case
    		return cls.getKnowledgeBase().getRootCls();
    	}
    	return (Cls) CollectionUtilities.getSoleItem(allowedClses);
    }


	public Slot getSlot() {
        return slot;
    }

	public Cls getCls() {
		return cls;
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


    public boolean isCustomType() {
    	return cls.getTemplateSlotAllowedClses(slot).size() > 0;
    }


    public boolean isMultiple() {
    	return cls.getTemplateSlotAllowsMultipleValues(slot);
    }

    public boolean isPrimitive() {
    	ValueType valueType = slot.getValueType();
    	return valueType == ValueType.BOOLEAN ||
    			valueType == ValueType.INTEGER ||
    			valueType == ValueType.FLOAT;
    }


	public int compareTo(SlotAtClassCode o) {
		SlotAtClassCode other = o;
        return getJavaName().compareTo(other.getJavaName());
	}

}
