package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquired a unique value for this own slot in the knowledge base.
 * 
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class UniqueStringWidget extends TextFieldWidget {

    private static final long serialVersionUID = 2383411272010532217L;

    protected String getInvalidTextDescription(String text) {
        String invalidText = null;
        if (text == null || !isUnique(text)) {
            invalidText = "String is not unique";

        }
        return invalidText;
    }

    protected boolean isUnique(String text) {
        Collection frames = getKnowledgeBase().getFramesWithValue(getSlot(), null, false, text);
        boolean isUnique;
        switch (frames.size()) {
            case 0 :
                isUnique = true;
                break;
            case 1 :
                Frame frame = (Frame) CollectionUtilities.getFirstItem(frames);
                isUnique = frame.equals(getInstance());
                break;
            default :
                isUnique = false;
                break;
        }
        return isUnique;
    }
}
