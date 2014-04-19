package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Widget for acquiring a list of symbol values.  This widget is of limited use but is included for completeness.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SymbolListWidget extends AbstractListWidget {

    private static final long serialVersionUID = -5238094337376212437L;

    private String edit(String label, Object initialValue) {
        Collection allowedValues = getCls().getTemplateSlotAllowedValues(getSlot());
        return (String) DisplayUtilities.pickSymbol(this, label, initialValue, allowedValues);
    }

    private Action getCreateAction() {
        return new CreateAction(ResourceKey.VALUE_ADD) {
            private static final long serialVersionUID = -5937947639182802969L;

            public void onCreate() {
                handleCreateAction();
            }
        };
    }

    private Action getEditAction() {
        return new ViewAction(ResourceKey.VALUE_VIEW, this) {
            private static final long serialVersionUID = -6781849184410952271L;

            public void onView(Object o) {
                handleViewAction((String) o);
            }
        };
    }

    private Action getRemoveAction() {
        return new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = -6176315941870004648L;

            public void onRemove(Collection values) {
                handleRemoveAction(values);
            }
        };
    }

    protected void handleCreateAction() {
        String s = edit("Create Symbol", null);
        if (s != null) {
            addItem(s);
        }
    }

    protected void handleRemoveAction(Collection values) {
        removeItems(values);
    }

    protected void handleViewAction(String symbol) {
        String s = edit("Edit Symbol", symbol);
        if (s != null) {
            replaceItem(symbol, s);
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
            boolean isString = equals(cls.getTemplateSlotValueType(slot), ValueType.SYMBOL);
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isString && isMultiple;
        }
        return isSuitable;
    }
}
