package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * The class knows how to write out knowledge base instances in clips file
 * format. Note that classes which contain own slots other than the standard
 * ones are also written out in this file. The instance name is that same as the
 * class name, and these are paired up when the files are parsed.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceStorer extends ClipsFileWriter {
    private Collection _errors;
    private Map _clsToSlotsMap = new HashMap();
    private Slot _constraintsSlot;
    private Set _slotNamesToNotSaveForClsesAndSlots = new HashSet();
    private Set _slotNamesNeverToSave = new HashSet();

    {
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.NAME);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_TYPES);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_INSTANCES);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_SUPERCLASSES);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_SUBCLASSES);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_TEMPLATE_SLOTS);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_DOMAIN);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DOCUMENTATION);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.ROLE);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.VALUE_TYPE);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.MINIMUM_CARDINALITY);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.MAXIMUM_CARDINALITY);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.NUMERIC_MINIMUM);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.NUMERIC_MAXIMUM);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DEFAULTS);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.VALUES);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.INVERSE);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_SUBSLOTS);
        _slotNamesToNotSaveForClsesAndSlots.add(Model.Slot.DIRECT_SUPERSLOTS);

        _slotNamesNeverToSave.add(Model.Slot.NAME);
        _slotNamesNeverToSave.add(Model.Slot.DIRECT_TYPES);
    }

    public InstanceStorer(Writer writer) {
        super(writer);
    }

    private Collection anyToStrings(Collection anyValues) {
        Collection strings = new ArrayList();
        Iterator i = anyValues.iterator();
        while (i.hasNext()) {
            String value;
            Object o = i.next();
            if (o instanceof Frame) {
                value = "[" + toExternalFrameName((Frame) o) + "]";
            } else if (o instanceof String) {
                value = ClipsUtil.toExternalString((String) o);
            } else if (o instanceof Boolean) {
                value = ((Boolean) o).booleanValue() ? ClipsUtil.TRUE : ClipsUtil.FALSE;
            } else {
                value = o.toString();
            }
            strings.add(value);
        }
        return strings;
    }

    private Collection booleansToStrings(Collection booleans) {
        Collection strings = new ArrayList();
        Iterator i = booleans.iterator();
        while (i.hasNext()) {
            Boolean b = (Boolean) i.next();
            String s = (b.booleanValue()) ? ClipsUtil.TRUE : ClipsUtil.FALSE;
            strings.add(s);
        }
        return strings;
    }

    private Collection clsesToStrings(Collection clses) {
        Collection strings = new ArrayList();
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Cls) {
                Cls cls = (Cls) o;
                strings.add(toExternalFrameName(cls));
            } else {
                Log.getLogger().warning("not a class: " + o);
            }
        }
        return strings;
    }

    private Collection getSlots(Instance instance) {
        Cls type = instance.getDirectType();
        List slots = (List) _clsToSlotsMap.get(type);
        if (slots == null) {
            slots = new ArrayList(type.getTemplateSlots());
            Collections.sort(slots);
            _clsToSlotsMap.put(type, slots);
        }
        return slots;
    }

    private boolean hasConstraintSlotValues(Instance instance) {
        if (_constraintsSlot == null) {
            _constraintsSlot = (Slot) instance.getKnowledgeBase().getFrame(Model.Slot.CONSTRAINTS);
        }
        return instance.getOwnSlotValueCount(_constraintsSlot) != 0;
    }

    private Collection instancesToStrings(Collection instances) {
        Collection strings = new ArrayList();
        Iterator i = instances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            strings.add("[" + toExternalFrameName(instance) + "]");
        }
        return strings;
    }

    private Collection internalToExternalStrings(Collection internalStrings) {
        Collection externalStrings = new ArrayList();
        Iterator i = internalStrings.iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            if (s.length() > 0) {
                externalStrings.add(ClipsUtil.toExternalString(s));
            }
        }
        return externalStrings;
    }

    private Collection internalToExternalSymbols(Collection internalSymbols) {
        Collection externalStrings = new ArrayList();
        Iterator i = internalSymbols.iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            if (s.length() > 0) {
                externalStrings.add(ClipsUtil.toExternalSymbol(s));
            }
        }
        return externalStrings;
    }

    private boolean isStandardClsSlot(Instance instance) {
        Collection types = instance.getDirectTypes();
        boolean isStandard = false;
        if (types.size() == 1) {
	        Cls type = instance.getDirectType();
	        FrameID typeID = type.getFrameID();
	        isStandard =  equals(typeID, Model.ClsID.STANDARD_CLASS)
	                || equals(typeID, Model.ClsID.STANDARD_SLOT);
        }
        return isStandard;
    }

    protected boolean isStorableInstance(Instance instance) {
        boolean isStorable = false;
        if (instance.isDeleted()) {
            Log.getLogger().warning("has been deleted: " + instance.getName());
        } else if (instance.getDirectType() == null) {
            Log.getLogger().warning("null type: " + instance.getName());
        } else if (isStandardClsSlot(instance)) {
            isStorable = hasConstraintSlotValues(instance);
        } else if (instance.getName() == null) {
            Log.getLogger().warning("null name: " + instance.hashCode());
        } else {
            isStorable = !instance.isIncluded();
        }
        return isStorable;
    }

    protected boolean isStorableSlot(Instance instance, Slot slot) {
        boolean storable;
        if (instance instanceof Cls || instance instanceof Slot) {
            storable = !_slotNamesToNotSaveForClsesAndSlots.contains(slot.getName());
        } else {
            storable = !_slotNamesNeverToSave.contains(slot.getName());
        }
        return storable;
    }

    private void printValues(Slot slot, Collection values, boolean onePerLine,
            boolean allowsMultiple) {
        println();
        print("\t(");
        printFrame(slot);
        if (values.size() == 1 || !allowsMultiple) {
            print(" ");
            print(values.iterator().next().toString());
            print(")");
        } else {
            Iterator i = values.iterator();
            while (i.hasNext()) {
                if (onePerLine) {
                    println();
                    print("\t\t");
                } else {
                    print(" ");
                }
                String value = i.next().toString();
                print(value);
            }
            print(")");
        }
    }

    private void storeInstance(Instance instance) {
        try {
            if (isStorableInstance(instance)) {
                println();
                print("([");
                printFrame(instance);
                print("] of ");
                printTypes(instance);
                storeSlotValues(instance);
                println(")");
            }
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            _errors.add(e);
        }
    }

    private void printTypes(Instance instance) {
        Collection types = instance.getDirectTypes();
        Iterator i = types.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            print(" ");
            printFrame(cls);
        
        }
        println();
    }

    public void storeInstances(KnowledgeBase kb, Collection errors) {
        _errors = errors;
        List instances = new ArrayList(kb.getInstances());
        // Log.trace("instance=" + instances, this, "storeInstances");
        Collections.sort(instances, new FrameNameComparator());
        Iterator i = instances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            storeInstance(instance);
        }
        flush();
        if (!printSucceeded()) {
            errors.add("Store instances failed");
        }
    }

    private void storeSlotValue(Instance instance, Slot slot, boolean multiple) {
        try {
            // Log.enter(this, "storeSlotValue", instance, slot);
            Collection values = instance.getDirectOwnSlotValues(slot);
            if (!values.isEmpty()) {
                boolean onePerLine = false;
                ValueType type = instance.getOwnSlotValueType(slot);
                if (equals(type, ValueType.BOOLEAN)) {
                    values = booleansToStrings(values);
                } else if (equals(type, ValueType.INSTANCE)) {
                    values = instancesToStrings(values);
                    onePerLine = true;
                } else if (equals(type, ValueType.CLS)) {
                    values = clsesToStrings(values);
                    onePerLine = true;
                } else if (equals(type, ValueType.STRING)) {
                    values = internalToExternalStrings(values);
                    onePerLine = true;
                } else if (equals(type, ValueType.SYMBOL)) {
                    values = internalToExternalSymbols(values);
                } else if (equals(type, ValueType.ANY)) {
                    values = anyToStrings(values);
                }
                if (!values.isEmpty()) {
                    printValues(slot, values, onePerLine, multiple);
                }
            }
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            _errors.add(e);
        }
    }

    private void storeSlotValues(Instance instance) {
        Iterator i = getSlots(instance).iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (isStorableSlot(instance, slot)) {
                // The "allowsMultiple" flag is a hack for the Protege feature
                // of allowing multiple
                // values for a cardinality single slot.
                boolean allowsMultiple = instance.getOwnSlotAllowsMultipleValues(slot);
                storeSlotValue(instance, slot, allowsMultiple);
            }
        }
    }
}