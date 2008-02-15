package edu.stanford.smi.protege.ui;

import java.util.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Render the slot default value facet.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultValueRenderer extends DefaultRenderer {

    public void load(Object value) {
        FrameSlotCombination combination = (FrameSlotCombination) value;
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();
        Collection values = cls.getTemplateSlotDefaultValues(slot);
        setMainText(CollectionUtilities.toString(values));
        if (!cls.isEditable()) {
            setGrayedText(true);
        }
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }
}
