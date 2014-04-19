package edu.stanford.smi.protege.ui;


import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for the value-type facet.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TypeFacetRenderer extends DefaultRenderer {
    private static final long serialVersionUID = -7154991031361614206L;

    public void load(Object o) {
        FrameSlotCombination combination = (FrameSlotCombination) o;
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();
        ValueType type = cls.getTemplateSlotValueType(slot);
        String text = type.toString();
        if (type == ValueType.INSTANCE) {
            Collection clses = cls.getTemplateSlotAllowedClses(slot);
            text = append(text, "of", clses);
        } else if (type == ValueType.CLS) {
            Collection clses = cls.getTemplateSlotAllowedParents(slot);
            text = append(text, "with superclass", clses);
        }
        setMainText(text);
        setGrayedText(!combination.getFrame().isEditable());
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }
    
    private static String append(String text, String separator, Collection clses) {
	    if (!clses.isEmpty()) {
	        text += " " + separator + " ";
	        boolean first = true;
	        Iterator i = clses.iterator();
	        while (i.hasNext()) {
	            Cls cls = (Cls) i.next();
	            if (!first) {
	                text += " or ";
	            }
	            first = false;
	            text += cls.getBrowserText();
	        }
	    }
	    return text;
    }
}
