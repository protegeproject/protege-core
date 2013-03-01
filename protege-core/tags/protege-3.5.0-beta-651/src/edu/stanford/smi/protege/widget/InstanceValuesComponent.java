package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * A ValuesComponent that can be used to edit a list of instances.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @author    Holger Knublauch <holger@smi.stanford.edu>  (minor refactorings)
 */
public class InstanceValuesComponent extends AbstractValuesComponent implements Selectable {
    private static final long serialVersionUID = 4953040207043705676L;
    private Project _project;
    private SelectableList _list;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;

    public InstanceValuesComponent(Project project) {
        _project = project;
        _list = ComponentFactory.createSelectableList(null, true);
        _list.setCellRenderer(FrameRenderer.createInstance());
        _list.addListSelectionListener(new ListSelectionListenerAdapter(this));
        String text = LocalizedText.getText(ResourceKey.ALLOWED_CLASSES);
        LabeledComponent c = new LabeledComponent(text, new JScrollPane(_list));
        c.addHeaderButton(getViewAction());
        c.addHeaderButton(getAddClsesAction());
        c.addHeaderButton(getRemoveClsesAction());
        add(c);
    }

    protected void addUniqueListValues(Collection clses) {
        ComponentUtilities.addUniqueListValues(_list, clses);
        valueChanged();
    }

    public void clearSelection() {
        _list.clearSelection();
    }

    private Action getAddClsesAction() {
        _addAction = new AddAction(ResourceKey.CLASS_ADD) {
            private static final long serialVersionUID = -5345344713282197201L;

            public void onAdd() {
                Collection clses = DisplayUtilities.pickClses(_list, getKnowledgeBase(), getBaseClses());
                addUniqueListValues(clses);
            }
        };
        return _addAction;
    }

    private Collection getBaseClses() {
        Collection baseClses;
        if (isOverride()) {
            baseClses = getInheritedAllowedClses();
            if (baseClses.isEmpty()) {
                baseClses = getKnowledgeBase().getRootClses();
            }
        } else {
            baseClses = getKnowledgeBase().getRootClses();
        }
        return baseClses;
    }

    private Collection getInheritedAllowedClses() {
        Set allowedClses = new HashSet();
        Iterator i = getAssociatedCls().getDirectSuperclasses().iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            allowedClses.addAll(superclass.getTemplateSlotAllowedClses(getSlotInstance()));
        }

        return allowedClses;
    }

    protected Project getProject() {
        return _project;
    }

    private Action getRemoveClsesAction() {
        _removeAction = new RemoveAction(ResourceKey.CLASS_REMOVE, this) {
            private static final long serialVersionUID = 4200700343932259499L;

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
        Collection clses = ComponentUtilities.getListValues(_list);
        return ValueTypeConstraint.getValues(ValueType.INSTANCE, clses);
    }

    private Action getViewAction() {
        return new ViewAction(ResourceKey.CLASS_VIEW, this) {
            private static final long serialVersionUID = -5159948547425769907L;

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

    protected void setSelection(Cls cls) {
        _list.setSelectedValue(cls, true);
    }

    public void setValues(Collection values) {
        Collection clses = ValueTypeConstraint.getAllowedClses(values);
        ComponentUtilities.setListValues(_list, clses);
    }
}
