package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * The left upper display of the classes tab. This holds the tree, the
 * relationship-selection drop-down list, and the class find component.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsesPanel extends SelectableContainer {
    // protected final static String SUBCLASS_RELATIONSHIP = "Class Hierarchy";
    // protected final static String REFERENCED_RELATIONSHIP = "Class Relations";

    protected Project _project;
    protected LabeledComponent _labeledComponent;
    // protected JComboBox _relationshipView;
    protected AllowableAction _createAction;
    protected Action _viewAction;
    protected AllowableAction _deleteAction;
    protected SubclassPane _subclassPane;
    protected RelationshipPane _relationshipPane;
    protected HeaderComponent _clsBrowserHeader;
    protected static final String ResourcesKey = null;

    //    protected SwitchableActionListener _relationshipListener = new SwitchableActionListener() {
    //        public void changed(ActionEvent e) {
    //            relationshipChanged();
    //        }
    //    };

    public ClsesPanel(Project project) {
        _project = project;

        _viewAction = getViewClsAction();
        _createAction = getCreateClsAction();
        _deleteAction = getDeleteClsAction();
        createPanes();
        String subclassesLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
        _labeledComponent = new LabeledComponent(subclassesLabel, _subclassPane, true);
        _labeledComponent.setBorder(ComponentUtilities.getAlignBorder());

        _labeledComponent.addHeaderButton(_viewAction);
        _labeledComponent.addHeaderButton(new ClsReferencersAction(this));
        _labeledComponent.addHeaderButton(_createAction);
        _labeledComponent.addHeaderButton(_deleteAction);
        _labeledComponent.addHeaderButton(createConfigureAction());

        add(_labeledComponent, BorderLayout.CENTER);
        add(createClsBrowserHeader(), BorderLayout.NORTH);
        setSelectable(_subclassPane);
        updateDeleteActionState();

    }

    public LabeledComponent getLabeledComponent() {
        return _labeledComponent;
    }

    protected HeaderComponent createClsBrowserHeader() {
        JLabel label = ComponentFactory.createLabel(_project.getName(), Icons.getProjectIcon(), SwingConstants.LEFT);
        String forProject = LocalizedText.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
        String classBrowser = LocalizedText.getText(ResourceKey.CLASS_BROWSER_TITLE);
        return new HeaderComponent(classBrowser, forProject, label);
    }

    protected void createPanes() {
        _subclassPane = createSubclassPane(_viewAction, getKnowledgeBase().getRootCls(), _createAction, _deleteAction);
        _relationshipPane = createRelationshipPane(_viewAction);
    }

    protected RelationshipPane createRelationshipPane(Action viewAction) {
        return new RelationshipPane(viewAction);
    }

    //    protected JComponent createRelationshipView() {
    //        _relationshipView = ComponentFactory.createComboBox();
    //        Dimension d = _relationshipView.getPreferredSize();
    //        d.width = Math.max(d.width, 150);
    //        _relationshipView.setPreferredSize(d);
    //        _relationshipView.addActionListener(_relationshipListener);
    //        _relationshipView.setRenderer(FrameRenderer.createInstance());
    //        return _relationshipView;
    //    }

    protected SubclassPane createSubclassPane(Action viewAction, Cls root, Action createAction, Action action) {
        return new SubclassPane(viewAction, root, createAction, action);
    }

    protected void enableButton(AllowableAction action, boolean enabled) {
        if (action != null) {
            action.setAllowed(enabled);
        }
    }

    protected void enableButtons(boolean enable) {
        enableButton(_createAction, enable);
        // updateDeleteActionState();
    }

    public JTree getClsesTree() {
        return (JTree) _subclassPane.getDropComponent();
    }

    protected AllowableAction getCreateClsAction() {
        return new CreateClsAction() {
            public void onCreate() {
                Collection parents = _subclassPane.getSelection();
                if (!parents.isEmpty()) {
                    Cls cls = getKnowledgeBase().createCls(null, parents);
                    _subclassPane.extendSelection(cls);
                }
            }
        };
    }

    protected AllowableAction getDeleteClsAction() {
        AllowableAction action = new DeleteClsAction(this) {
            public void onAboutToDelete(Object o) {
                _subclassPane.removeSelection();
            }

            public void onSelectionChange() {
                updateDeleteActionState();
            }
        };
        action.setEnabled(true);
        return action;
    }

    protected Action createConfigureAction() {
        return new ConfigureAction() {
            public void loadPopupMenu(JPopupMenu menu) {
                menu.add(createShowSubclassesAction());
                menu.add(createShowAllRelationsAction());
                Iterator i = getRelationSlots().iterator();
                while (i.hasNext()) {
                    Slot slot = (Slot) i.next();
                    menu.add(createShowRelationAction(slot));
                }
            }
        };
    }

    public Collection getRelationSlots() {
        Collection slots = new HashSet();
        Collection c = getSelection();
        if (c.size() == 1) {
            Frame selectedFrame = (Frame) c.iterator().next();
            if (selectedFrame instanceof Cls) {
                Cls selectedCls = (Cls) selectedFrame;
                Iterator i = selectedCls.getTemplateSlots().iterator();
                while (i.hasNext()) {
                    Slot slot = (Slot) i.next();
                    ValueType type = selectedCls.getTemplateSlotValueType(slot);
                    if (type == ValueType.INSTANCE || type == ValueType.CLS) {
                        slots.add(slot);
                    }
                }
            }
            Iterator j = selectedFrame.getOwnSlots().iterator();
            while (j.hasNext()) {
                Slot slot = (Slot) j.next();
                ValueType type = selectedFrame.getOwnSlotValueType(slot);
                if (!slot.isSystem() && (type == ValueType.INSTANCE || type == ValueType.CLS)) {
                    slots.add(slot);
                }
            }
        }
        return slots;
    }

    protected Action createShowSubclassesAction() {
        return new StandardAction(ResourceKey.CLASS_BROWSER_SHOW_CLASS_HIERARCHY_MENU_ITEM) {
            public void actionPerformed(ActionEvent event) {
                _subclassPane.setSelectedClses(getSelection());
                loadComponent(_subclassPane, ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
                enableButtons(true);
            }
        };
    }

    protected Action createShowAllRelationsAction() {
        return new StandardAction(ResourceKey.CLASS_BROWSER_SHOW_ALL_RELATIONS_MENU_ITEM) {
            public void actionPerformed(ActionEvent event) {
                _relationshipPane.load((Frame) getSoleSelection(), null);
                loadComponent(_relationshipPane, ResourceKey.CLASS_BROWSER_ALL_RELATIONS_LABEL);
                enableButtons(false);
            }
        };
    }

    protected Action createShowRelationAction(final Slot slot) {
        String showLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_SHOW_RELATION_MENU_ITEM, slot
                .getBrowserText());
        return new StandardAction(showLabel) {
            public void actionPerformed(ActionEvent event) {
                _relationshipPane.load((Frame) getSoleSelection(), slot);
                loadComponent(_relationshipPane, slot.getBrowserText());
                enableButtons(false);
            }
        };
    }

    protected JComponent getDisplayedComponent() {
        return (JComponent) _labeledComponent.getCenterComponent();
    }

    public Cls getDisplayParent() {
        return _subclassPane.getDisplayParent();
    }

    public JComponent getDropComponent() {
        return _subclassPane.getDropComponent();
    }

    protected KnowledgeBase getKnowledgeBase() {
        return _project.getKnowledgeBase();
    }

    /**
     * 
     * @return edu.stanford.smi.protege.model.Project
     */
    public Project getProject() {
        return _project;
    }

    protected Selectable getRelationshipPane() {
        return _relationshipPane;
    }

    //    protected JComboBox getRelationshipView() {
    //        return _relationshipView;
    //    }
    //
    public Collection getSelection() {
        return ((Selectable) getDisplayedComponent()).getSelection();
    }

    protected SubclassPane getSubclassPane() {
        return _subclassPane;
    }

    protected Action getViewClsAction() {
        return new ViewClsAction(this) {
            public void onView(Object o) {
                showInstance((Cls) o);
            }
        };
    }

    protected boolean isDisplayingSubclasses() {
        return _labeledComponent.getCenterComponent() == _subclassPane;
    }

    protected void loadComponent(Selectable component) {
        loadComponent(component, "");
    }

    protected void loadComponent(Selectable component, ResourceKey key) {
        loadComponent(component, LocalizedText.getText(key));
    }

    protected void loadComponent(Selectable component, String title) {
        _labeledComponent.setCenterComponent((JComponent) component);
        _labeledComponent.setHeaderLabel(title);
        setSelectable(component);
    }

    private Component getView() {
        return _labeledComponent.getCenterComponent();
    }

    //    public void notifySelectionListeners() {
    //        super.notifySelectionListeners();
    //        if (_relationshipView != null) {
    //            relationshipViewLoad();
    //        }
    //    }

    //    protected void relationshipChanged() {
    //        _relationshipListener.disable();
    //        reload();
    //        // setSelection(selectedCls);
    //        _relationshipListener.enable();
    //    }

    //    protected void relationshipViewLoad() {
    //        _relationshipListener.disable();
    //        Object selection = _relationshipView.getSelectedItem();
    //        if (selection == null) {
    //            selection = SUBCLASS_RELATIONSHIP;
    //        }
    //        Collection slots = new HashSet();
    //        Collection c = getSelection();
    //        if (c.size() == 1) {
    //            Frame selectedFrame = (Frame) c.iterator().next();
    //            if (selectedFrame instanceof Cls) {
    //                Cls selectedCls = (Cls) selectedFrame;
    //                Iterator i = selectedCls.getTemplateSlots().iterator();
    //                while (i.hasNext()) {
    //                    Slot slot = (Slot) i.next();
    //                    // ValueType type = selectedCls.getTemplateSlotValueType(slot);
    //                    ValueType type = slot.getValueType();
    //                    if (type == ValueType.INSTANCE || type == ValueType.CLS) {
    //                        slots.add(slot);
    //                    }
    //                }
    //            }
    //            Iterator j = selectedFrame.getOwnSlots().iterator();
    //            while (j.hasNext()) {
    //                Slot slot = (Slot) j.next();
    //                ValueType type = selectedFrame.getOwnSlotValueType(slot);
    //                if (!slot.isSystem() && (type == ValueType.INSTANCE || type == ValueType.CLS)) {
    //                    slots.add(slot);
    //                }
    //            }
    //        }
    //        List choices = new ArrayList(slots);
    //        Collections.sort(choices, new FrameComparator());
    //        choices.add(0, SUBCLASS_RELATIONSHIP);
    //        choices.add(1, REFERENCED_RELATIONSHIP);
    //        if (!choices.contains(selection)) {
    //            choices.add(selection);
    //        }
    //        _relationshipView.setModel(new DefaultComboBoxModel(choices.toArray()));
    //        _relationshipView.setSelectedItem(selection);
    //        _relationshipListener.enable();
    //    }

    /**
     * reload the tree as a result of a change in the displayed relationship
     */
    //    protected void reload() {
    //        Frame selectedFrame = (Frame) CollectionUtilities.getFirstItem(getSelection());
    //        Object selection = null;
    //        if (_relationshipView != null) {
    //            selection = _relationshipView.getSelectedItem();
    //        }
    //        if (selection == null) {
    //            selection = SUBCLASS_RELATIONSHIP;
    //        }
    //        if (selection.equals(SUBCLASS_RELATIONSHIP)) {
    //            if (selectedFrame instanceof Cls) {
    //                _subclassPane.setSelectedCls((Cls) selectedFrame);
    //            }
    //            loadComponent(_subclassPane);
    //            enableButtons(true);
    //        } else if (selection.equals(REFERENCED_RELATIONSHIP)) {
    //            _relationshipPane.load(selectedFrame, null);
    //            loadComponent(_relationshipPane);
    //            enableButtons(false);
    //        } else {
    //            Slot slot = (Slot) selection;
    //            _relationshipPane.load(selectedFrame, slot);
    //            loadComponent(_relationshipPane);
    //            enableButtons(false);
    //        }
    //        notifySelectionListeners();
    //    }
    /**
     * An obscure method to change the displayed parent of the selected class.
     * Imagine a selected class A with two parents "B" and "C". Currently "A" is
     * selected beneath "B". Calling setDisplayParent("C") will cause "A" to be
     * displayed beneath "C". This is the method used by the component below the
     * classes panel in the classes tab.
     */
    public void setDisplayParent(Cls cls) {
        if (isDisplayingSubclasses()) {
            _subclassPane.setDisplayParent(cls);
        }
    }

    public void setExpandedCls(Cls cls, boolean expanded) {
        if (isDisplayingSubclasses()) {
            _subclassPane.setExpandedCls(cls, expanded);
        } else {
        }
    }

    public void setFinderComponent(JComponent c) {
        _subclassPane.setFinderComponent(c);
    }

    public void setRenderer(DefaultRenderer renderer) {
        _subclassPane.setRenderer(renderer);
    }

    public void setSelectedCls(Cls cls) {
        if (isDisplayingSubclasses()) {
            _subclassPane.setSelectedCls(cls);
        } else {
        }
    }

    protected void showInstance(Instance instance) {
        _project.show(instance);
    }

    protected void updateDeleteActionState() {
        if (_deleteAction != null) {
            boolean isEditable = true;
            Iterator i = getSelection().iterator();
            while (i.hasNext()) {
                Frame frame = (Frame) i.next();
                if (!frame.isEditable()) {
                    isEditable = false;
                    break;
                }
            }
            boolean isCorrectView = getView() == _subclassPane;
            _deleteAction.setAllowed(isEditable && isCorrectView);
        }
    }
}