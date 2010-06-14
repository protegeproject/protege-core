package edu.stanford.smi.protege.model;

//ESCA*JAVA0136
//ESCA*JAVA0100

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FacetListener;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.InstanceListener;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.ServerProjectListener;
import edu.stanford.smi.protege.event.SlotListener;
import edu.stanford.smi.protege.event.TransactionListener;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.framestore.DefaultFrameFactory;
import edu.stanford.smi.protege.model.framestore.DeleteSimplificationFrameStore;
import edu.stanford.smi.protege.model.framestore.EventGeneratorFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.model.framestore.undo.UndoFrameStore;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.SynchronizeQueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.background.ServerCacheStateMachine;
import edu.stanford.smi.protege.server.job.GetServerProjectName;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Default implementation of the KnowledgeBase interface. Delegates almost everything to the FrameStore chain.
 * Implements wrapper methods for some calls to make the interface easier to use.
 * 
 * @author Ray Fergerson (fergerson@smi.stanford.edu)
 */
public class DefaultKnowledgeBase implements KnowledgeBase {
    private static transient Logger log = Log.getLogger(DefaultKnowledgeBase.class);
    private static final int GENERATED_NAME_LENGTH = 8;

    private FrameStoreManager _frameStoreManager;
    private SystemFrames _systemFrames;
    private FrameFactory _frameFactory;

    private Cls _defaultClsMetaCls;
    private Cls _defaultSlotMetaCls;
    private Cls _defaultFacetMetaCls;

    private String _buildString;
    private String _frameNamePrefix;
    private Map _clientInformation = new HashMap();
    private KnowledgeBaseFactory _knowledgeBaseFactory;
    private Project _project;
    private String _name;
    private String _versionString;
    private FrameNameValidator _frameNameValidator;
    private ServerCacheStateMachine cacheMachine;

    {
        initializeKBName();
        _systemFrames = createSystemFrames();
        _defaultClsMetaCls = _systemFrames.getStandardClsMetaCls();
        _defaultSlotMetaCls = _systemFrames.getStandardSlotMetaCls();
        _defaultFacetMetaCls = _systemFrames.getStandardFacetMetaCls();
        _frameFactory = createFrameFactory();
        _frameStoreManager = createFrameStoreManager();
    }

    private void initializeKBName() {
        String randomString = String.valueOf(Math.random());
        int len = Math.min(randomString.length(), GENERATED_NAME_LENGTH);
        _name = "KB_" + randomString.substring(2, len);
    }

    protected SystemFrames createSystemFrames() {
        return new SystemFrames(this);
    }

    protected FrameFactory createFrameFactory() {
        return new DefaultFrameFactory(this);
    }

    protected FrameStoreManager createFrameStoreManager() {
        return new FrameStoreManager(this);
    }

    public FrameStoreManager getFrameStoreManager() {
        return _frameStoreManager;
    }

    public FrameStore getHeadFrameStore() {
        if (_frameStoreManager == null) {
            throw new RuntimeException("Method called on closed knowledgeBase: " + getName());
        }
        return _frameStoreManager.getHeadFrameStore();
    }

    public DefaultKnowledgeBase(KnowledgeBaseFactory factory) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Phase 1 Initialization of Model starts");
        }
        _knowledgeBaseFactory = factory;
    }

    public DefaultKnowledgeBase() {
    }

    public synchronized SystemFrames getSystemFrames() {
        return _systemFrames;
    }

    public synchronized FrameFactory getFrameFactory() {
        return _frameFactory;
    }

    public synchronized void close() {
        _frameStoreManager.close();
    }

    public synchronized boolean setCleanDispatchEnabled(boolean b) {
        return _frameStoreManager.setCleanDispatchEnabled(b);
    }

    public synchronized boolean setArgumentCheckingEnabled(boolean b) {
        return _frameStoreManager.setArgumentCheckingEnabled(b);
    }

    public synchronized boolean isJournalingEnabled() {
        return _frameStoreManager.isJournalingEnabled();
    }

    public synchronized boolean isCallCachingEnabled() {
        return _frameStoreManager.isCallCachingEnabled();
    }


    public synchronized boolean setJournalingEnabled(boolean b) {
        return _frameStoreManager.setJournalingEnabled(b);
    }

    public synchronized boolean setUndoEnabled(boolean b) {
        return _frameStoreManager.setUndoEnabled(b);
    }

    public synchronized boolean setEventDispatchEnabled(boolean b) {
        return _frameStoreManager.setEventDispatchEnabled(b);
    }

    public synchronized boolean setCallCachingEnabled(boolean b) {
        return _frameStoreManager.setCallCachingEnabled(b);
    }

    public synchronized boolean setGenerateEventsEnabled(boolean b) {
        return _frameStoreManager.setGenerateEventsEnabled(b);
    }

    private UndoFrameStore getUndoFrameStore() {
        return _frameStoreManager.getUndoFrameStore();
    }

    public synchronized boolean isUndoEnabled() {
        return _frameStoreManager.isUndoEnabled();
    }

    public void flushEvents() throws ProtegeException {
      _frameStoreManager.flushEvents();
    }

    public synchronized List getDirectOwnSlotValues(Frame frame, Slot slot) {
        return getHeadFrameStore().getDirectOwnSlotValues(frame, slot);
    }

    public synchronized Cls createCls(String name, Collection directSuperclasses, Collection directTypes,
            boolean loadDefaults) {
      return createCls(new FrameID(name), directSuperclasses, directTypes, loadDefaults);
    }

    public synchronized Cls createCls(FrameID id, Collection directSuperclasses, Collection directTypes,
            boolean loadDefaults) {
        //        if (directTypes.isEmpty()) {
        //            Cls directType;
        //            Cls leadingSuperclass = (Cls) CollectionUtilities.getFirstItem(directSuperclasses);
        //            if (leadingSuperclass == null || equals(leadingSuperclass, _systemFrames.getRootCls())) {
        //                directType = _defaultClsMetaCls;
        //            } else {
        //                directType = leadingSuperclass.getDirectType();
        //            }
        //            directTypes = CollectionUtilities.createCollection(directType);
        //        }
        return getHeadFrameStore().createCls(id, directTypes, directSuperclasses, loadDefaults);
    }

    public synchronized Slot createSlot(String name, Cls directType, Collection superslots, boolean loadDefaults) {
        return createSlot(new FrameID(name), CollectionUtilities.createCollection(directType), superslots, loadDefaults);
    }

    public synchronized Slot createSlot(FrameID id, Collection directTypes, Collection superslots,
            boolean loadDefaults) {
        //        if (directTypes.isEmpty()) {
        //            directTypes = new ArrayList();
        //            directTypes.add(_defaultSlotMetaCls);
        //        }
        return getHeadFrameStore().createSlot(id, directTypes, superslots, loadDefaults);
    }

    public synchronized SimpleInstance createSimpleInstance(String name, Cls directType, boolean loadDefaults) {
        return createSimpleInstance(new FrameID(name), directType, loadDefaults);
    }

    public synchronized SimpleInstance createSimpleInstance(FrameID id, Collection types,
            boolean loadDefaults) {
        return getHeadFrameStore().createSimpleInstance(id, types, loadDefaults);
    }

    public synchronized SimpleInstance createSimpleInstance(FrameID id, Cls directType,
            boolean loadDefaults) {
        Collection types = CollectionUtilities.createCollection(directType);
        return createSimpleInstance(id, types, loadDefaults);
    }

    public synchronized void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        if (frame instanceof Slot && slot.equals(_systemFrames.getValueTypeSlot())) {
            ValueType type = ValueTypeConstraint.getType(values);
            values = CollectionUtilities.removeFirst(values);
            setValueTypeValues((Slot) frame, type, values);
        } else {
            getHeadFrameStore().setDirectOwnSlotValues(frame, slot, values);
        }
    }

    public synchronized Frame getFrame(FrameID id) {
        return getHeadFrameStore().getFrame(id);
    }

    public synchronized Frame getFrame(String name) {
        return getHeadFrameStore().getFrame(name);
    }

    public synchronized Collection getOwnSlotValues(Frame frame, Slot slot) {
        return getHeadFrameStore().getOwnSlotValues(frame, slot);
    }

    public synchronized Object getDirectOwnSlotValue(Frame frame, Slot slot) {
        List values = getDirectOwnSlotValues(frame, slot);
        return CollectionUtilities.getFirstItem(values);
    }

    public synchronized Collection<Slot> getOwnSlots(Frame frame) {
        return getHeadFrameStore().getOwnSlots(frame);
    }

    public synchronized Collection getTemplateSlots(Cls cls) {
        return getHeadFrameStore().getTemplateSlots(cls);
    }

    public synchronized Collection<Reference> getReferences(Frame frame) {
        return getHeadFrameStore().getReferences(frame);
    }

    public synchronized void deleteFrame(Frame frame) {
        if (frame instanceof Cls) {
            deleteCls((Cls) frame);
        } else if (frame instanceof Slot) {
            deleteSlot((Slot) frame);
        } else if (frame instanceof Facet) {
            deleteFacet((Facet) frame);
        } else {
            deleteSimpleInstance((SimpleInstance) frame);
        }
    }

    /**
     * @deprecated Use #setGenerateEventsEnabled(boolean)
     */
    @Deprecated
    public synchronized boolean setEventsEnabled(boolean b) {
        return setGenerateEventsEnabled(b);
    }

    /**
     * @deprecated Use #getGenerateEventsEnabled(boolean)
     */
    @Deprecated
    public synchronized boolean getEventsEnabled() {
        return getGenerateEventsEnabled();
    }

    public synchronized boolean getGenerateEventsEnabled() {
        return _frameStoreManager.getGenerateEventsEnabled();
    }

    public synchronized boolean getDispatchEventsEnabled() {
        return _frameStoreManager.getDispatchEventsEnabled();
    }

    public synchronized boolean setDispatchEventsEnabled(boolean b) {
        return _frameStoreManager.setEventDispatchEnabled(b);
    }

    public synchronized boolean setChangeMonitorEnabled(boolean b) {
        return _frameStoreManager.setChangeMonitorEnabled(b);
    }

    public synchronized Cls getDirectType(Instance instance) {
        Collection types = getDirectTypes(instance);
        return (Cls) CollectionUtilities.getFirstItem(types);
    }

    public synchronized Collection<Cls> getDirectSuperclasses(Cls cls) {
        return getHeadFrameStore().getDirectSuperclasses(cls);
    }

    public synchronized String getName(Frame frame) {
        return getHeadFrameStore().getFrameName(frame);
    }

    public synchronized void addJavaLoadPackage(String packageName) {
        _frameFactory.addJavaPackage(packageName);
    }

    public synchronized boolean areValidOwnSlotValues(Frame frame, Slot slot, Collection values) {
        boolean result = true;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.areValidValues(frame, slot, values);
        }
        return result;
    }

    public synchronized boolean containsFrame(String name) {
        return getFrame(name) != null;
    }

    public synchronized Cls createCls(String name, Collection superclasses) {
        return createCls(name, superclasses, getDefaultClsMetaCls(superclasses));
    }

    private Cls getDefaultClsMetaCls(Collection superclasses) {
        Cls clsMetaCls;
        Cls leadingSuperclass = (Cls) CollectionUtilities.getFirstItem(superclasses);
        if (leadingSuperclass == null || equals(leadingSuperclass, _systemFrames.getRootCls())) {
            clsMetaCls = _defaultClsMetaCls;
        } else {
            clsMetaCls = leadingSuperclass.getDirectType();
        }
        return clsMetaCls;
    }

    public synchronized Cls createCls(String name, Collection superclasses, Cls directType) {
        return createCls(name, superclasses, directType, true);
    }

    public synchronized Cls createCls(String name, Collection superclasses, Cls directType, boolean initializeDefaults) {
        Collection directTypes = CollectionUtilities.createCollection(directType);
        return createCls(name, superclasses, directTypes, initializeDefaults);
    }

    public synchronized Facet createFacet(String name) {
        return createFacet(name, _defaultFacetMetaCls);
    }

    public synchronized Facet createFacet(String name, Cls directType) {
        return createFacet(name, directType, true);
    }

    public synchronized Facet createFacet(String name, Cls directType, boolean initializeDefaults) {
        Collection types = CollectionUtilities.createCollection(directType);
        return createFacet(new FrameID(name), types, initializeDefaults);
    }

    public synchronized Facet createFacet(FrameID id, Collection directTypes, boolean initializeDefaults) {
        if (directTypes.isEmpty()) {
            directTypes = new ArrayList();
            directTypes.add(_defaultFacetMetaCls);
        }
        return getHeadFrameStore().createFacet(id, directTypes, initializeDefaults);
    }

    public synchronized Instance createInstance(String name, Cls directType) {
        return createInstance(name, directType, true);
    }

    public synchronized Instance createInstance(String name, Collection directTypes) {
        return createInstance(new FrameID(name), directTypes, true);
    }

    public synchronized Instance createInstance(String name, Cls directType, boolean initializeDefaults) {
        return createInstance(new FrameID(name), directType, initializeDefaults);
    }

    public synchronized Instance createInstance(FrameID id, Cls directType, boolean initializeDefaults) {
        Collection types = CollectionUtilities.createCollection(directType);
        return createInstance(id, types, initializeDefaults);
    }

    public synchronized Instance createInstance(FrameID id, Collection directTypes,
            boolean initializeDefaults) {
        Instance instance;
        // should do better than this
        Cls directType = (Cls) CollectionUtilities.getFirstItem(directTypes);
        if (directType == null) {
            instance = createSimpleInstance(id, directType, initializeDefaults);
        } else {
            if (isClsMetaCls(directType)) {
                instance = createCls(id, Collections.EMPTY_LIST, directTypes, initializeDefaults);
            } else if (isSlotMetaCls(directType)) {
                instance = createSlot(id, directTypes, Collections.EMPTY_LIST, initializeDefaults);
            } else if (isFacetMetaCls(directType)) {
                instance = createFacet(id, directTypes, initializeDefaults);
            } else {
                instance = createSimpleInstance(id, directTypes, initializeDefaults);
            }
        }
        return instance;
    }

    public synchronized Slot createSlot(String name) {
        return createSlot(name, _defaultSlotMetaCls);
    }

    public synchronized Slot createSlot(String name, Cls directType) {
        return createSlot(name, directType, true);
    }

    public synchronized Slot createSlot(String name, Cls directType, boolean loadDefaults) {
        return createSlot(name, directType, Collections.EMPTY_LIST, loadDefaults);
    }

    /**
     * @deprecated no longer needed
     */
    @Deprecated
    public String createUniqueFrameName(String name) {
        return null;
    }

    /*
     * delete a class
     * 
     * If the class has instances then they become instances of its parent classes. If the class has subclasses then
     * they become subclasses of its parent classes.
     */
    public synchronized void deleteCls(Cls cls) {
        if (true) {
            if (!getInstances(cls).isEmpty()) {
                throw new RuntimeException("Delete of class with instances.");
            }
            getHeadFrameStore().deleteCls(cls);
            markAsDeleted(cls);
        } else {
            Collection parents = getDirectSuperclasses(cls);
            try {
                beginTransaction("delete class " + cls.getBrowserText(), cls.getName());
                moveInstancesToParents(cls, parents);
                moveSubclassesToParents(cls, parents);
                getHeadFrameStore().deleteCls(cls);
                commitTransaction();				
			} catch (Exception e) {
				rollbackTransaction();
				Log.getLogger().warning("Error at deleting cls: " + cls);
			}
        }
    }

    private void moveSubclassesToParents(Cls cls, Collection parents) {
        Iterator i = new ArrayList(getDirectSubclasses(cls)).iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            moveSubclassToParents(subclass, cls, parents);
        }
    }

    /*
     * TODO Should remove facet overrides in all descendents for slots that will disappear
     */
    private void moveSubclassToParents(Cls subclass, Cls cls, Collection parents) {
        Iterator i = parents.iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            addDirectSuperclass(subclass, parent);
            removeDirectSuperclass(subclass, cls);
        }
    }

    private void moveInstancesToParents(Cls cls, Collection parents) {
        Iterator<Instance> i = new ArrayList<Instance>(getDirectInstances(cls)).iterator();
        while (i.hasNext()) {
            Instance instance = i.next();
            moveInstanceToParents(instance, cls, parents);
        }
    }

    /*
     * TODO Should remove own slot values for slots that will disappear
     */
    private void moveInstanceToParents(Instance instance, Cls cls, Collection parents) {
        Iterator i = parents.iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            addDirectType(instance, parent);
            removeDirectType(instance, cls);
        }
    }

    public synchronized void deleteFacet(Facet facet) {
        getHeadFrameStore().deleteFacet(facet);
        markAsDeleted(facet);
    }

    public synchronized void deleteInstance(Instance instance) {
        deleteFrame(instance);
    }

    public synchronized void deleteSimpleInstance(SimpleInstance simpleInstance) {
        getHeadFrameStore().deleteSimpleInstance(simpleInstance);
        markAsDeleted(simpleInstance);

    }

    public synchronized void deleteSlot(Slot slot) {
        getHeadFrameStore().deleteSlot(slot);
        markAsDeleted(slot);
    }

    private static void markAsDeleted(Frame frame) {
        frame.markDeleted(true);
    }

    public synchronized String getBuildString() {
        return _buildString;
    }

    public synchronized Object getClientInformation(Object key) {
        return _clientInformation.get(key);
    }

    public synchronized Cls getCls(String name) {
        return (Cls) getFrameOfType(name, Cls.class);
    }

    private Frame getFrameOfType(String name, Class type) {
        Frame frame = getFrame(name);
        if (frame != null && !type.isInstance(frame)) {
            getFrame(name);
            Log.getLogger().warning("Wrong type: " + frame);
            frame = null;
        }
        return frame;
    }

    public synchronized int getClsCount() {
        return getHeadFrameStore().getClsCount();
    }

    public synchronized int getSimpleInstanceCount() {
        return getHeadFrameStore().getSimpleInstanceCount();
    }

    public synchronized Collection<Cls> getClses() {
        return getHeadFrameStore().getClses();
    }

    public synchronized Collection getClsNameMatches(String name, int maxMatches) {
        Collection frames = getFrameNameMatches(name, maxMatches);
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof Cls)) {
                i.remove();
            }
        }
        return frames;
    }

    public synchronized Cls getDefaultClsMetaCls() {
        return _defaultClsMetaCls;
    }

    public synchronized Cls getDefaultFacetMetaCls() {
        return _defaultFacetMetaCls;
    }

    public synchronized Cls getDefaultSlotMetaCls() {
        return _defaultSlotMetaCls;
    }

    public synchronized Facet getFacet(String name) {
        return (Facet) getFrameOfType(name, Facet.class);
    }

    public synchronized int getFacetCount() {
        return getHeadFrameStore().getFacetCount();
    }

    public synchronized Collection getFacets() {
        return getHeadFrameStore().getFacets();
    }

    public synchronized int getFrameCount() {
        return getHeadFrameStore().getFrameCount();
    }

    public synchronized String getFrameCreationTimestamp(Frame frame) {
        return (String) getOwnSlotValue(frame, _systemFrames.getCreationTimestampSlot());
    }

    public synchronized String getFrameCreator(Frame frame) {
        return (String) getOwnSlotValue(frame, _systemFrames.getCreatorSlot());
    }

    public synchronized String getFrameLastModificationTimestamp(Frame frame) {
        return (String) getOwnSlotValue(frame, _systemFrames.getModificationTimestampSlot());
    }

    public synchronized String getFrameLastModifier(Frame frame) {
        return (String) getOwnSlotValue(frame, _systemFrames.getModifierSlot());
    }

    public synchronized Collection<Frame> getFrameNameMatches(String name, int maxMatches) {
        return getFramesWithMatchingDirectOwnSlotValue(_systemFrames.getNameSlot(), name, maxMatches);
    }

    public synchronized Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Collection frames;
        if (facet == null) {
            if (isTemplate) {
                frames = getHeadFrameStore().getClsesWithDirectTemplateSlotValue(slot, value);
            } else {
                frames = getHeadFrameStore().getFramesWithDirectOwnSlotValue(slot, value);
            }
        } else {
            frames = getHeadFrameStore().getClsesWithDirectTemplateFacetValue(slot, facet, value);
        }
        return frames;
    }

    public synchronized Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches) {
        return getHeadFrameStore().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
    }

    public synchronized String getFrameNamePrefix() {
        return _frameNamePrefix;
    }

    public synchronized Collection getFrames() {
        return getHeadFrameStore().getFrames();
    }

    public synchronized Instance getInstance(String name) {
        return (Instance) getFrame(name);
    }

    public synchronized SimpleInstance getSimpleInstance(String name) {
        return (SimpleInstance) getFrame(name);
    }

    public synchronized Collection<Instance> getInstances() {
        return getFrames();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public synchronized Collection<Instance> getInstances(Cls cls) {
        return getHeadFrameStore().getInstances(cls);
    }

    public synchronized String getInvalidOwnSlotValuesText(Frame frame, Slot slot, Collection values) {
        String result = null;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result == null && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.getInvalidValuesText(frame, slot, values);
        }
        return result;
    }

    public synchronized String getInvalidOwnSlotValueText(Frame frame, Slot slot, Object value) {
        String result = null;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result == null && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.getInvalidValueText(frame, slot, value);
        }
        return result;
    }

    public synchronized KnowledgeBaseFactory getKnowledgeBaseFactory() {
        return _knowledgeBaseFactory;
    }

    public synchronized Collection getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value,
            int maxMatches) {
        Collection frames;
        if (facet == null) {
            if (isTemplate) {
                frames = getHeadFrameStore().getClsesWithMatchingDirectTemplateSlotValue(slot, value, maxMatches);
            } else {
                frames = getHeadFrameStore().getFramesWithMatchingDirectOwnSlotValue(slot, value, maxMatches);
            }
        } else {
            frames = getHeadFrameStore().getClsesWithMatchingDirectTemplateFacetValue(slot, facet, value, maxMatches);
        }
        return frames;
    }

    public String getName() {
        return _name;
    }

    /**
     * @deprecated No longer implemented
     */
    @Deprecated
    public synchronized int getNextFrameNumber() {
        return 0;
    }

    public synchronized Project getProject() {
        return _project;
    }

    public synchronized Collection getReachableSimpleInstances(Collection rootInstances) {
        Collection reachableInstances = new HashSet();
        Iterator i = rootInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            addReachableSimpleInstances(instance, reachableInstances);
        }
        return reachableInstances;
    }

    private void addReachableSimpleInstances(Instance instance, Collection reachableInstances) {
        if (!reachableInstances.contains(instance)) {
            reachableInstances.add(instance);
            Iterator i = getOwnSlots(instance).iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                addInstances(instance, slot, reachableInstances);
            }
        }
    }

    private synchronized void addInstances(Instance instance, Slot slot, Collection reachableInstances) {
        if (instance.getOwnSlotValueType(slot) == ValueType.INSTANCE) {
            Iterator i = instance.getOwnSlotValues(slot).iterator();
            while (i.hasNext()) {
                Instance value = (Instance) i.next();
                if (value instanceof SimpleInstance) {
                    addReachableSimpleInstances(value, reachableInstances);
                }
            }
        }
    }

    public synchronized Collection getReferences(Object object, int maxRefs) {
        return getHeadFrameStore().getReferences(object);
    }

    public synchronized Collection<Reference> getMatchingReferences(String s, int maxRefs) {
        return getHeadFrameStore().getMatchingReferences(s, maxRefs);
    }

    public synchronized Collection<Cls> getClsesWithMatchingBrowserText(String s, Collection superclasses, int maxMatches) {
        return getHeadFrameStore().getClsesWithMatchingBrowserText(s, superclasses, maxMatches);
    }

    public synchronized Cls getRootCls() {
        return _systemFrames.getRootCls();
    }

    public synchronized Collection getRootClses() {
        return CollectionUtilities.createCollection(getRootCls());
    }

    public synchronized Cls getRootClsMetaCls() {
        return _systemFrames.getRootClsMetaCls();
    }

    public synchronized Cls getRootFacetMetaCls() {
        return _systemFrames.getRootFacetMetaCls();
    }

    public synchronized Cls getRootSlotMetaCls() {
        return _systemFrames.getRootSlotMetaCls();
    }

    public synchronized Collection getRootSlots() {
        Collection slots = new ArrayList(getSlots());
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (getDirectSuperslotCount(slot) > 0) {
                i.remove();
            }
        }
        return slots;
    }

    public synchronized Slot getSlot(String name) {
        return (Slot) getFrameOfType(name, Slot.class);
    }

    public synchronized int getSlotCount() {
        return getHeadFrameStore().getSlotCount();
    }

    public synchronized Collection<Slot> getSlots() {
        return getHeadFrameStore().getSlots();
    }

    /**
     * @deprecated this functionality is no longer available
     */
    @Deprecated
    public synchronized String getSlotValueLastModificationTimestamp(Frame frame, Slot slot, boolean isTemplate) {
        return null;
    }

    /**
     * @deprecated this functionality is no longer available
     */
    @Deprecated
    public synchronized String getSlotValueLastModifier(Frame frame, Slot slot, boolean isTemplate) {
        return null;
    }

    public synchronized Collection getSubclasses(Cls cls) {
        return getHeadFrameStore().getSubclasses(cls);
    }

    public synchronized Collection getUnreachableSimpleInstances(Collection rootInstances) {
        Collection instances = new HashSet(getFrames());
        instances.removeAll(getClses());
        instances.removeAll(getSlots());
        instances.removeAll(getFacets());
        instances.removeAll(getReachableSimpleInstances(rootInstances));
        return instances;
    }
    
    private String _userName;
    public final static String SERVER_PROCESS_USER = "**Server Process UserId**";
    public synchronized String getUserName() {
        if (_userName != null) {
            return _userName;
        }
        Project p = getProject();
        if (p != null && p.isMultiUserClient()) {
            FrameStoreManager fsm = getFrameStoreManager();
            RemoteClientFrameStore fs = fsm.getFrameStoreFromClass(RemoteClientFrameStore.class);
            return _userName = fs.getSession().getUserName();
        }
        else if (p != null && p.isMultiUserServer()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            if (session != null) {
                return session.getUserName();
            }
            else return SERVER_PROCESS_USER;
        }
        else { // standalone case
            return _userName = ApplicationProperties.getUserName();
        }
    }


    public synchronized String getVersionString() {
        return _versionString;
    }

    public synchronized boolean hasChanged() {
        return _frameStoreManager.hasChanged();
    }

    /**
     * TODO implement or deprecate isAutoUpdatingFacetValues
     */
    public synchronized boolean isAutoUpdatingFacetValues() {
        return false;
    }

    public synchronized boolean isClsMetaCls(Cls cls) {
        // Log.enter(this, "isClsMetaCls", cls);
        Cls rootMetaCls = getRootClsMetaCls();
        return equals(cls, rootMetaCls) || hasSuperclass(cls, rootMetaCls);
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public synchronized boolean isDefaultClsMetaCls(Cls cls) {
        return equals(cls, _defaultClsMetaCls);
    }

    public synchronized boolean isDefaultFacetMetaCls(Cls cls) {
        return equals(cls, _defaultFacetMetaCls);
    }

    public synchronized boolean isDefaultSlotMetaCls(Cls cls) {
        return equals(cls, _defaultSlotMetaCls);
    }

    public synchronized boolean isFacetMetaCls(Cls cls) {
        return hasSuperclass(cls, getRootFacetMetaCls());
    }

    /**
     * TODO implement or deprecate isLoading
     */
    public synchronized boolean isLoading() {
        return false;
    }

    public synchronized boolean isSlotMetaCls(Cls cls) {
        return hasSuperclass(cls, getRootSlotMetaCls());
    }

    public synchronized boolean isValidOwnSlotValue(Frame frame, Slot slot, Object value) {
        boolean result = true;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.isValidValue(frame, slot, value);
        }
        return result;
    }

    public synchronized void removeJavaLoadPackage(String packageName) {
        _frameFactory.removeJavaPackage(packageName);
    }

    /**
     * @deprecated Use setModificationRecordUpdatingEnabled
     */
    @Deprecated
    public synchronized void setAutoUpdateFacetValues(boolean autoUpdate) {
        setModificationRecordUpdatingEnabled(autoUpdate);
    }

    public synchronized boolean setModificationRecordUpdatingEnabled(boolean enabled) {
        return _frameStoreManager.setModificationRecordUpdatingEnabled(enabled);
    }

    public synchronized void setBuildString(String buildString) {
        _buildString = buildString;
    }

    public synchronized void setChanged(boolean changed) {
        _frameStoreManager.setChanged(changed);
    }

    public synchronized Object setClientInformation(Object key, Object value) {
        Object oldValue = _clientInformation.get(key);
        _clientInformation.put(key, value);
        return oldValue;
    }

    public synchronized void setDefaultClsMetaCls(Cls cls) {
        if (cls == null || isClsMetaCls(cls)) {
            _defaultClsMetaCls = cls;
        } else {
            Log.getLogger().warning("Not a class meta class: " + cls);
        }
    }

    public synchronized void setDefaultFacetMetaCls(Cls cls) {
        if (cls == null || isFacetMetaCls(cls)) {
            _defaultFacetMetaCls = cls;
        } else {
            Log.getLogger().warning("Not a facet meta class: " + cls);
        }
    }

    public synchronized void setDefaultSlotMetaCls(Cls cls) {
        if (cls == null || isSlotMetaCls(cls)) {
            _defaultSlotMetaCls = cls;
        } else {
            Log.getLogger().warning("Not a slot meta class: " + cls);
        }
    }

    public synchronized void setFrameNamePrefix(String prefix) {
        _frameNamePrefix = prefix;
    }

    /**
     * TODO implement or deprecate setLoading
     */
    public synchronized void setLoading(boolean loading) {
        setEventsEnabled(!loading);
    }

    public synchronized void setName(String name) {
        _name = name;
    }

    /**
     * @deprecated This functionality is no longer available
     */
    @Deprecated
    public synchronized void setNextFrameNumber(int number) {
    }

    private boolean clientServerAdjusted = false;
    
    public synchronized void setProject(Project project) {
        _project = project;
        if (!clientServerAdjusted) {
            if (project.isMultiUserClient()) {
                adjustForClient();
            }
            if (project.isMultiUserServer()) {
                adjustForServer();
            }
            clientServerAdjusted = true;
        }
    }

    protected void adjustForClient() {
        DeleteSimplificationFrameStore dsfs 
                = _frameStoreManager.getFrameStoreFromClass(DeleteSimplificationFrameStore.class);
        _frameStoreManager.setEnabled(dsfs, false);
        EventGeneratorFrameStore egfs
                = _frameStoreManager.getFrameStoreFromClass(EventGeneratorFrameStore.class);
        if (egfs != null) {
            _frameStoreManager.setEnabled(egfs, false);
        }
    }
    
    protected void adjustForServer() {
        
    }

    /**
     * @deprecated Use #setFacetCheckingEnabled(boolean)
     */
    @Deprecated
    public synchronized void setValueChecking(boolean checking) {
        setFacetCheckingEnabled(checking);
    }

    public synchronized boolean setFacetCheckingEnabled(boolean enabled) {
        return _frameStoreManager.setFacetCheckingEnabled(enabled);
    }

    public synchronized void setVersionString(String versionString) {
        _versionString = versionString;
    }

    public synchronized void dispose() {
    	if (_frameStoreManager != null) {
    		_frameStoreManager.close();
    	}
        _frameStoreManager = null;
        _project = null;
    }

    public synchronized boolean isClosed() {
        return _frameStoreManager != null;
    }

    public synchronized Collection getReachableSimpleInstances(Frame frame) {
        return getReachableSimpleInstances(CollectionUtilities.createCollection(frame));
    }

    /** @deprecated use #addDirectOwnSlotValues */
    @Deprecated
    public synchronized void addOwnSlotValue(Frame frame, Slot slot, Object value) {
        List values = new ArrayList(getDirectOwnSlotValues(frame, slot));
        values.add(value);
        setDirectOwnSlotValues(frame, slot, values);
    }

    public synchronized Collection getDocumentation(Frame frame) {
        return getOwnSlotValues(frame, _systemFrames.getDocumentationSlot());
    }

    public synchronized boolean getOwnSlotAllowsMultipleValues(Frame frame, Slot slot) {
        Cls type = ((Instance) frame).getDirectType();
        return getTemplateSlotAllowsMultipleValues(type, slot);
    }

    /** @deprecated use getOwnSlotValues */
    @Deprecated
    public synchronized Collection getOwnSlotAndSubslotValues(Frame frame, Slot slot) {
        return getOwnSlotValues(frame, slot);
    }

    public synchronized Collection getOwnSlotDefaultValues(Frame frame, Slot slot) {
        Collection values = new LinkedHashSet();
        Iterator i = ((Instance) frame).getDirectTypes().iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            values.addAll(getTemplateSlotDefaultValues(type, slot));
        }
        return values;
    }

    /** @deprecated renamed to #getOwnFacets */
    @Deprecated
    public synchronized Collection getOwnSlotFacets(Frame frame, Slot slot) {
        return getOwnFacets(frame, slot);
    }

    /** @deprecated renamed to #getOwnFacetValues */
    @Deprecated
    public synchronized Collection getOwnSlotFacetValues(Frame frame, Slot slot, Facet facet) {
        return getOwnFacetValues(frame, slot, facet);
    }

    public synchronized Collection getOwnFacets(Frame frame, Slot slot) {
        return getHeadFrameStore().getOwnFacets(frame, slot);
    }

    public synchronized Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        return getHeadFrameStore().getOwnFacetValues(frame, slot, facet);
    }

    public synchronized Object getOwnSlotValue(Frame frame, Slot slot) {
        Collection c = getOwnSlotValues(frame, slot);
        return c.isEmpty() ? null : c.iterator().next();
    }

    public synchronized int getOwnSlotValueCount(Frame frame, Slot slot) {
        return getOwnSlotValues(frame, slot).size();
    }

    public synchronized ValueType getOwnSlotValueType(Frame frame, Slot slot) {
        Collection values = getOwnFacetValues(frame, slot, _systemFrames.getValueTypeFacet());
        return ValueTypeConstraint.getType(values);
    }

    public synchronized boolean hasOwnSlot(Frame frame, Slot slot) {
        return getOwnSlots(frame).contains(slot);
    }

    public synchronized void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        getHeadFrameStore().moveDirectOwnSlotValue(frame, slot, from, to);
    }

    /** @deprecated use #setDirectOwnSlotValues */
    @Deprecated
    public synchronized void removeOwnSlotValue(Frame frame, Slot slot, Object value) {
        List values = new ArrayList(getDirectOwnSlotValues(frame, slot));
        values.remove(value);
        setDirectOwnSlotValues(frame, slot, values);
    }

    public synchronized void setDocumentation(Frame frame, String text) {
        setDirectOwnSlotValue(frame, _systemFrames.getDocumentationSlot(), text);
    }

    public synchronized void setDocumentation(Frame frame, Collection text) {
        setDirectOwnSlotValues(frame, _systemFrames.getDocumentationSlot(), text);
    }

    /** @deprecated renamed to #setDirectOwnSlotValue */
    @Deprecated
    public synchronized void setOwnSlotValue(Frame frame, Slot slot, Object value) {
        setDirectOwnSlotValue(frame, slot, value);
    }

    public synchronized void setDirectOwnSlotValue(Frame frame, Slot slot, Object value) {
        Collection values = CollectionUtilities.createCollection(value);
        setDirectOwnSlotValues(frame, slot, values);
    }

    /** @deprecated renamed to setDirectOwnSlotValues */
    @Deprecated
    public synchronized void setOwnSlotValues(Frame frame, Slot slot, Collection values) {
        setDirectOwnSlotValues(frame, slot, values);
    }

    /**
     * TODO implement notifyVisibilityChanged
     */
    public synchronized void notifyVisibilityChanged(Frame frame) {
    }

    public synchronized Slot getAssociatedSlot(Facet facet) {
        return (Slot) getOwnSlotValue(facet, _systemFrames.getAssociatedSlotSlot());
    }

    public synchronized void setAssociatedSlot(Facet facet, Slot slot) {
        setDirectOwnSlotValue(facet, _systemFrames.getAssociatedSlotSlot(), slot);
    }

    public synchronized void addDirectSuperclass(Cls cls, Cls superclass) {
        getHeadFrameStore().addDirectSuperclass(cls, superclass);
    }

    public synchronized void removeDirectSuperclass(Cls cls, Cls superclass) {
        getHeadFrameStore().removeDirectSuperclass(cls, superclass);
    }

    public synchronized void addDirectType(Instance instance, Cls directType) {
        getHeadFrameStore().addDirectType(instance, directType);
    }

    public synchronized void removeDirectType(Instance instance, Cls directType) {
        getHeadFrameStore().removeDirectType(instance, directType);
    }

    public synchronized void moveDirectType(Instance instance, Cls directType, int index) {
        getHeadFrameStore().moveDirectType(instance, directType, index);
    }

    public synchronized void addDirectTemplateSlot(Cls cls, Slot slot) {
        getHeadFrameStore().addDirectTemplateSlot(cls, slot);
    }

    public synchronized void removeDirectTemplateSlot(Cls cls, Slot slot) {
        getHeadFrameStore().removeDirectTemplateSlot(cls, slot);
    }

    /** @deprecated use #setDirectTemplateFacetValues */
    @Deprecated
    public synchronized void addTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        Collection values = new ArrayList(getDirectTemplateFacetValues(cls, slot, facet));
        values.add(value);
        setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    /** @deprecated use #setDirectTemplateSlotValues */
    @Deprecated
    public synchronized void addTemplateSlotValue(Cls cls, Slot slot, Object value) {
        Collection values = new ArrayList(getHeadFrameStore().getDirectTemplateSlotValues(cls, slot));
        values.add(value);
        setDirectTemplateSlotValues(cls, slot, values);
    }

    public synchronized Slot getNameSlot() {
        return _systemFrames.getNameSlot();
    }

    public synchronized int getDirectInstanceCount(Cls cls) {
        return getDirectInstances(cls).size();
    }

    public synchronized Collection<Instance> getDirectInstances(Cls cls) {
        return getHeadFrameStore().getDirectInstances(cls);
    }

    public synchronized int getDirectSubclassCount(Cls cls) {
        // return getDirectSubclasses(cls).size();
        return getHeadFrameStore().getDirectOwnSlotValuesCount(cls, _systemFrames.getDirectSubclassesSlot());
    }

    public synchronized Collection getDirectSubclasses(Cls cls) {
        return getHeadFrameStore().getDirectSubclasses(cls);
    }

    public synchronized int getDirectSuperclassCount(Cls cls) {
        return getDirectSuperclasses(cls).size();
    }

    public synchronized List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getHeadFrameStore().getDirectTemplateFacetValues(cls, slot, facet);
    }

    public synchronized Collection getDirectTemplateSlots(Cls cls) {
        return getHeadFrameStore().getDirectTemplateSlots(cls);
    }

    public synchronized List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        return getHeadFrameStore().getDirectTemplateSlotValues(cls, slot);
    }

    public synchronized int getInstanceCount(Cls cls) {
        return getInstances(cls).size();
    }

    public synchronized Collection getSuperclasses(Cls cls) {
        return getHeadFrameStore().getSuperclasses(cls);
    }

    public synchronized Collection getTemplateFacets(Cls cls, Slot slot) {
        return getHeadFrameStore().getTemplateFacets(cls, slot);
    }

    public synchronized Object getTemplateFacetValue(Cls cls, Slot slot, Facet facet) {
        Collection values = getTemplateFacetValues(cls, slot, facet);
        return CollectionUtilities.getFirstItem(values);
    }

    public synchronized Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        return getHeadFrameStore().getTemplateFacetValues(cls, slot, facet);
    }

    public synchronized Collection getTemplateSlotAllowedClses(Cls cls, Slot slot) {
        Collection values = getTemplateFacetValues(cls, slot, _systemFrames.getValueTypeFacet());
        return ValueTypeConstraint.getAllowedClses(values);
    }

    public synchronized Collection getTemplateSlotAllowedParents(Cls cls, Slot slot) {
        Collection values = getTemplateFacetValues(cls, slot, _systemFrames.getValueTypeFacet());
        return ValueTypeConstraint.getAllowedParents(values);
    }

    public synchronized Collection getTemplateSlotAllowedValues(Cls cls, Slot slot) {
        Collection values = getTemplateFacetValues(cls, slot, _systemFrames.getValueTypeFacet());
        return ValueTypeConstraint.getAllowedValues(values);
    }

    public synchronized boolean getTemplateSlotAllowsMultipleValues(Cls cls, Slot slot) {
        Integer value = getTemplateSlotMaximumCardinality2(cls, slot);
        return MaximumCardinalityConstraint.allowsMultipleValues(value);
    }

    public synchronized Collection getTemplateSlotDefaultValues(Cls cls, Slot slot) {
        return getTemplateFacetValues(cls, slot, _systemFrames.getDefaultValuesFacet());
    }

    public synchronized Collection getTemplateSlotDocumentation(Cls cls, Slot slot) {
        return getTemplateFacetValues(cls, slot, _systemFrames.getDocumentationFacet());
    }

    public synchronized int getTemplateSlotMaximumCardinality(Cls cls, Slot slot) {
        Integer i = getTemplateSlotMaximumCardinality2(cls, slot);
        return MaximumCardinalityConstraint.getValue(i);
    }

    public synchronized Integer getTemplateSlotMaximumCardinality2(Cls cls, Slot slot) {
        return (Integer) getTemplateFacetValue(cls, slot, _systemFrames.getMaximumCardinalityFacet());
    }

    public synchronized Integer getTemplateSlotMinimumCardinality2(Cls cls, Slot slot) {
        return (Integer) getTemplateFacetValue(cls, slot, _systemFrames.getMinimumCardinalityFacet());
    }

    public synchronized Number getTemplateSlotMaximumValue(Cls cls, Slot slot) {
        return (Number) getTemplateFacetValue(cls, slot, _systemFrames.getMaximumValueFacet());
    }

    public synchronized int getTemplateSlotMinimumCardinality(Cls cls, Slot slot) {
        Integer i = getTemplateSlotMinimumCardinality2(cls, slot);
        return MinimumCardinalityConstraint.getValue(i);
    }

    public synchronized Number getTemplateSlotMinimumValue(Cls cls, Slot slot) {
        return (Number) getTemplateFacetValue(cls, slot, _systemFrames.getMinimumValueFacet());
    }

    public synchronized Object getTemplateSlotValue(Cls cls, Slot slot) {
        Collection values = getTemplateSlotValues(cls, slot);
        return CollectionUtilities.getFirstItem(values);
    }

    public synchronized Collection getTemplateSlotValues(Cls cls, Slot slot) {
        return getHeadFrameStore().getTemplateSlotValues(cls, slot);
    }

    public synchronized ValueType getTemplateSlotValueType(Cls cls, Slot slot) {
        Collection values = getTemplateFacetValues(cls, slot, _systemFrames.getValueTypeFacet());
        return ValueTypeConstraint.getType(values);
    }

    public synchronized boolean hasDirectlyOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        Collection values = getDirectTemplateFacetValues(cls, slot, facet);
        return !values.isEmpty();
    }

    public synchronized boolean hasDirectlyOverriddenTemplateSlot(Cls cls, Slot slot) {
        return getDirectlyOverriddenTemplateSlots(cls).contains(slot);
    }

    public synchronized Collection getDirectlyOverriddenTemplateSlots(Cls cls) {
        return getHeadFrameStore().getDirectlyOverriddenTemplateSlots(cls);
    }

    public synchronized Collection getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        return getHeadFrameStore().getDirectlyOverriddenTemplateFacets(cls, slot);
    }

    public synchronized boolean hasDirectSuperslot(Slot slot, Slot superslot) {
        return getDirectSuperslots(slot).contains(superslot);
    }

    public synchronized boolean hasSuperslot(Slot slot, Slot superslot) {
        return getSuperslots(slot).contains(superslot);
    }

    public synchronized boolean hasDirectSuperclass(Cls cls, Cls superclass) {
        return getDirectSuperclasses(cls).contains(superclass);
    }

    public synchronized boolean hasDirectTemplateSlot(Cls cls, Slot slot) {
        return getDirectTemplateSlots(cls).contains(slot);
    }

    public synchronized boolean hasInheritedTemplateSlot(Cls cls, Slot slot) {
        return getInheritedTemplateSlots(cls).contains(slot);
    }

    public synchronized Collection getInheritedTemplateSlots(Cls cls) {
        Collection slots = new HashSet(getTemplateSlots(cls));
        slots.removeAll(getDirectTemplateSlots(cls));
        return slots;
    }

    public synchronized boolean hasOverriddenTemplateSlot(Cls cls, Slot slot) {
        return getOverriddenTemplateSlots(cls).contains(slot);
    }

    public synchronized Collection getOverriddenTemplateSlots(Cls cls) {
        return getHeadFrameStore().getOverriddenTemplateSlots(cls);
    }

    public synchronized boolean hasOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        return getOverriddenTemplateFacets(cls, slot).contains(facet);
    }

    public synchronized Collection getOverriddenTemplateFacets(Cls cls, Slot slot) {
        return getHeadFrameStore().getOverriddenTemplateFacets(cls, slot);
    }

    /** @deprecated renamed to #removeDirectTemplateFacetOverrides */
    @Deprecated
    public synchronized void removeTemplateFacetOverrides(Cls cls, Slot slot) {
        removeDirectTemplateFacetOverrides(cls, slot);
    }

    public synchronized void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        getHeadFrameStore().removeDirectTemplateFacetOverrides(cls, slot);
    }

    public synchronized boolean hasSuperclass(Cls cls, Cls superclass) {
        return getSuperclasses(cls).contains(superclass);
    }

    public synchronized boolean hasTemplateSlot(Cls cls, Slot slot) {
        return getTemplateSlots(cls).contains(slot);
    }

    public synchronized boolean isAbstract(Cls cls) {
        String s = (String) getOwnSlotValue(cls, _systemFrames.getRoleSlot());
        return RoleConstraint.isAbstract(s);
    }

    public synchronized boolean isMetaCls(Cls cls) {
        return hasSuperclass(cls, _systemFrames.getRootMetaCls());
    }

    public synchronized void moveDirectSubclass(Cls cls, Cls subclass, Cls afterclass) {
        List subclasses = new ArrayList(getDirectSubclasses(cls));
        int currentIndex = subclasses.indexOf(subclass);
        int index = (afterclass == null) ? 0 : subclasses.indexOf(afterclass);
        if (currentIndex > index) {
            ++index;
        }
        moveDirectSubclass(cls, subclass, index);
    }

    public synchronized void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        getHeadFrameStore().moveDirectSubclass(cls, subclass, index);
    }

    public synchronized void moveDirectSubslot(Slot slot, Slot subslot, Slot afterslot) {
        List subclasses = new ArrayList(getDirectSubslots(slot));
        int currentIndex = subclasses.indexOf(subslot);
        int index = (afterslot == null) ? 0 : subclasses.indexOf(afterslot);
        if (currentIndex > index) {
            ++index;
        }
        moveDirectSubslot(slot, subslot, index);
    }

    public synchronized void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        getHeadFrameStore().moveDirectSubslot(slot, subslot, index);
    }

    public synchronized void moveDirectTemplateSlot(Cls cls, Slot slot, int toIndex) {
        getHeadFrameStore().moveDirectTemplateSlot(cls, slot, toIndex);
    }

    public synchronized void setAbstract(Cls cls, boolean isAbstract) {
        String value = isAbstract ? RoleConstraint.ABSTRACT : RoleConstraint.CONCRETE;
        setDirectOwnSlotValue(cls, _systemFrames.getRoleSlot(), value);
    }

    /*
     * One of the few methods in this class that actually does something. It seems misplaced.
     */
    public synchronized void setDirectTypeOfSubclasses(Cls cls, Cls type) {
        Iterator i = getSubclasses(cls).iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            setDirectType(subclass, type);
        }
    }

    /** @deprecated renamed to #setDirectTemplateFacetValue */
    @Deprecated
    public synchronized void setTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        setDirectTemplateFacetValue(cls, slot, facet, value);
    }

    public synchronized void setDirectTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        Collection values = CollectionUtilities.createCollection(value);
        setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    /** @deprecated renamed to #setDirectTemplateFacetValues */
    @Deprecated
    public synchronized void setTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    public synchronized void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        if (facet.equals(_systemFrames.getValueTypeFacet())) {
            setTemplateSlotValueTypeValues(cls, slot, values);
        } else {
            getHeadFrameStore().setDirectTemplateFacetValues(cls, slot, facet, values);
        }
    }

    private void setTemplateSlotValueTypeValues(Cls cls, Slot slot, ValueType type, Collection otherValues) {
        Collection values = ValueTypeConstraint.getValues(type, otherValues);
        setTemplateSlotValueTypeValues(cls, slot, values);
    }

    public synchronized void setTemplateSlotAllowedClses(Cls cls, Slot slot, Collection values) {
        if (!values.isEmpty()) {
            values = ValueTypeConstraint.getValues(ValueType.INSTANCE, values);
        }
        setTemplateSlotValueTypeValues(cls, slot, values);
    }

    public synchronized void setTemplateSlotAllowedParents(Cls cls, Slot slot, Collection values) {
        if (!values.isEmpty()) {
            values = ValueTypeConstraint.getValues(ValueType.CLS, values);
        }
        setTemplateSlotValueTypeValues(cls, slot, values);
    }

    public synchronized void setTemplateSlotAllowedValues(Cls cls, Slot slot, Collection values) {
        if (!values.isEmpty()) {
            values = ValueTypeConstraint.getValues(ValueType.SYMBOL, values);
        }
        setTemplateSlotValueTypeValues(cls, slot, values);
    }

    public synchronized void setTemplateSlotAllowsMultipleValues(Cls cls, Slot slot, boolean allowsMultiple) {
        Integer value = MaximumCardinalityConstraint.getValue(allowsMultiple);
        setDirectTemplateFacetValue(cls, slot, _systemFrames.getMaximumCardinalityFacet(), value);
    }

    public synchronized void setTemplateSlotDefaultValues(Cls cls, Slot slot, Collection values) {
        setDirectTemplateFacetValues(cls, slot, _systemFrames.getDefaultValuesFacet(), values);
    }

    public synchronized void setTemplateSlotDocumentation(Cls cls, Slot slot, String doc) {
        setDirectTemplateFacetValue(cls, slot, _systemFrames.getDocumentationFacet(), doc);
    }

    public synchronized void setTemplateSlotDocumentation(Cls cls, Slot slot, Collection docs) {
        setDirectTemplateFacetValues(cls, slot, _systemFrames.getDocumentationFacet(), docs);
    }

    public synchronized void setTemplateSlotMaximumCardinality(Cls cls, Slot slot, int value) {
        Integer facetValue = MaximumCardinalityConstraint.getValue(value);
        setDirectTemplateFacetValue(cls, slot, _systemFrames.getMaximumCardinalityFacet(), facetValue);
    }

    public synchronized void setTemplateSlotMaximumValue(Cls cls, Slot slot, Number value) {
        setDirectTemplateFacetValue(cls, slot, _systemFrames.getMaximumValueFacet(), value);
    }

    public synchronized void setTemplateSlotMinimumCardinality(Cls cls, Slot slot, int value) {
        Integer facetValue = MinimumCardinalityConstraint.getValue(value);
        setDirectTemplateFacetValue(cls, slot, _systemFrames.getMinimumCardinalityFacet(), facetValue);
    }

    public synchronized void setTemplateSlotMinimumValue(Cls cls, Slot slot, Number value) {
        setDirectTemplateFacetValue(cls, slot, _systemFrames.getMinimumValueFacet(), value);
    }

    public synchronized void setTemplateSlotValue(Cls cls, Slot slot, Object value) {
        Collection values = CollectionUtilities.createCollection(value);
        setDirectTemplateSlotValues(cls, slot, values);
    }

    /** @deprecated renamed to #setDirectTemplateSlotValues */
    @Deprecated
    public synchronized void setTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        setDirectTemplateSlotValues(cls, slot, values);
    }

    public synchronized void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        getHeadFrameStore().setDirectTemplateSlotValues(cls, slot, values);
    }

    public synchronized void setTemplateSlotValueType(Cls cls, Slot slot, ValueType type) {
        if (!getTemplateSlotValueType(cls, slot).equals(type)) {
            setTemplateSlotValueTypeValues(cls, slot, type, Collections.EMPTY_LIST);
        }
    }

    /**
     * TODO: Implement addInstance
     */
    public synchronized void addInstance(Instance instance, String name, Cls type, boolean isNew) {
    }

    public synchronized String getBrowserText(Instance instance) {
        String value;
        if (instance.isDeleted()) {
            value = "<<deleted>>";
        } else {
            Cls directType = instance.getDirectType();
            if (_project == null) {
                value = getName(instance);
            } else if (directType == null) {
                value = getMissingTypeString(instance);
            } else {
                BrowserSlotPattern slotPattern = _project.getBrowserSlotPattern(directType);
                if (slotPattern == null) {
                    value = getDisplaySlotNotSetString(instance);
                } else {
                    value = slotPattern.getBrowserText(instance);
                    if (value == null) {
                        value = getDisplaySlotPatternValueNotSetString(instance, slotPattern);
                    }
                }
            }
        }
        return value;
    }

    //ESCA-JAVA0130 
    protected String getMissingTypeString(Instance instance) {
        return instance.getName();
    }

    //ESCA-JAVA0130 
    protected String getDisplaySlotNotSetString(Instance instance) {
        return instance.getName();
    }

    //ESCA-JAVA0130 
    protected String getDisplaySlotPatternValueNotSetString(Instance instance, BrowserSlotPattern slotPattern) {
        return instance.getName();
    }

    // Hack
    protected String toString(BrowserSlotPattern pattern) {
        return getBrowserText(pattern.getFirstSlot());
    }

    protected String toString(Object o) {
        return (o instanceof Frame) ? toStringForFrame((Frame) o) : toStringForObject(o);
    }

    //ESCA-JAVA0130 
    protected String toStringForFrame(Frame frame) {
        return frame.getBrowserText();
    }

    //ESCA-JAVA0130 
    protected String toStringForObject(Object o) {
        return o.toString();
    }

    public synchronized Collection getDirectTypes(Instance instance) {
        return getHeadFrameStore().getDirectTypes(instance);
    }

    public synchronized boolean hasDirectType(Instance instance, Cls cls) {
        return getDirectTypes(instance).contains(cls);
    }

    public synchronized boolean hasType(Instance instance, Cls cls) {
        return getTypes(instance).contains(cls);
    }

    public synchronized Collection getTypes(Instance instance) {
        return getHeadFrameStore().getTypes(instance);
    }

    /**
     * TODO setDirectType should return void
     */
    public synchronized Instance setDirectType(Instance instance, Cls type) {
        Collection types = CollectionUtilities.createCollection(type);
        return setDirectTypes(instance, types);
    }

    public synchronized Instance setDirectTypes(Instance instance, Collection newTypes) {
        Collection oldTypes = new ArrayList(getDirectTypes(instance));
        Collection typesToRemove = new ArrayList(oldTypes);
        typesToRemove.removeAll(newTypes);
        Collection typesToAdd = new ArrayList(newTypes);
        typesToAdd.removeAll(oldTypes);
        addDirectTypes(instance, typesToAdd);
        removeDirectTypes(instance, typesToRemove);
        return instance;
    }

    public synchronized void addDirectTypes(Instance instance, Collection types) {
        Iterator i = types.iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            getHeadFrameStore().addDirectType(instance, type);
        }
    }

    public synchronized void removeDirectTypes(Instance instance, Collection types) {
        Iterator i = types.iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            getHeadFrameStore().removeDirectType(instance, type);
        }
    }

    public synchronized void addDirectSuperslot(Slot slot, Slot superslot) {
        getHeadFrameStore().addDirectSuperslot(slot, superslot);
    }

    public synchronized Collection getAllowedClses(Slot slot) {
        Collection values = getStandardSlotValues(slot, _systemFrames.getValueTypeSlot());
        return ValueTypeConstraint.getAllowedClses(values);
    }

    public synchronized Collection getAllowedParents(Slot slot) {
        Collection values = getStandardSlotValues(slot, _systemFrames.getValueTypeSlot());
        return ValueTypeConstraint.getAllowedParents(values);
    }

    public synchronized Collection getAllowedValues(Slot slot) {
        Collection values = getStandardSlotValues(slot, _systemFrames.getValueTypeSlot());
        return ValueTypeConstraint.getAllowedValues(values);
    }

    private Object getStandardSlotValue(Slot slot, Slot associatedSlot) {
        Collection values = getStandardSlotValues(slot, associatedSlot);
        return CollectionUtilities.getFirstItem(values);
    }

    private Collection getStandardSlotValues(Slot slot, Slot associatedSlot) {
        Collection values = getDirectOwnSlotValues(slot, associatedSlot);
        if (values.isEmpty()) {
            values = getOwnSlotValues(slot, associatedSlot);
        }
        return values;
    }

    public synchronized boolean getAllowsMultipleValues(Slot slot) {
        Integer value = getMaximumCardinality2(slot);
        return MaximumCardinalityConstraint.allowsMultipleValues(value);
    }

    public synchronized Facet getAssociatedFacet(Slot slot) {
        Facet facet = null;
        Object o = getOwnSlotValue(slot, _systemFrames.getAssociatedFacetSlot());
        if (o instanceof Facet) {
            facet = (Facet) o;
        } else if (o != null) {
            Log.getLogger().warning("Invalid facet: " + o);
        }
        return facet;
    }

    public synchronized Collection getDefaultValues(Slot slot) {
        return getOwnSlotValues(slot, _systemFrames.getDefaultValuesSlot());
    }

    public synchronized int getDirectSubslotCount(Slot slot) {
        return getDirectSubslots(slot).size();
    }

    public synchronized Collection getDirectSubslots(Slot slot) {
        return getHeadFrameStore().getDirectSubslots(slot);
    }

    public synchronized Collection getDirectSuperslots(Slot slot) {
        return getHeadFrameStore().getDirectSuperslots(slot);
    }

    public synchronized int getDirectSuperslotCount(Slot slot) {
        return getDirectSuperslots(slot).size();
    }

    public synchronized Slot getInverseSlot(Slot slot) {
        return (Slot) getOwnSlotValue(slot, _systemFrames.getInverseSlotSlot());
    }

    public synchronized Integer getMaximumCardinality2(Slot slot) {
        return (Integer) getStandardSlotValue(slot, _systemFrames.getMaximumCardinalitySlot());
    }

    public synchronized int getMaximumCardinality(Slot slot) {
        Integer value = getMaximumCardinality2(slot);
        return MaximumCardinalityConstraint.getValue(value);
    }

    public synchronized Number getMaximumValue(Slot slot) {
        return (Number) getStandardSlotValue(slot, _systemFrames.getMaximumValueSlot());
    }

    public synchronized int getMinimumCardinality(Slot slot) {
        Integer value = (Integer) getOwnSlotValue(slot, _systemFrames.getMinimumCardinalitySlot());
        return MinimumCardinalityConstraint.getValue(value);
    }

    public synchronized Number getMinimumValue(Slot slot) {
        return (Number) getOwnSlotValue(slot, _systemFrames.getMinimumValueSlot());
    }

    public synchronized Collection getSubslots(Slot slot) {
        return getHeadFrameStore().getSubslots(slot);
    }

    public synchronized Collection getSuperslots(Slot slot) {
        return getHeadFrameStore().getSuperslots(slot);
    }

    public synchronized Collection getDirectDomain(Slot slot) {
        return getHeadFrameStore().getDirectDomain(slot);
    }

    public synchronized Collection getDomain(Slot slot) {
        return getHeadFrameStore().getDomain(slot);
    }

    public synchronized Collection getValues(Slot slot) {
        return getOwnSlotValues(slot, _systemFrames.getValuesSlot());
    }

    public synchronized ValueType getValueType(Slot slot) {
        Collection values = getStandardSlotValues(slot, _systemFrames.getValueTypeSlot());
        return ValueTypeConstraint.getType(values);
    }

    /**
     * TODO implement or deprecate hasSlotValueAtSomeFrame
     */
    public synchronized boolean hasSlotValueAtSomeFrame(Slot slot) {
        return false;
    }

    public synchronized void removeDirectSuperslot(Slot slot, Slot superslot) {
        getHeadFrameStore().removeDirectSuperslot(slot, superslot);
    }

    private void setValueTypeValues(Slot slot, ValueType newValueType, Collection otherValues) {
        ValueType oldValueType = getValueType(slot);
        Collection values = ValueTypeConstraint.getValues(newValueType, otherValues);
        getHeadFrameStore().setDirectOwnSlotValues(slot, _systemFrames.getValueTypeSlot(), values);
        if (!areComparableTypes(oldValueType, newValueType)) {
            setDefaultValues(slot, Collections.EMPTY_LIST);
            setValues(slot, Collections.EMPTY_LIST);
            clearValues(null, slot);
        }
    }

    private static boolean areComparableTypes(ValueType type1, ValueType type2) {
        return equals(type1, type2) || (isFrameType(type1) && isFrameType(type2))
                || (isStringType(type1) && isStringType(type2)) 
                || type1.equals(ValueType.ANY) || type2 == null  || type2.equals(ValueType.ANY);
    }

    private static boolean isFrameType(ValueType type) {
        return equals(type, ValueType.CLS) || equals(type, ValueType.INSTANCE);
    }

    private static boolean isStringType(ValueType type) {
        return equals(type, ValueType.STRING) || equals(type, ValueType.SYMBOL);
    }

    private void clearValues(Cls cls, Slot slot) {
        clearOwnSlotValues(cls, slot);
        clearTemplateSlotValues(cls, slot);
    }

    private void clearOwnSlotValues(Cls cls, Slot slot) {
        Iterator i = new ArrayList(getFramesWithAnyOwnSlotValue(slot)).iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (cls == null || hasType((Instance) frame, cls)) {
                setOwnSlotValues(frame, slot, Collections.EMPTY_SET);
            }
        }
    }

    private Collection<Frame> getFramesWithAnyOwnSlotValue(Slot slot) {
        return getHeadFrameStore().getFramesWithAnyDirectOwnSlotValue(slot);
    }

    private Collection<Cls> getClsesWithAnyTemplateSlotValue(Slot slot) {
        return getHeadFrameStore().getClsesWithAnyDirectTemplateSlotValue(slot);
    }

    private void clearTemplateSlotValues(Cls slotCls, Slot slot) {
        Iterator i = new ArrayList(getClsesWithAnyTemplateSlotValue(slot)).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (slotCls == null || hasSuperclass(cls, slotCls)) {
                setTemplateSlotValues(cls, slot, Collections.EMPTY_SET);
            }
        }
    }

    /*
     * private void ensureValidDefaults(Slot slot) { Collection defaults = getDefaultValues(slot); Collection
     * validDefaults = getValidValues(defaults, slot); if (defaults.size() != validDefaults.size()) {
     * setDefaultValues(slot, validDefaults); } }
     */

    /*
     * private void ensureValidTemplateSlotDefaults(Cls cls, Slot slot) { Collection defaults =
     * getTemplateSlotDefaultValues(cls, slot); Collection validDefaults = getValidValues(defaults, cls, slot); if
     * (defaults.size() != validDefaults.size()) { setTemplateSlotDefaultValues(cls, slot, validDefaults); } }
     */

    /*
     * private void ensureValidValues(Slot slot) { Collection values = getValues(slot); Collection validValues =
     * getValidValues(values, slot); if (values.size() != validValues.size()) { setValues(slot, validValues); } }
     */

    /*
     * private void ensureValidTemplateSlotValues(Cls cls, Slot slot) { Collection values = getTemplateSlotValues(cls,
     * slot); Collection validValues = getValidValues(values, cls, slot); if (values.size() != validValues.size()) {
     * setTemplateSlotValues(cls, slot, validValues); } }
     */

    /*
     * private Collection getValidValues(Collection possibleValues, Slot slot) { Collection validValues; ValueType type =
     * getValueType(slot); if (type.equals(ValueType.SYMBOL)) { validValues = getValidAllowedValueValues(possibleValues,
     * getAllowedValues(slot)); } else if (type.equals(ValueType.CLS)) { validValues =
     * getValidAllowedParentValues(possibleValues, getAllowedParents(slot)); } else if (type.equals(ValueType.INSTANCE)) {
     * validValues = getValidAllowedClsValues(possibleValues, getAllowedClses(slot)); } else { validValues =
     * possibleValues; } return validValues; }
     * 
     * private Collection getValidValues(Collection possibleValues, Cls cls, Slot slot) { Collection validValues;
     * ValueType type = getTemplateSlotValueType(cls, slot); if (type.equals(ValueType.SYMBOL)) { validValues =
     * getValidAllowedValueValues(possibleValues, getTemplateSlotAllowedValues( cls, slot)); } else if
     * (type.equals(ValueType.CLS)) { validValues = getValidAllowedParentValues(possibleValues,
     * getTemplateSlotAllowedParents(cls, slot)); } else if (type.equals(ValueType.INSTANCE)) { validValues =
     * getValidAllowedClsValues(possibleValues, getTemplateSlotAllowedClses(cls, slot)); } else { validValues =
     * possibleValues; } return validValues; }
     */

    private void setTemplateSlotValueTypeValues(Cls cls, Slot slot, Collection values) {
        ValueType oldValueType = getTemplateSlotValueType(cls, slot);
        ValueType newValueType = (values.isEmpty()) ? null : ValueTypeConstraint.getType(values);
        getHeadFrameStore().setDirectTemplateFacetValues(cls, slot, _systemFrames.getValueTypeFacet(), values);
        if (!areComparableTypes(oldValueType, newValueType)) {
            setTemplateSlotDefaultValues(cls, slot, Collections.EMPTY_LIST);
            setTemplateSlotValues(cls, slot, Collections.EMPTY_LIST);
            clearValues(cls, slot);
        }
    }

    public synchronized void setAllowedClses(Slot slot, Collection clses) {
        setValueTypeValues(slot, ValueType.INSTANCE, clses);
    }

    public synchronized void setAllowedParents(Slot slot, Collection parents) {
        setValueTypeValues(slot, ValueType.CLS, parents);
    }

    public synchronized void setAllowedValues(Slot slot, Collection values) {
        setValueTypeValues(slot, ValueType.SYMBOL, values);
    }

    /*
     * private Collection getValidAllowedValueValues(Collection possibleValues, Collection allowedValues) { Collection
     * validValues = new ArrayList(); Iterator i = possibleValues.iterator(); while (i.hasNext()) { Object value =
     * i.next(); if (allowedValues.contains(value)) { validValues.add(value); } } return validValues; }
     */

    /*
     * private Collection getValidAllowedParentValues(Collection possibleClses, Collection allowedParents) { Collection
     * validValues = new ArrayList(); Iterator i = possibleClses.iterator(); while (i.hasNext()) { Cls value = (Cls)
     * i.next(); if (hasAncestor(value, allowedParents)) { validValues.add(value); } } return validValues; }
     */

    /*
     * private boolean hasAncestor(Cls cls, Collection parents) { return parents.contains(cls) || new
     * ArrayList(parents).removeAll(getSuperclasses(cls)); }
     * 
     * private boolean hasType(Instance instance, Collection directTypes) { return new
     * ArrayList(directTypes).removeAll(getTypes(instance)); }
     * 
     * private Collection getValidAllowedClsValues(Collection possibleValues, Collection allowedClses) { Collection
     * validValues = new ArrayList(); Iterator i = possibleValues.iterator(); while (i.hasNext()) { Instance value =
     * (Instance) i.next(); if (hasType(value, allowedClses)) { validValues.add(value); } } return validValues; }
     */

    public synchronized void setAllowsMultipleValues(Slot slot, boolean allowsMultiple) {
        Number value = MaximumCardinalityConstraint.getValue(allowsMultiple);
        setDirectOwnSlotValue(slot, _systemFrames.getMaximumCardinalitySlot(), value);
    }

    public synchronized void setAssociatedFacet(Slot slot, Facet facet) {
        setDirectOwnSlotValue(slot, _systemFrames.getAssociatedFacetSlot(), facet);
    }

    public synchronized void setDefaultValues(Slot slot, Collection values) {
        setDirectOwnSlotValues(slot, _systemFrames.getDefaultValuesSlot(), values);
    }

    /**
     * TODO do we need this?
     */
    public synchronized void setDirectTypeOfSubslots(Slot slot, Cls type) {
        Iterator i = getSubslots(slot).iterator();
        while (i.hasNext()) {
            Slot subslot = (Slot) i.next();
            setDirectType(subslot, type);
        }
    }

    public synchronized void setInverseSlot(Slot slot, Slot inverseSlot) {
        setDirectOwnSlotValue(slot, _systemFrames.getInverseSlotSlot(), inverseSlot);
    }

    public synchronized void setMaximumCardinality(Slot slot, int max) {
        Integer value = MaximumCardinalityConstraint.getValue(max);
        setDirectOwnSlotValue(slot, _systemFrames.getMaximumCardinalitySlot(), value);
    }

    public synchronized void setMaximumValue(Slot slot, Number max) {
        setDirectOwnSlotValue(slot, _systemFrames.getMaximumValueSlot(), max);
    }

    public synchronized void setMinimumCardinality(Slot slot, int min) {
        Integer value = MinimumCardinalityConstraint.getValue(min);
        setDirectOwnSlotValue(slot, _systemFrames.getMinimumCardinalitySlot(), value);
    }

    public synchronized void setMinimumValue(Slot slot, Number min) {
        setDirectOwnSlotValue(slot, _systemFrames.getMinimumValueSlot(), min);
    }

    public synchronized void setValues(Slot slot, Collection values) {
        setDirectOwnSlotValues(slot, _systemFrames.getValuesSlot(), values);
    }

    public synchronized void setValueType(Slot slot, ValueType newType) {
        if (!getValueType(slot).equals(newType)) {
            setValueTypeValues(slot, newType, Collections.EMPTY_LIST);
        }
    }
    
    public synchronized void setUserName(String userName) {
		_userName = userName;
	}

    public synchronized void addFrameListener(Frame frame, FrameListener listener) {
        addListener(FrameListener.class, frame, listener);
    }

    public synchronized void addFrameListener(FrameListener listener) {
        addFrameListener(null, listener);
    }

    public synchronized void removeFrameListener(Frame frame, FrameListener listener) {
        removeListener(FrameListener.class, frame, listener);
    }

    public synchronized void removeFrameListener(FrameListener listener) {
        removeFrameListener(null, listener);
    }

    public synchronized void addSlotListener(Slot slot, SlotListener listener) {
        addListener(SlotListener.class, slot, listener);
    }

    public synchronized void addSlotListener(SlotListener listener) {
        addSlotListener(null, listener);
    }

    public synchronized void removeSlotListener(Slot slot, SlotListener listener) {
        removeListener(SlotListener.class, slot, listener);
    }

    public synchronized void removeSlotListener(SlotListener listener) {
        removeSlotListener(null, listener);
    }

    public synchronized void addInstanceListener(Instance instance, InstanceListener listener) {
        addListener(InstanceListener.class, instance, listener);
    }

    public synchronized void addInstanceListener(InstanceListener listener) {
        addInstanceListener(null, listener);
    }

    public synchronized void removeInstanceListener(Instance instance, InstanceListener listener) {
        removeListener(InstanceListener.class, instance, listener);
    }

    public synchronized void removeInstanceListener(InstanceListener listener) {
        removeInstanceListener(null, listener);
    }

    public synchronized void addClsListener(ClsListener listener) {
        addClsListener(null, listener);
    }

    public synchronized void addClsListener(Cls cls, ClsListener listener) {
        addListener(ClsListener.class, cls, listener);
    }

    public synchronized void removeClsListener(Cls cls, ClsListener listener) {
        removeListener(ClsListener.class, cls, listener);
    }

    public synchronized void removeClsListener(ClsListener listener) {
        removeClsListener(null, listener);
    }

    public synchronized void addKnowledgeBaseListener(KnowledgeBaseListener listener) {
        addListener(KnowledgeBaseListener.class, this, listener);
    }

    public synchronized void removeKnowledgeBaseListener(KnowledgeBaseListener listener) {
        removeListener(KnowledgeBaseListener.class, this, listener);
    }

    public synchronized void addFacetListener(Facet facet, FacetListener listener) {
        addListener(FacetListener.class, this, listener);
    }

    public synchronized void addFacetListener(FacetListener listener) {
        addFacetListener(null, listener);
    }

    public synchronized void removeFacetListener(Facet facet, FacetListener listener) {
        removeListener(FacetListener.class, facet, listener);
    }

    public synchronized void removeFacetListener(FacetListener listener) {
        removeFacetListener(null, listener);
    }
    
    public synchronized void addServerProjectListener(ServerProjectListener listener) {
        String projectName = (String) new GetServerProjectName(this).execute();
        addListener(ServerProjectListener.class, projectName, listener);
    }
    
    public synchronized void removeServerProjectListener(ServerProjectListener listener) {
        String projectName = (String) new GetServerProjectName(this).execute();
        removeListener(ServerProjectListener.class, projectName, listener);
    }

    private void addListener(Class c, Object o, EventListener listener) {
        _frameStoreManager.addListener(c, o, listener);
    }

    private void removeListener(Class c, Object o, EventListener listener) {
        _frameStoreManager.removeListener(c, o, listener);
    }

    @Override
    public String toString() {
        return StringUtilities.getClassName(this) + "(" + getName() + ")";
    }

    public synchronized Collection getCurrentUsers() {
        return Collections.EMPTY_LIST;
    }

    public synchronized boolean beginTransaction(String name) {
        return getHeadFrameStore().beginTransaction(name);
    }
    
    public synchronized boolean beginTransaction(String name, String appliedToFrameName) {
    	if (appliedToFrameName == null) {
    		return beginTransaction(name);
    	}
    	return beginTransaction(name + Transaction.APPLY_TO_TRAILER_STRING + appliedToFrameName);		
	}

    public synchronized boolean commitTransaction() {
        return getHeadFrameStore().commitTransaction();
    }

    public synchronized boolean rollbackTransaction() {
        return getHeadFrameStore().rollbackTransaction();
    }

    /**
     * @deprecated Use #commitTransaction or #rollbackTransaction
     */
    @Deprecated
    public synchronized boolean endTransaction(boolean doCommit) {
        boolean committed;
        if (doCommit) {
            committed = commitTransaction();
        } else {
            committed = !rollbackTransaction();
        }
        return committed;
    }

    /**
     * @deprecated Use #commitTransaction
     */
    @Deprecated
    public synchronized boolean endTransaction() {
    	return commitTransaction();
    }
    
    public synchronized void setFrameFactory(FrameFactory factory) {
        _frameFactory = factory;
    }

    public CommandManager getCommandManager() {
        return getUndoFrameStore();
    }

    public synchronized void setFrameNameValidator(FrameNameValidator validator) {
        _frameNameValidator = validator;
    }

    public synchronized boolean isValidFrameName(String name, Frame frame) {
        return (_frameNameValidator == null) ? true : _frameNameValidator.isValid(name, frame);
    }

    public synchronized String getInvalidFrameNameDescription(String name, Frame frame) {
        String description = null;
        if (_frameNameValidator != null) {
            description = _frameNameValidator.getErrorMessage(name, frame);
        }
        return description;
    }

    public synchronized void setPollForEvents(boolean b) {
        _frameStoreManager.setPollForEvents(b);
    }

    /**
     * @deprecated
     * @param cls
     * @param slot
     */
    @Deprecated
    public synchronized void setDirectBrowserSlot(Cls cls, Slot slot) {
        setDirectBrowserSlotPattern(cls, new BrowserSlotPattern(slot));
    }

    public synchronized void setDirectBrowserSlotPattern(Cls cls, BrowserSlotPattern slotPattern) {
        getProject().setDirectBrowserSlotPattern(cls, slotPattern);
        _frameStoreManager.notifyInstancesOfBrowserTextChange(cls);
    }

    public synchronized void removeFrameStore(FrameStore frameStore) {
        _frameStoreManager.removeFrameStore(frameStore);
    }

    public synchronized void insertFrameStore(FrameStore newFrameStore) {
        _frameStoreManager.insertFrameStore(newFrameStore);
    }

    public synchronized void insertFrameStore(FrameStore newFrameStore, int position) {
        _frameStoreManager.insertFrameStore(newFrameStore, position);
    }

    public synchronized List<FrameStore> getFrameStores() {
        return _frameStoreManager.getFrameStores();
    }

    public synchronized void setTerminalFrameStore(FrameStore store) {
        _frameStoreManager.setTerminalFrameStore(store);

    }

    public synchronized FrameStore getTerminalFrameStore() {
        return _frameStoreManager.getTerminalFrameStore();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.stanford.smi.protege.model.KnowledgeBase#clearAllListeners()
     */
    public synchronized void clearAllListeners() {
        _frameStoreManager.clearAllListeners();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getFrameCounts()
     */
    public synchronized FrameCounts getFrameCounts() {
        return getProject().getFrameCounts();
    }

    public BrowserSlotPattern getDirectBrowserSlotPattern(Cls cls) {
        return getProject().getDirectBrowserSlotPattern(cls);
    }

    public void setDirectBrowserTextPattern(Cls cls, BrowserSlotPattern pattern) {
        getProject().setDirectBrowserSlotPattern(cls, pattern);
    }

    public synchronized Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        return getHeadFrameStore().getDirectOwnSlotValuesClosure(frame, slot);
    }

    /**
     * @deprecated Use #getFacetCheckingEnabled
     */
    @Deprecated
    public boolean getValueChecking() {
        return getFacetCheckingEnabled();
    }

    public synchronized boolean getFacetCheckingEnabled() {
        return _frameStoreManager.getFacetCheckingEnabled();
    }

    public void startJournaling(URI journal) {
        _frameStoreManager.startJournaling(journal);
    }

    public void stopJournaling() {
        _frameStoreManager.stopJournaling();
    }

    public synchronized boolean setGenerateDeletingFrameEventsEnabled(boolean enabled) {
        return _frameStoreManager.setGenerateDeletingFrameEventsEnabled(enabled);
    }

    public synchronized void flushCache() {
        getFrameStoreManager().reinitialize();
    }

    public synchronized Cls getReifiedRelationCls() {
        return _systemFrames.getRelationCls();
    }

    public synchronized Slot getReifedRelationFromSlot() {
        return _systemFrames.getFromSlot();
    }

    public synchronized Slot getReifedRelationToSlot() {
        return _systemFrames.getToSlot();
    }
    
    public synchronized void addTransactionListener(TransactionListener listener) {
        addListener(TransactionListener.class, this, listener);
    }
    
    public synchronized void removeTransactionListener(TransactionListener listener) {
        removeListener(TransactionListener.class, this, listener);
    }
    
    public Collection<Frame> executeQuery(Query q) {
      SynchronizeQueryCallback callback = new SynchronizeQueryCallback(this);
      getHeadFrameStore().executeQuery(q, callback);
      return callback.waitForResults();
    }
    
    public ServerCacheStateMachine getCacheMachine() {
        return cacheMachine;
    }
    
    public void setCacheMachine(ServerCacheStateMachine machine) {
        cacheMachine = machine;
    }

    public Frame rename(Frame frame, String name) {
        FrameFactory ff = getFrameFactory();
        return ff.rename(frame, name);
    }
    
    public void assertFrameName(Frame frame) {
    	setDirectOwnSlotValue(frame, _systemFrames.getNameSlot(), frame.getName());
    }
}
