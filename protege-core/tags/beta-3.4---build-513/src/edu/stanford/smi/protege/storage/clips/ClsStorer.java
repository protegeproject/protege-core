package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Store a set of classes in Clips ontology file format Clips requires that
 *  clses be stored so that there are no forward references so we start at root
 *  and descend the class hierarchy writing out classes whose parents have
 *  already been written out.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsStorer extends ClipsFileWriter {
    private static Map _typeStrings = new HashMap(); // <valueType, String>
    private Collection _storedClses = new HashSet();
    private KnowledgeBase _kb;

    static {
        _typeStrings.put(ValueType.ANY, "ANY");
        _typeStrings.put(ValueType.BOOLEAN, "SYMBOL");
        _typeStrings.put(ValueType.CLS, "SYMBOL");
        _typeStrings.put(ValueType.FLOAT, "FLOAT");
        _typeStrings.put(ValueType.INSTANCE, "INSTANCE");
        _typeStrings.put(ValueType.INTEGER, "INTEGER");
        _typeStrings.put(ValueType.STRING, "STRING");
        _typeStrings.put(ValueType.SYMBOL, "SYMBOL");
    }

    private Collection _errors;

    public ClsStorer(Writer writer) {
        super(writer);
    }

    protected static boolean isStorable(Cls cls) {
        return cls == null || (!cls.isSystem() && !cls.isIncluded());
    }

    private void storeAccessorFacet() {
        println();
        print("\t\t(create-accessor read-write)");
    }
    
    private static ValueType getTemplateSlotValueType(Cls cls, Slot slot) {
        return (cls == null) ? slot.getValueType() : cls.getTemplateSlotValueType(slot);
    }

    private static Collection getTemplateSlotAllowedClses(Cls cls, Slot slot) {
        return (cls == null) ? slot.getAllowedClses() : cls.getTemplateSlotAllowedClses(slot);
    }

    private static Collection getTemplateSlotAllowedParents(Cls cls, Slot slot) {
        return (cls == null) ? slot.getAllowedParents() : cls.getTemplateSlotAllowedParents(slot);
    }

    private static Collection getTemplateSlotAllowedValues(Cls cls, Slot slot) {
        return (cls == null) ? slot.getAllowedValues() : cls.getTemplateSlotAllowedValues(slot);
    }
    
    // ????
    private static Collection getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return (cls == null) ? Collections.EMPTY_LIST : cls.getDirectTemplateFacetValues(slot, facet);
    }
    
    private static Collection getTemplateFacets(Cls cls, Slot slot) {
        return (cls == null) ? Collections.EMPTY_LIST : cls.getTemplateFacets(slot);
    }
    
    private static boolean getTemplateSlotAllowsMultipleValues(Cls cls, Slot slot) {
        return (cls == null) ? slot.getAllowsMultipleValues() : cls.getTemplateSlotAllowsMultipleValues(slot);
    }
    
    private static Collection getTemplateSlotDefaultValues(Cls cls, Slot slot) {
        return (cls == null) ? slot.getDefaultValues() : cls.getTemplateSlotDefaultValues(slot);
    }
    
    private static Collection getTemplateSlotDocumentation(Cls cls, Slot slot) {
        return (cls == null) ? slot.getDocumentation() : cls.getTemplateSlotDocumentation(slot);
    }
    
    private static int getTemplateSlotMaximumCardinality(Cls cls, Slot slot) {
        return (cls == null) ? slot.getMaximumCardinality() : cls.getTemplateSlotMaximumCardinality(slot);
    }
    
    private static int getTemplateSlotMinimumCardinality(Cls cls, Slot slot) {
        return (cls == null) ? slot.getMinimumCardinality() : cls.getTemplateSlotMinimumCardinality(slot);
    }
    
    private static Number getTemplateSlotMaximumValue(Cls cls, Slot slot) {
        return (cls == null) ? slot.getMaximumValue() : cls.getTemplateSlotMaximumValue(slot);
    }
    
    private static Number getTemplateSlotMinimumValue(Cls cls, Slot slot) {
        return (cls == null) ? slot.getMinimumValue() : cls.getTemplateSlotMinimumValue(slot);
    }
    
    private static Collection getDirectSubclasses(Cls cls) {
        return (cls == null) ? Collections.EMPTY_LIST : cls.getDirectSubclasses();
    }

    private Collection<Cls> getDirectSuperclasses(Cls cls) {
        return (cls == null) ? _kb.getRootClses() : cls.getDirectSuperclasses();
    }
    
    private static Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return (cls == null) ? slot.getValues() : cls.getTemplateSlotValues(slot);
    }
    
    private static boolean isAbstract(Cls cls) {
        return (cls == null) ? true : cls.isAbstract();
    }
    
    private static boolean hasDirectlyOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        return (cls == null) ? false : cls.hasDirectlyOverriddenTemplateFacet(slot, facet);
    }
    
    private Cls getRootCls() {
        return _kb.getRootCls();
    }
    
    private static String getComment(Cls cls) {
        return (cls == null) ? "Fake class to save top-level slot information" : (String) CollectionUtilities.getFirstItem(cls.getDocumentation())        ;
    }

    private void storeAllowedClsesFacet(Cls cls, Slot slot) {
        ValueType type = getTemplateSlotValueType(cls, slot);
        if (type == ValueType.INSTANCE) {
            Collection clses = getTemplateSlotAllowedClses(cls, slot);
            storeCollectionFacet("allowed-classes", clses, true, ValueType.CLS, true);
        }
    }

    private void storeAllowedParentsFacet(Cls cls, Slot slot) {
        ValueType type = getTemplateSlotValueType(cls, slot);
        if (type == ValueType.CLS) {
            Collection clses = getTemplateSlotAllowedParents(cls, slot);
            storeCollectionFacet("allowed-parents", clses, true, type, true);
        }
    }

    private void storeAllowedValuesFacet(Cls cls, Slot slot) {
        ValueType type = getTemplateSlotValueType(cls, slot);
        Collection values = null;
        if (type == ValueType.BOOLEAN) {
            values = new ArrayList();
            values.add(Boolean.FALSE);
            values.add(Boolean.TRUE);
        } else if (type == ValueType.SYMBOL) {
            values = getTemplateSlotAllowedValues(cls, slot);
        }
        storeCollectionFacet("allowed-values", values, false, type, false);
    }

    private void storeAssociatedFacet(Slot slot) {
        Facet facet = slot.getAssociatedFacet();
        if (facet != null) {
            println();
            print(";+\t\t(associated-facet ");
            printFrame(facet);
            print(")");
        }
    }

    private void storeCardinalityFacet(Cls cls, Slot slot, boolean allowsMultiple) {
        int min = getTemplateSlotMinimumCardinality(cls, slot);
        int max = getTemplateSlotMaximumCardinality(cls, slot);
        if (min != 0 || max != KnowledgeBase.MAXIMUM_CARDINALITY_UNBOUNDED) {
            println();
            if (!allowsMultiple) {
                print(";+");
            }
            print("\t\t(cardinality ");
            print(min);
            print(" ");
            if (max == KnowledgeBase.MAXIMUM_CARDINALITY_UNBOUNDED) {
                print("?VARIABLE");
            } else {
                print(max);
            }
            print(")");
        }
    }

    private void storeCls(Cls cls) {
        // Log.enter(this, "storeCls", cls);
        try {
            if (isStorable(cls)) {
                printCls(cls);
            }
        } catch (Exception e) {
        	String message = "Errors at storing class " + cls;
            Log.getLogger().log(Level.WARNING, message, e);            
            _errors.add(new MessageError(e, message));
        }
    }

    private void printCls(Cls cls) {
        println();
        println();
        print("(defclass ");
        storeFrameName(cls);
        
        storeComment(cls);
        storeSuperclasses(cls);
        storeRole(cls);
        storeSlots(cls);
        print(")");
    }
    
    private void storeFrameName(Cls cls) {
        if (cls == null) {
            printFrameName(ClipsUtil.TOP_LEVEL_SLOT_CLASS);
        } else {
            printFrame(cls);
        }
    }

    private void storeClsAndSubclasses(Cls cls) {
        _storedClses.add(cls);
        storeCls(cls);
        storeSubclasses(cls);
    }

    public void storeClses(KnowledgeBase kb, Collection errors) {
        _kb = kb;
        //ESCA-JAVA0256 
        _errors = errors;
        storeTopLevelSlots(kb);
        Cls root = kb.getRootCls();
        _storedClses.add(root);
        storeSubclasses(root);
        flush();
        if (!printSucceeded()) {
            errors.add(new MessageError("Store classes failed."));
            Log.getLogger().warning("Store classes failed.");
        }
        _kb = null;
    }

    private void storeCollectionFacet(
        String name,
        Collection c,
        boolean isExtension,
        ValueType type,
        boolean storeIfEmpty) {
        if (c != null && (storeIfEmpty || !c.isEmpty())) {
            println();
            if (isExtension) {
                print(";+");
            }
            print("\t\t(");
            print(name);
            Iterator i = c.iterator();
            while (i.hasNext()) {
                print(" ");
                Object o = i.next();
                String s;
                if (o == null) {
                    Log.getLogger().warning("ignoring null facet value");
                } else {
                    if (o instanceof Cls || o instanceof Slot || o instanceof Facet) {
	                    s = toExternalFrameName((Frame) o);
	                } else if (o instanceof Instance) {
	                    s = "[" + toExternalFrameName((Frame) o) + "]";
	                } else if (o instanceof Boolean) {
	                    s = ((Boolean) o).booleanValue() ? ClipsUtil.TRUE : ClipsUtil.FALSE;
	                } else if (type == ValueType.STRING) {
	                    s = ClipsUtil.toExternalString((String) o);
	                } else {
	                    s = ClipsUtil.toExternalSymbol(o.toString());
	                }
                    print(s);
                }
            }
            print(")");
        }
    }

    private void storeComment(Cls cls) {
        String comment = getComment(cls);
        if (comment != null) {
            print(" ");
            print(ClipsUtil.toExternalString(comment));
        }
    }

    private void storeDefaultValueFacet(Cls cls, Slot slot) {
        ValueType type = getTemplateSlotValueType(cls, slot);
        storeCollectionFacet("default", getTemplateSlotDefaultValues(cls, slot), false, type, false);
    }

    private void storeInverseProperty(Slot slot) {
        Slot inverseSlot = slot.getInverseSlot();
        if (inverseSlot != null) {
            println();
            print(";+\t\t(inverse-slot ");
            printFrame(inverseSlot);
            print(")");

        }
    }

    private void storeRangeFacet(Cls cls, Slot slot) {
        ValueType type = getTemplateSlotValueType(cls, slot);
        if (type == ValueType.INTEGER || type == ValueType.FLOAT) {
            Number minValue = getTemplateSlotMinimumValue(cls, slot);
            Number maxValue = getTemplateSlotMaximumValue(cls, slot);

            if (minValue != null || maxValue != null) {
                Collection c = new ArrayList();
                if (type == ValueType.INTEGER) {
                    minValue = (minValue == null) ? (Number) null : new Integer(minValue.intValue());
                    maxValue = (maxValue == null) ? (Number) null : new Integer(maxValue.intValue());
                }
                c.add((minValue == null) ? "?VARIABLE" : minValue.toString());
                c.add((maxValue == null) ? "?VARIABLE" : maxValue.toString());
                storeCollectionFacet("range", c, false, type, false);
            }
        }
    }

    private void storeRole(Cls cls) {
        println();
        print("\t(role ");
        String text = isAbstract(cls) ? "abstract" : "concrete";
        print(text);
        print(")");
    }

    private void storeSlot(Cls cls, Slot slot) {
        try {
            println();
            boolean allowsMultiple = getTemplateSlotAllowsMultipleValues(cls, slot);
            if (allowsMultiple) {
                print("\t(multislot ");
            } else {
                print("\t(single-slot ");
            }
            String name = slot.getName();
            if (name.equals("name")) {
                print("name_");
            } else if (name.equals("is-a")) {
                print("is-a_");
            } else {
                printFrame(slot);
            }

            storeSlotDocumentation(cls, slot);
            storeTypeFacet(cls, slot);
            storeAllowedValuesFacet(cls, slot);
            storeAllowedParentsFacet(cls, slot);
            storeAllowedClsesFacet(cls, slot);
            storeRangeFacet(cls, slot);
            storeDefaultValueFacet(cls, slot);
            storeValueFacet(cls, slot);
            storeCardinalityFacet(cls, slot, allowsMultiple);
            if (cls == null) {
                storeInverseProperty(slot);
                storeSuperslotProperty(slot);
                storeAssociatedFacet(slot);
            }
            storeConstraintsFacet(cls, slot);
            storeUserFacets(cls, slot);
            storeAccessorFacet();
            print(")");
        } catch (Exception e) {
        	String message = "Errors at storing slot " + slot + " at class " + cls;
            Log.getLogger().log(Level.WARNING, message, e);
            _errors.add(new MessageError(e,message));
        }
    }

    private void storeSlotDocumentation(Cls cls, Slot slot) {
        String text = (String) CollectionUtilities.getFirstItem(getTemplateSlotDocumentation(cls, slot));
        if (text != null) {
            println();
            print(";+\t\t(comment ");
            print(ClipsUtil.toExternalString(text));
            print(")");
        }
    }

    private void storeSlots(Cls cls) {
        Collection slotsToStore = getSlotsToStore(cls);
        Iterator i = slotsToStore.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            storeSlot(cls, slot);
        }
    }
    
    private Collection getSlotsToStore(Cls cls) {
        Collection slotsToStore;
        if (cls == null) {
            slotsToStore = getDirectSlots();
        } else {
            slotsToStore = new HashSet(cls.getDirectTemplateSlots());
            slotsToStore.addAll(_kb.getDirectlyOverriddenTemplateSlots(cls));
        }
        return slotsToStore;
    }
    
    private Collection getDirectSlots() {
        Collection slots = new HashSet(_kb.getSlots());
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (slot.isIncluded()) {
                i.remove();
            }
        }
        return slots;
    }

    private void storeSubclasses(Cls cls) {
        Iterator i = getDirectSubclasses(cls).iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            if (!_storedClses.contains(subclass)) {
                if (superclassesStored(subclass)) {
                    storeClsAndSubclasses(subclass);
                }
            }
        }
    }

    private void storeSuperclasses(Cls cls) {
        println();
        print("\t(is-a");
        Collection superclasses = getDirectSuperclasses(cls);
        Cls rootCls = getRootCls();
        Iterator i = superclasses.iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            if (equals(superclass, rootCls)) {
                print(" USER");
            } else {
                print(" ");
                printFrame(superclass);
            }
        }
        print(")");
    }

    private void storeSuperslotProperty(Slot slot) {
        Collection superslots = slot.getDirectSuperslots();
        if (!superslots.isEmpty()) {
            println();
            print(";+\t\t(subslot-of ");
            boolean isFirst = true;
            Iterator i = superslots.iterator();
            while (i.hasNext()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    print(" ");
                }
                Slot superslot = (Slot) i.next();
                printFrame(superslot);
            }
            print(")");

        }
    }

    /**
     *  Top level slots are stored as slots on a fake class
     *
     * @param  kb  Description of Parameter
     */
    private void storeTopLevelSlots(KnowledgeBase kb) {
        /*
        boolean wasUndoEnabled = kb.setUndoEnabled(false);
        boolean wasGenerateEventsEnabled = kb.setGenerateEventsEnabled(false);
        Cls cls = kb.createCls(ClipsUtil.TOP_LEVEL_SLOT_CLASS, CollectionUtilities.createCollection(kb.getRootCls()));
        Iterator i = kb.getSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (!slot.isIncluded()) {
                cls.addDirectTemplateSlot(slot);
            }
        }
        storeCls(cls);
        kb.deleteCls(cls);
        kb.setUndoEnabled(wasUndoEnabled);
        kb.setGenerateEventsEnabled(wasGenerateEventsEnabled);
        */
        printCls(null);
    }

    private void storeTypeFacet(Cls cls, Slot slot) {
        println();
        ValueType type = getTemplateSlotValueType(cls, slot);
        if (type == ValueType.ANY) {
            print(";+");
        }
        print("\t\t(type ");
        String typeName = (String) _typeStrings.get(type);
        print(typeName);
        print(")");
    }

    private void storeConstraintsFacet(Cls cls, Slot slot) {
        Facet facet = _kb.getFacet(Model.Facet.CONSTRAINTS);
        storeUserFacet(cls, slot, facet);
    }

    private void storeUserFacet(Cls cls, Slot slot, Facet facet) {
        if (hasDirectlyOverriddenTemplateFacet(cls, slot, facet)) {
            Collection values = getDirectTemplateFacetValues(cls, slot, facet);
            ValueType type = facet.getValueType();
            storeCollectionFacet("user-facet " + facet.getName(), values, true, type, true);
        }
    }

    private void storeUserFacets(Cls cls, Slot slot) {
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            if (!facet.isSystem()) {
                storeUserFacet(cls, slot, facet);
            }
        }
    }

    private void storeValueFacet(Cls cls, Slot slot) {
        ValueType type = getTemplateSlotValueType(cls, slot);
        storeCollectionFacet("value", getTemplateSlotValues(cls, slot), true, type, false);
    }

    private boolean superclassesStored(Cls cls) {
        boolean superclassesStored = true;
        Iterator i = cls.getDirectSuperclasses().iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            if (!_storedClses.contains(superclass)) {
                superclassesStored = false;
                break;
            }
        }
        return superclassesStored;
    }
}
