package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring a list of strings.  This is of limited utility but is included for completeness.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class StringListWidget extends AbstractListWidget {
    private static final long serialVersionUID = 5874778974979833802L;
    private AllowableAction _createAction;
    private AllowableAction _removeAction;

    private Action getCreateAction() {
        _createAction = new CreateAction(ResourceKey.VALUE_ADD) {
            private static final long serialVersionUID = 4889608712670405498L;

            public void onCreate() {
                handleCreateAction();
            }
        };
        return _createAction;
    }

    private Action getEditAction() {
        return new ViewAction(ResourceKey.VALUE_VIEW, this) {
            private static final long serialVersionUID = -1543226916049064459L;

            public void onView(Object o) {
                handleViewAction((String) o);
            }
        };
    }

    private Action getRemoveAction() {
        _removeAction = new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = -7407688724548195021L;

            public void onRemove(Collection strings) {
                handleRemoveAction(strings);
            }
        };
        return _removeAction;
    }

    protected void handleCreateAction() {
        String s = DisplayUtilities.editString(StringListWidget.this, "Create String Value", null, null);
        if (s != null) {
            addItem(s);
        }
    }

    protected void handleRemoveAction(Collection strings) {
        removeItems(strings);
    }

    protected void handleViewAction(String str) {
        String s = DisplayUtilities.editString(StringListWidget.this, "Edit String Value", str, null);
        if (s != null) {
            replaceItem(str, s);
        }
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
            boolean isString = equals(cls.getTemplateSlotValueType(slot), ValueType.STRING);
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isString && isMultiple;
        }
        return isSuitable;
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
    	
        _createAction.setAllowed(b);
        _removeAction.setAllowed(b);
    }
}
