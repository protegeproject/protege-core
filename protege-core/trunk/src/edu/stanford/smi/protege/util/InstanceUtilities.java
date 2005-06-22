package edu.stanford.smi.protege.util;

import java.awt.*;

import edu.stanford.smi.protege.model.*;

/**
 * Utilities for handling project knowledge base instances.  These provide standard ways of encoding/decoding rectangles
 * and dimensions into an instance.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceUtilities {
    private static final String CLASS_DIMENSION = "Dimension";

    private static final String SLOT_X = "x";
    private static final String SLOT_Y = "y";
    private static final String SLOT_WIDTH = "width";
    private static final String SLOT_HEIGHT = "height";

    public static Dimension getDimension(Instance instance) {
        Assert.assertEquals("class name", instance.getDirectType().getName(), CLASS_DIMENSION);
        Dimension d = new Dimension();
        d.width = getInt(instance, SLOT_WIDTH);
        d.height = getInt(instance, SLOT_HEIGHT);
        return d;
    }

    private static int getInt(Instance instance, String name) {
        Object o = ModelUtilities.getDirectOwnSlotValue(instance, name);
        Integer i;
        if (o instanceof String) {
            i = new Integer((String) o);
        } else {
            i = (Integer) o;
        }
        // Integer i = (Integer) ModelUtilities.getOwnSlotValue(instance, name);
        return (i == null) ? 0 : i.intValue();
    }

    public static Rectangle getRectangle(Instance instance) {
        Rectangle r = new Rectangle();
        r.x = getInt(instance, SLOT_X);
        r.y = getInt(instance, SLOT_Y);
        r.width = getInt(instance, SLOT_WIDTH);
        r.height = getInt(instance, SLOT_HEIGHT);
        return r;
    }

    public static void setDimension(Instance instance, Dimension d) {
        Assert.assertEquals("class name", instance.getDirectType().getName(), CLASS_DIMENSION);
        setInt(instance, SLOT_WIDTH, d.width);
        setInt(instance, SLOT_HEIGHT, d.height);
    }

    private static void setInt(Instance instance, String name, int i) {
        ModelUtilities.setOwnSlotValue(instance, name, new Integer(i));
    }

    public static void setRectangle(Instance instance, Rectangle r) {
        setInt(instance, SLOT_X, r.x);
        setInt(instance, SLOT_Y, r.y);
        setInt(instance, SLOT_WIDTH, r.width);
        setInt(instance, SLOT_HEIGHT, r.height);
    }
}
