package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * Slot widget for acquiring and displaying a floating point value.
 * 
 * @author Ray Fergerson
 */

public class FloatFieldWidget extends NumberFieldWidget {

    private static final long serialVersionUID = -2408555647675847253L;

    protected String getInvalidTextDescription(String text) {
        String result = null;
        try {
            float f = Float.parseFloat(text);
            Number min = getCls().getTemplateSlotMinimumValue(getSlot());
            if (min != null && f < min.floatValue()) {
                result = "The minimum value is " + min;
            }
            Number max = getCls().getTemplateSlotMaximumValue(getSlot());
            if (max != null && f > max.floatValue()) {
                result = "The maximum value is " + max;
            }
        } catch (NumberFormatException e) {
            result = "The value must be a number";
        }
        return result;
    }

    public Collection getValues() {
        Collection values = new ArrayList();
        String text = getText();
        try {
            if (text != null) {
                values.add(new Float(text));
            }
        } catch (NumberFormatException e) {
            setText(null);
        }
        return values;
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean result = false;
        if (cls != null && slot != null) {
            boolean isFloat = cls.getTemplateSlotValueType(slot) == ValueType.FLOAT;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            result = isFloat && !isMultiple;
        }
        return result;
    }
}