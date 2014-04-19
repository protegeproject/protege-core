package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A comparer for slot widgets.  This class doesn't actually compare the widgets, just their types.  The widgets are 
 * then "sorted" in such a way that their display will look reasonable (big widgets on the right and bottom).  A better
 * algorithm would actually look at the sizes of the widgets themselves.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotWidgetComparer implements Comparator {

    private final static int SINGLE = 0;
    private final static int MULTIPLE = 1;
    private final static int _rank[][];

    static {
        _rank = new int[2][8];
        _rank[SINGLE][ValueType.ANY.getIntValue()] = 10;
        _rank[SINGLE][ValueType.BOOLEAN.getIntValue()] = 1;
        _rank[SINGLE][ValueType.CLS.getIntValue()] = 5;
        _rank[SINGLE][ValueType.FLOAT.getIntValue()] = 3;
        _rank[SINGLE][ValueType.INSTANCE.getIntValue()] = 6;
        _rank[SINGLE][ValueType.INTEGER.getIntValue()] = 2;
        _rank[SINGLE][ValueType.STRING.getIntValue()] = 0;
        _rank[SINGLE][ValueType.SYMBOL.getIntValue()] = 4;
        _rank[MULTIPLE][ValueType.ANY.getIntValue()] = 11;
        _rank[MULTIPLE][ValueType.BOOLEAN.getIntValue()] = 7;
        _rank[MULTIPLE][ValueType.CLS.getIntValue()] = 8;
        _rank[MULTIPLE][ValueType.FLOAT.getIntValue()] = 7;
        _rank[MULTIPLE][ValueType.INSTANCE.getIntValue()] = 9;
        _rank[MULTIPLE][ValueType.INTEGER.getIntValue()] = 7;
        _rank[MULTIPLE][ValueType.STRING.getIntValue()] = 7;
        _rank[MULTIPLE][ValueType.SYMBOL.getIntValue()] = 7;
    }

    public int compare(Object o1, Object o2) {
        SlotWidget w1 = (SlotWidget) o1;
        SlotWidget w2 = (SlotWidget) o2;
        int diff = getRank(w1) - getRank(w2);
        if (diff == 0) {
            diff = ((Component) w1).getHeight() - ((Component) w2).getHeight();
        }
        if (diff == 0) {
            String s1 = w1.getLabel();
            String s2 = w2.getLabel();
            if (s1 == null || s2 == null) {
                Log.getLogger().warning("null label: " + o1 + " " + o2);
                diff = 0;
            } else {
                diff = s1.compareToIgnoreCase(s2);
            }
        }
        return diff;
    }

    private static int getRank(SlotWidget widget) {
        Cls cls = widget.getCls();
        Slot slot = widget.getSlot();
        boolean allowsMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
        ValueType type = cls.getTemplateSlotValueType(slot);
        return _rank[allowsMultiple ? MULTIPLE : SINGLE][type.getIntValue()];
    }
}
