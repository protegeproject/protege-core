package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * A holder for the display of a runtime "ClsForm". This holder handles the
 * "yellow sticky" ui and logic. This class inherits from JDesktopPane because
 * it uses internal frames to display yellow stickies.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceDisplay extends JDesktopPane implements Disposable {
    private Project _project;
    private JScrollPane _scrollPane;
    private ClsWidget _currentWidget;
    private Instance _currentInstance;
    private HeaderComponent _header;
    private JComponent _child;
    private Point _lastYellowStickyPosition = new Point();
    private Slot _templateSlotsSlot;
    private AbstractButton _hideNotesButton;
    private AbstractButton _createNoteButton;
    private AbstractButton _deleteNoteButton;

    private ClsListener _clsListener = new ClsAdapter() {
        public void directSuperclassAdded(ClsEvent event) {
            reloadForm();
        }

        public void directSuperclassRemoved(ClsEvent event) {
            reloadForm();
        }

        public void templateSlotAdded(ClsEvent event) {
            reloadForm();
        }

        public void templateSlotRemoved(ClsEvent event) {
            reloadForm();
        }

        public void templateFacetValueChanged(ClsEvent event) {
            reloadForm();
        }
    };
    private FrameListener _frameListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            if (event.getSlot().hasSuperslot(_templateSlotsSlot)) {
                reloadForm();
            }
        }
    };

    private WidgetListener _widgetListener = new WidgetAdapter() {
        public void labelChanged(WidgetEvent event) {
            if (_header != null) {
                loadHeader();
            }
        }
    };

    private ProjectListener _projectListener = new ProjectAdapter() {
        public void formChanged(ProjectEvent event) {
            Cls cls = event.getCls();
            if (_currentWidget != null && InstanceDisplay.equals(_currentWidget.getCls(), cls)) {
                reloadForm();
            }
        }
    };

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private InstanceListener _instanceListener = new InstanceListener() {
        public void directTypeAdded(InstanceEvent event) {
            setInstance(_currentWidget.getInstance(), _currentWidget.getAssociatedCls());
        }

        public void directTypeRemoved(InstanceEvent event) {
            setInstance(_currentWidget.getInstance(), _currentWidget.getAssociatedCls());
        }
    };

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
        project.addProjectListener(_projectListener);
        _scrollPane = makeInstanceScrollPane();
        _child.add(_scrollPane, BorderLayout.CENTER);
        add(_child);

        if (showHeader) {
            loadHeader();
        }
    }

    public void setBorder(Border border) {
        _child.setBorder(border);
    }

    public HeaderComponent getHeaderComponent() {
        return _header;
    }

    protected JLabel getHeaderLabel() {
        return (JLabel) _header.getComponent();
    }

    protected JScrollPane makeInstanceScrollPane() {
        JScrollPane pane = ComponentFactory.createScrollPane();
        pane.setBorder(null);
        return pane;
    }

    protected void addRuntimeWidget(Instance instance, Cls associatedCls) {
        Cls type = instance.getDirectType();
        if (type == null) {
            Log.getLogger().warning("instance has no type" + instance.getName());
        } else {
            type.addClsListener(_clsListener);
            type.addFrameListener(_frameListener);
            _currentWidget = _project.createRuntimeClsWidget(instance, associatedCls);
            _currentWidget.addWidgetListener(_widgetListener);
            JComponent component = createWidgetContainer(_currentWidget);
            _scrollPane.setViewportView(component);
            update();
        }
    }

    protected JComponent createWidgetContainer(ClsWidget widget) {
        return (JComponent) widget;
    }

    private AbstractAction createCreateYellowStickiesAction() {
        return new StandardAction(ResourceKey.INSTANCE_NOTE_CREATE) {
            public void actionPerformed(ActionEvent event) {
                createYellowSticky();
            }
        };
    }

    private AbstractAction createDeleteYellowStickiesAction() {
        return new StandardAction(ResourceKey.INSTANCE_NOTE_DELETE) {
            public void actionPerformed(ActionEvent event) {
                deleteYellowSticky();
            }
        };
    }

    private AbstractAction createHideYellowStickiesAction() {
        return new StandardAction(ResourceKey.INSTANCE_NOTE_HIDE) {
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
        _project.removeProjectListener(_projectListener);
        if (_currentInstance != null) {
            _currentInstance.removeInstanceListener(_instanceListener);
        }
        if (_currentWidget != null) {
            _currentWidget.removeWidgetListener(_widgetListener);
            _currentWidget.getCls().removeClsListener(_clsListener);
            _currentWidget.getCls().removeFrameListener(_frameListener);
        }
    }

    private void ensureYellowStickiesAreVisible() {
    }

    public ClsWidget getCurrentClsWidget() {
        return _currentWidget;
    }

    public Instance getCurrentInstance() {
        return _currentInstance;
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
                Instance pointedAtInstance = (Instance) annotationInstance
                        .getOwnSlotValue(annotationSlot);
                if (equals(pointedAtInstance, _currentInstance)) {
                    stickyInstances.add(annotationInstance);
                }
            }
        }
        return stickyInstances;
    }

    private Map getYellowStickyMap() {
        String mapName = "InstanceDisplay.yellow_stickies";
        Map map = (Map) _project.getClientInformation(mapName);
        if (map == null) {
            map = new HashMap();
            _project.setClientInformation(mapName, map);
        }
        return map;
    }

    protected void loadHeader() {
        Instance instance = null;
        instance = (_currentWidget == null) ? null : _currentWidget.getInstance();
        if (instance instanceof Cls) {
            loadHeaderWithCls((Cls) instance);
        } else if (instance instanceof Slot) {
            loadHeaderWithSlot((Slot) instance);
        } else {
            loadHeaderWithSimpleInstance(instance);
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

    private void setResource(AbstractButton button, ResourceKey key) {
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
                text = LocalizedText.getText(ResourceKey.FRAME_EDITOR_FRAME_TYPE_AND_NAME,
                        typeText, name);
            }
            buffer.append(text);
            label.setText(buffer.toString());
        } else {
            label.setIcon(null);
            label.setText("");
        }
    }

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
        _header.setComponentLabel(LocalizedText
                .getText(ResourceKey.INSTANCE_EDITOR_FOR_INSTANCE_LABEL));
        setResource(_hideNotesButton, ResourceKey.INSTANCE_NOTE_HIDE);
        setResource(_createNoteButton, ResourceKey.INSTANCE_NOTE_CREATE);
        setResource(_deleteNoteButton, ResourceKey.INSTANCE_NOTE_DELETE);
    }

    private JInternalFrame loadIntoFrame(final Instance instance) {
        JInternalFrame frame = _project.showInInternalFrame(instance);
        Map propertyMap = getYellowStickyMap();
        Rectangle r = (Rectangle) propertyMap.get(instance);
        if (r == null) {
            frame.setLocation(getNextYellowStickyPosition());
            propertyMap.put(instance, frame.getBounds());
        } else {
            frame.setBounds(r);
        }
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                getYellowStickyMap().put(instance, event.getComponent().getBounds());
            }

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
        Instance instance = _currentWidget.getInstance();
        Cls cls = _currentWidget.getAssociatedCls();
        removeCurrentWidget();
        setInstance(instance, cls);
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

    protected void removeCurrentWidget() {
        _currentWidget.getCls().removeClsListener(_clsListener);
        _currentWidget.getCls().removeFrameListener(_frameListener);
        _currentWidget.removeWidgetListener(_widgetListener);
        Component c = (Component) _currentWidget;
        _scrollPane.setViewportView(null);
        ComponentUtilities.dispose(c);
        _currentWidget = null;
        _currentInstance = null;
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
    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        _child.setBounds(0, 0, w, h);
    }

    public void setInstance(Instance instance) {
        setInstance(instance, null);
    }

    public void setInstance(Instance instance, Cls associatedCls) {
        // if (instance != _currentInstance) {
        if (_currentInstance != null) {
            _currentInstance.removeInstanceListener(_instanceListener);
        }
        if (instance == null) {
            if (_currentWidget != null) {
                removeCurrentWidget();
            }
        } else {
            if (_currentWidget == null) {
                addRuntimeWidget(instance, associatedCls);
            } else {
                if (instance.hasDirectType(_currentWidget.getCls())) {
                    _currentWidget.setInstance(instance);
                    _currentWidget.setAssociatedCls(associatedCls);
                } else {
                    removeCurrentWidget();
                    addRuntimeWidget(instance, associatedCls);
                }
            }
            instance.addInstanceListener(_instanceListener);
        }
        _currentInstance = instance;
        if (_header != null) {
            loadHeader();
        }
        updateStickies();
        // }
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
        if (author == null || author.length() == 0)
            author = "<unknown author>";
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
        }
    }

    private String getTimeString(Instance instance) {
        String timeString = null;
        String timestamp = (String) ModelUtilities.getDirectOwnSlotValue(instance,
                Model.Slot.CREATION_TIMESTAMP);
        if (timestamp != null) {
            SimpleDateFormat formatter = new StandardDateFormat();
            try {
                Date date = formatter.parse(timestamp);
                Calendar calendar = new GregorianCalendar();
                int thisYear = calendar.get(Calendar.YEAR);
                calendar.setTime(date);
                int stickyYear = calendar.get(Calendar.YEAR);
                String pattern = "MMM dd " + ((thisYear == stickyYear) ? "" : "yyyy ") + "HH:mm";
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