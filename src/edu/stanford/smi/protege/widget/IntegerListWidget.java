package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * A slot widget used to acquire and display a list of integral values.  This is not too useful but is included for
 * completeness.
 *
 * @author Ray Fergerson
 */
public class IntegerListWidget extends AbstractListWidget {

    private Action getCreateAction() {
        return new CreateAction(ResourceKey.VALUE_ADD) {
            public void onCreate() {
                handleCreateAction();
            }
        };
    }

    private Action getDeleteAction() {
        return new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            public void onRemove(Collection integers) {
                handleRemoveAction(integers);
            }
        };
    }

    private Action getEditAction() {
        return new ViewAction(ResourceKey.VALUE_VIEW, this) {
            public void onView(Object o) {
                handleViewAction((Integer) o);
            }
        };
    }

    protected void handleCreateAction() {
        String s = DisplayUtilities.editString(IntegerListWidget.this, "Create Integer Value", null, null);
        if (s != null) {
            addItem(new Integer(s));
        }
    }

    protected void handleRemoveAction(Collection integers) {
        removeItems(integers);
    }

    protected void handleViewAction(Integer integer) {
        String s = DisplayUtilities.editString(IntegerListWidget.this, "Edit Integer Value", integer.toString(), null);
        if (s != null) {
            replaceItem(integer, new Integer(s));
        }
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
            boolean isInteger = equals(cls.getTemplateSlotValueType(slot), ValueType.INTEGER);
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isInteger && isMultiple;
        }
        return isSuitable;
    }
}
