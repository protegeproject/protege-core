package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * ValuesComponent implementation for value-type=symbol slots.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class SymbolValuesComponent extends AbstractValuesComponent implements Selectable {

    private static final long serialVersionUID = 3508078941013882199L;
    private SelectableList _list;
    private AllowableAction _createAction;
    private AllowableAction _editAction;
    private AllowableAction _removeAction;

    public SymbolValuesComponent() {
        Action editAction = getEditAction();
        _list = ComponentFactory.createSelectableList(editAction, true);
        _list.addListSelectionListener(new ListSelectionListenerAdapter(this));
        String text = LocalizedText.getText(ResourceKey.ALLOWED_VALUES);
        LabeledComponent c = new LabeledComponent(text, new JScrollPane(_list));
        c.addHeaderButton(editAction);
        c.addHeaderButton(getCreateAction());
        c.addHeaderButton(getRemoveAction());
        add(c);
    }

    public void clearSelection() {
        _list.clearSelection();
    }

    private Action getCreateAction() {
        _createAction = new CreateAction(ResourceKey.VALUE_CREATE) {
            private static final long serialVersionUID = -3411238108344825853L;

            public void onCreate() {
                String s = DisplayUtilities.editString(_list, "Create Symbol", null, new SymbolValidator());
                if (s != null && s.length() > 0) {
                    ComponentUtilities.addUniqueListValues(_list, CollectionUtilities.createCollection(s));
                    valueChanged();
                }
            }
        };
        return _createAction;
    }

    private Action getEditAction() {
        _editAction = new ViewAction(ResourceKey.VALUE_VIEW, this) {
            private static final long serialVersionUID = -1379237486395269996L;

            public void onView(Object o) {
                String s = DisplayUtilities.editString(_list, "Edit Symbol", o, new SymbolValidator());
                if (s != null) {
                    ComponentUtilities.replaceListValue(_list, o, s);
                    valueChanged();
                }
            }
        };
        return _editAction;
    }

    private Action getRemoveAction() {
        _removeAction = new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
            private static final long serialVersionUID = 3520724966158279341L;

            public void onRemove(Collection values) {
                ComponentUtilities.removeListValues(_list, values);
                valueChanged();
            }
        };
        return _removeAction;
    }

    public Collection getSelection() {
        return _list.getSelection();
    }

    public Collection getValues() {
        Collection values = ComponentUtilities.getListValues(_list);
        return ValueTypeConstraint.getValues(ValueType.SYMBOL, values);
    }

    public void setEditable(boolean b) {
        _createAction.setAllowed(b);
        _editAction.setAllowed(b);
        _removeAction.setAllowed(b);
    }

    public void setValues(Collection values) {
        Collection allowedValues = ValueTypeConstraint.getAllowedValues(values);
        ComponentUtilities.setListValues(_list, allowedValues);
    }
}
