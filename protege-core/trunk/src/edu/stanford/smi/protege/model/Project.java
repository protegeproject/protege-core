package edu.stanford.smi.protege.model;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.storage.clips.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * The aggregation of a domain knowledge base with its user interface.
 * 
 * Methods on this class that take an "errors" collection may insert any object
 * into this collection These objects can be strings or exceptions. All that is
 * guaranteed is that the toString() method on each object will produce a usable
 * error message. If the method call succeeds then no error objects will have
 * been added to the collection. Eventually this hack will be replaced with some
 * more reasonable interface for collecting errors. Note that we do not want to
 * throw an exception because we would like to accumulate errors (e.g. parse
 * errors) and let the user know about them all at once rather than one at a
 * time. One downside of the current approach is that it leads to cascading
 * errors.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Project {
    private final static String CLASS_PROJECT = "Project";
    private final static String SLOT_DEFAULT_INSTANCE_WIDGET_CLASS_NAME = "default_instance_widget_class_name";
    private final static String SLOT_CUSTOMIZED_INSTANCE_WIDGETS = "customized_instance_widgets";
    private final static String SLOT_BROWSER_SLOTS = "browser_slot_names";
    private final static String SLOT_TABS = "tabs";
    private final static String SLOT_INCLUDED_PROJECTS = "included_projects";
    private final static String SLOT_ALL_KNOWLEDGE_BASE_FACTORY_NAMES = "all_knowledge_base_factory_names";
    private final static String SLOT_SOURCES = "sources";
    private final static String SLOT_JAVA_PACKAGES = "java_packages";
    private final static String SLOT_HIDDEN_FRAMES = "hidden_classes";
    private final static String SLOT_JOURNALING_ENABLED = "journaling_enabled";
    private final static String SLOT_DEFAULT_CLS_METACLASS = "default_cls_metaclass";
    private final static String SLOT_DEFAULT_SLOT_METACLASS = "default_slot_metaclass";
    private final static String SLOT_DEFAULT_FACET_METACLASS = "default_facet_metaclass";
    private final static String SLOT_NEXT_FRAME_NUMBER = "next_frame_number";
    private final static String SLOT_IS_READONLY = "is_readonly";
    private final static String SLOT_PRETTY_PRINT_SLOT_WIDGET_LABELS = "pretty_print_slot_widget_labels";

    private final static String CLASS_OPTIONS = "Options";
    private final static String SLOT_OPTIONS = "options";
    private final static String SLOT_DISPLAY_HIDDEN_FRAMES = "display_hidden_classes";
    private final static String SLOT_DISPLAY_ABSTRACT_CLASS_ICON = "display_abstract_class_icon";
    private final static String SLOT_DISPLAY_MULTI_PARENT_CLASS_ICON = "display_multi_parent_class_icon";
    private final static String SLOT_DISPLAY_REMOVE_CONFIRMATION_DIALOG = "confirm_on_remove";
    private final static String SLOT_UPDATE_MODIFICATION_SLOTS = "update_modification_slots";

    private final static String CLASS_MAP = "Map";
    private final static String SLOT_PROPERTY_MAP = "property_map";

    private URI _uri;
    private URI _loadingProjectURI;
    private KnowledgeBase _projectKB;
    private Instance _projectInstance;
    private KnowledgeBase _domainKB;
    private String _defaultClsWidgetClassName;
    private Map _activeClsWidgetDescriptors = new HashMap(); // <Cls,
    // WidgetDescriptor>
    private Map _cachedDesignTimeClsWidgets = new HashMap(); // Cls -> ClsWidget
    private Map _frames = new HashMap(); // <Instance or FrameSlotPair, JFrame>
    private Map _objects = new HashMap(); // <JFrame, Instance or FrameSlotPair>
    private WidgetMapper _widgetMapper;
    
    // private Collection _cachedIncludedProjectURIs = new ArrayList();
    private Tree projectURITree = new Tree();
    private URI activeRootURI;
    
    private Point _lastLocation;
    private ListenerCollection _listeners = new ListenerList(new ProjectEventDispatcher());
    private Boolean _displayHiddenClasses;
    private Boolean _displayAbstractClassIcon;
    private Boolean _displayMultiParentClassIcon;
    private Boolean _displayConfirmationOnRemove;
    private Boolean _isReadonly;
    private Boolean _updateModificationSlots;
    private Boolean prettyPrintSlotWidgetLabels;
    private Map _includedBrowserSlotPatterns = new HashMap(); // <Cls,
    // BrowserSlotPattern>
    private Map _directBrowserSlotPatterns = new HashMap(); // <Cls,
    // BrowserSlotPattern>
    private Set _hiddenFrames = new HashSet();
    private Set _includedHiddenFrames = new HashSet();
    private boolean _hasChanged;
    private Collection _tabWidgetDescriptors;
    private Map _clientInformation = new HashMap();
    private FrameCountsImpl _frameCounts = new FrameCountsImpl();
    private boolean isMultiUserServer;
    private Class _instanceDisplayClass;

    private WindowListener _closeListener = new WindowAdapter() {
        public void windowClosing(WindowEvent event) {
            JFrame frame = (JFrame) event.getWindow();
            frame.setVisible(false);
            Object o = _objects.remove(frame);
            _frames.remove(o);
            _objects.remove(frame);
            ComponentUtilities.dispose(frame);
            edu.stanford.smi.protege.Application.repaint();
        }
    };

    private KnowledgeBaseListener _knowledgeBaseListener = new KnowledgeBaseAdapter() {
        public void clsCreated(KnowledgeBaseEvent event) {
            // do nothing
        }

        public void clsDeleted(KnowledgeBaseEvent event) {
            // Log.enter(this, "clsDeleted", event);
            Cls cls = event.getCls();
            _activeClsWidgetDescriptors.remove(cls);
            ClsWidget widget = (ClsWidget) _cachedDesignTimeClsWidgets.remove(cls);
            if (widget != null) {
                ComponentUtilities.dispose((Component) widget);
            }
            _directBrowserSlotPatterns.remove(cls);
            removeDisplay(cls);
            _hiddenFrames.remove(cls);
        }

        public void frameNameChanged(KnowledgeBaseEvent event) {
            Frame frame = event.getFrame();
            WidgetDescriptor d = (WidgetDescriptor) _activeClsWidgetDescriptors.get(frame);
            if (d != null) {
                d.setName(frame.getName());
            }
        }

        public void facetDeleted(KnowledgeBaseEvent event) {
            Frame facet = event.getFrame();
            removeDisplay(facet);
            _hiddenFrames.remove(facet);
        }

        public void slotDeleted(KnowledgeBaseEvent event) {
            Slot slot = (Slot) event.getFrame();
            removeDisplay(slot);
            Iterator i = _directBrowserSlotPatterns.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                BrowserSlotPattern pattern = (BrowserSlotPattern) entry.getValue();
                if (pattern.contains(slot)) {
                    i.remove();
                }
            }
            _hiddenFrames.remove(slot);
        }

        public void instanceDeleted(KnowledgeBaseEvent event) {
            super.instanceDeleted(event);
            // Log.enter(this, "instanceDeleted");
            Frame frame = event.getFrame();
            removeDisplay(frame);
            _hiddenFrames.remove(frame);
        }
    };

    static {
        SystemUtilities.initialize();
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    protected Project(URI uri, KnowledgeBaseFactory factory, Collection errors,
            boolean createDomainKB) {
        this(uri, factory, errors, createDomainKB, false);
    }

    protected Project(URI uri, KnowledgeBaseFactory factory, Collection errors,
            boolean createDomainKB, boolean isMultiUserServer) {
        this.isMultiUserServer = isMultiUserServer;
        // Log.enter(this, "Project", uri);
        setProjectURI(uri);
        _projectKB = loadProjectKB(uri, factory, errors);
        if (_projectKB != null) {
            _projectInstance = getProjectInstance(_projectKB);
        }
        if (_projectInstance != null && createDomainKB) {
            boolean load = uri != null;
            createDomainKnowledgeBase(factory, errors, load);
        }
        if (_projectKB != null && errors.isEmpty()) {
            setupJournaling();
        }
        updateKBNames();
    }

    public Project(String projectString, Collection errors) {
        this(URIUtilities.createURI(projectString), errors);
    }

    /**
     * creates a project and loads the project kb from the project file and the
     * associated domain kb
     * 
     * @param errors
     *            See class note for information about this argument.
     */
    private Project(URI uri, Collection errors) {
        this(uri, null, errors, true);
    }

    public void addJavaPackageName(String packageName) {
        addProjectSlotValue(SLOT_JAVA_PACKAGES, packageName);
        _domainKB.addJavaLoadPackage(packageName);
    }

    public void addProjectListener(ProjectListener listener) {
        _listeners.add(this, listener);
    }

    private void addProjectSlotValue(String slotName, Object value) {
        ModelUtilities.addOwnSlotValue(_projectInstance, slotName, value);
    }

    public void clearCachedWidgets() {
        _cachedDesignTimeClsWidgets.clear();
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public static Project createBuildProject(KnowledgeBaseFactory factory, Collection errors) {
        return new Project(null, factory, errors, false);
    }

    public static Project createBuildProject(KnowledgeBase kb, Collection errors) {
        Project p = new Project(null, null, errors, false);
        p._domainKB = kb;
        kb.setProject(p);
        return p;
    }

    private void createDomainKB(KnowledgeBaseFactory factory, Collection errors) {
        if (factory == null) {
            factory = getKnowledgeBaseFactory();
        }
        _domainKB = factory.createKnowledgeBase(errors);
        Iterator i = getProjectSlotValues(SLOT_JAVA_PACKAGES).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            _domainKB.addJavaLoadPackage(name);
        }
        _domainKB.setProject(this);
        _frameCounts.updateSystemFrameCounts(_domainKB);
        setKnowledgeBaseFactory(factory);
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public void createDomainKnowledgeBase(KnowledgeBaseFactory factory, Collection errors,
            boolean load) {
        createDomainKB(factory, errors);
        if (load) {
            Collection uris = loadIncludedProjects(getProjectURI(), _projectInstance, errors);
            loadDomainKB(uris, errors);
        }
        _domainKB.addKnowledgeBaseListener(_knowledgeBaseListener);
        loadCachedKnowledgeBaseObjects(_projectInstance);
        _domainKB.setGenerateEventsEnabled(true);
        _domainKB.setChanged(false);
        _projectKB.setChanged(false);
    }

    /*
     * public static Project createFileProject(String fileName, Collection
     * errors) { return new Project(fileName, errors); }
     */

    protected JFrame createFrame(Cls cls, Slot slot) {
        InstanceDisplay d = createInstanceDisplay(this, false, false);
        d.setInstance(slot, cls);
        return createFrame(d, new FrameSlotCombination(cls, slot));
    }

    protected JFrame createFrame(Instance instance) {
        InstanceDisplay d = createInstanceDisplay(this, true, false);
        d.setInstance(instance);
        return createFrame(d, instance);
    }

    public void setInstanceDisplayClass(Class instanceDisplayClass) {
        _instanceDisplayClass = instanceDisplayClass;
    }

    protected InstanceDisplay createInstanceDisplay(Project project, boolean showHeader,
            boolean showHeaderLabel) {
        InstanceDisplay instanceDisplay;
        if (_instanceDisplayClass != null) {
            instanceDisplay = createInstanceDisplaySubclass(_instanceDisplayClass, project,
                    showHeader, showHeaderLabel);
        } else {
            instanceDisplay = new InstanceDisplay(project, showHeader, showHeaderLabel);
        }
        return instanceDisplay;
    }

    private static InstanceDisplay createInstanceDisplaySubclass(Class clas, Project project,
            boolean showHeader, boolean showHeaderLabel) {
        InstanceDisplay instanceDisplay = null;
        Class[] classes = new Class[] { Project.class, Boolean.TYPE, Boolean.TYPE };
        Object[] args = new Object[] { project, new Boolean(showHeader),
                new Boolean(showHeaderLabel) };
        try {
            Constructor constructor = clas.getConstructor(classes);
            instanceDisplay = (InstanceDisplay) constructor.newInstance(args);
        } catch (Exception e) {
            Log.getLogger().warning(e.getMessage());
        }
        return instanceDisplay;
    }

    protected JFrame createFrame(InstanceDisplay display, Object o) {
        final JFrame frame = ComponentFactory.createFrame();
        frame.addWindowListener(_closeListener);
        frame.getContentPane().add(display, BorderLayout.CENTER);
        ComponentUtilities.pack(frame);
        ClsWidget widget = display.getCurrentClsWidget();
        ((FormWidget) widget).setResizeVertically(true);
        frame.setTitle(widget.getLabel());
        widget.addWidgetListener(new WidgetAdapter() {
            public void labelChanged(WidgetEvent event) {
                frame.setTitle(event.getWidget().getLabel());
            }
        });
        setLocation(frame);
        frame.setVisible(true);
        _frames.put(o, frame);
        _objects.put(frame, o);
        return frame;
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public static Project createNewProject(KnowledgeBaseFactory factory, Collection errors) {
        return new Project(null, factory, errors, true);
    }

    private void createNewTabWidgetDescriptors(Collection names) {
        Iterator i = names.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            WidgetDescriptor d = WidgetDescriptor.create(_projectKB);
            d.setWidgetClassName(name);
            d.setVisible(false);
            _tabWidgetDescriptors.add(d);
        }
    }

    public ClsWidget createRuntimeClsWidget(Instance instance) {
        return createRuntimeClsWidget(instance, null);
    }

    public ClsWidget createRuntimeClsWidget(Instance instance, Cls associatedCls) {
        ClsWidget widget;
        Cls cls = instance.getDirectType();
        if (cls == null) {
            Log.getLogger().severe("no direct type: " + instance.getName());
            widget = new UglyClsWidget();
        } else {
            ClsWidget designTimeWidget = getDesignTimeClsWidget(cls);
            WidgetDescriptor d = designTimeWidget.getDescriptor();
            widget = WidgetUtilities.createClsWidget(d, false, this, cls);
        }
        widget.setInstance(instance);
        if (associatedCls != null) {
            widget.setAssociatedCls(associatedCls);
        }
        postRuntimeClsWidgetCreatedEvent(widget);
        return widget;
    }

    /**
     * @deprecated use createRuntimeClsWidget
     */
    public Widget createRuntimeWidget(Instance instance) {
        return createRuntimeClsWidget(instance);
    }

    public WidgetDescriptor createWidgetDescriptor() {
        return WidgetDescriptor.create(_projectKB);
    }

    public WidgetDescriptor createWidgetDescriptor(Cls cls, Slot slot, Facet facet) {
        return _widgetMapper.createWidgetDescriptor(cls, slot, facet);
    }

    public void dispose() {
        // Log.enter(this, "dispose", _uri);
        postProjectEvent(ProjectEvent.PROJECT_CLOSED);
        if (_domainKB != null) {
            _domainKB.dispose();
        }
        if (_projectKB != null) {
            _projectKB.dispose();
        }
        _domainKB = null;
        _projectKB = null;
    }

    private void flushProjectKBCache() {
        saveBrowserSlots();
        saveCustomizedWidgets();
        saveDefaultMetaclasses();
        saveHiddenFrameFlags();
        saveNextFrameNumber();
        saveClientInformation();
    }

    public Collection getAllKnowledgeBaseFactories() {
        Collection factories = new ArrayList();
        Iterator i = getProjectSlotValues(SLOT_ALL_KNOWLEDGE_BASE_FACTORY_NAMES).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            factories.add(SystemUtilities.newInstance(name));
        }
        return factories;
    }

    /**
     * @deprecated
     */
    public Slot getBrowserSlot(Cls cls) {
        return getPatternSlot(getBrowserSlotPattern(cls));
    }

    public Collection getBrowserSlots(Cls cls) {
        return getBrowserSlotPattern(cls).getSlots();
    }

    public Collection getClsesWithDirectBrowserSlots() {
        return _directBrowserSlotPatterns.keySet();
    }

    public Collection getClsesWithCustomizedForms() {
        return _activeClsWidgetDescriptors.keySet();
    }

    public Collection getHiddenFrames() {
        return new HashSet(_hiddenFrames);
    }

    public BrowserSlotPattern getBrowserSlotPattern(Cls cls) {
        BrowserSlotPattern slotPattern = getDirectBrowserSlotPattern(cls);
        if (slotPattern == null) {
            slotPattern = getInheritedBrowserSlotPattern(cls);
        }
        return slotPattern;
    }

    /**
     * @deprecated
     */
    public Slot getInheritedBrowserSlot(Cls cls) {
        return getPatternSlot(getInheritedBrowserSlotPattern(cls));
    }

    /**
     * @deprecated
     */
    private Slot getPatternSlot(BrowserSlotPattern pattern) {
        return (pattern == null) ? null : pattern.getFirstSlot();
    }

    public BrowserSlotPattern getInheritedBrowserSlotPattern(Cls cls) {
        BrowserSlotPattern slotPattern = null;
        Iterator i = cls.getSuperclasses().iterator();
        while (i.hasNext() && slotPattern == null) {
            Cls superclass = (Cls) i.next();
            slotPattern = getDirectBrowserSlotPattern(superclass);
        }
        return slotPattern;
    }

    public PropertyList getClsWidgetPropertyList(Cls cls) {
        ClsWidget widget = getDesignTimeClsWidget(cls);
        PropertyList list = widget.getDescriptor().getPropertyList();
        return list;
    }

    public String getDefaultWidgetClassName(Cls cls, Slot slot, Facet facet) {
        return _widgetMapper.getDefaultWidgetClassName(cls, slot, facet);
    }

    public ClsWidget getDesignTimeClsWidget(Cls cls) {
        ClsWidget widget = (ClsWidget) _cachedDesignTimeClsWidgets.get(cls);
        if (widget == null) {
            // Log.enter(this, "createClsWidget", cls, new Boolean(designTime));
            WidgetDescriptor d = getClsWidgetDescriptor(cls);
            widget = WidgetUtilities.createClsWidget(d, true, this, cls);
            // Widgets have to go into a cache because we only want one design
            // time widget per widget descriptor.
            // Otherwise when a dt widget writes to the descriptor (to delete
            // the current property list, for example)
            // it ends up corrupting the state of the
            // other widgets.
            _cachedDesignTimeClsWidgets.put(cls, widget);
        }
        return widget;
    }

    public Slot _getDirectBrowserSlot(Cls cls) {
        Slot slot = null;
        BrowserSlotPattern pattern = getDirectBrowserSlotPattern(cls);
        if (pattern != null) {
            slot = pattern.getFirstSlot();
        }
        return slot;
    }

    /**
     * @deprecated
     */
    public Slot getDirectBrowserSlot(Cls cls) {
        return getPatternSlot(getDirectBrowserSlotPattern(cls));
    }

    public BrowserSlotPattern getDirectBrowserSlotPattern(Cls cls) {
        return (BrowserSlotPattern) _directBrowserSlotPatterns.get(cls);
    }

    public boolean getDisplayAbstractClassIcon() {
        if (_displayAbstractClassIcon == null) {
            _displayAbstractClassIcon = loadOption(SLOT_DISPLAY_ABSTRACT_CLASS_ICON, true);
        }
        return _displayAbstractClassIcon.booleanValue();
    }

    public boolean getDisplayConfirmationOnRemove() {
        if (_displayConfirmationOnRemove == null) {
            _displayConfirmationOnRemove = loadOption(SLOT_DISPLAY_REMOVE_CONFIRMATION_DIALOG,
                    false);
        }
        return _displayConfirmationOnRemove.booleanValue();
    }

    public boolean getDisplayHiddenClasses() {
        return getDisplayHiddenFrames();
    }

    public boolean getDisplayHiddenFrames() {
        if (_displayHiddenClasses == null) {
            _displayHiddenClasses = loadOption(SLOT_DISPLAY_HIDDEN_FRAMES, true);
        }
        return _displayHiddenClasses.booleanValue();
    }

    public boolean getDisplayMultiParentClassIcon() {
        if (_displayMultiParentClassIcon == null) {
            _displayMultiParentClassIcon = loadOption(SLOT_DISPLAY_MULTI_PARENT_CLASS_ICON, true);
        }
        return _displayMultiParentClassIcon.booleanValue();
    }

    private void setIconImage(JFrame frame, Instance instance) {
        Icon icon = instance.getIcon();
        if (icon instanceof ImageIcon) {
            ImageIcon iconImage = (ImageIcon) icon;
            Image image = iconImage.getImage();
            frame.setIconImage(image);
        }
    }

    /**
     * @return the URIs of all included projects, including indirectly included
     *         ones
     */
    public Collection getIncludedProjects() {
        return projectURITree.getDescendents(projectURITree.getRoot());
    }
    
    public Tree getProjectTree() {
        return (Tree) projectURITree.clone();
    }

    public void setDirectIncludedProjectURIs(Collection projectURIs) {
        Collection uriStrings = new ArrayList();
        Iterator i = projectURIs.iterator();
        while (i.hasNext()) {
            URI includedUri = (URI) i.next();
            URI relativeURI = URIUtilities.relativize(_uri, includedUri);
            uriStrings.add(relativeURI.toString());
        }
        setProjectSlotValues(SLOT_INCLUDED_PROJECTS, uriStrings);
    }

    /**
     * @deprecated Use #getDirectIncludedProjectURIs()
     * @return A collection of strings which are the absolute file paths
     *         directly included Projects that are files. If an directly
     *         included Project is not a file then IllegalArgumentException is
     *         thrown
     */
    public Collection getDirectIncludedProjects() {
        Collection paths = new ArrayList();
        Iterator i = getDirectIncludedProjectURIs().iterator();
        while (i.hasNext()) {
            URI uri = (URI) i.next();
            File file = new File(uri);
            paths.add(file.getPath());
        }
        return paths;
    }

    /**
     * @return the absolute URI's of all directly included projects
     */
    public Collection getDirectIncludedProjectURIs() {
        Collection uris = new ArrayList();
        Iterator i = getProjectSlotValues(SLOT_INCLUDED_PROJECTS).iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            URI uri = _uri.resolve(s);
            uris.add(uri);
        }
        return uris;
    }

    public KnowledgeBase getInternalProjectKnowledgeBase() {
        return _projectKB;
    }

    public URI getJournalURI() {
        URI uri = URIUtilities.replaceExtension(_uri, ".pjrn");
        if (uri == null) {
            File dir = ApplicationProperties.getApplicationDirectory();
            File journal = new File(dir, "remote_project.pjrn");
            uri = journal.toURI();
        }
        return uri;
    }

    public KnowledgeBase getKnowledgeBase() {
        return _domainKB;
    }

    public KnowledgeBaseFactory getKnowledgeBaseFactory() {
        KnowledgeBaseFactory factory;
        String name = getSources().getString(KnowledgeBaseFactory.FACTORY_CLASS_NAME);
        if (name == null) {
            factory = new ClipsKnowledgeBaseFactory();
        } else {
            factory = (KnowledgeBaseFactory) SystemUtilities.newInstance(name);
        }
        return factory;
    }

    /**
     * same as #getProjectName()
     */
    public String getName() {
        return getProjectName();
    }

    /**
     * @return the "base" project name (no path, no ".pprj" extension)
     */
    public String getProjectName() {
        return URIUtilities.getBaseName(_uri);
    }

    public Collection getOpenWindows() {
        return Collections.unmodifiableCollection(_frames.values());
    }

    private boolean getOption(String slotName, boolean defaultValue) {
        Boolean b = (Boolean) getOwnSlotValue(getOptionsInstance(), slotName);
        return (b == null) ? defaultValue : b.booleanValue();
    }

    private Instance getOptionsInstance() {
        Instance instance = (Instance) getProjectSlotValue(SLOT_OPTIONS);
        if (instance == null) {
            Cls optionsCls = _projectKB.getCls(CLASS_OPTIONS);
            instance = _projectKB.createInstance(null, optionsCls);
            setProjectSlotValue(SLOT_OPTIONS, instance);
        }
        return instance;
    }

    private static Object getOwnSlotValue(Frame frame, String slotName) {
        return ModelUtilities.getDirectOwnSlotValue(frame, slotName);
    }

    private static Reader getProjectClsesReader() {
        Reader reader = Files.getSystemClsesReader();
        if (reader == null) {
            Log.getLogger().severe("Unable to read system ontology");
        }
        return reader;
    }

    public URI getProjectURI() {
        return _uri;
    }

    public URI getProjectDirectoryURI() {
        return URIUtilities.getParentURI(_uri);
    }

    /**
     * @deprecated Use #getProjectDirectoryURI()
     */
    public File getProjectDirectoryFile() {
        File projectFile = getProjectFile();
        return (projectFile == null) ? null : projectFile.getParentFile();
    }

    /**
     * @deprecated Use #getProjectURI()
     */
    public File getProjectFile() {
        return (_uri == null) ? null : new File(_uri);
    }

    /**
     * @deprecated Use #getProjectURI()
     */
    public String getProjectFilePath() {
        File file = getProjectFile();
        return (file == null) ? null : file.getPath();
    }

    protected static Instance getProjectInstance(KnowledgeBase kb) {
        Instance result = null;
        Cls cls = kb.getCls(CLASS_PROJECT);
        if (cls == null) {
            Log.getLogger().severe("no project class");
        } else {
            Collection instances = cls.getDirectInstances();
            // Assert.areEqual(instances.size(), 1);
            result = (Instance) CollectionUtilities.getFirstItem(instances);
        }
        if (result == null) {
            Log.getLogger().severe("no project instance");
        }
        return result;
    }

    private static Reader getProjectInstancesReader(URI uri, KnowledgeBaseFactory factory,
            Collection errors) {
        Reader reader = null;
        if (uri != null) {
            reader = URIUtilities.createBufferedReader(uri);
            if (reader == null) {
                errors.add("Unable to load project: " + uri);
            }
        }
        if (reader == null && factory != null) {
            String path = factory.getProjectFilePath();
            if (path != null) {
                reader = FileUtilities.getResourceReader(factory.getClass(), path);
                if (reader == null) {
                    Log.getLogger().severe("Unable to read factory project: " + path);
                }
            }
        }
        if (reader == null) {
            reader = Files.getSystemInstancesReader();
            if (reader == null) {
                Log.getLogger().severe("Unable to read system instances");
            }
        }
        return reader;
    }

    private static Object getProjectSlotValue(Instance projectInstance, String slotName) {
        return ModelUtilities.getDirectOwnSlotValue(projectInstance, slotName);
    }

    private Object getProjectSlotValue(String slotName) {
        return getProjectSlotValue(_projectInstance, slotName);
    }

    private static Collection getProjectSlotValues(Instance projectInstance, String slotName) {
        return ModelUtilities.getDirectOwnSlotValues(projectInstance, slotName);
    }

    private Collection getProjectSlotValues(String slotName) {
        return getProjectSlotValues(_projectInstance, slotName);
    }

    private static PropertyList getPropertyList(Instance instance, String slotName) {
        PropertyList propertyList;
        Instance plInstance = (Instance) getOwnSlotValue(instance, slotName);
        if (plInstance == null) {
            // Log.trace("creating property list", Project.class,
            // "getPropertyList", instance, slotName);
            propertyList = PropertyList.create(instance.getKnowledgeBase());
            setOwnSlotValue(instance, slotName, propertyList.getWrappedInstance());
        } else {
            propertyList = new PropertyList(plInstance);
        }
        return propertyList;
    }

    private PropertyList getPropertyList(String name) {
        return getPropertyList(_projectInstance, name);
    }

    public PropertyList getSources() {
        return new PropertyList((Instance) getProjectSlotValue(SLOT_SOURCES));
    }

    private static PropertyList getSources(Instance projectInstance) {
        return new PropertyList((Instance) getProjectSlotValue(projectInstance, SLOT_SOURCES));
    }

    public Collection getSuitableWidgetClassNames(Cls cls, Slot slot, Facet facet) {
        return _widgetMapper.getSuitableWidgetClassNames(cls, slot, facet);
    }

    /*
    public WidgetDescriptor getTabWidgetDescriptor(String widgetName) {
        WidgetDescriptor descriptor = null;
        Iterator i = getProjectSlotValues(SLOT_TABS).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            WidgetDescriptor d = WidgetDescriptor.create(instance);
            if (widgetName.equals(d.getWidgetClassName())) {
                descriptor = d;
                break;
            }
        }
        return descriptor;
    }
    */
    
    public WidgetDescriptor getTabWidgetDescriptor(String widgetName) {
        WidgetDescriptor descriptor = null;
        Iterator i = getTabWidgetDescriptors().iterator();
        while (i.hasNext()) {
            WidgetDescriptor testDescriptor = (WidgetDescriptor) i.next();
            if (testDescriptor.getWidgetClassName().equals(widgetName)) {
                descriptor = testDescriptor;
                break;
            }
        }
        return descriptor;
    }

    public Collection getTabWidgetDescriptors() {
        if (_tabWidgetDescriptors == null) {
            Set availableTabNames = new HashSet(PluginUtilities.getAvailableTabWidgetClassNames());

            _tabWidgetDescriptors = new ArrayList();
            Iterator i = getProjectSlotValues(SLOT_TABS).iterator();
            while (i.hasNext()) {
                Instance instance = (Instance) i.next();
                WidgetDescriptor d = WidgetDescriptor.create(instance);
                if (PluginUtilities.isLoadableClass(d.getWidgetClassName())) {
                    _tabWidgetDescriptors.add(d);
                } else {
                    // String text = "removing reference to missing tab: " +
                    // d.getWidgetClassName();
                    // Log.warning(text, this, "getTabWidgetDescriptors");
                }
                String name = d.getWidgetClassName();
                boolean removed = availableTabNames.remove(name);
                if (!removed) {
                    // Log.warning("tab " + name + " not in manifest", this,
                    // "getTabWidgetDescriptors");
                }
            }

            createNewTabWidgetDescriptors(availableTabNames);
            // saveTabWidgetInstances();
        }
        return _tabWidgetDescriptors;
    }

    public boolean getUpdateModificationSlots() {
        if (_updateModificationSlots == null) {
            _updateModificationSlots = loadOption(SLOT_UPDATE_MODIFICATION_SLOTS, false);
        }
        return _updateModificationSlots.booleanValue();
    }

    private boolean hasChanged() {
        return _hasChanged;
    }

    public boolean hasCompleteSources() {
        KnowledgeBaseFactory factory = getKnowledgeBaseFactory();
        boolean hasCompleteSources = (_uri != null) && (factory != null);
        if (hasCompleteSources) {
            hasCompleteSources = factory.isComplete(getSources());
        }
        return hasCompleteSources;
    }

    public boolean hasCustomizedDescriptor(Cls cls) {
        WidgetDescriptor d = (WidgetDescriptor) _activeClsWidgetDescriptors.get(cls);
        if (d != null && !d.isDirectlyCustomizedByUser()) {
            d = null;
        }
        return d != null;
    }

    public boolean hasIncludedProjects() {
        return !getIncludedProjects().isEmpty();
    }

    private void includeDomainKB(Instance projectInstance, String name, Collection uris, Collection errors) {
        String factoryName = getSources(projectInstance)
                .getString(KnowledgeBaseFactory.FACTORY_CLASS_NAME);
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) SystemUtilities.newInstance(factoryName);
        PropertyList sources = getSources(projectInstance);

        if (factory instanceof KnowledgeBaseFactory2) {
            NarrowFrameStore nfs = ((KnowledgeBaseFactory2)factory).createNarrowFrameStore(name);
            MergingNarrowFrameStore mergingFrameStore = getMergingFrameStore();
            mergingFrameStore.addActiveFrameStore(nfs, uris);
        }
        factory.includeKnowledgeBase(_domainKB, sources, errors);

    }
    
    public void includeProject(String path, Collection errors) {
        includeProject(URIUtilities.createURI(path), errors);
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public void includeProject(URI uri, Collection errors) {
        includeProject(uri, true, errors);
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public void includeProject(URI uri, boolean doLoad, Collection errors) {
        if (doLoad) {
            loadIncludedProject(getProjectURI(), uri, errors);
        }
        recordIncludedProject(uri);
    }

    public boolean isDirty() {
        return hasChanged(_domainKB) || hasChanged(_projectKB) || hasChanged();
    }

    private boolean hasChanged(KnowledgeBase kb) {
        return kb != null && kb.hasChanged();
    }

    public boolean isHidden(Frame frame) {
        return _hiddenFrames.contains(frame);
    }

    private boolean isAlreadyIncluded(URI uri) {
        return projectURITree.isReachable(uri);
    }

    private boolean isIncludedBrowserSlotPattern(Cls cls, BrowserSlotPattern slotPattern) {
        return equals(_includedBrowserSlotPatterns.get(cls), slotPattern);
    }

    public boolean isJournalingEnabled() {
        Boolean b = (Boolean) getProjectSlotValue(SLOT_JOURNALING_ENABLED);
        return (b == null) ? false : b.booleanValue();
    }

    public boolean isReadonly() {
        if (_isReadonly == null) {
            _isReadonly = loadOption(SLOT_IS_READONLY, false);

        }
        return _isReadonly.booleanValue();
    }

    public boolean isSuitableWidget(Cls cls, Slot slot, Facet facet, WidgetDescriptor d) {
        return _widgetMapper.isSuitableWidget(cls, slot, facet, d);
    }

    public void loadBrowserSlots(Instance projectInstance) {
        PropertyList browserSlots = getPropertyList(projectInstance, SLOT_BROWSER_SLOTS);
        Iterator i = browserSlots.getNames().iterator();
        while (i.hasNext()) {
            String clsName = (String) i.next();
            if (clsName == null) {
                Log.getLogger().warning("null class name");
            } else {
                Cls cls = _domainKB.getCls(clsName);
                String patternText = browserSlots.getString(clsName);
                BrowserSlotPattern slotPattern = BrowserSlotPattern.createFromSerialization(
                        _domainKB, patternText);
                if (cls != null && slotPattern != null) {
                    recordDirectBrowserSlotPattern(cls, slotPattern);
                    if (projectInstance != _projectInstance) {
                        _includedBrowserSlotPatterns.put(cls, slotPattern);
                    }
                } else {
                    // Log.warning("Bad frame properties: " + clsName + " " +
                    // slotName, this, "loadFrameProperties");
                    browserSlots.remove(clsName);
                }
            }
        }
        // TODO this needs to be handled more cleanly
        Slot nameSlot = _domainKB.getSlot(Model.Slot.NAME);
        Cls rootMetaCls = _domainKB.getCls(Model.Cls.ROOT_META_CLASS);
        if (rootMetaCls == null) {
            Cls classCls = _domainKB.getCls(Model.Cls.CLASS);
            recordDirectBrowserSlotPattern(classCls, new BrowserSlotPattern(nameSlot));
            Cls slotCls = _domainKB.getCls(Model.Cls.SLOT);
            recordDirectBrowserSlotPattern(slotCls, new BrowserSlotPattern(nameSlot));
        } else {
            recordDirectBrowserSlotPattern(rootMetaCls, new BrowserSlotPattern(nameSlot));
        }
    }

    private void loadCachedKnowledgeBaseObjects(Instance projectInstance) {
        loadClientInformation(projectInstance);
        loadNextFrameNumber(projectInstance);
        loadWidgetMapper(projectInstance);
        loadWidgetDescriptors(projectInstance);
        loadBrowserSlots(projectInstance);
        loadDefaultMetaclasses(projectInstance);
        loadHiddenFrameFlags(projectInstance);
        _defaultClsWidgetClassName = (String) getProjectSlotValue(projectInstance,
                SLOT_DEFAULT_INSTANCE_WIDGET_CLASS_NAME);
        _domainKB.setModificationRecordUpdatingEnabled(getUpdateModificationSlots());
    }

    public void setDefaultClsWidgetClassName(String s) {
        _defaultClsWidgetClassName = s;
        setProjectSlotValue(SLOT_DEFAULT_INSTANCE_WIDGET_CLASS_NAME, s);
    }

    private void loadDefaultMetaclasses(Instance projectInstance) {
        String clsClsName = (String) getProjectSlotValue(projectInstance,
                SLOT_DEFAULT_CLS_METACLASS);
        if (clsClsName != null && !clsClsName.equals(Model.Cls.STANDARD_CLASS)) {
            Cls clsMetaCls = _domainKB.getCls(clsClsName);
            if (clsMetaCls != null) {
                _domainKB.setDefaultClsMetaCls(clsMetaCls);
            }
        }
        String slotClsName = (String) getProjectSlotValue(projectInstance,
                SLOT_DEFAULT_SLOT_METACLASS);
        if (slotClsName != null && !slotClsName.equals(Model.Cls.STANDARD_SLOT)) {
            Cls slotMetaCls = _domainKB.getCls(slotClsName);
            if (slotMetaCls != null) {
                _domainKB.setDefaultSlotMetaCls(slotMetaCls);
            }
        }
        String facetClsName = (String) getProjectSlotValue(projectInstance,
                SLOT_DEFAULT_FACET_METACLASS);
        if (facetClsName != null && !facetClsName.equals(Model.Cls.STANDARD_FACET)) {
            Cls facetMetaCls = _domainKB.getCls(facetClsName);
            if (facetMetaCls != null) {
                _domainKB.setDefaultFacetMetaCls(facetMetaCls);
            }
        }
    }

    private void loadDomainKB(Collection uris, Collection errors) {
        KnowledgeBaseFactory factory = getKnowledgeBaseFactory();
        if (factory != null) {
            _frameCounts.updateIncludedFrameCounts(_domainKB);
            boolean enabled = _domainKB.setGenerateEventsEnabled(false);

            if (factory instanceof KnowledgeBaseFactory2) {
                NarrowFrameStore nfs = ((KnowledgeBaseFactory2)factory).createNarrowFrameStore(getProjectURI().toString());
                MergingNarrowFrameStore mergingFrameStore = getMergingFrameStore();
                mergingFrameStore.addActiveFrameStore(nfs, uris);
            }
            
            factory.loadKnowledgeBase(_domainKB, getSources(), errors);
            _domainKB.setGenerateEventsEnabled(enabled);
        }
    }

    private void loadHiddenFrameFlags(Instance projectInstance) {
        Iterator i = getProjectSlotValues(projectInstance, SLOT_HIDDEN_FRAMES).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            Frame frame = _domainKB.getFrame(name);
            if (frame == null) {
                // Log.trace("class not found: " + name, this,
                // "loadHiddenClassFlags");
            } else {
                recordHidden(frame, true);
                if (isIncluded(projectInstance)) {
                    _includedHiddenFrames.add(frame);
                }
            }
        }
    }

    private boolean isIncluded(Instance projectInstance) {
        return _projectInstance != projectInstance;
    }

    public URI getLoadingURI() {
        return _loadingProjectURI == null ? getProjectURI() : _loadingProjectURI;
    }
    
    private void loadIncludedProject(URI includingURI, URI includedURI, Collection errors) {
        includedURI = includedURI.normalize();
        boolean alreadyIncluded = isAlreadyIncluded(includedURI);
        projectURITree.addChild(includingURI, includedURI);
        if (alreadyIncluded) {
            getMergingFrameStore().addRelation(includingURI.toString(), includedURI.toString());
        } else {
            // Log.enter(this, "loadIncludedProject", includedURI);
            KnowledgeBase kb = loadProjectKB(includedURI, null, errors);
            if (kb != null) { // && errors.size() == 0) {
                // This business allows included projects (and their domain kbs)
                // to load from other subdirectories
                URI oldLoadingProjectURI = _loadingProjectURI;
                _loadingProjectURI = includedURI;
                
                kb.setName(URIUtilities.getName(includedURI));
                Instance projectInstance = getProjectInstance(kb);
                Collection includedProjectURIs = loadIncludedProjects(includedURI, projectInstance, errors);
                includeDomainKB(projectInstance, includedURI.toString(), includedProjectURIs, errors);
                loadCachedKnowledgeBaseObjects(projectInstance);

                _loadingProjectURI = oldLoadingProjectURI;
            }
        }
    }


    /**
     * @param errors  See class note for information about this argument.
     */
    public Collection loadIncludedProjects(URI projectURI, Instance projectInstance, Collection errors) {
        Collection uris = new ArrayList();
        Iterator i = getProjectSlotValues(projectInstance, SLOT_INCLUDED_PROJECTS).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            URI uri;
            if (_uri == null) {
                uri = URI.create(name);
            } else {
                uri = _uri.resolve(name);
            }
            loadIncludedProject(projectURI, uri, errors);
            uris.add(uri);
        }
        return uris;
    }

    private void loadNextFrameNumber(Instance projectInstance) {
        Integer i = (Integer) getProjectSlotValue(SLOT_NEXT_FRAME_NUMBER);
        int number;
        if (i == null) {
            number = ApplicationProperties.getOldNextFrameNumber();
        } else {
            number = i.intValue();
        }

        int nextFrameNumber = Math.max(_domainKB.getNextFrameNumber(), number);
        _domainKB.setNextFrameNumber(nextFrameNumber);
    }

    private Boolean loadOption(String name, boolean defaultValue) {
        boolean b = getOption(name, defaultValue);
        return new Boolean(b);
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public static Project loadProjectFromURI(URI uri, Collection errors) {
        return new Project(uri, null, errors, true);
    }

    public static Project loadProjectFromURI(URI uri, Collection errors, boolean isMultiUserServer) {
        return new Project(uri, null, errors, true, isMultiUserServer);
    }

    public static Project loadProjectFromFile(String fileName, Collection errors) {
        return loadProjectFromURI(new File(fileName).toURI(), errors);
    }

    protected static KnowledgeBase loadProjectKB(URI uri, KnowledgeBaseFactory factory,
            Collection errors) {
        KnowledgeBase kb = null;
        Reader clsesReader = null;
        Reader instancesReader = null;
        try {
            clsesReader = getProjectClsesReader();
            instancesReader = getProjectInstancesReader(uri, factory, errors);
            if (instancesReader == null) {
                errors.add("Unable to open project: " + uri);
            } else {
                kb = new ClipsKnowledgeBaseFactory().loadKnowledgeBase(clsesReader,
                        instancesReader, errors);
                if (errors.size() == 0) {
                    BackwardsCompatibilityProjectFixups.fix(kb);
                }

                /*
                 * This should really be done on save but it is difficult then
                 * because there are lot of unreferenced instances (from
                 * temporary widget descriptors) that have to be kept in memory.
                 * It is safe to do it here. The downside is that the
                 * unreferenced instances are actually written to file.
                 */
                removeUnreferencedInstances(kb);

                kb.setGenerateEventsEnabled(false);
                kb.setDispatchEventsEnabled(false);
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "Error loading project kb", e);
            errors.add(e);
        } finally {
            FileUtilities.close(clsesReader);
            FileUtilities.close(instancesReader);
        }
        return kb;
    }

    /*
     * This method doesn't merge the client information from included projects.
     * It is unclear how to even do this. The included project client
     * information shouldn't be written out into the including project file.
     * This means that not only must the information from multiple project be
     * merged there must be a way to separate it back out on save. I'm not
     * really sure how to do this.
     * 
     * For the moment the client information is just the information from the
     * last loaded (outermost) project.
     */
    private void loadClientInformation(Instance projectInstance) {
        Instance instance = (Instance) getOwnSlotValue(projectInstance, SLOT_PROPERTY_MAP);
        if (instance == null) {
            _clientInformation = new HashMap();
        } else {
            _clientInformation = PropertyMapUtil.load(instance, _domainKB);
        }
    }

    private void loadWidgetDescriptors(Instance projectInstance) {
        Iterator i = new ArrayList(getProjectSlotValues(projectInstance,
                SLOT_CUSTOMIZED_INSTANCE_WIDGETS)).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();

            // duplicate included widget descriptors into main project
            if (isIncluded(projectInstance)) {
                instance = (Instance) instance.deepCopy(_projectKB, null);
            }

            WidgetDescriptor d = WidgetDescriptor.create(instance);
            if (d == null) {
                Log.getLogger().severe("Invalid widget instance: " + instance);
                removeProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, instance);
            } else {
                Cls cls = _domainKB.getCls(d.getName());
                if (cls == null) {
                    Log.getLogger().warning("unknown class: " + d.getName());
                    removeProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, instance);
                } else {
                    if (isIncluded(projectInstance)) {
                        d.setIncluded(true);
                    }
                    d.setDirectlyCustomizedByUser(true);
                    // be careful not to overwrite widgets on an "include"
                    // command.
                    WidgetDescriptor existingDescriptor = (WidgetDescriptor) _activeClsWidgetDescriptors
                            .get(cls);
                    if (existingDescriptor == null || existingDescriptor.isIncluded()) {
                        _activeClsWidgetDescriptors.put(cls, d);
                    }
                }
            }
        }
    }

    private void loadWidgetMapper(Instance projectInstance) {
        if (_widgetMapper == null) {
            _widgetMapper = new DefaultWidgetMapper(_projectKB);
        }
    }

    public void setWidgetMapper(WidgetMapper mapper) {
        _widgetMapper = mapper;
    }

    public WidgetMapper getWidgetMapper() {
        return _widgetMapper;
    }

    private void makeTemporaryWidgetsIncluded(boolean b) {
        Iterator i = _activeClsWidgetDescriptors.values().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (d.isTemporary()) {
                d.setIncluded(b);
            }
        }
    }

    private void mergeIncludedFrames() {
        Iterator i = _domainKB.getFrames().iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (!frame.isSystem()) {
                frame.setIncluded(false);
            }
        }
    }

    public void mergeIncludedProjects() {
        mergeIncludedFrames();
        removeIncludedProjectReferences();
    }

    private void postRuntimeClsWidgetCreatedEvent(ClsWidget widget) {
        postProjectEvent(ProjectEvent.RUNTIME_CLS_WIDGET_CREATED, widget);
    }

    public void postFormChangeEvent(Cls cls) {
        ClsWidget widget = getDesignTimeClsWidget(cls);
        postProjectEvent(ProjectEvent.FORM_CHANGED, widget);
    }

    public void postFormChangeEvent(WidgetDescriptor d) {
        Cls cls = getKnowledgeBase().getCls(d.getName());
        postFormChangeEvent(cls);
    }

    public void postProjectEvent(int type) {
        postProjectEvent(type, null);
    }

    public void postProjectEvent(int type, ClsWidget widget) {
        _listeners.postEvent(this, type, widget);
    }

    private void recordDirectBrowserSlotPattern(Cls cls, BrowserSlotPattern slotPattern) {
        Assert.assertNotNull("class", cls);
        if (slotPattern == null) {
            _directBrowserSlotPatterns.remove(cls);
        } else {
            _directBrowserSlotPatterns.put(cls, slotPattern);
        }
    }

    private void recordHidden(Frame frame, boolean hidden) {
        if (hidden) {
            _hiddenFrames.add(frame);
        } else {
            if (_includedHiddenFrames.contains(frame)) {
                Log.getLogger().warning("Cannot 'unhide' an included hidden frame");
            } else {
                _hiddenFrames.remove(frame);
            }
        }
    }

    private void recordIncludedProject(URI name) {
        if (_uri != null) {
            name = _uri.relativize(name);
        }
        addProjectSlotValue(SLOT_INCLUDED_PROJECTS, name.toString());
    }

    private void removeDisplay(Frame frame) {
        JFrame jframe = (JFrame) _frames.get(frame);
        if (jframe != null) {
            ComponentUtilities.closeWindow(jframe);
        }
    }

    public void removeIncludedProjectReferences() {
        Map browserSlots = new HashMap();
        browserSlots.putAll(_includedBrowserSlotPatterns);
        browserSlots.putAll(_directBrowserSlotPatterns);
        _directBrowserSlotPatterns = browserSlots;
        _includedBrowserSlotPatterns.clear();

        Iterator i = _activeClsWidgetDescriptors.values().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (d.isIncluded()) {
                d.setIncluded(false);
            }
        }

        projectURITree = new Tree(getProjectURI());
        setProjectSlotValue(SLOT_INCLUDED_PROJECTS, null);

        _hiddenFrames.addAll(_includedHiddenFrames);
        _includedHiddenFrames.clear();
    }

    public void removeJavaPackageName(String packageName) {
        removeProjectSlotValue(SLOT_JAVA_PACKAGES, packageName);
        _domainKB.removeJavaLoadPackage(packageName);
    }

    public void removeProjectListener(ProjectListener listener) {
        _listeners.remove(this, listener);
    }

    private void removeProjectSlotValue(String slotName, Object value) {
        ModelUtilities.removeOwnSlotValue(_projectInstance, slotName, value);
    }

    private static void removeUnreferencedInstances(KnowledgeBase kb) {
        Instance projectInstance = getProjectInstance(kb);
        Collection roots = CollectionUtilities.createCollection(projectInstance);

        Iterator i = kb.getUnreachableSimpleInstances(roots).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            // Log.trace("found unreachable instance: " + instance,
            // Project.class, "removeUnreferencedInstances");
            if (instance.isEditable()) {
                kb.deleteInstance(instance);
            }
        }
    }

    public void save(Collection errors) {
        /*
         * save domain information first in case there is a problem with the
         * project file We would rather lose project information than domain
         * information. In addition, this gives the backend a chance to hack the
         * project on save.
         */
        saveDomainKB(errors);
        if (errors.isEmpty()) {
            flushProjectKBCache();
            makeTemporaryWidgetsIncluded(true);
            // removeUnreferencedInstances(); moved to load time
            saveProjectKB(errors);
            makeTemporaryWidgetsIncluded(false);
        }
        if (errors.isEmpty()) {
            clearIsDirty();
            postProjectEvent(ProjectEvent.PROJECT_SAVED);
        }
    }

    public void clearIsDirty() {
        _projectKB.setChanged(false);
        _domainKB.setChanged(false);
        setChanged(false);
    }

    private void saveBrowserSlots() {
        PropertyList browserSlots = getPropertyList(SLOT_BROWSER_SLOTS);
        browserSlots.clear();
        Iterator i = _directBrowserSlotPatterns.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Cls cls = (Cls) entry.getKey();
            BrowserSlotPattern slotPattern = (BrowserSlotPattern) entry.getValue();
            if (!isIncludedBrowserSlotPattern(cls, slotPattern)) {
                browserSlots.setString(cls.getName(), slotPattern.getSerialization());
            }
        }
    }

    private void saveCustomizedWidgets() {
        setProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, null);
        Iterator i = _activeClsWidgetDescriptors.values().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (!d.isTemporary() && !d.isIncluded()) {
                addProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, d.getInstance());
            }
        }
    }

    private void saveDefaultMetaclasses() {
        setProjectSlotValue(SLOT_DEFAULT_CLS_METACLASS, getName(_domainKB.getDefaultClsMetaCls()));
        setProjectSlotValue(SLOT_DEFAULT_SLOT_METACLASS, getName(_domainKB.getDefaultSlotMetaCls()));
        setProjectSlotValue(SLOT_DEFAULT_FACET_METACLASS, getName(_domainKB
                .getDefaultFacetMetaCls()));
    }

    private static String getName(Cls cls) {
        return (cls == null) ? null : cls.getName();
    }

    private void saveDomainKB(Collection errors) {
        KnowledgeBaseFactory factory = getKnowledgeBaseFactory();
        if (factory != null) {
            factory.saveKnowledgeBase(_domainKB, getSources(), errors);
        }
    }

    private void saveHiddenFrameFlags() {
        Collection hiddenFrames = new ArrayList();
        Iterator i = _hiddenFrames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (!_includedHiddenFrames.contains(frame)) {
                String name = frame.getName();
                if (name == null) {
                    Log.getLogger().warning("Ignoring nameless frame: " + frame);
                } else {
                    hiddenFrames.add(name);
                }
            }
        }
        setProjectSlotValues(SLOT_HIDDEN_FRAMES, hiddenFrames);
    }

    private void saveNextFrameNumber() {
        int number = _domainKB.getNextFrameNumber();
        setProjectSlotValue(SLOT_NEXT_FRAME_NUMBER, new Integer(number));
    }

    private void saveProjectKB(Collection errors) {
        String s = new File(_uri).toString();
        new ClipsKnowledgeBaseFactory().saveKnowledgeBase(_projectKB, null, s, errors);
    }

    private void saveClientInformation() {
        if (!_clientInformation.isEmpty()) {
            Instance propertyMapInstance = (Instance) getOwnSlotValue(_projectInstance,
                    SLOT_PROPERTY_MAP);
            if (propertyMapInstance == null) {
                Cls cls = _projectKB.getCls(CLASS_MAP);
                propertyMapInstance = _projectKB.createInstance(null, cls);
                ModelUtilities.addOwnSlotValue(_projectInstance, SLOT_PROPERTY_MAP,
                        propertyMapInstance);
            }
            PropertyMapUtil.store(_clientInformation, propertyMapInstance);
        }
    }

    private void saveTabWidgetInstances() {
        Collection instances = new ArrayList();
        Iterator i = _tabWidgetDescriptors.iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            String clsName = d.getWidgetClassName();
            if (SystemUtilities.forName(clsName) != null) {
                instances.add(d.getInstance());
            }
        }
        setProjectSlotValues(SLOT_TABS, instances);
    }

    private void setChanged(boolean b) {
        // Log.stack("***", this, "setChanged", new Boolean(b));
        _hasChanged = b;
    }

    /*
     * Deliberately reduce the visibility of this method. Instead people should
     * call Cls.setDirectBrowserSlot or KnowledgeBase.setDirectBrowserSlot
     */
    void setDirectBrowserSlotPattern(Cls cls, BrowserSlotPattern slotPattern) {
        recordDirectBrowserSlotPattern(cls, slotPattern);
        setChanged(true);
    }

    public void setDisplayAbstractClassIcon(boolean b) {
        _displayAbstractClassIcon = new Boolean(b);
        setOption(SLOT_DISPLAY_ABSTRACT_CLASS_ICON, b);
    }

    public void setDisplayConfirmationOnRemove(boolean b) {
        _displayConfirmationOnRemove = new Boolean(b);
        setOption(SLOT_DISPLAY_REMOVE_CONFIRMATION_DIALOG, b);
    }

    public void setDisplayHiddenFrames(boolean b) {
        _displayHiddenClasses = new Boolean(b);
        setOption(SLOT_DISPLAY_HIDDEN_FRAMES, b);
    }

    public void setDisplayHiddenClasses(boolean b) {
        setDisplayHiddenFrames(b);
    }

    public void setDisplayMultiParentClassIcon(boolean b) {
        _displayMultiParentClassIcon = new Boolean(b);
        setOption(SLOT_DISPLAY_MULTI_PARENT_CLASS_ICON, b);
    }

    public void setHidden(Frame frame, boolean hidden) {
        recordHidden(frame, hidden);
        setChanged(true);
    }

    public void setIsReadonly(boolean b) {
        _isReadonly = new Boolean(b);
        setOption(SLOT_IS_READONLY, b);
    }

    public void setJournalingEnabled(boolean enable) {
        setProjectSlotValue(SLOT_JOURNALING_ENABLED, new Boolean(enable));
        if (enable) {
            _domainKB.startJournaling(getJournalURI());
        } else {
            _domainKB.stopJournaling();
        }
    }

    public void setKnowledgeBaseFactory(KnowledgeBaseFactory factory) {
        Assert.assertNotNull("factory", factory);
        getSources().setString(KnowledgeBaseFactory.FACTORY_CLASS_NAME,
                factory.getClass().getName());
    }

    private void setLocation(Window window) {
        if (_lastLocation == null) {
            ComponentUtilities.center(window);
            _lastLocation = window.getLocation();
        } else {
            _lastLocation.x += 25;
            _lastLocation.y += 25;
            Dimension screenSize = window.getToolkit().getScreenSize();

            if (_lastLocation.x + window.getWidth() > screenSize.width
                    || _lastLocation.y + window.getHeight() > screenSize.height) {
                _lastLocation = new Point();
            }
            window.setLocation(_lastLocation);
        }
    }

    private void setOption(String slotName, boolean value) {
        setOwnSlotValue(getOptionsInstance(), slotName, new Boolean(value));
    }

    private static void setOwnSlotValue(Frame frame, String slotName, Object value) {
        ModelUtilities.setOwnSlotValue(frame, slotName, value);
    }

    public void setProjectFilePath(String s) {
        setProjectURI(URIUtilities.createURI(s));
    }

    public void setProjectURI(URI uri) {
        if (_uri != null) {
            updateDirectIncludedProjectURIs(uri);
        }
        _uri = (uri == null) ? null : uri.normalize();
        updateKBNames();
        updateJournaling();
        projectURITree.swapNode(projectURITree.getRoot(), uri);
        setActiveFrameStore(uri);
        activeRootURI = uri;
        // Log.trace("uri=" + _uri, this, "setProjectURI", uri);
    }
    
    private MergingNarrowFrameStore getMergingFrameStore() {
        return MergingNarrowFrameStore.get(_domainKB);
    }
    
    private void setActiveFrameStore(URI uri) {
        MergingNarrowFrameStore nfs = getMergingFrameStore();
        if (nfs != null && uri != null) {
            nfs.setActiveFrameStore(uri.toString());
        }
    }
    
    public URI getActiveRootURI() {
        return activeRootURI;
    }
    
    public void setActiveRootURI(URI uri) {
        activeRootURI = uri;
        setActiveFrameStore(uri);
        _domainKB.flushCache();
    }

    /*
     * Direct included projects are stored as paths relative to the project uri.
     * When the project uri changes these paths need to be recalculated relative
     * to the new project uri.
     */
    private void updateDirectIncludedProjectURIs(URI newBaseURI) {
        Collection relativeURIs = new ArrayList();
        Iterator i = getDirectIncludedProjectURIs().iterator();
        while (i.hasNext()) {
            URI includedURI = (URI) i.next();
            URI relativeURI = URIUtilities.relativize(newBaseURI, includedURI);
            relativeURIs.add(relativeURI.toString());
        }
        setProjectSlotValues(SLOT_INCLUDED_PROJECTS, relativeURIs);
    }

    private void setProjectSlotValue(String slotName, Object value) {
        ModelUtilities.setOwnSlotValue(_projectInstance, slotName, value);
    }

    private void setProjectSlotValues(String slotName, Collection values) {
        ModelUtilities.setOwnSlotValues(_projectInstance, slotName, values);
    }

    public void setTabWidgetDescriptorOrder(Collection c) {
        _tabWidgetDescriptors = new ArrayList(c);
        saveTabWidgetInstances();
    }

    public void setUpdateModificationSlots(boolean b) {
        _updateModificationSlots = new Boolean(b);
        setOption(SLOT_UPDATE_MODIFICATION_SLOTS, b);
        _domainKB.setModificationRecordUpdatingEnabled(b);
    }

    private void setupJournaling() {
        if (isJournalingEnabled()) {
            URI journalURI = getJournalURI();
            _domainKB.startJournaling(journalURI);
        }
    }

    public JFrame show(Cls cls, Slot slot) {
        FrameSlotCombination combination = new FrameSlotCombination(cls, slot);
        JFrame frame = (JFrame) _frames.get(combination);
        if (frame == null) {
            frame = createFrame(cls, slot);
            setIconImage(frame, slot);
        } else {
            frame.toFront();
            frame.requestFocus();
        }
        return frame;
    }

    public JFrame show(Instance instance) {
        Assert.assertNotNull("instance", instance);
        JFrame frame = (JFrame) _frames.get(instance);
        if (frame == null) {
            frame = createFrame(instance);
            setIconImage(frame, instance);
        } else {
            frame.toFront();
            frame.requestFocus();
        }
        return frame;
    }

    public JFrame show(String instanceName) {
        Assert.assertNotNull("instance name", instanceName);
        Instance instance = _domainKB.getInstance(instanceName);
        return show(instance);
    }

    public JInternalFrame showInInternalFrame(Instance instance) {
        Assert.assertNotNull("instance", instance);
        InstanceDisplay display = createInstanceDisplay(this, false, false);
        display.setInstance(instance);
        ClsWidget widget = display.getCurrentClsWidget();
        ((FormWidget) widget).setResizeVertically(true);
        String title = widget.getLabel();
        JInternalFrame frame = new JInternalFrame(title, true);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(display);
        frame.pack();

        return frame;
    }

    public String toString() {
        return "Project(" + getProjectName() + ")";
    }

    private void updateJournaling() {
        if (_projectKB != null && isJournalingEnabled()) {
            _domainKB.stopJournaling();
            _domainKB.startJournaling(getJournalURI());
        }
    }

    private void updateKBNames() {
        String name = getProjectName();
        if (name != null) {
            if (_domainKB != null) {
                _domainKB.setName(name);
            }
            if (_projectKB != null) {
                _projectKB.setName(name + "_ProjectKB");
            }
        }
    }

    private WidgetDescriptor getClsWidgetDescriptor(Cls cls) {
        WidgetDescriptor d = (WidgetDescriptor) _activeClsWidgetDescriptors.get(cls);
        if (d == null) {
            d = WidgetDescriptor.create(_projectKB);
            d.setWidgetClassName(_defaultClsWidgetClassName);
            d.setName(cls.getName());
            d.setTemporary(true);
            _activeClsWidgetDescriptors.put(cls, d);
        }
        return d;
    }

    /**
     * Returns previously stored "arbitrary" client information. This
     * information is view specific and differs from the client information
     * available on the {@link KnowledgeBase}class. Examples of this type of
     * information is the positioning of a yellow sticky on a form, or the
     * positioning of a node in the diagram widget. <br>
     * <br>
     * The "client information" attached to a project is persistent. In order
     * for the persistence mechanism to work correctly the keys and values
     * stored as client information must either be a domain knowledge base frame
     * or be convertible to a string. <br>
     * <br>
     * Typical values are things such as Strings, Rectangles, and Maps of other
     * objects. The other objects included in a map must themselves be either
     * Frames or must be convertable to and from a string. Being convertable to
     * and from a String means: <br>
     * (1) The object.toString() method must return the state of the object.
     * <br>
     * (2) There class must has a constructor that takes a single string
     * argument. <br>
     * It must be the case that for any instance "a" of "MyClass"
     * 
     * <pre><code>
     * 
     *  
     *        a.equals(new MyClass(a.toString());
     *   
     *  
     * </pre></code>
     * 
     * <br>
     * <br>
     * 
     * A typical use of "client information" is the following:
     * 
     * <pre><code>
     * final String KEY = &quot;MyClass.frame_rectangles&quot;;
     * Map frameRectangleMap = (Map) getClientInformation(KEY);
     * if (frameRectangleMap == null) {
     *     frameRectangleMap = new HashMap();
     *     getProject().setClientInformation(KEY, frameRectangleMap);
     * }
     * // ...
     * // save rectangles associated with frames into frameRectangleMap with code like
     * frameRectangleMap.put(frameX, rectangleX);
     * frameRectangleMap.put(frameY, rectangleY);
     * // ..
     * 
     * // elsewhere
     * // retrieve positions
     * Map map = (Map) getClientInformation(KEY);
     * Rectangle x = (Rectangle) map.get(frameX);
     * Rectangle y = (Rectangle) map.get(frameY);
     * </code></pre>
     */

    public Object getClientInformation(Object key) {
        return _clientInformation.get(key);
    }

    /**
     * Set persistent client information. See
     * {@link #getClientInformation(Object)}for more information about "client
     * information".
     */
    public void setClientInformation(Object key, Object value) {
        if (value == null) {
            _clientInformation.remove(key);
        } else {
            _clientInformation.put(key, value);
        }
    }

    public Collection getCurrentUsers() {
        return _projectKB.getCurrentUsers();
    }

    public String getLocalUser() {
        return null;
    }

    protected void setKnowledgeBases(KnowledgeBase domainKb, KnowledgeBase projectKb) {
        _domainKB = domainKb;
        _domainKB.addKnowledgeBaseListener(_knowledgeBaseListener);
        _domainKB.setProject(this);
        _projectKB = projectKb;
        _projectInstance = getProjectInstance(_projectKB);
        loadCachedKnowledgeBaseObjects(_projectInstance);
    }

    public FrameCounts getFrameCounts() {
        _frameCounts.updateDirectFrameCounts(_domainKB);
        return _frameCounts;
    }

    public boolean isMultiUserClient() {
        return false;
    }

    public boolean isMultiUserServer() {
        return isMultiUserServer;
    }

    public String getUserName() {
        return ApplicationProperties.getUserName();
    }

    public void setPrettyPrintSlotWidgetLabels(boolean b) {
        prettyPrintSlotWidgetLabels = new Boolean(b);
    }

    public boolean getPrettyPrintSlotWidgetLabels() {
        if (prettyPrintSlotWidgetLabels == null) {
            boolean b = ApplicationProperties.getPrettyPrintSlotWidgetLabels();
            prettyPrintSlotWidgetLabels = loadOption(SLOT_PRETTY_PRINT_SLOT_WIDGET_LABELS, b);
        }
        return prettyPrintSlotWidgetLabels.booleanValue();
    }
}