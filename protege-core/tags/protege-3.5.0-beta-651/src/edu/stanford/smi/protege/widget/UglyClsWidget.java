package edu.stanford.smi.protege.widget;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A Cls widget used for displaying an "invalid" class form.  If for some reason the constructor or the initialize method
 * on a cls widget fail to execute (throw and exception) then that class from is replace with this one.  This doesn't
 * happen very often (if ever) because users typically don't develop ClsWidgets.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class UglyClsWidget extends AbstractClsWidget {

    private static final long serialVersionUID = -5154218132120842156L;
    public String getLabel() {
        return "Ugly Cls Widget (tm)";
    }

    public void initialize() {
        JComponent c = ComponentFactory.createLabel("The Ugly Cls Widget (tm)", SwingConstants.CENTER);
        c.setOpaque(true);
        c.setBackground(Color.red);
        c.setForeground(Color.black);
        c.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.green));
        add(c);
    }

    public void layoutLikeCls(Cls cls) {
    }

    public void relayout() {
    }

    public void removeCustomizations() {
    }

    public boolean configure() {
        return false;
    }

    public SlotWidget getSlotWidget(Slot slot) {
        return null;
    }
    public void replaceWidget(Slot slot, String className) {
        // do nothing
    }
}
