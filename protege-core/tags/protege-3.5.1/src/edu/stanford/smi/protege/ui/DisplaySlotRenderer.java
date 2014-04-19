package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

class DisplaySlotRenderer extends DefaultRenderer {

    private static final long serialVersionUID = -5128165997768412257L;

    DisplaySlotRenderer(Cls cls) {
    }

    public void loadNull() {
        clear();
    }

    public void load(Object o) {
        if (o instanceof BrowserSlotPattern) {
            BrowserSlotPattern pattern = (BrowserSlotPattern) o;
            addText(pattern);
        } else {
            addText(o.toString());
        }
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }

    private void addText(BrowserSlotPattern pattern) {
        Iterator i = pattern.getElements().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Slot) {
                Slot slot = (Slot) o;
                addIcon(slot.getIcon());
                addText(slot.getBrowserText());
            } else {
                addText(o.toString());
            }
        }
    }
}