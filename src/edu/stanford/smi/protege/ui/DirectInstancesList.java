package edu.stanford.smi.protege.ui;
//ESCA*JAVA0100

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * The panel that holds the list of direct instances of one or more classes. If
 * only one class is chosen then you can also create new instances of this
 * class.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DirectInstancesList extends SelectableContainer implements Disposable {
    // private static final String SHOW_SUBCLASS_INSTANCES = DirectInstancesList.class.getName()
    //        + ".show_subclass_instances";
    private Collection _clses = Collections.EMPTY_LIST;
    private SelectableList _list;
    private Project _project;
    private AllowableAction _createAction;
    private AllowableAction _copyAction;
    private AllowableAction _deleteAction;
    private HeaderComponent _header;
    private Collection listenedToInstances = new ArrayList();
    private static final int SORT_LIMIT;
    private boolean _showSubclassInstances;
    private LabeledComponent _labeledComponent;

    static {
        SORT_LIMIT = ApplicationProperties.getIntegerProperty("ui.DirectInstancesList.sort_limit", 1000);
    }

    private ClsListener _clsListener = new ClsAdapter() {
        public void directInstanceAdded(ClsEvent event) {
            Instance instance = event.getInstance();
            if (!getModel().contains(instance)) {
                ComponentUtilities.addListValue(_list, instance);
                instance.addFrameListener(_instanceFrameListener);
            }
        }

        public void directInstanceRemoved(ClsEvent event) {
            removeInstance(event.getInstance());
        }
    };

    private FrameListener _clsFrameListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            updateButtons();
        }
    };

    private FrameListener _instanceFrameListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            super.browserTextChanged(event);
            // Log.enter(this, "browserTextChanged", event);
            sort();
            repaint();
        }
    };

    public DirectInstancesList(Project project) {
        _project = project;
        Action viewAction = createViewAction();

        _list = ComponentFactory.createSelectableList(viewAction);
        _list.setCellRenderer(FrameRenderer.createInstance());

        _labeledComponent = new LabeledComponent(null, ComponentFactory.createScrollPane(_list));
        addButtons(viewAction, _labeledComponent);
        _labeledComponent.setFooterComponent(new ListFinder(_list, ResourceKey.INSTANCE_SEARCH_FOR));
        _labeledComponent.setBorder(ComponentUtilities.getAlignBorder());
        add(_labeledComponent, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader(), BorderLayout.NORTH);
        add(panel, BorderLayout.NORTH);

        setSelectable(_list);
        // initializeShowSubclassInstances();
    }

    private void updateLabel() {
        String text;
        Cls cls = getSoleAllowedCls();
        BrowserSlotPattern pattern = (cls == null) ? null : cls.getBrowserSlotPattern();
        if (pattern == null) {
            text = null;
        } else {
            // text = "Instances by ";
            if (pattern.isSimple()) {
                text = pattern.getFirstSlot().getBrowserText();
            } else {
                text = "multiple slots";
            }
        }
        _labeledComponent.setHeaderLabel(text);
    }

    private HeaderComponent createHeader() {
        JLabel label = ComponentFactory.createLabel();
        String instanceBrowserLabel = LocalizedText.getText(ResourceKey.INSTANCE_BROWSER_TITLE);
        String forClassLabel = LocalizedText.getText(ResourceKey.CLASS_EDITOR_FOR_CLASS_LABEL);
        _header = new HeaderComponent(instanceBrowserLabel, forClassLabel, label);
        _header.setColor(Colors.getInstanceColor());
        return _header;
    }

    private void fixRenderer() {
        FrameRenderer frameRenderer = (FrameRenderer) _list.getCellRenderer();
        frameRenderer.setDisplayType(_showSubclassInstances);
    }

    protected void addButtons(Action viewAction, LabeledComponent c) {
        c.addHeaderButton(viewAction);
        c.addHeaderButton(createReferencersAction());
        c.addHeaderButton(createCreateAction());
        c.addHeaderButton(createCopyAction());
        c.addHeaderButton(createDeleteAction());
        c.addHeaderButton(createConfigureAction());
    }

    private void addClsListeners() {
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.addClsListener(_clsListener);
            cls.addFrameListener(_clsFrameListener);
        }
    }

    private void addInstanceListeners() {
        ListModel model = _list.getModel();
        int start = _list.getFirstVisibleIndex();
        int stop = _list.getLastVisibleIndex();
        for (int i = start; i < stop; ++i) {
            Instance instance = (Instance) model.getElementAt(i);
            addInstanceListener(instance);

        }
    }

    private void removeInstanceListeners() {
        Iterator i = listenedToInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.removeFrameListener(_instanceFrameListener);
        }
        listenedToInstances.clear();
    }

    private void addInstanceListener(Instance instance) {
        instance.addFrameListener(_instanceFrameListener);
        listenedToInstances.add(instance);
    }

    protected Action createCreateAction() {
        _createAction = new CreateAction(ResourceKey.INSTANCE_CREATE) {
            public void onCreate() {
                if (!_clses.isEmpty()) {
                    KnowledgeBase kb = _project.getKnowledgeBase();
                    Instance instance = kb.createInstance(null, _clses);
                    if (instance instanceof Cls) {
                        Cls newCls = (Cls) instance;
                        if (newCls.getDirectSuperclassCount() == 0) {
                            newCls.addDirectSuperclass(kb.getRootCls());
                        }
                    }
                    _list.setSelectedValue(instance, true);
                }
            }
        };
        return _createAction;
    }

    protected Action createConfigureAction() {
        return new ConfigureAction() {
            public void loadPopupMenu(JPopupMenu menu) {
                menu.add(createSetDisplaySlotAction());
                menu.add(createShowAllInstancesAction());
            }
        };
    }

    protected JMenuItem createShowAllInstancesAction() {
        Action action = new AbstractAction("Show Subclass Instances") {
            public void actionPerformed(ActionEvent event) {
                setShowAllInstances(!_showSubclassInstances);
            }
        };
        JMenuItem item = new JCheckBoxMenuItem(action);
        item.setSelected(_showSubclassInstances);
        return item;
    }

    //    private void initializeShowSubclassInstances() {
    //        _showSubclassInstances = ApplicationProperties.getBooleanProperty(SHOW_SUBCLASS_INSTANCES, false);
    //        reload();
    //        fixRenderer();
    //    }

    private void setShowAllInstances(boolean b) {
        _showSubclassInstances = b;
        // ApplicationProperties.setBoolean(SHOW_SUBCLASS_INSTANCES, b);
        reload();
        fixRenderer();
    }

    protected Cls getSoleAllowedCls() {
        Cls cls;
        if (_clses.size() == 1) {
            cls = (Cls) CollectionUtilities.getFirstItem(_clses);
        } else {
            cls = null;
        }
        return cls;
    }

    protected JMenu createSetDisplaySlotAction() {
        JMenu menu = ComponentFactory.createMenu("Set Display Slot");
        boolean enabled = false;
        Cls cls = getSoleAllowedCls();
        if (cls != null) {
            BrowserSlotPattern pattern = cls.getBrowserSlotPattern();
            Slot browserSlot = (pattern != null && pattern.isSimple()) ? pattern.getFirstSlot() : null;
            Iterator i = cls.getVisibleTemplateSlots().iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(createSetDisplaySlotAction(slot));
                if (slot.equals(browserSlot)) {
                    item.setSelected(true);
                }
                menu.add(item);
                enabled = true;
            }
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(createSetDisplaySlotMultipleAction());
            if (browserSlot == null) {
                item.setSelected(true);
            }
            menu.add(item);
        }
        menu.setEnabled(enabled);
        return menu;
    }

    protected Action createSetDisplaySlotAction(final Slot slot) {
        return new AbstractAction(slot.getBrowserText(), slot.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                getSoleAllowedCls().setDirectBrowserSlot(slot);
                updateLabel();
                repaint();
            }
        };
    }

    protected Action createSetDisplaySlotMultipleAction() {
        return new AbstractAction("Multiple Slots...") {
            public void actionPerformed(ActionEvent event) {
                Cls cls = getSoleAllowedCls();
                BrowserSlotPattern currentPattern = getSoleAllowedCls().getBrowserSlotPattern();
                MultiSlotPanel panel = new MultiSlotPanel(currentPattern, cls);
                int rval = ModalDialog.showDialog(DirectInstancesList.this, panel, "Multislot Display Pattern",
                        ModalDialog.MODE_OK_CANCEL);
                if (rval == ModalDialog.OPTION_OK) {
                    BrowserSlotPattern pattern = panel.getBrowserTextPattern();
                    if (pattern != null) {
                        cls.setDirectBrowserSlotPattern(pattern);
                    }
                }
                updateLabel();
                repaint();
            }
        };
    }

    protected Action createDeleteAction() {
        _deleteAction = new DeleteInstancesAction(this);
        return _deleteAction;
    }

    protected Action createCopyAction() {
        _copyAction = new MakeCopiesAction(ResourceKey.INSTANCE_COPY, this) {
            protected Instance copy(Instance instance, boolean isDeep) {
                Instance copy = super.copy(instance, isDeep);
                setSelectedInstance(copy);
                return copy;
            }
        };
        return _copyAction;
    }

    protected Action createReferencersAction() {
        return new ReferencersAction(ResourceKey.INSTANCE_VIEW_REFERENCES, this);
    }

    protected Action createViewAction() {
        return new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            public void onView(Object o) {
                _project.show((Instance) o);
            }
        };
    }

    public void dispose() {
        removeClsListeners();
        removeInstanceListeners();
    }

    public JComponent getDragComponent() {
        return _list;
    }

    private SimpleListModel getModel() {
        return (SimpleListModel) _list.getModel();
    }

    private boolean isSelectionEditable() {
        boolean isEditable = true;
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isEditable()) {
                isEditable = false;
                break;
            }
        }
        return isEditable;
    }

    public void onSelectionChange() {
        // Log.enter(this, "onSelectionChange");
        boolean editable = isSelectionEditable();
        ComponentUtilities.setDragAndDropEnabled(_list, editable);
        updateButtons();
    }

    private void removeInstance(Instance instance) {
        ComponentUtilities.removeListValue(_list, instance);
        instance.removeFrameListener(_instanceFrameListener);
    }

    private void removeClsListeners() {
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.removeClsListener(_clsListener);
            cls.removeFrameListener(_clsFrameListener);
        }
    }

    public void setClses(Collection newClses) {
        removeClsListeners();
        _clses = new ArrayList(newClses);
        reload();
        updateButtons();
        addClsListeners();
    }

    public void reload() {
        removeInstanceListeners();
        Object selectedValue = _list.getSelectedValue();
        Set instanceSet = new LinkedHashSet();
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            instanceSet.addAll(getInstances(cls));
        }
        List instances = new ArrayList(instanceSet);
        if (instances.size() <= SORT_LIMIT) {
            Collections.sort(instances, new FrameComparator());
        }
        getModel().setValues(instances);
        if (instances.contains(selectedValue)) {
            _list.setSelectedValue(selectedValue, true);
        } else if (!instances.isEmpty()) {
            _list.setSelectedIndex(0);
        }
        addInstanceListeners();
        reloadHeader(_clses);
        updateLabel();
    }

    private void reloadHeader(Collection clses) {
        StringBuffer text = new StringBuffer();
        Icon icon = null;
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (icon == null) {
                icon = cls.getIcon();
            }
            if (text.length() != 0) {
                text.append(", ");
            }
            text.append(cls.getName());
        }
        JLabel label = (JLabel) _header.getComponent();
        label.setText(text.toString());
        label.setIcon(icon);
    }

    private Collection getInstances(Cls cls) {
        Collection instances;
        if (_showSubclassInstances) {
            instances = cls.getInstances();
        } else {
            instances = cls.getDirectInstances();
        }
        if (!_project.getDisplayHiddenFrames()) {
            instances = removeHiddenInstances(instances);
        }
        return instances;
    }

    private static Collection removeHiddenInstances(Collection instances) {
        Collection visibleInstances = new ArrayList(instances);
        Iterator i = visibleInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isVisible()) {
                i.remove();
            }
        }
        return visibleInstances;
    }

    public void sort() {
        _list.setListenerNotificationEnabled(false);
        Object selectedValue = _list.getSelectedValue();
        List instances = new ArrayList(getModel().getValues());
        if (instances.size() <= SORT_LIMIT) {
            Collections.sort(instances, new FrameComparator());
        }
        getModel().setValues(instances);
        _list.setSelectedValue(selectedValue);
        _list.setListenerNotificationEnabled(true);
    }

    public void setSelectedInstance(Instance instance) {
        _list.setSelectedValue(instance, true);
        updateButtons();
    }

    private void updateButtons() {
        Cls cls = (Cls) CollectionUtilities.getFirstItem(_clses);
        _createAction.setEnabled(cls == null ? false : cls.isConcrete());
        Instance instance = (Instance) getSoleSelection();
        boolean allowed = instance != null && instance instanceof SimpleInstance;
        _copyAction.setAllowed(allowed);
    }

    /**
     * Does nothing anymore. This functionality moved to the menu button.
     * 
     * @deprecated
     */
    public void setShowDisplaySlotPanel(boolean b) {

    }
}