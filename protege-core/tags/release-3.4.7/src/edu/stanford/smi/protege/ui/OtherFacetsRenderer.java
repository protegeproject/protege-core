package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for "other" facets. Other is defined as not cardinality or type.
 * User-defined facets should also be displayed here but aren't at the moment.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OtherFacetsRenderer extends DefaultRenderer {

    private static final long serialVersionUID = 2666897368349118508L;

    private static void addAllowedValuesText(StringBuffer s, Cls cls, Slot slot) {
        if (equals(cls.getTemplateSlotValueType(slot), ValueType.SYMBOL)) {
            Collection values = cls.getTemplateSlotAllowedValues(slot);
            appendValues(s, "allowed-values", values);
        }
    }

    private static void addDefaultValuesText(StringBuffer s, Cls cls, Slot slot) {
        Collection defaults = cls.getTemplateSlotDefaultValues(slot);
        if (!defaults.isEmpty()) {
            addObjects(s, "default", defaults);
        }
    }

    private static void addObjects(StringBuffer s, String text, Collection objects) {
        Collection strings = new ArrayList();
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            String name;
            if (o instanceof Frame) {
                name = ((Frame) o).getBrowserText();
            } else {
                name = o.toString();
            }
            strings.add(name);
        }
        appendValues(s, text, strings);
    }

    private static void addRangeText(StringBuffer s, Cls cls, Slot slot, ValueType type) {
        Number min = cls.getTemplateSlotMinimumValue(slot);
        Number max = cls.getTemplateSlotMaximumValue(slot);
        if (equals(type, ValueType.INTEGER)) {
            if (min != null) {
                min = new Integer(min.intValue());
            }
            if (max != null) {
                max = new Integer(max.intValue());
            }
        }
        if (min != null) {
            s.append("minimum=");
            s.append(min);
        }
        if (min != null && max != null) {
            s.append(", ");
        }
        if (max != null) {
            s.append("maximum=");
            s.append(max);
        }
        if (min != null || max != null) {
            s.append(' ');
        }
    }

    private static void addValuesText(StringBuffer s, Cls cls, Slot slot) {
        Collection values = cls.getTemplateSlotValues(slot);
        if (!values.isEmpty()) {
            addObjects(s, "value", values);
        }
    }

    private static void appendValues(StringBuffer s, String text, Collection values) {
        boolean first = true;
        s.append(text);
        s.append("=");
        if (values.size() > 1) {
            s.append("{");
        }
        Iterator i = values.iterator();
        while (i.hasNext()) {
            if (first) {
                first = false;
            } else {
                s.append(',');
            }
            s.append(i.next());
        }
        if (values.size() > 1) {
            s.append("}");
        }
        s.append(" ");
    }

    private static void addInverseSlotText(StringBuffer buffer, Slot slot) {
        Slot inverseSlot = slot.getInverseSlot();
        if (inverseSlot != null) {
            buffer.append("inverse-slot=");
            buffer.append(inverseSlot.getBrowserText());
            buffer.append(" ");
        }
    }

    public void load(Object o) {
        FrameSlotCombination combination = (FrameSlotCombination) o;
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();

        ValueType type = cls.getTemplateSlotValueType(slot);

        StringBuffer text = new StringBuffer();

        if (equals(type, ValueType.INTEGER) || equals(type, ValueType.FLOAT)) {
            addRangeText(text, cls, slot, type);
        } else if (equals(type, ValueType.SYMBOL)) {
            addAllowedValuesText(text, cls, slot);
        }
        addValuesText(text, cls, slot);
        addDefaultValuesText(text, cls, slot);
        addInverseSlotText(text, slot);

        if (!cls.isEditable()) {
            setGrayedText(true);
        }
        setMainText(text.toString());
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }
}