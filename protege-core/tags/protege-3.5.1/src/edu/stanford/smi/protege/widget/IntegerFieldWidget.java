package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * A slot widget used to acquire and display an integral value.
 *
 * @author Ray Fergerson
 */
public class IntegerFieldWidget extends NumberFieldWidget {

    private static final long serialVersionUID = 6750189081136351439L;

    protected String getInvalidTextDescription(String text) {
        String result = null;
        try {
            int i = new Integer(text).intValue();
            Number min = getCls().getTemplateSlotMinimumValue(getSlot());
            if (min != null && i < min.intValue()) {
                result = "The minimum value is " + min;
            }
            Number max = getCls().getTemplateSlotMaximumValue(getSlot());
            if (max != null && i > max.intValue()) {
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
                values.add(new Integer(text));
            }
        } catch (NumberFormatException e) {
            setText(null);
        }
        return values;
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean result = false;
        if (cls != null && slot != null) {
            boolean isInteger = equals(cls.getTemplateSlotValueType(slot), ValueType.INTEGER);
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            result = isInteger && !isMultiple;
        }
        return result;
    }
}
