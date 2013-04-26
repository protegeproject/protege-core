package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring and displaying a list of floating point values.  This is probably pointless but it is 
 * included for completeness.
 *
 * @author  Ray Fergerson
 */
public class FloatListWidget extends AbstractListWidget {

    private static final long serialVersionUID = 3428837978085767231L;

    private Action getCreateAction() {
        return new CreateAction(ResourceKey.VALUE_ADD) {
            private static final long serialVersionUID = -8103626732943969984L;

            public void onCreate() {
                String s = DisplayUtilities.editString(FloatListWidget.this, "Create Float Value", null,
                        new FloatValidator());
                if (s != null) {
                    addItem(new Float(s));
                }
            }
        };
    }

    private Action getDeleteAction() {
        return new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = -5242502708398191556L;

            public void onRemove(Collection elements) {
                removeItems(elements);
            }
        };
    }

    private Action getEditAction() {
        return new ViewAction(ResourceKey.VALUE_VIEW, this) {
            private static final long serialVersionUID = 1016994137862284684L;

            public void onView(Object o) {
                String s = DisplayUtilities.editString(FloatListWidget.this, "Edit Float Value", o.toString(), null);
                if (s != null) {
                    replaceItem(o, new Float(s));
                }
            }
        };
    }

    public void initialize() {
        Action editAction = getEditAction();
        super.initialize(editAction);
        addButton(editAction);
        addButton(getCreateAction());
        addButton(getDeleteAction());
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            boolean isFloat = cls.getTemplateSlotValueType(slot) == ValueType.FLOAT;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isFloat && isMultiple;
        }
        return isSuitable;
    }
}