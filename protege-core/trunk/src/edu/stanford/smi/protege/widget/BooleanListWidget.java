package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring a list of booleans.  This probably isn't really useful but it needs to be here for
 * completeness.  If the user selects cardinality multiple boolean, something has to show up!
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class BooleanListWidget extends AbstractListWidget {
    private static final long serialVersionUID = -4884073729270631387L;
    private static final Collection ALLOWED_VALUES = new ArrayList();

    static {
        ALLOWED_VALUES.add(Boolean.TRUE);
        ALLOWED_VALUES.add(Boolean.FALSE);
    }

    private Action getCreateAction() {
        return new CreateAction(ResourceKey.VALUE_CREATE) {
            private static final long serialVersionUID = -4209277615930518731L;

            public void onCreate() {
                handleCreateAction();
            }
        };
    }

    private Action getEditAction() {
        return new ViewAction(ResourceKey.VALUE_VIEW, this) {
            private static final long serialVersionUID = 8093323243741620L;

            public void onView(Object o) {
                handleViewAction((Boolean) o);
            }
        };
    }

    private Action getRemoveAction() {
        return new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = 3228703105413852749L;

            public void onRemove(Collection values) {
                handleRemoveAction(values);
            }
        };
    }

    protected void handleCreateAction() {
        Boolean b = edit("Create Boolean Value", null);
        if (b != null) {
            addItem(b);
        }
    }

    protected void handleRemoveAction(Collection values) {
        removeItems(values);
    }

    protected void handleViewAction(Boolean start) {
        Boolean end = edit("Edit Boolean Value", start);
        if (end != null) {
            replaceItem(start, end);
        }
    }

    private Boolean edit(String label, Boolean initialValue) {
        return (Boolean) DisplayUtilities.pickSymbol(this, label, initialValue, ALLOWED_VALUES);
    }

    public void initialize() {
        Action editAction = getEditAction();
        super.initialize(editAction);
        addButton(editAction);
        addButton(getCreateAction());
        addButton(getRemoveAction());
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            boolean isString = cls.getTemplateSlotValueType(slot) == ValueType.BOOLEAN;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isString && isMultiple;
        }
        return isSuitable;
    }
}
