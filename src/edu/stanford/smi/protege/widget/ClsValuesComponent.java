package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Value type component implementation for value-type=Cls.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class ClsValuesComponent extends AbstractValuesComponent implements Selectable {
    private static final long serialVersionUID = -7439215898289435911L;
    private Project _project;
    private SelectableList _list;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;

    public ClsValuesComponent(Project p) {
        _project = p;
        _list = ComponentFactory.createSelectableList(null, true);
        _list.setCellRenderer(FrameRenderer.createInstance());
        _list.addListSelectionListener(new ListSelectionListenerAdapter(this));
        String text = LocalizedText.getText(ResourceKey.ALLOWED_SUPERCLASSES);
        LabeledComponent c = new LabeledComponent(text, new JScrollPane(_list));
        c.addHeaderButton(getViewAction());
        c.addHeaderButton(getAddAction());
        c.addHeaderButton(getRemoveAction());
        add(c);
    }

    public void clearSelection() {
        _list.clearSelection();
    }

    private Action getAddAction() {
        _addAction = new AddAction(ResourceKey.CLASS_ADD) {
            private static final long serialVersionUID = -8162486295759857232L;

            public void onAdd() {
                Collection clses = DisplayUtilities.pickClses(_list, getKnowledgeBase(), getBaseClses());
                ComponentUtilities.addUniqueListValues(_list, clses);
                valueChanged();
            }
        };
        return _addAction;
    }

    private Collection getBaseClses() {
        Collection baseClses;
        if (isOverride()) {
            baseClses = getInheritedParents();
            if (baseClses.isEmpty()) {
                baseClses = getKnowledgeBase().getRootClses();
            }
        } else {
            baseClses = getKnowledgeBase().getRootClses();
        }
        return baseClses;
    }

    private Collection getInheritedParents() {
        Set allowedClses = new HashSet();
        Iterator i = getAssociatedCls().getDirectSuperclasses().iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            allowedClses.addAll(superclass.getTemplateSlotAllowedParents(getSlotInstance()));
        }

        return allowedClses;
    }

    private Action getRemoveAction() {
        _removeAction = new RemoveAction(ResourceKey.CLASS_REMOVE, this) {
            private static final long serialVersionUID = -528378262571013235L;

            public void onRemove(Collection clses) {
                ComponentUtilities.removeListValues(_list, clses);
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
        return ValueTypeConstraint.getValues(ValueType.CLS, values);
    }

    private Action getViewAction() {
        return new ViewAction(ResourceKey.CLASS_VIEW, this) {
            private static final long serialVersionUID = 7713020762470047677L;

            public void onView(Object o) {
                Cls cls = (Cls) o;
                _project.show(cls);
            }
        };
    }

    public void setEditable(boolean b) {
        _addAction.setAllowed(b);
        _removeAction.setAllowed(b);
    }

    public void setValues(Collection values) {
        Collection allowedParents = ValueTypeConstraint.getAllowedParents(values);
        ComponentUtilities.setListValues(_list, allowedParents);
    }
}
