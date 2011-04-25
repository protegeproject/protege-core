package edu.stanford.smi.protege.model;
//ESCA*JAVA0100
//ESCA*JAVA0136

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.ProjectEvent;
import edu.stanford.smi.protege.event.ProjectEventDispatcher;
import edu.stanford.smi.protege.event.ProjectListener;
import edu.stanford.smi.protege.event.WidgetAdapter;
import edu.stanford.smi.protege.event.WidgetEvent;
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.plugin.ProjectFixupsPluginManager;
import edu.stanford.smi.protege.resource.Files;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.ListenerCollection;
import edu.stanford.smi.protege.util.ListenerList;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.Tree;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.DefaultWidgetMapper;
import edu.stanford.smi.protege.widget.UglyClsWidget;
import edu.stanford.smi.protege.widget.Widget;
import edu.stanford.smi.protege.widget.WidgetMapper;
import edu.stanford.smi.protege.widget.WidgetUtilities;

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
	private static Logger log = Log.getLogger(Project.class);

    private static final String CLASS_PROJECT = "Project";
    private static final String SLOT_DEFAULT_INSTANCE_WIDGET_CLASS_NAME = "default_instance_widget_class_name";
    private static final String SLOT_CUSTOMIZED_INSTANCE_WIDGETS = "customized_instance_widgets";
    private static final String SLOT_BROWSER_SLOTS = "browser_slot_names";
    private static final String SLOT_TABS = "tabs";
    private static final String SLOT_INCLUDED_PROJECTS = "included_projects";
    private static final String SLOT_ALL_KNOWLEDGE_BASE_FACTORY_NAMES = "all_knowledge_base_factory_names";
    private static final String SLOT_SOURCES = "sources";
    private static final String SLOT_JAVA_PACKAGES = "java_packages";
    private static final String SLOT_HIDDEN_FRAMES = "hidden_classes";
    private static final String SLOT_JOURNALING_ENABLED = "journaling_enabled";
    private static final String SLOT_DEFAULT_CLS_METACLASS = "default_cls_metaclass";
    private static final String SLOT_DEFAULT_SLOT_METACLASS = "default_slot_metaclass";
    private static final String SLOT_DEFAULT_FACET_METACLASS = "default_facet_metaclass";
    private static final String SLOT_NEXT_FRAME_NUMBER = "next_frame_number";
    private static final String SLOT_IS_READONLY = "is_readonly";
    private static final String SLOT_PRETTY_PRINT_SLOT_WIDGET_LABELS = "pretty_print_slot_widget_labels";

    private static final String CLASS_OPTIONS = "Options";
    private static final String SLOT_OPTIONS = "options";
    private static final String SLOT_OPTIONS_INSTANCE_NAME = "option_instance";
    private static final String SLOT_DISPLAY_HIDDEN_FRAMES = "display_hidden_classes";
    private static final String SLOT_DISPLAY_ABSTRACT_CLASS_ICON = "display_abstract_class_icon";
    private static final String SLOT_DISPLAY_MULTI_PARENT_CLASS_ICON = "display_multi_parent_class_icon";
    private static final String SLOT_DISPLAY_REMOVE_CONFIRMATION_DIALOG = "confirm_on_remove";
    private static final String SLOT_UPDATE_MODIFICATION_SLOTS = "update_modification_slots";
    private static final String SLOT_TABBED_INSTANCE_FORM_LAYOUT = "tabbed_instance_form_layout";
    private static final String SLOT_IS_UNDO_ENABLED = "undo_enabled";

    private static final String CLIENT_PROPERTY_ADD_NAME_ON_INSTANCE_FORM = "add_name_on_instance_form";
    private static final String CHANGE_TRACKING_ACTIVE = "change_tracking_active";
    private static final String SUPRESS_INSTANCE_COUNT_DISPLAY = "suppress_instance_counting";

    protected static final String CLASS_MAP = "Map";
    protected static final String SLOT_PROPERTY_MAP = "property_map";
    private static final String SLOT_PROPERTY_MAP_INSTANCE_NAME = "property_map_instance";

    private static final int WINDOW_OFFSET_PIXELS = 25;

    private URI _uri;
    private URI _loadingProjectURI;
    private KnowledgeBase _projectKB;
    private Instance _projectInstance;
    private KnowledgeBase _domainKB;
    private String _defaultClsWidgetClassName;

    private Map<Cls, WidgetDescriptor> _activeClsWidgetDescriptors = new HashMap<Cls, WidgetDescriptor>();
    private Map<Cls, ClsWidget> _cachedDesignTimeClsWidgets = new HashMap<Cls, ClsWidget>(); 
    
    private Map<Object, JFrame> _frames = new HashMap<Object, JFrame>(); // <Instance or FrameSlotPair, JFrame>
    private Map<JFrame, Object> _objects = new HashMap<JFrame, Object>(); // <JFrame, Instance or FrameSlotPair>
    
    private WidgetMapper _widgetMapper;

    private Tree<URI> projectURITree = new Tree<URI>();
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
    private Boolean _isUndoEnabled;

    private Map<Cls, BrowserSlotPattern> _includedBrowserSlotPatterns = new HashMap<Cls, BrowserSlotPattern>();
    private Map<Cls, BrowserSlotPattern> _directBrowserSlotPatterns = new HashMap<Cls, BrowserSlotPattern>();
    private Map<Cls, BrowserSlotPattern> _inheritedBrowserSlotPatterns = new HashMap<Cls, BrowserSlotPattern>();
    
    private Set<Frame> _hiddenFrames = new HashSet<Frame>();
    private Set<Frame> _includedHiddenFrames = new HashSet<Frame>();
    private boolean _hasChanged;

    private Collection<WidgetDescriptor> _tabWidgetDescriptors;

    private Map _clientInformation = new HashMap();
    private Map _includedClientInformation = new HashMap();
        
    private FrameCountsImpl _frameCounts = new FrameCountsImpl();
    private boolean isMultiUserServer;
    private Class _instanceDisplayClass;
	
	private Instance _includedClientInfoInstance;
    

    private WindowListener _closeListener = new WindowAdapter() {
        @Override
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
        @Override
		public void clsDeleted(KnowledgeBaseEvent event) {
            if (log.isLoggable(Level.FINE)) {
              log.fine("cls eleted for project " + this + " event = " + event);
            }
            Cls cls = event.getCls();
            _activeClsWidgetDescriptors.remove(cls);
            ClsWidget widget = _cachedDesignTimeClsWidgets.remove(cls);
            if (widget != null) {
                ComponentUtilities.dispose((Component) widget);
            }
            _directBrowserSlotPatterns.remove(cls);
            removeInheritedBrowserSlotPattern(cls);           
            
            if (!event.isReplacementEvent()) {
            	removeDisplay(cls);
            }
            _hiddenFrames.remove(cls);
        }
    
        @Override
		public void frameReplaced(KnowledgeBaseEvent event) {        
            onFrameReplace(event.getFrame(), event.getNewFrame());
        }

        @Override
		public void facetDeleted(KnowledgeBaseEvent event) {
            Frame facet = event.getFrame();
            if (!event.isReplacementEvent()) {
            	removeDisplay(facet);
            }
            _hiddenFrames.remove(facet);
        }

        @Override
		public void slotDeleted(KnowledgeBaseEvent event) {
            Slot slot = (Slot) event.getFrame();
            if (!event.isReplacementEvent()) {
            	removeDisplay(slot);
            }
            Iterator<Map.Entry<Cls, BrowserSlotPattern>> i = _directBrowserSlotPatterns.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Cls, BrowserSlotPattern> entry = i.next();
                BrowserSlotPattern pattern = entry.getValue();
                if (pattern.contains(slot)) {
                    i.remove();
                }
            }
            Iterator<Map.Entry<Cls, BrowserSlotPattern>> j = _inheritedBrowserSlotPatterns.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry<Cls, BrowserSlotPattern> entry = j.next();
                BrowserSlotPattern pattern = entry.getValue();
                if (pattern != null && pattern.contains(slot)) {
                    j.remove();
                }
            }
            _hiddenFrames.remove(slot);
        }

        @Override
		public void instanceDeleted(KnowledgeBaseEvent event) {
            super.instanceDeleted(event);
            // Log.enter(this, "instanceDeleted");
            Frame frame = event.getFrame();
            if (!event.isReplacementEvent()) {
            	removeDisplay(frame);
            }
            _hiddenFrames.remove(frame);
        }
    };
	
    protected void onFrameReplace(Frame oldFrame, Frame newFrame) {
    	try {    		
	    	if (oldFrame instanceof Cls) {	    		
	            Cls oldCls = (Cls) oldFrame;
	            
	            //keep class form customizations
	            WidgetDescriptor widgetDesc = _activeClsWidgetDescriptors.get(oldCls);
	            if (widgetDesc != null) {
	            	widgetDesc.setName(newFrame.getName());
	            }
	            
	            //clear cached old class widget
	            ClsWidget widget = _cachedDesignTimeClsWidgets.remove(oldCls);
	            if (widget != null) {
	                ComponentUtilities.dispose((Component) widget);
	            }
	            
	            //replace the browser slot patterns
	            BrowserSlotPattern bsp = _directBrowserSlotPatterns.get(oldCls);
	            if (bsp != null) {
	            	_directBrowserSlotPatterns.put((Cls) newFrame, bsp);
	            }
	            _directBrowserSlotPatterns.remove(oldCls);
	            
	            BrowserSlotPattern ibsp = _inheritedBrowserSlotPatterns.get(oldCls);
	            if (ibsp != null) {
	                _inheritedBrowserSlotPatterns.put((Cls)newFrame, ibsp);
	            }
	     
	    	} else if (oldFrame instanceof Slot) {
	    		 Slot oldSlot = (Slot) oldFrame;
	    		 
	    		 //replace browser slot patterns
	             Iterator<Map.Entry<Cls, BrowserSlotPattern>> i = _directBrowserSlotPatterns.entrySet().iterator();
	             while (i.hasNext()) {
	                 Map.Entry<Cls, BrowserSlotPattern> entry = i.next();
	                 BrowserSlotPattern pattern = entry.getValue();
	                 if (pattern.contains(oldSlot)) {
	                	pattern.replaceSlot(oldSlot, (Slot) newFrame);
	                 }
	             }
	             Iterator<Map.Entry<Cls, BrowserSlotPattern>> j = _inheritedBrowserSlotPatterns.entrySet().iterator();
                 while (j.hasNext()) {
                     Map.Entry<Cls, BrowserSlotPattern> entry = j.next();
                     BrowserSlotPattern pattern = entry.getValue();
                     if (pattern != null && pattern.contains(oldSlot)) {
                        pattern.replaceSlot(oldSlot, (Slot) newFrame);
                     }
                 }
	    	}
	    	//TODO: client information
	    	
            //update hidden frames
            if (_hiddenFrames.contains(oldFrame)) {
            	_hiddenFrames.add(newFrame);
            }
            _hiddenFrames.remove(oldFrame);

	    	
    	} catch (Throwable t) {
    		Log.getLogger().log(Level.WARNING, "Error at replacing project information (Old: " + 
    				oldFrame + " , New: " + newFrame + ")", t);
    	}
    }
    
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
        if (log.isLoggable(Level.FINE)) {
          log.fine("Creating Project " + uri + " multiserver = " + isMultiUserServer);
        }
        setProjectURI(uri);
        _projectKB = loadProjectKB(uri, factory, errors);
        if (_projectKB != null) {
            _projectInstance = getProjectInstance(_projectKB);
        }
        
        //Multi-user client won't go in there
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
        Iterator<ClsWidget> i = _cachedDesignTimeClsWidgets.values().iterator();
        while (i.hasNext()) {
            ClsWidget widget = i.next();
            ComponentUtilities.dispose((Component)widget);
        }
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

    private boolean createDomainKB(KnowledgeBaseFactory factory, Collection errors) {
        if (factory == null) {
            factory = getKnowledgeBaseFactory();
        }
        if (factory == null) {
        	String errorMsg = "Cannot find knowledgebase factory: " + getSources().getString(KnowledgeBaseFactory.FACTORY_CLASS_NAME) + "\nPlease check that you have the required plug-in.";        	
        	errors.add(new MessageError(errorMsg));
        	Log.getLogger().severe(errorMsg);
        	return false;
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
        return true;
    }

    /**
     * @param errors
     *            See class note for information about this argument.
     */
    public void createDomainKnowledgeBase(KnowledgeBaseFactory factory, Collection errors,
            boolean load) {
        if (!createDomainKB(factory, errors))
        	return;
        
        if (load) {
            MergingNarrowFrameStore mnfs = MergingNarrowFrameStore.get(_domainKB);
            if (mnfs != null) {
                mnfs.setQueryAllFrameStores(true);
            }
            Collection uris = loadIncludedProjects(getProjectURI(), _projectInstance, errors);
            loadDomainKB(uris, errors);
            
            if (mnfs != null) {
                mnfs.setQueryAllFrameStores(false);
            }
        }
        
       	_domainKB.addKnowledgeBaseListener(_knowledgeBaseListener);
       	loadCachedKnowledgeBaseObjects(_projectInstance);
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
        Object[] args = new Object[] { project, Boolean.valueOf(showHeader),
                Boolean.valueOf(showHeaderLabel) };
        try {
            Constructor constructor = clas.getConstructor(classes);
            instanceDisplay = (InstanceDisplay) constructor.newInstance(args);
        //ESCA-JAVA0166
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
        display.setResizeVertically(true);
        ClsWidget widget = display.getFirstClsWidget();
        frame.setTitle(widget.getLabel());
        widget.addWidgetListener(new WidgetAdapter() {
            @Override
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
        return createRuntimeClsWidget(instance.getDirectType(), instance, associatedCls);
    }

    public ClsWidget createRuntimeClsWidget(Cls type, Instance instance, Cls associatedCls) {
        ClsWidget widget;
        if (type == null) {
            Log.getLogger().severe("no direct type: " + instance.getName());
            widget = new UglyClsWidget();
        } else {
            ClsWidget designTimeWidget = getDesignTimeClsWidget(type);
            WidgetDescriptor d = designTimeWidget.getDescriptor();
            widget = WidgetUtilities.createClsWidget(d, false, this, type);
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
    @Deprecated
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
        clearWidgets();
        if (_domainKB != null) {
        	try {
        		_domainKB.dispose();
			} catch (Throwable t) {
				log.log(Level.WARNING, "Errors at disposing domain KB", t);
			}            
        }
        if (_projectKB != null) {
        	try {
                _projectKB.dispose();				
			} catch (Throwable t) {
				log.log(Level.WARNING, "Errors at disposing project KB", t);
			}
        }
        _domainKB = null;
        _projectKB = null;
        _projectInstance = null;
        _activeClsWidgetDescriptors = null;
        _cachedDesignTimeClsWidgets = null;
        _frames = null;
        _objects = null;
        
        //_widgetMapper = null;
        
        _directBrowserSlotPatterns = null;
        _inheritedBrowserSlotPatterns = null;
        _includedBrowserSlotPatterns = null;
        
        _clientInformation = null;
        _includedClientInformation = null;
        
        PropertyMapUtil.dispose();
    }

    private void clearWidgets() {
        if (_cachedDesignTimeClsWidgets != null) {
            Iterator<ClsWidget> i = _cachedDesignTimeClsWidgets.values().iterator();
            while (i.hasNext()) {
                JComponent widget = (JComponent) i.next();
                ComponentUtilities.dispose(widget);
            }
            _cachedDesignTimeClsWidgets.clear();
        }
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
    @Deprecated
	public Slot getBrowserSlot(Cls cls) {
        return getPatternSlot(getBrowserSlotPattern(cls));
    }

    public Collection getBrowserSlots(Cls cls) {
        return getBrowserSlotPattern(cls).getSlots();
    }

    public Collection<Cls> getClsesWithDirectBrowserSlots() {
        return _directBrowserSlotPatterns.keySet();
    }

    public Collection<Cls> getClsesWithCustomizedForms() {
        return _activeClsWidgetDescriptors.keySet();
    }

    public Collection<Frame> getHiddenFrames() {
        return new HashSet<Frame>(_hiddenFrames);
    }

    public BrowserSlotPattern getBrowserSlotPattern(Cls cls) {
        BrowserSlotPattern slotPattern = getDirectBrowserSlotPattern(cls);
        if (slotPattern == null) {
            slotPattern = getInheritedBrowserSlotPattern(cls);
        }
        return slotPattern;
    }

    /**
     * @deprecated Use {@link #getInheritedBrowserSlotPattern(Cls)}
     */
    @Deprecated
	public Slot getInheritedBrowserSlot(Cls cls) {
        return getPatternSlot(getInheritedBrowserSlotPattern(cls));
    }

    /**
     * @deprecated
     */
    @Deprecated
	private static Slot getPatternSlot(BrowserSlotPattern pattern) {
        return (pattern == null) ? null : pattern.getFirstSlot();
    }

    public BrowserSlotPattern getInheritedBrowserSlotPattern(Cls cls) {
        BrowserSlotPattern slotPattern = _inheritedBrowserSlotPatterns.get(cls);
        if (_inheritedBrowserSlotPatterns.containsKey(cls)) { return slotPattern; }
        
        Iterator i = cls.getSuperclasses().iterator();
        while (i.hasNext() && slotPattern == null) {
            Cls superclass = (Cls) i.next();
            slotPattern = getDirectBrowserSlotPattern(superclass);
        }       
        _inheritedBrowserSlotPatterns.put(cls, slotPattern);        
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
        ClsWidget widget = _cachedDesignTimeClsWidgets.get(cls);
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

    /**
     * @deprecated
     */
    @Deprecated
	public Slot getDirectBrowserSlot(Cls cls) {
        return getPatternSlot(getDirectBrowserSlotPattern(cls));
    }

    public BrowserSlotPattern getDirectBrowserSlotPattern(Cls cls) {
        return _directBrowserSlotPatterns.get(cls);
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

    private static void setIconImage(JFrame frame, Instance instance) {
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
    public Collection<URI> getIncludedProjects() {
        return projectURITree.getDescendents(projectURITree.getRoot());
    }

    public Tree<URI> getProjectTree() {
        return projectURITree.clone();
    }

    public void setDirectIncludedProjectURIs(Collection projectURIs) {
        Collection uriStrings = new ArrayList();
        Iterator i = projectURIs.iterator();
        while (i.hasNext()) {
            URI includedURI = (URI) i.next();
            URI relativeURI = URIUtilities.relativize(_uri, includedURI);
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
    @Deprecated
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
    public Collection<URI> getDirectIncludedProjectURIs() {
        Collection uris = new ArrayList();
        Iterator i = getProjectSlotValues(SLOT_INCLUDED_PROJECTS).iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            URI uri = null;
            if (URIUtilities.isURI(s))
        		uri = URIUtilities.createURI(s);
        	else
        		uri = URIUtilities.resolve(getProjectURI(), s);
            //URI uri = URIUtilities.resolve(_uri, s);
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

    public Collection<JFrame> getOpenWindows() {
        return Collections.unmodifiableCollection(_frames.values());
    }

    private boolean getOption(String slotName, boolean defaultValue) {
        Object b = getOwnSlotValue(getOptionsInstance(), slotName);
        
        if (b != null && b instanceof Boolean)
        	return ((Boolean)b).booleanValue();
        
        return defaultValue;
    }

    private Instance getOptionsInstance() {
        Instance instance = (Instance) getProjectSlotValue(SLOT_OPTIONS);
        if (instance == null) {
            Cls optionsCls = _projectKB.getCls(CLASS_OPTIONS);
            instance = _projectKB.createInstance(SLOT_OPTIONS_INSTANCE_NAME, optionsCls);
            setProjectSlotValue(SLOT_OPTIONS, instance);
        }
        return instance;
    }

    protected static Object getOwnSlotValue(Frame frame, String slotName) {
        return ModelUtilities.getDirectOwnSlotValue(frame, slotName);
    }

    protected static Reader getProjectClsesReader() {
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
    @Deprecated
	public File getProjectDirectoryFile() {
        File projectFile = getProjectFile();
        return (projectFile == null) ? null : projectFile.getParentFile();
    }

    /**
     * @deprecated Use #getProjectURI()
     */
    @Deprecated
	public File getProjectFile() {
        return (_uri == null) ? null : new File(_uri);
    }

    /**
     * @deprecated Use #getProjectURI()
     */
    @Deprecated
	public String getProjectFilePath() {
        File file = getProjectFile();
        return (file == null) ? null : file.getPath();
    }


    public Instance getProjectInstance() {
        return _projectInstance;
    }

    protected static Instance getProjectInstance(KnowledgeBase kb) {
        Instance result = null;
        Cls cls = kb.getCls(CLASS_PROJECT);
        if (cls == null) {
            Log.getLogger().severe("no project class");
        } else {
            Collection<Instance> instances = cls.getDirectInstances();
            // Assert.areEqual(instances.size(), 1);
            result = CollectionUtilities.getFirstItem(instances);
        }
        if (result == null) {
            Log.getLogger().severe("no project instance");
        }
        return result;
    }

    protected static Reader getProjectInstancesReader(URI uri, KnowledgeBaseFactory factory,
            Collection errors) {
        Reader reader = null;
        if (uri != null) {
            reader = URIUtilities.createBufferedReader(uri);
            if (reader == null) {
            	String message = "Unable to load project from: " + uri;
                errors.add(new MessageError(message));
                Log.getLogger().severe(message);
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

    public Collection<WidgetDescriptor> getTabWidgetDescriptors() {
        if (_tabWidgetDescriptors == null) {
            Set availableTabNames = new HashSet(PluginUtilities.getAvailableTabWidgetClassNames());

            _tabWidgetDescriptors = new ArrayList<WidgetDescriptor>();
            Iterator i = getProjectSlotValues(SLOT_TABS).iterator();
            while (i.hasNext()) {
                Instance instance = (Instance) i.next();
                WidgetDescriptor d = WidgetDescriptor.create(instance);
                String name = d.getWidgetClassName();
                if (log.isLoggable(Level.FINE)) {
                	log.fine("Project found tab plugin called " + name);
                }
                if (PluginUtilities.isLoadableClass(name)) {
                    _tabWidgetDescriptors.add(d);
                }
                boolean removed = availableTabNames.remove(name);
                if (!removed && log.isLoggable(Level.FINE)) {
                  log.fine("tab " + name + " not in manifest");
                }
            }

            createNewTabWidgetDescriptors(availableTabNames);
            // saveTabWidgetInstances();
        }
        //ESCA-JAVA0259
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
        WidgetDescriptor d = _activeClsWidgetDescriptors.get(cls);
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
        if (factoryName == null){
        	Log.getLogger().warning("Unable to load prj:" + name);
        	return;
        }
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) SystemUtilities.newInstance(factoryName);
        PropertyList sources = getSources(projectInstance);
        // TODO remove this fragment of code and include it in the
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

    private static boolean hasChanged(KnowledgeBase kb) {
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
        	if (isMultiUserClient()) {
        		_isReadonly = !RemoteClientFrameStore.isOperationAllowed(getKnowledgeBase(), MetaProjectConstants.OPERATION_WRITE);
        	} else {
        		_isReadonly = loadOption(SLOT_IS_READONLY, false);
        	}        	
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
    			BrowserSlotPattern slotPattern = BrowserSlotPattern.createFromSerialization(_domainKB, patternText);
    			
    			if (cls != null && slotPattern != null) {
    				recordDirectBrowserSlotPattern(cls, slotPattern);
    				if (isIncluded(projectInstance)) {
    					_includedBrowserSlotPatterns.put(cls, slotPattern);
    					
    					//TT - do only if server - should not be saved because they are part of _includedBrowserSlotPattern
    					if (isMultiUserServer()) {
    						copyIncludedBrowserSlot(projectInstance, clsName, patternText);
    					}
    					
    				}
    			} else {
    				// Log.warning("Bad frame properties: " + clsName + " " + slotName, this, "loadFrameProperties");
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

    
    private void copyIncludedBrowserSlot(Instance projectInstance, String clsName, String patternText) {
    	PropertyList browserSlots = getPropertyList(_projectInstance, SLOT_BROWSER_SLOTS);
    	browserSlots.setString(clsName, patternText);    	
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

            // TODO - remove this fragment and include it in the factory
            if (factory instanceof KnowledgeBaseFactory2) {
                URI uri = getProjectURI();
                String name = (uri == null) ? "<new>" : uri.toString();
                NarrowFrameStore nfs = ((KnowledgeBaseFactory2)factory).createNarrowFrameStore(name);
                MergingNarrowFrameStore mergingFrameStore = getMergingFrameStore();
                mergingFrameStore.addActiveFrameStore(nfs, uris);
            }
            // TODO - remove this fragment of code by merging the new interface with the old
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
                    
                  //TT - do only if server - should not be saved because they are part of _includedHiddenFrames
                    if (isMultiUserServer()) {
                    	copyIncludedHiddenFrameFlag(projectInstance, name);
                    }
                }
            }
        }
    }

    private void copyIncludedHiddenFrameFlag(Instance projectInstance, String includedFrameName) {
		addProjectSlotValue(SLOT_HIDDEN_FRAMES, includedFrameName);
	}

    private boolean isIncluded(Instance projectInstance) {
        return _projectInstance != projectInstance;
    }

    public URI getLoadingURI() {
        return _loadingProjectURI == null ? getProjectURI() : _loadingProjectURI;
    }

    private void loadIncludedProject(URI includingURI, URI includedURI, Collection errors) {
        includedURI = URIUtilities.normalize(includedURI);
        boolean alreadyIncluded = isAlreadyIncluded(includedURI);
        projectURITree.addChild(includingURI, includedURI);
        if (!alreadyIncluded) {
            // Log.enter(this, "loadIncludedProject", includedURI);
            KnowledgeBase kb = loadProjectKB(includedURI, null, errors);
            if (kb != null) {
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
        Collection uris = new LinkedHashSet();
        Iterator i = getProjectSlotValues(projectInstance, SLOT_INCLUDED_PROJECTS).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            URI uri;
            if (_uri == null) {
                uri = URIUtilities.createURI(name);
            } else {
            	if (URIUtilities.isURI(name))
            		uri = URIUtilities.createURI(name);
            	else
            		uri = URIUtilities.resolve(projectURI, name);
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
        return Boolean.valueOf(b);
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
            	String message = "Unable to open project file: " + uri; 
                errors.add(new MessageError(message));
                Log.getLogger().severe(message);
            } else {
                //kb = new ClipsKnowledgeBaseFactory().loadKnowledgeBase(clsesReader, instancesReader, errors);
            	ClipsKnowledgeBaseFactory clipsFactory = new ClipsKnowledgeBaseFactory();
            	
            	kb = clipsFactory.createKnowledgeBase(errors);
            	kb.setGenerateEventsEnabled(false);
            	
            	clipsFactory.loadKnowledgeBase(kb, clsesReader, instancesReader, false, errors);
            	
                if (errors.size() == 0) {
                   ProjectFixupsPluginManager.fixProject(kb);
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
        	errors.add(new MessageError(e));
            Log.getLogger().log(Level.SEVERE, "Error loading project kb", e);            
        } finally {
            FileUtilities.close(clsesReader);
            FileUtilities.close(instancesReader);
        }
        return kb;
    }

    /*
     * This method merges the client information from the including projects.
     * It also saves the included client info in an instance, so that at
     * save time it can remove the included information.     
     */
    private void loadClientInformation(Instance projectInstance) {   		
   		
   		Map clientInfoMap = getClientInfoMap(projectInstance);
   		
   		if (isIncluded(projectInstance)) {
   			_includedClientInformation.putAll(clientInfoMap);   		
   		}
   		
   		_clientInformation.putAll(clientInfoMap);
   		
   		/* 
   		 * This is executed only once. Need to make a copy of the included client info in an instance
   		 * otherwise the included client info is modified when the content of the map changes.
   		 * (This happens esp. with the GraphWidget when it saves the positions of the nodes)
   		 * 
   		 */
   		if (!isIncluded(projectInstance)) {
   			PropertyMapUtil.store(_clientInformation, getClientInfoInstance(projectInstance));
   			
   			//store included client info in a map instance
   			Cls cls = projectInstance.getKnowledgeBase().getCls(CLASS_MAP);
            _includedClientInfoInstance = projectInstance.getKnowledgeBase().createInstance(null, cls);
   			
            PropertyMapUtil.store(_includedClientInformation, _includedClientInfoInstance);
   		}
    }
        

    private Instance getClientInfoInstance(Instance projectInstance) {
    	Instance clientInfoInstance = (Instance) getOwnSlotValue(projectInstance, SLOT_PROPERTY_MAP);
    	
    	if (clientInfoInstance == null) {
            Cls cls = projectInstance.getKnowledgeBase().getCls(CLASS_MAP);
            clientInfoInstance = projectInstance.getKnowledgeBase().createInstance(null, cls);
            ModelUtilities.addOwnSlotValue(projectInstance, SLOT_PROPERTY_MAP,  clientInfoInstance);
    	}
    	
    	return clientInfoInstance;
    }
    
    
	private Map getClientInfoMap(Instance projectInstance) {    	
    	Instance clientInfoInstance = (Instance) getOwnSlotValue(projectInstance, SLOT_PROPERTY_MAP);
    	
    	if (clientInfoInstance == null) {
    		return new HashMap();
    	}
    	
    	return PropertyMapUtil.load(clientInfoInstance, _domainKB);
    }
    

    /**
     * Utility method that copies the client information from the source project KB to a target project KB. 
     * @param sourceKb - the source project KB
     * @param targetKb - the target project KB
     * @param domainKb - the target domain KB
     * @param appendInfo - if true, it appends the source client information to the target client information. 
     * If false, the target client info is overridden by the copied source client information
     */
    protected static void copyClientInformation(KnowledgeBase sourceKb, KnowledgeBase targetKb, KnowledgeBase domainKb, boolean appendInfo) {
    	//source info
    	Instance sourceProjectInstance = getProjectInstance(sourceKb);
    	Instance sourceClientInfoInstance = (Instance) getOwnSlotValue(sourceProjectInstance, SLOT_PROPERTY_MAP);
    	Map clientInformation = PropertyMapUtil.load(sourceClientInfoInstance, domainKb);
    	
    	//target info
    	Instance targetProjectInstance = getProjectInstance(targetKb);
    	Instance targetClientInfoInstance = (Instance) getOwnSlotValue(targetProjectInstance, SLOT_PROPERTY_MAP);
    	
    	if (targetClientInfoInstance != null) {
    		if (appendInfo) {
    			Map targetClientInformation = PropertyMapUtil.load(targetClientInfoInstance, domainKb);
    			clientInformation.putAll(targetClientInformation);
    		}
    		targetClientInfoInstance.delete();
    	}
    	
        Cls cls = targetKb.getCls(CLASS_MAP);
        targetClientInfoInstance = targetKb.createInstance(null, cls);
        ModelUtilities.addOwnSlotValue(targetProjectInstance, SLOT_PROPERTY_MAP,  targetClientInfoInstance);
        
        PropertyMapUtil.store(clientInformation, targetClientInfoInstance);
	}
        

    private void loadWidgetDescriptors(Instance projectInstance) {
    	Iterator i = new ArrayList(getProjectSlotValues(projectInstance, SLOT_CUSTOMIZED_INSTANCE_WIDGETS)).iterator();

    	while (i.hasNext()) {
    		Instance instance = (Instance) i.next();

			// duplicate included widget descriptors into main project
    		if (isIncluded(projectInstance)) {
    			Instance  includingInstance = (Instance) instance.deepCopy(_projectKB, null);
    			
    			//support forms inclusion in client-server
    			if (isMultiUserServer()) {
    				_projectInstance.addOwnSlotValue(_projectKB.getSlot(SLOT_CUSTOMIZED_INSTANCE_WIDGETS), includingInstance);
    			}
    			
    			instance = includingInstance;
    		}

    		WidgetDescriptor d = WidgetDescriptor.create(instance);
    		if (d == null) {
    			Log.getLogger().severe("Invalid widget instance: " + instance);
    			removeProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, instance);
    		} else {
    			Cls cls = _domainKB.getCls(d.getName());
    			if (cls == null) {
    				Log.getLogger().warning("Unknown class: " + d.getName());
    				removeProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, instance);
    			} else {
    				if (isIncluded(projectInstance)) {
    					d.setIncluded(true);
    				}
    				d.setDirectlyCustomizedByUser(true);
    				// Log.getLogger().info("**" + cls + " " + d.getWidgetClassName());
    				// be careful not to overwrite widgets on an "include"
    				// command.
    				WidgetDescriptor existingDescriptor = _activeClsWidgetDescriptors.get(cls);
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
        Iterator<WidgetDescriptor> i = _activeClsWidgetDescriptors.values().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = i.next();
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

    private void removeInheritedBrowserSlotPattern(Cls cls) {
        _inheritedBrowserSlotPatterns.remove(cls);
        for (Iterator<Cls> iterator = _inheritedBrowserSlotPatterns.keySet().iterator(); iterator.hasNext();) {
            Cls scls = iterator.next();
            if (scls.hasSuperclass(cls)) {
                iterator.remove();
            }
        }
    }
    
    private void recordDirectBrowserSlotPattern(Cls cls, BrowserSlotPattern slotPattern) {
        Assert.assertNotNull("class", cls);
        if (slotPattern == null) {
            _directBrowserSlotPatterns.remove(cls);            
        } else {
            _directBrowserSlotPatterns.put(cls, slotPattern);            
        }        
        removeInheritedBrowserSlotPattern(cls);
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
            name = URIUtilities.relativize(_uri, name);
        }
        addProjectSlotValue(SLOT_INCLUDED_PROJECTS, name.toString());
    }

    private void removeDisplay(Frame frame) {
        JFrame jframe = _frames.get(frame);
        if (jframe != null) {
            ComponentUtilities.closeWindow(jframe);
        }
    }

    public void removeIncludedProjectReferences() {
        Map<Cls, BrowserSlotPattern> browserSlots = new HashMap<Cls, BrowserSlotPattern>();
        browserSlots.putAll(_includedBrowserSlotPatterns);
        browserSlots.putAll(_directBrowserSlotPatterns);
        _directBrowserSlotPatterns = browserSlots;
        _includedBrowserSlotPatterns.clear();
        //TODO: do something with the inherited browser slot patterns?

        Iterator<WidgetDescriptor> i = _activeClsWidgetDescriptors.values().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = i.next();
            if (d.isIncluded()) {
                d.setIncluded(false);
            }
        }

        projectURITree = new Tree<URI>(getProjectURI());
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

    protected static void removeUnreferencedInstances(KnowledgeBase kb) {
        if (!isNewProject(kb)) {
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
    }

    public static boolean isNewProject(KnowledgeBase kb) {
        return kb.getBuildString() == null;
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
        Iterator<Map.Entry<Cls, BrowserSlotPattern>> i = _directBrowserSlotPatterns.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<Cls, BrowserSlotPattern> entry = i.next();
            Cls cls = entry.getKey();
            BrowserSlotPattern slotPattern = entry.getValue();
            if (!isIncludedBrowserSlotPattern(cls, slotPattern)) {
                browserSlots.setString(cls.getName(), slotPattern.getSerialization());
            }
        }
    }

    private void saveCustomizedWidgets() {
        setProjectSlotValue(SLOT_CUSTOMIZED_INSTANCE_WIDGETS, null);
        Iterator<WidgetDescriptor> i = _activeClsWidgetDescriptors.values().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = i.next();
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
        Collection<String> hiddenFrames = new ArrayList<String>();
        Iterator<Frame> i = _hiddenFrames.iterator();
        while (i.hasNext()) {
            Frame frame = i.next();
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
    	/* 
    	 * This happens in OWL mode, if the owl model is opened with the JenaOWLModel calls
    	 * and not with the project calls.
    	 */
    	if (_uri == null) {
    		return;
    	}
    	
        String s = new File(_uri).toString();
        new ClipsKnowledgeBaseFactory().saveKnowledgeBase(_projectKB, null, s, errors);
    }

    /**
     * This method saves the client information in a property map instance.
     * The included client information is filtered out from the save by
     * comparing the keys and values of the client info with the ones from
     * the _includedClientInformation map. If the included value has changed, \
     * then the change is saved, otherwise not.  
     */
    private void saveClientInformation() {
        if (!_clientInformation.isEmpty()) {
            Instance propertyMapInstance = (Instance) getOwnSlotValue(_projectInstance, SLOT_PROPERTY_MAP);
            if (propertyMapInstance == null) {
                Cls cls = _projectKB.getCls(CLASS_MAP);
                propertyMapInstance = _projectKB.createInstance(SLOT_PROPERTY_MAP_INSTANCE_NAME, cls);
                ModelUtilities.addOwnSlotValue(_projectInstance, SLOT_PROPERTY_MAP, propertyMapInstance);
            }
            
            /* 
             * TT - don't save in the including project the included client info unless it has been modified.
             * This is not a perfect algorithm, but it seems to work well.
             * Don't modify the _clientInformation, because the user might have just saved but not exited the project
             */ 
            
            Map copyClientInfo = new HashMap();
            copyClientInfo.putAll(_clientInformation);
            
            _includedClientInformation = PropertyMapUtil.load(_includedClientInfoInstance, _domainKB);
            
            for (Iterator iterator = copyClientInfo.keySet().iterator(); iterator.hasNext();) {
				Object key = iterator.next();
				Object value = copyClientInfo.get(key);
									
				if (value != null) {
					Object includedValue = _includedClientInformation.get(key);
					
					if (includedValue != null && value.equals(includedValue)) {
						iterator.remove();
					}
				}
			}
            
            PropertyMapUtil.store(copyClientInfo, propertyMapInstance);
        }
    }

    private void saveTabWidgetInstances() {
        Collection<Instance> instances = new ArrayList<Instance>();
        Iterator<WidgetDescriptor> i = _tabWidgetDescriptors.iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = i.next();
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
        _displayAbstractClassIcon = Boolean.valueOf(b);
        setOption(SLOT_DISPLAY_ABSTRACT_CLASS_ICON, b);
    }

    public void setDisplayConfirmationOnRemove(boolean b) {
        _displayConfirmationOnRemove = Boolean.valueOf(b);
        setOption(SLOT_DISPLAY_REMOVE_CONFIRMATION_DIALOG, b);
    }

    public void setDisplayHiddenFrames(boolean b) {
        _displayHiddenClasses = Boolean.valueOf(b);
        setOption(SLOT_DISPLAY_HIDDEN_FRAMES, b);
    }

    public void setDisplayHiddenClasses(boolean b) {
        setDisplayHiddenFrames(b);
    }

    public void setDisplayMultiParentClassIcon(boolean b) {
        _displayMultiParentClassIcon = Boolean.valueOf(b);
        setOption(SLOT_DISPLAY_MULTI_PARENT_CLASS_ICON, b);
    }

    public void setHidden(Frame frame, boolean hidden) {
        recordHidden(frame, hidden);
        setChanged(true);
    }

    public void setIsReadonly(boolean b) {
        _isReadonly = Boolean.valueOf(b);
        setOption(SLOT_IS_READONLY, b);
    }

    public void setJournalingEnabled(boolean enable) {
        setProjectSlotValue(SLOT_JOURNALING_ENABLED, Boolean.valueOf(enable));
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
            _lastLocation.x += WINDOW_OFFSET_PIXELS;
            _lastLocation.y += WINDOW_OFFSET_PIXELS;
            Dimension screenSize = window.getToolkit().getScreenSize();

            if (_lastLocation.x + window.getWidth() > screenSize.width
                    || _lastLocation.y + window.getHeight() > screenSize.height) {
                _lastLocation = new Point();
            }
            window.setLocation(_lastLocation);
        }
    }

    private void setOption(String slotName, boolean value) {
        ModelUtilities.setOwnSlotValue(getOptionsInstance(), slotName, Boolean.valueOf(value));
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
        _uri = URIUtilities.normalize(uri);
        updateKBNames();
        updateJournaling();
        projectURITree.swapNode(projectURITree.getRoot(), uri);
        if (uri != null) {
            setActiveFrameStoreName(uri);
        }
        activeRootURI = uri;
    }

    private MergingNarrowFrameStore getMergingFrameStore() {
        return MergingNarrowFrameStore.get(_domainKB);
    }

    private void setActiveFrameStoreName(URI uri) {
        if (uri != null) {
            MergingNarrowFrameStore nfs = getMergingFrameStore();
            if (nfs != null) {
                nfs.getActiveFrameStore().setName(uri.toString());
            }
        }
    }

    private void setActiveFrameStore(URI uri) {
        if (uri != null) {
            MergingNarrowFrameStore nfs = getMergingFrameStore();
            if (nfs != null) {
                nfs.setActiveFrameStore(uri.toString());
            }
        }
    }

    public URI getActiveRootURI() {
        return activeRootURI;
    }

    public void setActiveRootURI(URI uri) {
        uri = URIUtilities.normalize(uri);
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
        _tabWidgetDescriptors = new ArrayList<WidgetDescriptor>(c);
        saveTabWidgetInstances();
    }

    public void setUpdateModificationSlots(boolean b) {
        _updateModificationSlots = Boolean.valueOf(b);
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
        JFrame frame = _frames.get(combination);
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
        JFrame frame = _frames.get(instance);
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
        display.setResizeVertically(true);
        ClsWidget widget = display.getFirstClsWidget();
        String title = widget.getLabel();
        JInternalFrame frame = new JInternalFrame(title, true);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(display);
        frame.pack();

        return frame;
    }

    @Override
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
        WidgetDescriptor d = _activeClsWidgetDescriptors.get(cls);
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
     *        a.equals(new MyClass(a.toString());
     *
     * </pre></code>
     *
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

    //ESCA-JAVA0130
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

    //ESCA-JAVA0130
    public boolean isMultiUserClient() {
        return false;
    }

    public boolean isMultiUserServer() {
        return isMultiUserServer;
    }

    //ESCA-JAVA0130
    public String getUserName() {
        return ApplicationProperties.getUserName();
    }

    public void setPrettyPrintSlotWidgetLabels(boolean b) {
        prettyPrintSlotWidgetLabels = Boolean.valueOf(b);
    }

    public boolean getPrettyPrintSlotWidgetLabels() {
        if (prettyPrintSlotWidgetLabels == null) {
            boolean b = ApplicationProperties.getPrettyPrintSlotWidgetLabels();
            prettyPrintSlotWidgetLabels = loadOption(SLOT_PRETTY_PRINT_SLOT_WIDGET_LABELS, b);
        }
        return prettyPrintSlotWidgetLabels.booleanValue();
    }

    public boolean getTabbedInstanceFormLayout() {
        return getOption(SLOT_TABBED_INSTANCE_FORM_LAYOUT, false);
    }

    public void setTabbedInstanceFormLayout(boolean b) {
        setOption(SLOT_TABBED_INSTANCE_FORM_LAYOUT, b);
    }

    
	public boolean isUndoOptionEnabled() {	
		if (_isUndoEnabled == null) {
			_isUndoEnabled = loadOption(SLOT_IS_UNDO_ENABLED, !isMultiUserClient());			
		}
		return _isUndoEnabled.booleanValue();
	}

	public void setUndoOption(boolean enabled) {
		_isUndoEnabled = Boolean.valueOf(enabled);
		setOption(SLOT_IS_UNDO_ENABLED, enabled);		
	}
    
    public boolean getAddNameOnInstanceForm() {
    	String addNameOnInstanceForm = (String) getClientInformation(CLIENT_PROPERTY_ADD_NAME_ON_INSTANCE_FORM);
    	
    	if (addNameOnInstanceForm == null || !addNameOnInstanceForm.equals("true")) {
    		return false;
    	}
    	
    	return true;
    }

    public void setAddNameOnInstanceForm(boolean b) {    	
        setClientInformation(CLIENT_PROPERTY_ADD_NAME_ON_INSTANCE_FORM, b ? "true" : "false");
    }
    
    public boolean getChangeTrackingActive() {
    	String changeTrackingActive = (String) getClientInformation(CHANGE_TRACKING_ACTIVE);
    	
    	if (changeTrackingActive == null || !changeTrackingActive.equals("true")) {
    		return false;
    	}
    	
    	return true;
    }

    public void setChangeTrackingActive(boolean b) {    	
        setClientInformation(CHANGE_TRACKING_ACTIVE, b ? "true" : "false");
    }
    
    public boolean getSuppressInstanceCounting() {
        String suppressInstanceCounting = (String) getClientInformation(SUPRESS_INSTANCE_COUNT_DISPLAY);
        
        if (suppressInstanceCounting == null || !suppressInstanceCounting.equals("true")) {
            return false;
        }
        
        return true;
    }

    public void setSuppressInstanceCounting(boolean b) {        
        setClientInformation(SUPRESS_INSTANCE_COUNT_DISPLAY, b ? "true" : "false");
    }

}
