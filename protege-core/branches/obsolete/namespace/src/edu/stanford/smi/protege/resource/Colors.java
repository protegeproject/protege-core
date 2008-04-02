/*
 * Created on Jul 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.stanford.smi.protege.resource;

import java.awt.*;

/**
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 *
 * TODO Class Comment
 */
public class Colors {
    private static final Color instanceSelectionColor = new Color(233, 226, 231);
    private static final Color clsSelectionColor = new Color(240, 230, 202);
    private static final Color slotSelectionColor = new Color(196, 218, 229);
    private static final Color formSelectionColor = new Color(211, 228, 209);

    private static final Color clsColor = new Color(204, 159, 42);
    private static final Color instanceColor = new Color(83, 24, 82);
    private static final Color slotColor = new Color(41, 119, 167);
    private static final Color facetColor = new Color(164, 59, 93);
    private static final Color formColor = new Color(39, 119, 22);

    public static Color getClsColor() {
        return clsColor;
    }

    public static Color getInstanceColor() {
        return instanceColor;
    }

    public static Color getSlotColor() {
        return slotColor;
    }

    public static Color getFacetColor() {
        return facetColor;
    }

    public static Color getFormColor() {
        return formColor;
    }

    public static Color getClsSelectionColor() {
        return clsSelectionColor;
    }

    public static Color getInstanceSelectionColor() {
        return instanceSelectionColor;
    }

    public static Color getSlotSelectionColor() {
        return slotSelectionColor;
    }

    public static Color getFormSelectionColor() {
        return formSelectionColor;
    }
}
