package edu.stanford.smi.protege.ui;

//ESCA*JAVA0100

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.InstanceEvent;
import edu.stanford.smi.protege.event.InstanceListener;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.ProjectAdapter;
import edu.stanford.smi.protege.event.ProjectEvent;
import edu.stanford.smi.protege.event.ProjectListener;
import edu.stanford.smi.protege.event.WidgetAdapter;
import edu.stanford.smi.protege.event.WidgetEvent;
import edu.stanford.smi.protege.event.WidgetListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.StandardAction;
import edu.stanford.smi.protege.util.StandardDateFormat;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.FormWidget;

/**
 * A holder for the display of a runtime "ClsForm". This holder handles the
 * "yellow sticky" ui and logic. This class inherits from JDesktopPane because
 * it uses internal frames to display yellow stickies.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceDisplay extends JDesktopPane implements Disposable {
    private static final long serialVersionUID = 4777948146986438293L;
    private Project _project;
    private JScrollPane _scrollPane;
    private Collection<ClsWidget> _currentWidgets = new ArrayList<ClsWidget>();
    private Instance _currentInstance;
    private Cls _currentAssociatedCls;
    private HeaderComponent _header;
    private JComponent _child;
    private Point _lastYellowStickyPosition = new Point();
    private Slot _templateSlotsSlot;
    private AbstractButton _hideNotesButton;
    private AbstractButton _createNoteButton;
    private AbstractButton _deleteNoteButton;
    private boolean resizeVertically;

    private ClsListener _clsListener = new ClsAdapter() {

        @Override
		public void directSuperclassAdded(ClsEvent event) {
        	if (event.isReplacementEvent()) {
				return;
			}
            reloadForm();
        }

        @Override
		public void directSuperclassRemoved(ClsEvent event) {
        	if (event.isReplacementEvent()) {
				return;
			}
            reloadForm();
        }

        @Override
		public void templateSlotAdded(ClsEvent event) {
        	if (event.isReplacementEvent()) {
				return;
			}
            if (shouldDisplaySlot(event.getCls(), event.getSlot())) {
                reloadForm();
            }
        }

        @Override
		public void templateSlotRemoved(ClsEvent event) {
        	if (event.isReplacementEvent()) {
				return;
			}
            if (isDisplayingSlot(event.getCls(), event.getSlot())) {
                reloadForm();
            }
        }

        @Override
		public void templateFacetValueChanged(ClsEvent event) {
        	if (event.isReplacementEvent()) {
				return;
			}
            if (isDisplayingSlot(event.getCls(), event.getSlot())) {
                reloadForm();
            }
        }
    };
    private FrameListener _frameListener = new FrameAdapter() {
        @Override
		public void ownSlotValueChanged(FrameEvent event) {
        	if (event.isReplacementEvent()) {
				return;
			}
            Slot slot = event.getSlot();
            if (slot.hasSuperslot(_templateSlotsSlot)) {
                reloadForm();
            }
        }
    };

    private KnowledgeBaseListener _kbListener = new KnowledgeBaseAdapter() {
    	@Override
		public void frameReplaced(KnowledgeBaseEvent event) {
    		Frame oldFrame = event.getFrame();
    		Frame newFrame = event.getNewFrame();

    		if (_currentInstance != null && _currentInstance.equals(oldFrame)) {
    			 setInstance((Instance)newFrame);
    		}
    		if (_currentAssociatedCls != null && _currentAssociatedCls.equals(oldFrame)) {
    			 setInstance(_currentInstance);
    		}
    	}
    };

    private WidgetListener _widgetListener = new WidgetAdapter() {
        @Override
		public void labelChanged(WidgetEvent event) {
            if (_header != null) {
                loadHeader();
            }
        }
    };

    private ProjectListener _projectListener = new ProjectAdapter() {
        @Override
		public void formChanged(ProjectEvent event) {
            Cls cls = event.getCls();
            if (isDisplayingCls(cls)) {
                reloadForm();
            }
        }
    };

    private InstanceListener _instanceListener = new InstanceListener() {
        public void directTypeAdded(InstanceEvent event) {
            onDirectTypeAdded(event.getCls());
        }

        public void directTypeRemoved(InstanceEvent event) {
            onDirectTypeRemoved(event.getCls());
        }
    };

    //ESCA-JAVA0130
    protected boolean shouldDisplaySlot(Cls cls, Slot slot) {
        return true;
    }

    protected boolean isDisplayingSlot(Cls cls, Slot slot) {
        ClsWidget widget = getClsWidget(cls);
        return widget.getSlotWidget(slot) != null;
    }

    private boolean isDisplayingCls(Cls cls) {
        return getClsWidget(cls) != null;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    protected void onDirectTypeAdded(Cls type) {
        reloadForm();
    }

    protected void onDirectTypeRemoved(Cls type) {
        reloadForm();
    }

    public InstanceDisplay(Project project) {
        this(project, true, true);
    }

    public InstanceDisplay(Project project, boolean showHeader, boolean showHeaderLabel) {
        _child = new JPanel(new BorderLayout());
        if (showHeader) {
            _child.add(createHeaderComponent(), BorderLayout.NORTH);
            if (!showHeaderLabel) {
                _header.setVisible(false);
            }
        }
        _project = project;
        _templateSlotsSlot = project.getKnowledgeBase().getSlot(Model.Slot.DIRECT_TEMPLATE_SLOTS);
    	project.getKnowledgeBase().addKnowledgeBaseListener(_kbListener);
        project.addProjectListener(_projectListener);
        _scrollPane = makeInstanceScrollPane();
        _child.add(_scrollPane, BorderLayout.CENTER);
        add(_child);

        if (showHeader) {
            loadHeader();
        }
    }

    @Override
	public void setBorder(Border border) {
        if (_child != null) {
            _child.setBorder(border);
        }
    }

    public HeaderComponent getHeaderComponent() {
        return _header;
    }

    protected JLabel getHeaderLabel() {
        return (JLabel) _header.getComponent();
    }

    //ESCA-JAVA0130
    protected JScrollPane makeInstanceScrollPane() {
        JScrollPane pane = ComponentFactory.createScrollPane();
        pane.setBorder(null);
        return pane;
    }

    protected void addRuntimeWidgets(Instance instance, Cls associatedCls) {
        Collection types = instance.getDirectTypes();
        _currentWidgets.clear();
        Iterator i = types.iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            ClsWidget widget = getWidget(type, instance, associatedCls);
            if (widget != null) {
                type.addClsListener(_clsListener);
                type.addFrameListener(_frameListener);
                widget.addWidgetListener(_widgetListener);
                _currentWidgets.add(widget);
            }

        }
        JComponent component = createWidgetContainer(_currentWidgets);
        _scrollPane.setViewportView(component);
        update();
    }

    /**
     * return null to prevent form from being displayed.
     */
    protected ClsWidget getWidget(Cls type, Instance instance, Cls associatedCls) {
        return _project.createRuntimeClsWidget(type, instance, associatedCls);
    }

    protected JComponent createWidgetContainer(Collection widgets) {
        JComponent container;
        if (widgets.size() == 1) {
            FormWidget widget = (FormWidget) CollectionUtilities.getFirstItem(widgets);
            widget.setResizeVertically(resizeVertically);
            container = widget;
        } else if (doTabbedFormLayout()) {
            container = createTabbedWidgetLayout(widgets);
        } else {
            container = createSingleFormWidgetLayout(widgets);
        }
        return container;
    }

    protected boolean doTabbedFormLayout() {
        return _project.getTabbedInstanceFormLayout();
    }

    //ESCA-JAVA0130
    protected JComponent createTabbedWidgetLayout(Collection widgets) {
        JTabbedPane tabbedPane = ComponentFactory.createTabbedPane(false);
        Iterator i = widgets.iterator();
        while (i.hasNext()) {
            ClsWidget widget = (ClsWidget) i.next();
            tabbedPane.addTab(widget.getCls().getBrowserText(), (JComponent) widget);
        }
        tabbedPane.setBorder(ComponentUtilities.getAlignBorder());
        return tabbedPane;
    }

    protected JComponent createSingleFormWidgetLayout(Collection widgets) {
        Box boxPanel = Box.createVerticalBox();
        Iterator i = widgets.iterator();
        while (i.hasNext()) {
            FormWidget widget = (FormWidget) i.next();
            widget.setResizeVertically(resizeVertically);
            JComponent componentToAdd = widget;

            JPanel body = new JPanel(new BorderLayout());
            body.add(widget);
            String text = "as " + widget.getCls().getBrowserText();
            JLabel header = ComponentFactory.createSmallFontLabel(text);
            header.setPreferredSize(ComponentFactory.STANDARD_BUTTON_SIZE);
            body.setBorder(BorderFactory.createEtchedBorder());
            componentToAdd = new JPanel(new BorderLayout());
            componentToAdd.add(header, BorderLayout.NORTH);
            componentToAdd.add(body, BorderLayout.CENTER);
            componentToAdd.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            boxPanel.add(componentToAdd);
        }
        JPanel container = new JPanel(new BorderLayout());
        String position = resizeVertically ? BorderLayout.CENTER : BorderLayout.NORTH;
        container.add(boxPanel, position);
        return container;
    }

    private AbstractAction createCreateYellowStickiesAction() {
        return new StandardAction(ResourceKey.INSTANCE_NOTE_CREATE) {
            private static final long serialVersionUID = -693378347365867392L;

            public void actionPerformed(ActionEvent event) {
                createYellowSticky();
            }
        };
    }

    private AbstractAction createDeleteYellowStickiesAction() {
        return new StandardAction(ResourceKey.INSTANCE_NOTE_DELETE) {
            private static final long serialVersionUID = 4056875859826717485L;

            public void actionPerformed(ActionEvent event) {
                deleteYellowSticky();
            }
        };
    }

    private AbstractAction createHideYellowStickiesAction() {
        return new StandardAction(ResourceKey.INSTANCE_NOTE_HIDE) {
            private static final long serialVersionUID = 4169231408766745704L;

            public void actionPerformed(ActionEvent event) {
                updateYellowStickiesView();
            }
        };
    }

    protected JComponent createHeaderComponent() {
        _header = new HeaderComponent(ComponentFactory.createLabel());
        _hideNotesButton = _header.addToggleButton(createHideYellowStickiesAction());
        _createNoteButton = _header.addButton(createCreateYellowStickiesAction());
        _deleteNoteButton = _header.addButton(createDeleteYellowStickiesAction());
        return _header;
    }

    private void createYellowSticky() {
        ensureYellowStickiesAreVisible();
        KnowledgeBase kb = _project.getKnowledgeBase();
        Instance instance = kb.createInstance(null, kb.getCls(Model.Cls.INSTANCE_ANNOTATION));
        ModelUtilities.setOwnSlotValue(instance, Model.Slot.CREATOR, _project.getUserName());
        DateFormat formatter = new StandardDateFormat();
        String date = formatter.format(new Date());
        ModelUtilities.setOwnSlotValue(instance, Model.Slot.CREATION_TIMESTAMP, date);
        ModelUtilities.setOwnSlotValue(instance, Model.Slot.ANNOTATED_INSTANCE, _currentInstance);
        showYellowSticky(instance);
    }

    private void deleteYellowSticky() {
        Collection stickyInstances = getStickyInstances();
        int count = stickyInstances.size();
        if (count == 1) {
            String text = "Are you sure that you want to delete this note";
            int result = ModalDialog.showMessageDialog(this, text, ModalDialog.MODE_YES_NO);
            if (result == ModalDialog.OPTION_YES) {
                Instance instance = (Instance) CollectionUtilities.getFirstItem(stickyInstances);
                removeSticky(instance);
                instance.delete();
            }
        } else if (count > 1) {
            Collection c = DisplayUtilities.pickInstancesFromCollection(this, stickyInstances,
                    "Select a note to delete");
            Iterator i = c.iterator();
            while (i.hasNext()) {
                Instance instance = (Instance) i.next();
                removeSticky(instance);
                instance.delete();
            }
        }
    }

    private void updateYellowStickiesView() {
        boolean hide = _hideNotesButton.isSelected();
        if (hide) {
            removeAllStickies();
        } else {
            showAllStickies();
        }

    }

    public void dispose() {
    	if (_project == null) { //already disposed
    		return;
    	}
    	_project.getKnowledgeBase().removeKnowledgeBaseListener(_kbListener);
        _project.removeProjectListener(_projectListener);
        if (_currentInstance != null) {
            _currentInstance.removeInstanceListener(_instanceListener);
        }
        for (ClsWidget widget : _currentWidgets) {
            widget.removeWidgetListener(_widgetListener);
            widget.getCls().removeClsListener(_clsListener);
            widget.getCls().removeFrameListener(_frameListener);
        }
        _currentWidgets.clear();

        _project = null;
    }

    private void ensureYellowStickiesAreVisible() {
    }

    /**
     * @deprecated Use #getCurrentClsWidgets() or #getFirstClsWidget
     */
    @Deprecated
	public ClsWidget getCurrentClsWidget() {
        return getFirstClsWidget();
    }

    public ClsWidget getFirstClsWidget() {
        return CollectionUtilities.getFirstItem(_currentWidgets);
    }

    private ClsWidget getClsWidget(Cls cls) {
        ClsWidget widget = null;
        for (ClsWidget clsWidget : _currentWidgets) {
            if (clsWidget.getCls().equals(cls)) {
                widget = clsWidget;
                break;
            }
        }
        return widget;
    }

    public Instance getCurrentInstance() {
        return _currentInstance;
    }

    public void setResizeVertically(boolean b) {
        resizeVertically = b;
        reloadForm();
    }

    private Point getNextYellowStickyPosition() {
        int OFFSET = 25;
        int MAX_OFFSET = 100;

        _lastYellowStickyPosition.x += OFFSET;
        _lastYellowStickyPosition.x %= MAX_OFFSET;

        _lastYellowStickyPosition.y += OFFSET;
        _lastYellowStickyPosition.y %= MAX_OFFSET;

        return _lastYellowStickyPosition;
    }

    @Override
	public Dimension getPreferredSize() {
        return _child.getPreferredSize();
    }

    private Collection getStickyInstances() {
        Collection stickyInstances = new ArrayList();
        if (_currentInstance != null) {
            KnowledgeBase kb = _project.getKnowledgeBase();
            /*
             * Slot annotationSlot = kb.getSlot(Model.Slot.ANNOTATED_INSTANCE);
             * Collection refs = kb.getReferences(_currentInstance, 0); Iterator
             * i = refs.iterator(); Log.trace("references=" + refs.size(), this,
             * "getStickyInstances"); while (i.hasNext()) { Reference ref =
             * (Reference) i.next(); if (ref.getSlot() == annotationSlot) {
             * stickyInstances.add(ref.getFrame()); } }
             */
            Slot annotationSlot = kb.getSlot(Model.Slot.ANNOTATED_INSTANCE);
            Iterator i = kb.getCls(Model.Cls.INSTANCE_ANNOTATION).getInstances().iterator();
            while (i.hasNext()) {
                Instance annotationInstance = (Instance) i.next();
                Instance pointedAtInstance = (Instance) annotationInstance.getOwnSlotValue(annotationSlot);
                if (equals(pointedAtInstance, _currentInstance)) {
                    stickyInstances.add(annotationInstance);
                }
            }
        }
        return stickyInstances;
    }

    @SuppressWarnings("unchecked")
    private Map<Instance, Rectangle> getYellowStickyMap() {
        String mapName = "InstanceDisplay.yellow_stickies";
        Map<Instance, Rectangle> map = (Map<Instance, Rectangle>) _project.getClientInformation(mapName);
        if (map == null) {
            map = new HashMap<Instance, Rectangle>();
            _project.setClientInformation(mapName, map);
        }
        return map;
    }

    protected void loadHeader() {
        if (_currentInstance instanceof Cls) {
            loadHeaderWithCls((Cls) _currentInstance);
        } else if (_currentInstance instanceof Slot) {
            loadHeaderWithSlot((Slot) _currentInstance);
        } else {
            loadHeaderWithSimpleInstance(_currentInstance);
        }
    }

    protected void loadHeaderWithCls(Cls cls) {
        loadHeaderLabel(cls);
        _header.setColor(Colors.getClsColor());
        _header.setTitle(LocalizedText.getText(ResourceKey.CLASS_EDITOR_TITLE));
        _header.setComponentLabel(LocalizedText.getText(ResourceKey.CLASS_EDITOR_FOR_CLASS_LABEL));
        setResource(_hideNotesButton, ResourceKey.CLASS_NOTE_HIDE_ALL);
        setResource(_createNoteButton, ResourceKey.CLASS_NOTE_CREATE);
        setResource(_deleteNoteButton, ResourceKey.CLASS_NOTE_DELETE);
    }

    private static void setResource(AbstractButton button, ResourceKey key) {
        button.setIcon(Icons.getIcon(key));
        button.setToolTipText(LocalizedText.getText(key));
    }

    protected void loadHeaderWithSlot(Slot slot) {
        loadHeaderLabel(slot);
        _header.setColor(Colors.getSlotColor());
        _header.setTitle(LocalizedText.getText(ResourceKey.SLOT_EDITOR_TITLE));
        _header.setComponentLabel(LocalizedText.getText(ResourceKey.SLOT_EDITOR_FOR_SLOT_LABEL));
        setResource(_hideNotesButton, ResourceKey.SLOT_NOTE_HIDE);
        setResource(_createNoteButton, ResourceKey.SLOT_NOTE_CREATE);
        setResource(_deleteNoteButton, ResourceKey.SLOT_NOTE_DELETE);
    }

    protected void loadHeaderLabel(Instance instance) {
        JLabel label = getHeaderLabel();
        if (instance != null) {
            label.setIcon(instance.getIcon());
            String browserText = instance.getBrowserText();
            StringBuffer buffer = new StringBuffer(browserText);
            buffer.append("     ");
            String typeText = getTypeText(instance);
            String name = instance.getName();
            String text;
            if (name.equals(browserText)) {
                text = LocalizedText.getText(ResourceKey.FRAME_EDITOR_FRAME_TYPE, typeText);
            } else {
                text = LocalizedText.getText(ResourceKey.FRAME_EDITOR_FRAME_TYPE_AND_NAME, typeText, name);
            }
            buffer.append(text);
            label.setText(buffer.toString());
        } else {
            label.setIcon(null);
            label.setText("");
        }
    }

    //ESCA-JAVA0130
    protected String getTypeText(Instance instance) {
        StringBuffer typeText = new StringBuffer();
        Iterator i = instance.getDirectTypes().iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            typeText.append(type.getBrowserText());
            if (i.hasNext()) {
                typeText.append(", ");
            }
        }
        return typeText.toString();
    }

    protected void loadHeaderWithSimpleInstance(Instance instance) {
        loadHeaderLabel(instance);
        _header.setColor(Colors.getInstanceColor());
        _header.setTitle(LocalizedText.getText(ResourceKey.INSTANCE_EDITOR_TITLE));
        _header.setComponentLabel(LocalizedText.getText(ResourceKey.INSTANCE_EDITOR_FOR_INSTANCE_LABEL));
        setResource(_hideNotesButton, ResourceKey.INSTANCE_NOTE_HIDE);
        setResource(_createNoteButton, ResourceKey.INSTANCE_NOTE_CREATE);
        setResource(_deleteNoteButton, ResourceKey.INSTANCE_NOTE_DELETE);
    }

    private JInternalFrame loadIntoFrame(final Instance instance) {
        JInternalFrame frame = _project.showInInternalFrame(instance);
        Map<Instance, Rectangle> propertyMap = getYellowStickyMap();
        Rectangle r = propertyMap.get(instance);
        if (r == null) {
            frame.setLocation(getNextYellowStickyPosition());
            propertyMap.put(instance, frame.getBounds());
        } else {
            frame.setBounds(r);
        }
        frame.addComponentListener(new ComponentAdapter() {
            @Override
			public void componentResized(ComponentEvent event) {
                getYellowStickyMap().put(instance, event.getComponent().getBounds());
            }

            @Override
			public void componentMoved(ComponentEvent event) {
                getYellowStickyMap().put(instance, event.getComponent().getBounds());
            }
        });
        frame.setFrameIcon(getNoteIcon());
        return frame;
    }

    private Icon getNoteIcon() {
        Icon icon;
        if (_currentInstance instanceof Cls) {
            icon = Icons.getIcon(ResourceKey.CLASS_NOTE);
        } else if (_currentInstance instanceof Slot) {
            icon = Icons.getIcon(ResourceKey.SLOT_NOTE);
        } else {
            icon = Icons.getIcon(ResourceKey.INSTANCE_NOTE);
        }
        return icon;
    }

    private void reloadForm() {
        Instance instance = _currentInstance;
        Cls cls = _currentAssociatedCls;
        removeCurrentWidgets();
        setInstance(instance, cls);
        // Log.getLogger().info("reload form: " + this);
    }

    private void removeAllStickies() {
        Iterator i = new ArrayList(Arrays.asList(getComponents())).iterator();
        while (i.hasNext()) {
            Component c = (Component) i.next();
            if (c instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) c;
                frame.setVisible(false);
                frame.dispose();
                remove(frame);
            }
        }
    }

    protected void removeCurrentWidgets() {
        for (ClsWidget widget : _currentWidgets) {
            widget.getCls().removeClsListener(_clsListener);
            widget.getCls().removeFrameListener(_frameListener);
            widget.removeWidgetListener(_widgetListener);
        }
        Component c = _scrollPane.getViewport().getView();
        _scrollPane.setViewportView(null);
        ComponentUtilities.dispose(c);
        _currentWidgets.clear();
        if (_currentInstance != null) {
            _currentInstance.removeInstanceListener(_instanceListener);
        }
        _currentInstance = null;
        _currentAssociatedCls = null;
        update();
    }

    private void removeSticky(Instance instance) {
        Iterator i = new ArrayList(Arrays.asList(getComponents())).iterator();
        while (i.hasNext()) {
            Component c = (Component) i.next();
            if (c instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) c;
                InstanceDisplay display = (InstanceDisplay) frame.getContentPane().getComponent(0);
                if (equals(display.getCurrentInstance(), instance)) {
                    frame.setVisible(false);
                    frame.dispose();
                    remove(frame);
                    break;
                }
            }
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
	@Override
	public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        _child.setBounds(0, 0, w, h);
    }

    public void setInstance(Instance instance) {
        setInstance(instance, null);
    }

    public void setInstance(Instance instance, Cls associatedCls) {
        if (_currentInstance != null) {
            _currentInstance.removeInstanceListener(_instanceListener);
        }
        if (instance == null) {
            removeCurrentWidgets();
        } else {
            if (_currentWidgets.isEmpty()) {
                addRuntimeWidgets(instance, associatedCls);
            } else {
                if (typesMatchCurrentWidgets(instance)) {
                    for (ClsWidget clsWidget : _currentWidgets) {
                        clsWidget.setInstance(instance);
                        clsWidget.setAssociatedCls(associatedCls);
                    }
                } else {
                    removeCurrentWidgets();
                    addRuntimeWidgets(instance, associatedCls);
                }
            }
            instance.addInstanceListener(_instanceListener);
        }
        _currentInstance = instance;
        _currentAssociatedCls = associatedCls;
        if (_header != null) {
            loadHeader();
        }
        updateStickies();
    }

    private boolean typesMatchCurrentWidgets(Instance instance) {
        boolean typesMatch = false;
        Set types = new HashSet(instance.getDirectTypes());
        if (types.size() == _currentWidgets.size()) {
            typesMatch = true;
            for (ClsWidget widget : _currentWidgets) {
                if (!types.contains(widget.getCls())) {
                    typesMatch = false;
                    break;
                }
            }
        }
        return typesMatch;
    }

    private void showAllStickies() {
        if (_currentInstance != null) {
            Iterator i = getStickyInstances().iterator();
            while (i.hasNext()) {
                Instance instance = (Instance) i.next();
                showYellowSticky(instance);
            }
        }
    }

    private void showYellowSticky(Instance instance) {
        JInternalFrame frame = loadIntoFrame(instance);
        String author = (String) ModelUtilities.getDirectOwnSlotValue(instance, Model.Slot.CREATOR);
        if (author == null || author.length() == 0) {
            author = "<unknown author>";
        }
        String timeString = getTimeString(instance);
        String title = author;
        if (timeString != null) {
            title += ", " + timeString;
        }
        frame.setTitle(title);
        frame.setVisible(true);
        add(frame);
        frame.toFront();
        try {
            frame.setSelected(true);
        } catch (Exception e) {
            // do nothing
        }
    }

    private static String getTimeString(Instance instance) {
        String timeString = null;
        String timestamp = (String) ModelUtilities.getDirectOwnSlotValue(instance, Model.Slot.CREATION_TIMESTAMP);
        if (timestamp != null) {
            SimpleDateFormat formatter = new StandardDateFormat();
            try {
                Date date = formatter.parse(timestamp);
                Calendar calendar = new GregorianCalendar();
                int thisYear = calendar.get(Calendar.YEAR);
                calendar.setTime(date);
                int stickyYear = calendar.get(Calendar.YEAR);
                String pattern = "MMM dd " + (thisYear == stickyYear ? "" : "yyyy ") + "HH:mm";
                formatter.applyPattern(pattern);
                timeString = formatter.format(date);
            } catch (ParseException e) {
                Log.getLogger().warning(e.toString());
                timeString = timestamp;
            }
        }
        return timeString;
    }

    private void update() {
        revalidate();
        repaint();
    }

    private void updateStickies() {
        removeAllStickies();
        if (_hideNotesButton != null && !_hideNotesButton.isSelected()) {
            showAllStickies();
        }
    }
}