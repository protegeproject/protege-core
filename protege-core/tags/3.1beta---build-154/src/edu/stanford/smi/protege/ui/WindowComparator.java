package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

/**
 * Comparator implementation for windows which just comparese their heights.
 */

class WindowComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        Window w1 = (Window) o1;
        Window w2 = (Window) o2;
        return w2.getHeight() - w1.getHeight();
    }
}
