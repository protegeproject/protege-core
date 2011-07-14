package edu.stanford.smi.protege.ui;
//ESCA*JAVA0100

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.action.DeleteInstancesAction;
import edu.stanford.smi.protege.action.MakeCopiesAction;
import edu.stanford.smi.protege.action.ReferencersAction;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.ConcurrentListModel;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.ViewAction;

/**
 * The panel that holds the list of direct instances of one or more classes. If
 * only one class is chosen then you can also create new instances of this
 * class.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DirectInstancesList extends SelectableContainer implements Disposable {

    private static final long serialVersionUID = 3123829893591425192L;

    public final static String SORT_LIMIT_PROPERTY = "ui.DirectInstancesList.sort_limit";
    public static final int SORT_LIMIT;
    static {
        SORT_LIMIT = ApplicationProperties.getIntegerProperty(SORT_LIMIT_PROPERTY, 1000);
    }

    private Collection<Cls> _clses = Collections.EMPTY_LIST;
    private SelectableList _list;
    private Project _project;
    private AllowableAction _createAction;
    private AllowableAction _copyAction;
    private AllowableAction _deleteAction;
    private HeaderComponent _header;
    private boolean _showSubclassInstances;
    private LabeledComponent _labeledComponent;

    private AddInstancesRunner background;


    private ClsListener _clsListener = new ClsAdapter() {
        @Override
        public void directInstanceAdded(ClsEvent event) {
            synchronized (DirectInstancesList.this) {
                if (background != null && !event.isReplacementEvent()) {
                    background.setDeferredSelection(event.getInstance());
                    background.addChange(event);
                }
            }
        }

        @Override
        public void directInstanceRemoved(ClsEvent event) {
            synchronized (DirectInstancesList.this) {
                if (background != null && !event.isReplacementEvent()) {
                    background.addChange(event);
                }
            }
        }
    };

    private FrameListener _clsFrameListener = new FrameAdapter() {
        @Override
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            background.addChange(event);
        }
    };
    
    private KnowledgeBaseListener kbListener = new KnowledgeBaseAdapter() {
        @Override
		public void frameReplaced(KnowledgeBaseEvent event) {
        	Instance inst = (Instance)event.getNewFrame();
        	Collection<Cls> types = inst.getDirectTypes();
        	for (Cls type : types) {
        		if (_clses.contains(type)) {
        			background.addChange(event);
        			return;
        		}
        	}            
        }
    };

    public DirectInstancesList(Project project) {
        _project = project;
        Action viewAction = createViewAction();

        _list = ComponentFactory.createSelectableList(viewAction);
        _list.setCellRenderer(FrameRenderer.createInstance());
        _list.setModel(new ConcurrentListModel());

        _labeledComponent = new LabeledComponent(null, ComponentFactory.createScrollPane(_list));
        addButtons(viewAction, _labeledComponent);
        _labeledComponent.setFooterComponent(new ListFinder(_list, ResourceKey.INSTANCE_SEARCH_FOR));
        _labeledComponent.setBorder(ComponentUtilities.getAlignBorder());
        add(_labeledComponent, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader(), BorderLayout.NORTH);
        add(panel, BorderLayout.NORTH);

        setSelectable(_list);
        _project.getKnowledgeBase().addKnowledgeBaseListener(kbListener);
    }

    private void updateLabel() {
        String text;
        Cls cls = getSoleAllowedCls();
        BrowserSlotPattern pattern = cls == null ? null : cls.getBrowserSlotPattern();
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
        Iterator<Cls> i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = i.next();
            cls.addClsListener(_clsListener);
            cls.addFrameListener(_clsFrameListener);
        }
    }

    protected Action createCreateAction() {
        _createAction = new CreateAction(ResourceKey.INSTANCE_CREATE) {
            private static final long serialVersionUID = -7153511943330887472L;

            @Override
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
                    synchronized (DirectInstancesList.this) {
                        if (background != null) {
                            background.setDeferredSelection(instance);
                        }
                    }
                }
            }
        };
        return _createAction;
    }

    protected Action createConfigureAction() {
        return new ConfigureAction() {
            private static final long serialVersionUID = -201941911745358583L;

            @Override
            public void loadPopupMenu(JPopupMenu menu) {
                menu.add(createSetDisplaySlotAction());
                menu.add(createShowAllInstancesAction());
            }
        };
    }

    protected JMenuItem createShowAllInstancesAction() {
        Action action = new AbstractAction("Show Subclass Instances") {
            private static final long serialVersionUID = -5993246896156508981L;

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
            cls = CollectionUtilities.getFirstItem(_clses);
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
            Slot browserSlot = pattern != null && pattern.isSimple() ? pattern.getFirstSlot() : null;
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
            private static final long serialVersionUID = 8128282947095201947L;

            public void actionPerformed(ActionEvent event) {
                getSoleAllowedCls().setDirectBrowserSlot(slot);
                updateLabel();
                repaint();
            }
        };
    }

    protected Action createSetDisplaySlotMultipleAction() {
        return new AbstractAction("Multiple Slots...") {
            private static final long serialVersionUID = 7430434198286882076L;

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
            private static final long serialVersionUID = -3162114702044458484L;

            @Override
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
            private static final long serialVersionUID = -521270402099135685L;

            @Override
            public void onView(Object o) {
                _project.show((Instance) o);
            }
        };
    }

    @Override
    public void dispose() {
        removeClsListeners();
        _project.getKnowledgeBase().removeKnowledgeBaseListener(kbListener);
        if (background != null) {
            background.cancel();
            background = null;
        }
    }

    public JComponent getDragComponent() {
        return _list;
    }

    private ConcurrentListModel getModel() {
        return (ConcurrentListModel) _list.getModel();
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

    @Override
    public void onSelectionChange() {
        // Log.enter(this, "onSelectionChange");
        boolean editable = isSelectionEditable();
        ComponentUtilities.setDragAndDropEnabled(_list, editable);
        updateButtons();
    }


    private void removeClsListeners() {
        Iterator<Cls> i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = i.next();
            cls.removeClsListener(_clsListener);
            cls.removeFrameListener(_clsFrameListener);
        }
    }

    public void setClses(Collection<Cls> newClses) {
        removeClsListeners();
        _clses = new ArrayList<Cls>(newClses);
        reload();
        updateButtons();
        addClsListeners();
    }

    public void reload() {
        synchronized (this) {
            if (background != null) {
                background.cancel();
                background = null;
            }
        }
        Object selectedValue = _list.getSelectedValue();
        Set<Instance> instanceSet = new LinkedHashSet<Instance>();
        Iterator<Cls> i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = i.next();
            instanceSet.addAll(getInstances(cls));
        }
        getModel().clear();
        synchronized (this) {
            if (background != null) {
                background.cancel();
            }
            background = new AddInstancesRunner(new ArrayList<Instance>(instanceSet));
            Thread th = new Thread(background, "Calculate Instances For Panel");
            th.start();
            if (instanceSet.contains(selectedValue) && selectedValue instanceof Instance) {
                background.setDeferredSelection((Instance) selectedValue);
            }
        }

        if (!instanceSet.isEmpty()) {
            _list.setSelectedIndex(0);
        }
        reloadHeader(_clses);
        updateLabel();
    }

    private void reloadHeader(Collection<Cls> clses) {
        StringBuffer text = new StringBuffer();
        Icon icon = null;
        Iterator<Cls> i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = i.next();
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

    private Collection<Instance> getInstances(Cls cls) {
        Collection<Instance> instances;
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

    private static Collection<Instance> removeHiddenInstances(Collection<Instance> instances) {
        Collection<Instance> visibleInstances = new ArrayList<Instance>(instances);
        Iterator<Instance> i = visibleInstances.iterator();
        while (i.hasNext()) {
            Instance instance = i.next();
            if (!instance.isVisible()) {
                i.remove();
            }
        }
        return visibleInstances;
    }

    public synchronized void setSelectedInstance(Instance instance) {
        if (background != null && !getModel().contains(instance)) {
            background.setDeferredSelection(instance);
        }
        else {
        	_list.setSelectedValue(instance);
        }
    }

    private void updateButtons() {
        Cls cls = CollectionUtilities.getFirstItem(_clses);
        _createAction.setEnabled(cls == null ? false : cls.isConcrete());
        Instance instance = (Instance) getSoleSelection();
        boolean allowed = instance != null && instance instanceof SimpleInstance;
        _copyAction.setAllowed(allowed);
    }

    public void setListRenderer(ListCellRenderer renderer) {
        _list.setCellRenderer(renderer);

        if (renderer instanceof FrameRenderer) {
            ((FrameRenderer)renderer).setDisplayType(_showSubclassInstances);
        }
    }

    /**
     * Does nothing anymore. This functionality moved to the menu button.
     *
     * @deprecated
     */
    @Deprecated
    public void setShowDisplaySlotPanel(boolean b) {

    }



    private class AddInstancesRunner implements Runnable {
        private List<Instance> instances;
        private List<AbstractEvent> changes = new ArrayList<AbstractEvent>();
        private Instance deferredSelection;
        private boolean sorted;

        private boolean cancelled = false;

        public AddInstancesRunner(List<Instance> instances) {
            this.instances = instances;
            sorted = SORT_LIMIT < 0  || instances.size() < SORT_LIMIT;
        }

        public void run() {
            while (true) {
                waitToProcessEvents();
                addOneInstance(getNextInstance());
                synchronized (this) {
                    if (cancelled) {
                        return;
                    }
                }
                handleChanges();
            }
        }

        private void waitToProcessEvents() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        ;
                    }
                });
            } catch (InterruptedException e) {
                Log.getLogger().log(Level.SEVERE, "Interrupt caught - why?", e);
            } catch (InvocationTargetException e) {
                Log.getLogger().log(Level.SEVERE, "Programmer error", e);
            }
            synchronized (this) {
                if (instances.isEmpty() && !cancelled && changes.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Log.getLogger().log(Level.SEVERE, "Interrupted thread - why?", e);
                    }
                }
            }
        }

        private void addOneInstance(final Instance instance) {
            if (instance == null) {
                return;
            }
            if (!getModel().contains(instance)) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            /* Since I am writing you a message - you know it is not good.
                             *
                             * If AddInstanceRunner.cancel is called in the AWT event queue thread
                             * then it will happen either before or after the following.  Therefore
                             * we won't get a cancel in the middle of the addValue operation.
                             */
                            if (!cancelled) {
                                insertInstanceInList(instance);
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Log.getLogger().log(Level.SEVERE, "Exception caught talking to swing", e);
                } catch (InvocationTargetException e) {
                    Log.getLogger().log(Level.SEVERE, "Exception caught talking to swing", e);
                }
            }
        }

        private synchronized Instance getNextInstance() {
            if (instances.isEmpty() || cancelled)  {
                return null;
            }
            Instance instance = instances.get(0);
            instances.remove(0);
            return instance;
        }

        private void handleChanges() {
            final List<AbstractEvent> localChanges = new ArrayList<AbstractEvent>();
            final Instance localDeferredSelection;
            boolean noChanges;
            synchronized (this) {
                noChanges = changes.isEmpty();
                localChanges.addAll(changes);
                changes.clear();
                localDeferredSelection = deferredSelection;
                deferredSelection = null;
            }
            /* Another warning comment
             * I need to get into the swing thread so that I can be synchronized with the cancel method.
             */
            if (!noChanges || localDeferredSelection != null) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            if (cancelled) {
                                return;
                            }
                            for (AbstractEvent event : localChanges) {
                                if (event instanceof ClsEvent) {
                                    ClsEvent clsEvent = (ClsEvent) event;
                                    if (clsEvent.getEventType() == ClsEvent.DIRECT_INSTANCE_ADDED) {
                                        Instance instance = clsEvent.getInstance();
                                        insertInstanceInList(instance);
                                        instances.remove(instance);
                                    }
                                    else if (clsEvent.getEventType() == ClsEvent.DIRECT_INSTANCE_REMOVED) {
                                        Instance instance = clsEvent.getInstance();
                                        getModel().removeValue(instance);
                                        instances.remove(instance);
                                    }
                                }
                                else if (event instanceof FrameEvent) {
                                    FrameEvent frameEvent = (FrameEvent) event;
                                    Frame frame = frameEvent.getFrame();
                                    if (frameEvent.getEventType() == FrameEvent.OWN_SLOT_VALUE_CHANGED &&
                                            getModel().contains(frame) &&
                                            frame instanceof Instance) {
                                        getModel().removeValue(frame);
                                        updateButtons();
                                    }
                                }
                                else if (event instanceof KnowledgeBaseEvent) {
                                    KnowledgeBaseEvent kbEvent = (KnowledgeBaseEvent) event;
                                    if (kbEvent.getEventType() == KnowledgeBaseEvent.FRAME_REPLACED) {
                                        boolean selected =
                                            _list.getSelection().contains(kbEvent.getFrame())
                                                    && localDeferredSelection  == null;
                                        instances.remove(kbEvent.getFrame());
                                        getModel().removeValue(kbEvent.getFrame());
                                        insertInstanceInList((Instance) kbEvent.getNewFrame());
                                        if (selected) {
                                            _list.setSelectedValue(kbEvent.getNewFrame());
                                        }
                                    }
                                }
                            }
                            if (localDeferredSelection != null) {
                                if (instances.contains(localDeferredSelection)) {
                                    insertInstanceInList(localDeferredSelection);
                                    instances.remove(localDeferredSelection);
                                }
                                _list.setSelectedValue(localDeferredSelection, true);
                                updateButtons();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Log.getLogger().log(Level.SEVERE, "Interrupted Thread - why?", e);
                } catch (InvocationTargetException e) {
                    Log.getLogger().log(Level.WARNING, "Exception processing updates to instance list", e);
                }
            }
        }

        private void insertInstanceInList(Instance instance) {
            if (sorted) {
                List list = getModel().toList();
                int index = Collections.binarySearch(list, instance, new FrameComparator());
                if (index >= 0) {
                    getModel().addValue(instance, index);
                }
                else if (index < 0) {
                    getModel().addValue(instance, -index - 1);
                }
            }
            else {
                getModel().addValue(instance);
            }
        }

        public synchronized void addChange(AbstractEvent event) {
            if (event.isReplacementEvent() && event.getEventType() != KnowledgeBaseEvent.FRAME_REPLACED) {
                return;
            }
            changes.add(event);
            notifyAll();
        }

        public synchronized void setDeferredSelection(Instance instance) {
            deferredSelection = instance;
        }

        public synchronized void cancel() {
            cancelled = true;
            notifyAll();
        }

    }
}
