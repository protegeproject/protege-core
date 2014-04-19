package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * This panel displays the superclasses of a given class and allows the user to
 * add or remove a superclass.
 * 
 * The original intent of the class was to display an arbitrary (inverse
 * relationship) but the only relationship implemented at the moment if the
 * "inverse of subclass".
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @author Holger Knublauch <holger@smi.stanford.edu>(minor refactorings)
 */
public class ClsInverseRelationshipPanel extends SelectableContainer {
    private static final long serialVersionUID = -2313695350432734056L;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;
    private Project _project;
    private SelectableList _list;
    private Cls _viewedCls;

    private ClsListener _clsListener = new ClsAdapter() {
        public void directSuperclassAdded(ClsEvent event) {
            ComponentUtilities.addListValue(_list, event.getSuperclass());
        }

        public void directSuperclassRemoved(ClsEvent event) {
            boolean wasEnabled = setNotificationsEnabled(false);
            ComponentUtilities.removeListValue(_list, event.getSuperclass());
            setNotificationsEnabled(wasEnabled);
        }
    };

    public ClsInverseRelationshipPanel(Project p) {
        _project = p;
        setLayout(new BorderLayout());
        _list = createList();
        String superclassesLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_SUPERCLASSES_LABEL);
        LabeledComponent c = new LabeledComponent(superclassesLabel, ComponentFactory.createScrollPane(_list));
        addHeaderButtons(c);
        add(c);
        setSelectable(_list);

        setPreferredSize(new Dimension(0, 100));
    }

    protected void addHeaderButtons(LabeledComponent labeledComponent) {
        labeledComponent.addHeaderButton(getAddParentAction());
        labeledComponent.addHeaderButton(getRemoveParentAction());
    }

    private boolean canBeSuperclass(Cls cls) {
        boolean canBeSuperclass = true;
        if (cls == _viewedCls || cls.hasSuperclass(_viewedCls) || _viewedCls.hasSuperclass(cls)) {
            canBeSuperclass = false;
            ModalDialog.showMessageDialog(this, cls.getName() + " can not be a superclass of " + _viewedCls.getName());
        }
        return canBeSuperclass;
    }

    private static SelectableList createList() {
        SelectableList list = ComponentFactory.createSelectableList(null);
        list.setCellRenderer(FrameRenderer.createInstance());
        return list;
    }

    private void doRemoveSuperclasses(Collection superclasses) {
        Iterator i = superclasses.iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            _viewedCls.removeDirectSuperclass(superclass);
        }
    }

    private Action getAddParentAction() {
        _addAction = new AddAction(ResourceKey.CLASS_ADD_SUPERCLASS) {
            private static final long serialVersionUID = -3168791335270944513L;

            public void onAdd() {
                if (_viewedCls != null) {
                    addSuperclass();
                }
            }
        };
        return _addAction;
    }

    private void addSuperclass() {
        KnowledgeBase kb = getKnowledgeBase();
        Slot slot = kb.getSystemFrames().getDirectSuperclassesSlot();
        Cls type = _viewedCls.getDirectType();
        Collection parents = type.getTemplateSlotAllowedParents(slot);
        Iterator i = DisplayUtilities.pickClses(this, kb, parents).iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            if (canBeSuperclass(parent)) {
                _viewedCls.addDirectSuperclass(parent);
            }
        }
    }

    private KnowledgeBase getKnowledgeBase() {
        return _project.getKnowledgeBase();
    }

    protected Project getProject() {
        return _project;
    }

    private Action getRemoveParentAction() {
        _removeAction = new RemoveAction(ResourceKey.CLASS_REMOVE_SUPERCLASS, this) {
            private static final long serialVersionUID = -1774220070697400829L;

            public void onRemove(Collection values) {
                if (_viewedCls != null) {
                    removeSuperclasses(values);
                }
            }
        };
        return _removeAction;
    }

    protected Cls getViewedCls() {
        return _viewedCls;
    }

    public void onSelectionChange() {
        updateButtons();
    }

    private void reload() {
        Collection values = (_viewedCls == null) ? Collections.EMPTY_LIST : _viewedCls.getDirectSuperclasses();
        ComponentUtilities.setListValues(_list, values);
    }

    private void removeSuperclasses(Collection superclasses) {
        Set allSuperclasses = new HashSet(_viewedCls.getDirectSuperclasses());
        if (superclasses.size() != allSuperclasses.size()) {
            allSuperclasses.removeAll(superclasses);
            Cls newSelection = (Cls) CollectionUtilities.getFirstItem(allSuperclasses);
            setSelection(newSelection);
            doRemoveSuperclasses(superclasses);
        }
    }

    public void setCls(Cls newCls, Cls parent) {
        // Log.enter(this, "setCls", newCls, parent);
        if (!equals(newCls, _viewedCls)) {
            if (_viewedCls != null) {
                _viewedCls.removeClsListener(_clsListener);
            }
            _viewedCls = newCls;
            if (_viewedCls != null) {
                _viewedCls.addClsListener(_clsListener);
            }
            reload();
        }
        boolean wasPosting = setNotificationsEnabled(false);
        setSelection(parent);
        setNotificationsEnabled(wasPosting);

        updateButtons();
    }

    protected void setSelection(Cls parent) {
        _list.setSelectedValue(parent, true);
    }

    protected void updateButtons() {
        boolean canModify = (_viewedCls != null) && _viewedCls.isEditable();
        _addAction.setEnabled(canModify);
        boolean canRemove = canModify && getSelection().size() < _viewedCls.getDirectSuperclassCount();
        _removeAction.setAllowed(canRemove);
    }
}