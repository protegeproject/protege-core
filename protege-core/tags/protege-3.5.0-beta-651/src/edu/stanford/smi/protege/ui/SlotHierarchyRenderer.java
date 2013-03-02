package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotHierarchyRenderer extends FrameRenderer {
    private static final long serialVersionUID = 1434872557496889985L;

    public void loadSlot(Slot slot) {
        super.loadSlot(slot);
        Slot inverseSlot = slot.getInverseSlot();
        if (inverseSlot != null) {
            appendText(" \u2194 ");
            appendText(inverseSlot.getBrowserText());
        }
    }
}
