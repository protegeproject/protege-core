package edu.stanford.smi.protege.storage.jdbc;

import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * Default implementation of the KnowledgeBase interface.  Handles
 * event generation but delegates most frame operations to FrameHandler.  Implements wrapper methods for some
 * calls to make the interface easier to use.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OldJdbcDefaultKnowledgeBase implements KnowledgeBase {
    private Cls _defaultClsMetaCls;
    private Cls _rootClsMetaCls;
    private Cls _defaultSlotMetaCls;
    private Cls _rootSlotMetaCls;
    private Cls _defaultFacetMetaCls;
    private Cls _rootFacetMetaCls;
    private Cls _rootCls;
    private String _kbName;
    private String _frameNamePrefix;
    private boolean _isLoading;
    private String _buildString;
    private String _versionString;
    private String _userName;
    private boolean _slotValueFacetChecking;

    private int _nextFrameNumber;

    private Slot _directTemplateSlotsSlot;
    private Slot _directSuperclassesSlot;
    private Slot _directSubclassesSlot;
    private Slot _directSubslotsSlot;
    private Slot _directSuperslotsSlot;
    private Slot _directInstancesSlot;
    private Slot _directTypeSlot;
    private Slot _nameSlot;
    private Slot _roleSlot;
    private Slot _constraintsSlot;
    private Slot _inverseSlot;
    private Slot _defaultValuesSlot;
    private Slot _valuesSlot;
    private Slot _documentationSlot;
    private Slot _valueTypeSlot;
    private Slot _minimumCardinalitySlot;
    private Slot _maximumCardinalitySlot;
    private Slot _numericMinimumSlot;
    private Slot _numericMaximumSlot;
    private Slot _associatedSlotSlot;
    private Slot _associatedFacetSlot;

    private Facet _documentationFacet;
    private Facet _numericMaximumFacet;
    private Facet _numericMinimumFacet;
    private Facet _valueTypeFacet;
    private Facet _minimumCardinalityFacet;
    private Facet _maximumCardinalityFacet;
    private Facet _defaultValuesFacet;
    private Facet _valuesFacet;
    private Facet _constraintsFacet;
    private Facet _inverseFacet;

    private boolean _hasChanged;
    private KnowledgeBaseFactory _knowledgeBaseFactory;
    private Project _project;
    private Collection _javaLoadPackages = new ArrayList();
    private Map _clientInformation = CollectionUtilities.createSmallMap();
    private FrameManager _frameManager;

    private ListenerCollection _frameListeners = new ListenerMap(new FrameEventDispatcher());
    private ListenerCollection _clsListeners = new ListenerMap(new ClsEventDispatcher());
    private ListenerCollection _slotListeners = new ListenerMap(new SlotEventDispatcher());
    private ListenerCollection _facetListeners = new ListenerMap(new FacetEventDispatcher());
    private ListenerCollection _instanceListeners = new ListenerMap(new InstanceEventDispatcher());
    private ListenerCollection _knowledgeBaseListeners = new ListenerMap(new KnowledgeBaseEventDispatcher());

    private final static Class[] CONSTRUCTOR_ARGUMENT_CLASSES = { KnowledgeBase.class, FrameID.class };

    private Set _failedClsLoads = new HashSet();

    private boolean _isCheckingArgs = true;
    // argument types
    private static final int PRIMITIVE = 1;
    private static final int VALUE = 2;
    private static final int FRAME = 3;
    private static final int STRING = 4;
    private static final int CLS = 5;
    private static final int SLOT = 6;
    private static final int FACET = 7;
    private static final int INSTANCE = 8;
    private static final int VALUE_TYPE = 9;
    private static final int LISTENER = 10;
    private static final int FRAME_OR_DELETED_FRAME = 11;

    private static final int VALUE_OR_NULL = 101;
    private static final int FRAME_OR_NULL = 102;
    private static final int STRING_OR_NULL = 103;
    private static final int CLS_OR_NULL = 104;
    private static final int SLOT_OR_NULL = 105;
    private static final int FACET_OR_NULL = 106;
    private static final int CONCRETE_CLS_OR_NULL = 107;

    private static final int VALUE_COLLECTION = 201;
    private static final int STRING_COLLECTION = 202;
    private static final int FRAME_COLLECTION = 203;
    private static final int CLS_COLLECTION = 204;
    private static final int SLOT_COLLECTION = 205;
    private static final int FACET_COLLECTION = 206;
    private static final int INSTANCE_COLLECTION = 207;

    public OldJdbcDefaultKnowledgeBase(KnowledgeBaseFactory factory) {
        // Log.enter(this, "DefaultKnowledgeBase");
        setName("KB");
        _knowledgeBaseFactory = factory;
        load();
    }

    public synchronized void addClsListener(Cls cls, ClsListener listener) {
        checkArgs(CLS, cls, LISTENER, listener);
        _clsListeners.add(cls, listener);
    }

    public synchronized void addDirectSuperclass(Cls child, Cls parent) {
        checkArgs(CLS, child, CLS, parent);
        if (hasDirectSuperclass(child, parent)) {
            Log.getLogger().warning("Class " + child + " already has superclass " + parent);
        } else {
            journal("addDirectSuperclass", "child", child, "parent", parent);
            _frameManager.addDirectSuperclass(child, parent);
            postClsEvent(child, ClsEvent.DIRECT_SUPERCLASS_ADDED, parent);
            postClsEvent(parent, ClsEvent.DIRECT_SUBCLASS_ADDED, child);
        }
    }

    private void addDirectSuperclasses(Cls child, Collection parents) {
        Iterator i = parents.iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            addDirectSuperclass(child, parent);
        }
    }

    public synchronized void addDirectSuperslot(Slot slot, Slot superslot) {
        checkArgs(SLOT, slot, SLOT, superslot);
        if (hasDirectSuperslot(slot, superslot)) {
            Log.getLogger().warning("Slot " + slot + " already has superslot " + superslot);
        } else {
            journal("addDirectSuperslot", "slot", slot, "superslot", superslot);
            internalAddOwnSlotValue(slot, _directSuperslotsSlot, superslot);
            if (_isLoading) {
                internalAddOwnSlotValue(superslot, _directSubslotsSlot, slot);
            }
        }
    }

    public synchronized void addDirectTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        if (hasDirectTemplateSlot(cls, slot)) {
            Log.getLogger().warning("already has template slot: " + slot);
        } else {
            journal("addDirectTemplateSlot", "cls", cls, "slot", slot);
            _frameManager.addDirectTemplateSlot(cls, slot);
            if (isSlotMetaCls(cls)) {
                if (getAssociatedFacet(slot) == null) {
                    Facet facet = createFacet(null);
                    setAssociatedFacet(slot, facet);
                    setAssociatedSlot(facet, slot);
                }
            }
            postClsEvent(cls, ClsEvent.TEMPLATE_SLOT_ADDED, slot);
        }
    }

    public synchronized void addFacetListener(Facet facet, FacetListener listener) {
        checkArgs(FACET, facet, LISTENER, listener);
        _facetListeners.add(facet, listener);
    }

    public synchronized void addFrameListener(Frame frame, FrameListener listener) {
        checkArgs(FRAME, frame, LISTENER, listener);
        _frameListeners.add(frame, listener);
    }

    public void addInstance(Instance instance, String name, Cls type, boolean isNew) {
        // Log.enter(this, "addInstance", instance, name, type);

        // This test keeps us from creating new names for database instances.  There should be
        // a better way.
        if (isNew || name == null) {
            name = createUniqueFrameName(name);
        }
        _frameManager.addInstance(instance, name, type);
        if (isNew) {
            initializeOwnSlots(instance, type);
        }
    }

    public synchronized void addInstanceListener(Instance instance, InstanceListener listener) {
        checkArgs(INSTANCE, instance, LISTENER, listener);
        _instanceListeners.add(instance, listener);
    }

    private void addInstances(Instance instance, Slot slot, Collection reachableInstances) {
        if (equals(instance.getOwnSlotValueType(slot), ValueType.INSTANCE)) {
            Iterator i = instance.getOwnSlotValues(slot).iterator();
            while (i.hasNext()) {
                Instance value = (Instance) i.next();
                addReachableSimpleInstances(value, reachableInstances);
            }
        }
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private void addInverseOwnSlotValue(Frame sourceFrame, Slot forwardSlot, Object target) {
        Assert.assertNotNull("forward slot", forwardSlot);
        Slot inverseSlot = forwardSlot.getInverseSlot();
        if (inverseSlot != null && !_isLoading && target instanceof Frame) {
            Frame targetFrame = (Frame) target;
            if (isSingleValued(targetFrame, inverseSlot)) {
                Object o = internalGetOwnSlotValue(targetFrame, inverseSlot);
                if (o != null) {
                    removeSingleOwnSlotValue(targetFrame, inverseSlot, o);
                    removeSingleOwnSlotValue((Instance) o, forwardSlot, targetFrame);
                }
            }
            _frameManager.addOwnSlotValue(targetFrame, inverseSlot, sourceFrame);
            postFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, targetFrame, inverseSlot);
        }
    }

    private void addInverseOwnSlotValues(Frame frame, Slot forwardSlot, Collection values) {
        if (!_isLoading) {
            Slot inverseSlot = forwardSlot.getInverseSlot();
            if (inverseSlot != null) {
                Iterator i = values.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (o instanceof Instance) {
                        addInverseOwnSlotValue(frame, forwardSlot, o);
                    }
                }
            }
        }
    }

    private void addInverseSlotRelationship(Slot slot, Slot inverseSlot) {
        _frameManager.setOwnSlotValue(slot, _inverseSlot, inverseSlot);
        _frameManager.setOwnSlotValue(inverseSlot, _inverseSlot, slot);
        postOwnSlotValueChanged(slot, _inverseSlot);
        postOwnSlotValueChanged(inverseSlot, _inverseSlot);
    }

    public synchronized void addJavaLoadPackage(String packageName) {
        /*
        if (SystemUtilities.hasAvailablePackage(packageName)) {
            Log.trace("Found package", this, "addJavaLoadPackage", packageName);
        } else {
            Log.warning("Unable to locate package", this, "addJavaLoadPackage", packageName);
        }
        */
        _javaLoadPackages.add(packageName);
        _failedClsLoads.clear();
    }

    public synchronized void addKnowledgeBaseListener(KnowledgeBaseListener listener) {
        _knowledgeBaseListeners.add(this, listener);
    }

    public synchronized void addOwnSlotValue(Frame frame, Slot slot, Object value) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE, value);
        journal("addOwnSlotValue", "frame", frame, "slot", slot, "value", value);
        if (_slotValueFacetChecking) {
            ArrayList values = new ArrayList(internalGetOwnSlotValues(frame, slot));
            values.add(value);
            checkAllFacets(frame, slot, values);
        } else {
            checkPrimitiveType(frame, slot, value);
        }
        internalAddOwnSlotValue(frame, slot, value);
    }

    public synchronized void addOwnSlotValue(Frame frame, Slot slot, Object value, int index) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE, value);
        journal("addOwnSlotValue", "frame", frame, "slot", slot, "value", value, "index", new Integer(index));
        if (_slotValueFacetChecking) {
            ArrayList values = new ArrayList(internalGetOwnSlotValues(frame, slot));
            values.add(index, value);
            checkAllFacets(frame, slot, values);
        } else {
            checkPrimitiveType(frame, slot, value);
        }
        internalAddOwnSlotValue(frame, slot, value, index);
    }

    private void addReachableSimpleInstances(Instance instance, Collection reachableInstances) {
        if (!reachableInstances.contains(instance)) {
            reachableInstances.add(instance);
            // Log.trace("reachable", this, "addReachableSimpleInstances", instance);
            Iterator i = getTemplateSlots(getDirectType(instance)).iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                addInstances(instance, slot, reachableInstances);
            }
        }
    }

    public synchronized void addSlotListener(Slot slot, SlotListener listener) {
        // generic listeners not supported by this backend
        if (slot != null) {
            checkArgs(SLOT, slot, LISTENER, listener);
            _slotListeners.add(slot, listener);
        }
    }

    public synchronized void addTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet, VALUE, value);
        journal("addTemplateFacetValue", "cls", cls, "slot", slot, "facet", facet, "value", value);
        _frameManager.addTemplateFacetValue(cls, slot, facet, value);
        postTemplateFacetValueChanged(cls, slot, facet);
    }

    public synchronized void addTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value, int index) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet, VALUE, value);
        journal(
            "addTemplateFacetValue",
            "cls",
            cls,
            "slot",
            slot,
            "facet",
            facet,
            "value",
            value,
            "index",
            new Integer(index));
        _frameManager.addTemplateFacetValue(cls, slot, facet, value, index);
        postTemplateFacetValueChanged(cls, slot, facet);
    }

    public synchronized void addTemplateSlotValue(Cls cls, Slot slot, Object value) {
        checkArgs(CLS, cls, SLOT, slot, VALUE, value);
        journal("addTemplateSlotValue", "cls", cls, "slot", slot, "value", value);
        _frameManager.addTemplateSlotValue(cls, slot, value);
        postTemplateSlotValueChanged(cls, slot);
    }

    public synchronized boolean areValidOwnSlotValues(Frame frame, Slot slot, Collection values) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE_COLLECTION, values);
        boolean result = true;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.areValidValues(frame, slot, values);
        }
        return result;
    }

    public synchronized boolean beginTransaction(String name) {
        return _frameManager.beginTransaction();
    }

    /**
     * @deprecated Use #setFrameName(Frame, String)
     */
    public void changeFrameName(Frame frame, String newName) {
        setFrameName(frame, newName);
    }

    public synchronized void setFrameName(Frame frame, String newName) {
        checkArgs(FRAME, frame, STRING, newName);
        journal("changeFrameName", "frame", frame, "newName", newName);
        String oldName = frame.getName();
        if (!newName.equals(oldName)) {
            String uniqueName = createUniqueFrameName(newName);
            _frameManager.setName(frame, uniqueName);
            postFrameEvent(FrameEvent.NAME_CHANGED, frame, oldName);
            postBrowserTextChanged(frame);
        }
    }

    private void checkAllFacets(Frame frame, Slot slot, Collection values) {
        if (!areValidOwnSlotValues(frame, slot, values)) {
            throw new IllegalArgumentException(getInvalidOwnSlotValuesText(frame, slot, values));
        }
    }

    private void checkArg(int position, int type, Object arg) {
        switch (type) {
            case CLS :
            case SLOT :
            case INSTANCE :
            case FACET :
            case FRAME :
                checkFrame(position, (Frame) arg);
                break;
            case CLS_OR_NULL :
            case SLOT_OR_NULL :
            case FACET_OR_NULL :
            case FRAME_OR_NULL :
                if (arg != null) {
                    checkFrame(position, (Frame) arg);
                }
                break;
            case CONCRETE_CLS_OR_NULL :
                if (arg != null) {
                    Cls cls = (Cls) arg;
                    checkFrame(position, cls);
                    if (isAbstract(cls)) {
                        // Log.error("Concrete class required", this, "checkArg", new Integer(position), arg);
                        // throw new IllegalArgumentException("Concrete class required at position " + position + " - " + arg);
                    }
                }
                break;
            case VALUE :
                if (arg == null) {
                    throw new IllegalArgumentException("null parameter in position " + position);
                }
                checkValue(position, arg);
                break;
            case VALUE_OR_NULL :
                if (arg instanceof Frame) {
                    checkValue(position, arg);
                }
                break;
            case CLS_COLLECTION :
            case SLOT_COLLECTION :
            case INSTANCE_COLLECTION :
            case FACET_COLLECTION :
            case FRAME_COLLECTION :
                Iterator i = ((Collection) arg).iterator();
                while (i.hasNext()) {
                    Object element = i.next();
                    checkFrame(position, (Frame) element);
                }
                break;
            case STRING_COLLECTION :
            case VALUE_COLLECTION :
                Iterator j = ((Collection) arg).iterator();
                while (j.hasNext()) {
                    Object element = j.next();
                    checkValue(position, element);
                }
                break;
            case STRING :
            case LISTENER :
            case VALUE_TYPE :
            case FRAME_OR_DELETED_FRAME :
                if (arg == null) {
                    throw new IllegalArgumentException("Null parameter at position: " + position);
                }
            case STRING_OR_NULL :
            case PRIMITIVE :
                // do nothing
                break;
            default :
                Assert.fail("invalid type");
        }
    }

    private void checkArgs(int type, Object o) {
        if (_isCheckingArgs) {
            checkArg(1, type, o);
        }
    }

    private void checkArgs(int type1, Object arg1, int type2, Object arg2) {
        if (_isCheckingArgs) {
            checkArg(1, type1, arg1);
            checkArg(2, type2, arg2);
        }
    }

    private void checkArgs(int type1, Object arg1, int type2, Object arg2, int type3, Object arg3) {
        if (_isCheckingArgs) {
            checkArg(1, type1, arg1);
            checkArg(2, type2, arg2);
            checkArg(3, type3, arg3);
        }
    }

    private void checkArgs(int type1, Object a1, int type2, Object a2, int type3, Object a3, int type4, Object a4) {
        if (_isCheckingArgs) {
            checkArg(1, type1, a1);
            checkArg(2, type2, a2);
            checkArg(3, type3, a3);
            checkArg(4, type4, a4);
        }
    }

    private void checkForDanglingListeners(Frame frame) {
        checkForDanglingListeners(_frameListeners, frame);
        checkForDanglingListeners(_clsListeners, frame);
        checkForDanglingListeners(_slotListeners, frame);
        checkForDanglingListeners(_facetListeners, frame);
        checkForDanglingListeners(_instanceListeners, frame);
    }

    private void checkForDanglingListeners(ListenerCollection listeners, Frame frame) {
        listeners.removeAllListeners(frame);
    }

    private void checkFrame(int position, Frame frame) {
        if (frame == null) {
            throw new NullFrameException("argument: " + position);
        } else if (frame.isDeleted()) {
            Log.getLogger().severe("access with deleted frame: " + frame.getName());
            throw new DeletedFrameException("argument: " + position);
        } else if (frame.getKnowledgeBase() != this) {
            String name;
            try {
                name = frame.getName();
            } catch (Exception e) {
                name = "<unknown frame>";
            }
            throw new MissingFrameException("argument: " + position + " - " + name);
        }
    }

    private void checkPrimitiveType(Frame frame, Slot slot, Object value) {
        if (value != null) {
            checkPrimitiveType(getPrimitiveOwnSlotType(frame, slot), value);
        }
    }

    private void checkPrimitiveType(Frame frame, Slot slot, Collection values) {
        checkPrimitiveType(getPrimitiveOwnSlotType(frame, slot), values);
    }

    private void checkPrimitiveType(Class clas, Object value) {
        if (!clas.isInstance(value)) {
            throw new IllegalArgumentException(value + " is not an instance of " + clas);
        }
    }

    private void checkPrimitiveType(Class clas, Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            checkPrimitiveType(clas, value);
        }
    }

    private void checkValue(int position, Object value) {
        if (value instanceof Frame) {
            checkFrame(position, (Frame) value);
        } else if (value instanceof String) {
            // do nothing
        } else if (value instanceof Number) {
            // do nothing
        } else if (value instanceof Boolean) {
            // do nothing
        } else {
            String classText = (value == null) ? "" : ", class=" + value.getClass();
            String text = "Illegal argument at position: " + position + ": value=" + value + classText;
            throw new IllegalArgumentException(text);
        }
    }

    public synchronized boolean containsFrame(String name) {
        return _frameManager.containsFrame(name);
    }

    private void createAnnotationFrames() {
        Cls systemCls = getCls(Model.Cls.SYSTEM_CLASS);
        Cls annotation = createSystemCls(Model.Cls.ANNOTATION, Model.ClsID.ANNOTATION);
        annotation.addDirectSuperclass(systemCls);
        annotation.setAbstract(true);

        Slot author = createSystemSlot(Model.Slot.CREATOR, Model.SlotID.CREATOR);
        Slot timestamp = createSystemSlot(Model.Slot.CREATION_TIMESTAMP, Model.SlotID.CREATION_TIMESTAMP);

        Cls instanceAnnotation = createSystemCls(Model.Cls.INSTANCE_ANNOTATION, Model.ClsID.INSTANCE_ANNOTATION);
        instanceAnnotation.addDirectSuperclass(annotation);
        Slot instance = createSystemSlot(Model.Slot.ANNOTATED_INSTANCE, Model.SlotID.ANNOTATED_INSTANCE);
        instance.setAllowedClses(getRootClses());

        Slot text = createSystemSlot(Model.Slot.ANNOTATION_TEXT, Model.SlotID.ANNOTATION_TEXT);
        instanceAnnotation.addDirectTemplateSlot(instance);
        instanceAnnotation.addDirectTemplateSlot(text);
        instanceAnnotation.addDirectTemplateSlot(author);
        instanceAnnotation.addDirectTemplateSlot(timestamp);

        Facet modifierFacet = createSystemFacet(Model.Facet.MODIFIER, null, Model.FacetID.MODIFIER);
        Facet modificationTimestampFacet =
            createSystemFacet(Model.Facet.MODIFICATION_TIMESTAMP, null, Model.FacetID.MODIFICATION_TIMESTAMP);

        createSystemSlot(Model.Slot.MODIFIER, modifierFacet, Model.SlotID.MODIFIER);
        createSystemSlot(
            Model.Slot.MODIFICATION_TIMESTAMP,
            modificationTimestampFacet,
            Model.SlotID.MODIFICATION_TIMESTAMP);
    }

    private void createRelationFrames() {
        Cls relationCls = createSystemCls(Model.Cls.RELATION, Model.ClsID.RELATION);
        Cls directedBinaryRelationCls =
            createSystemCls(Model.Cls.DIRECTED_BINARY_RELATION, Model.ClsID.DIRECTED_BINARY_RELATION);
        Slot fromSlot = createSystemSlot(Model.Slot.FROM, Model.SlotID.FROM);
        Slot toSlot = createSystemSlot(Model.Slot.TO, Model.SlotID.TO);

        relationCls.setAbstract(true);
        setupSlot(fromSlot, false, ValueType.INSTANCE, getRootCls());
        fromSlot.setMinimumCardinality(1);
        setupSlot(toSlot, false, ValueType.INSTANCE, getRootCls());
        toSlot.setMinimumCardinality(1);

        relationCls.addDirectSuperclass(getCls(Model.Cls.SYSTEM_CLASS));
        directedBinaryRelationCls.addDirectSuperclass(relationCls);
        directedBinaryRelationCls.addDirectTemplateSlot(fromSlot);
        directedBinaryRelationCls.addDirectTemplateSlot(toSlot);
    }

    public synchronized Cls createCls(String name, Collection directSuperclasses) {
        checkArgs(STRING_OR_NULL, name, CLS_COLLECTION, directSuperclasses);
        Cls firstParent = (Cls) CollectionUtilities.getFirstItem(directSuperclasses);
        Cls metaCls = (firstParent == null) ? _defaultClsMetaCls : firstParent.getDirectType();
        return createCls(name, directSuperclasses, metaCls);
    }

    public synchronized Cls createCls(String name, Collection parents, Cls metaCls) {
        return createCls(name, parents, metaCls, true);
    }

    public synchronized Cls createCls(String name, Collection parents, Cls metaCls, boolean isNew) {
        checkArgs(STRING_OR_NULL, name, CLS_COLLECTION, parents, CONCRETE_CLS_OR_NULL, metaCls);
        journal("createClass", "name", name, "parents", parents, "metaCls", metaCls, "isNew", new Boolean(isNew));
        Cls cls = newCls(name, metaCls, null, isNew);
        Iterator i = parents.iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            _frameManager.addDirectSuperclass(cls, parent);
        }

        postInstanceCreatedEvents(KnowledgeBaseEvent.CLS_CREATED, cls, metaCls);
        Iterator j = parents.iterator();
        while (j.hasNext()) {
            Cls parent = (Cls) j.next();
            postClsEvent(parent, ClsEvent.DIRECT_SUBCLASS_ADDED, cls);
        }

        return cls;
    }

    public synchronized Facet createFacet(String name) {
        Assert.assertNotNull("default facet meta class", _defaultFacetMetaCls);
        return createFacet(name, _defaultFacetMetaCls, true);
    }

    public synchronized Facet createFacet(String name, Cls metaCls) {
        return createFacet(name, metaCls, true);
    }

    public synchronized Facet createFacet(String name, Cls metaCls, boolean isNew) {
        checkArgs(STRING_OR_NULL, name, CONCRETE_CLS_OR_NULL, metaCls);
        journal("createCacet", "name", name, "metacls", metaCls);
        Facet facet = newFacet(name, metaCls, null, isNew);
        postInstanceCreatedEvents(KnowledgeBaseEvent.FACET_CREATED, facet, metaCls);
        return facet;
    }

    public synchronized Instance createInstance(String name, Cls cls) {
        return createInstance(name, cls, true);
    }

    public synchronized Instance createInstance(String name, Collection types) {
        return createInstance(name, (Cls) CollectionUtilities.getFirstItem(types), true);
    }

    public synchronized Instance createInstance(String name, Cls cls, boolean isNew) {
        checkArgs(STRING_OR_NULL, name, CONCRETE_CLS_OR_NULL, cls);
        Instance instance = newInstance(name, cls, null, isNew);
        if (instance instanceof Cls) {
            postInstanceCreatedEvents(KnowledgeBaseEvent.CLS_CREATED, instance, cls);
        } else if (instance instanceof Slot) {
            postInstanceCreatedEvents(KnowledgeBaseEvent.SLOT_CREATED, instance, cls);
        } else if (instance instanceof Facet) {
            postInstanceCreatedEvents(KnowledgeBaseEvent.FACET_CREATED, instance, cls);
        } else {
            postInstanceCreatedEvents(KnowledgeBaseEvent.INSTANCE_CREATED, instance, cls);
        }
        return instance;
    }

    private Instance createJavaClassInstance(Class javaClass, FrameID id) {
        Object[] arguments = new Object[] { this, id };
        return (Instance) SystemUtilities.newInstance(javaClass, CONSTRUCTOR_ARGUMENT_CLASSES, arguments);
    }

    private Cls createPALCls(String name, FrameID id) {
        DefaultCls cls = createSystemCls(name, id);
        // cls.setSystem(false);
        return cls;
    }

    private void createPALFrames() {
        Slot nameSlot = createPALSlot(Model.Slot.PAL_NAME, Model.SlotID.PAL_NAME);
        Slot descriptionSlot = createPALSlot(Model.Slot.PAL_DESCRIPTION, Model.SlotID.PAL_DESCRIPTION);
        Slot statementSlot = createPALSlot(Model.Slot.PAL_STATEMENT, Model.SlotID.PAL_STATEMENT);
        Slot rangeSlot = createPALSlot(Model.Slot.PAL_RANGE, Model.SlotID.PAL_RANGE);

        Cls palConstraintCls = createPALCls(Model.Cls.PAL_CONSTRAINT, Model.ClsID.PAL_CONSTRAINT);
        palConstraintCls.addDirectTemplateSlot(nameSlot);
        palConstraintCls.addDirectTemplateSlot(descriptionSlot);
        palConstraintCls.addDirectTemplateSlot(statementSlot);
        palConstraintCls.addDirectTemplateSlot(rangeSlot);

        Cls constraint = getCls(Model.Cls.CONSTRAINT);
        palConstraintCls.addDirectSuperclass(constraint);
    }

    private Slot createPALSlot(String name, FrameID id) {
        DefaultSlot slot = createSystemSlot(name, id);
        // slot.setSystem(false);
        return slot;
    }

    public synchronized Instance createSimpleInstance(String name, Cls cls) {
        return createSimpleInstance(name, cls, true);
    }

    public synchronized Instance createSimpleInstance(String name, Cls cls, boolean isNew) {
        checkArgs(STRING_OR_NULL, name, CONCRETE_CLS_OR_NULL, cls);
        journal("createSimpleInstance", "name", name, "class", cls);
        Instance instance = newSimpleInstance(name, cls, null, isNew);
        postSimpleInstanceCreatedEvents(instance, cls);
        return instance;
    }

    public synchronized Slot createSlot(String name) {
        Assert.assertNotNull("default slot meta class", _defaultSlotMetaCls);
        return createSlot(name, _defaultSlotMetaCls, Collections.EMPTY_LIST, true);
    }

    public synchronized Slot createSlot(String name, Cls metaCls) {
        return createSlot(name, metaCls, Collections.EMPTY_LIST, true);
    }

    public synchronized Slot createSlot(String name, Cls metaCls, Collection superslots, boolean isNew) {
        checkArgs(STRING_OR_NULL, name, CONCRETE_CLS_OR_NULL, metaCls, SLOT_COLLECTION, superslots);
        journal("createSlot", "name", name, "metaCls", metaCls, "superslots", superslots, "isNew", new Boolean(isNew));
        Slot slot = null;
        if (name != null) {
            slot = getSlot(name);
            if (slot != null) {
                Log.getLogger().warning("slot already exists: " + name);
            }
        }
        if (slot == null) {
            slot = newSlot(name, metaCls, null, isNew);
            setSuperslots(slot, superslots);
            postInstanceCreatedEvents(KnowledgeBaseEvent.SLOT_CREATED, slot, metaCls);
        }
        return slot;
    }

    public synchronized Slot createSlot(String name, Cls metaCls, boolean isNew) {
        return createSlot(name, metaCls, Collections.EMPTY_LIST, isNew);
    }

    private DefaultCls createSystemCls(String name, Cls metaCls, FrameID frameID) {
        DefaultCls cls = (DefaultCls) newCls(name, metaCls, frameID, true);
        cls.setEditable(false);
        cls.setIncluded(true);
        cls.setSystem(true);
        return cls;
    }

    private DefaultCls createSystemCls(String name, FrameID frameID) {
        return createSystemCls(name, _defaultClsMetaCls, frameID);
    }

    private DefaultCls createSystemCls(String name, boolean isAbstract, FrameID frameID) {
        DefaultCls cls = createSystemCls(name, frameID);
        cls.setAbstract(isAbstract);
        return cls;
    }

    private Facet createSystemFacet(String name, FacetConstraint c, FrameID id) {
        DefaultFacet facet = (DefaultFacet) newFacet(name, _defaultFacetMetaCls, id, true);
        facet.setEditable(false);
        facet.setSystem(true);
        facet.setIncluded(true);
        facet.setConstraint(c);
        return facet;
    }

    // knowledge model methods
    private void createSystemFrames() {

        // create the all important name slot
        _nameSlot = createSystemSlot(Model.Slot.NAME, Model.SlotID.NAME);
        _nameSlot.setName(Model.Slot.NAME);

        _directTypeSlot = createSystemSlot(Model.Slot.DIRECT_TYPES, Model.SlotID.DIRECT_TYPES);
        _directInstancesSlot = createSystemSlot(Model.Slot.DIRECT_INSTANCES, Model.SlotID.DIRECT_INSTANCES);

        // create the facets
        Cls standardFacet = createSystemCls(Model.Cls.STANDARD_FACET, Model.ClsID.STANDARD_FACET);
        _defaultFacetMetaCls = standardFacet;

        _defaultValuesFacet = createSystemFacet(Model.Facet.DEFAULTS, null, Model.FacetID.DEFAULTS);
        _valuesFacet = createSystemFacet(Model.Facet.VALUES, null, Model.FacetID.VALUES);
        _valueTypeFacet =
            createSystemFacet(Model.Facet.VALUE_TYPE, new ValueTypeConstraint(), Model.FacetID.VALUE_TYPE);
        _maximumCardinalityFacet =
            createSystemFacet(
                Model.Facet.MAXIMUM_CARDINALITY,
                new MaximumCardinalityConstraint(),
                Model.FacetID.MAXIMUM_CARDINALITY);
        _minimumCardinalityFacet =
            createSystemFacet(
                Model.Facet.MINIMUM_CARDINALITY,
                new MinimumCardinalityConstraint(),
                Model.FacetID.MINIMUM_CARDINALITY);
        _documentationFacet = createSystemFacet(Model.Facet.DOCUMENTATION, null, Model.FacetID.DOCUMENTATION);
        _numericMinimumFacet =
            createSystemFacet(
                Model.Facet.NUMERIC_MINIMUM,
                new NumericMinimumConstraint(),
                Model.FacetID.NUMERIC_MINIMUM);
        _numericMaximumFacet =
            createSystemFacet(
                Model.Facet.NUMERIC_MAXIMUM,
                new NumericMaximumConstraint(),
                Model.FacetID.NUMERIC_MAXIMUM);
        _constraintsFacet = createSystemFacet(Model.Facet.CONSTRAINTS, null, Model.FacetID.CONSTRAINTS);
        _inverseFacet = createSystemFacet(Model.Facet.INVERSE, null, Model.FacetID.INVERSE);

        // create the slots
        Cls standardSlot = createSystemCls(Model.Cls.STANDARD_SLOT, Model.ClsID.STANDARD_SLOT);
        _defaultSlotMetaCls = standardSlot;
        _nameSlot.setDirectType(standardSlot);
        _directTypeSlot.setDirectType(standardSlot);
        _directInstancesSlot.setDirectType(standardSlot);

        // must create before any slots with facets
        _associatedSlotSlot = createSystemSlot(Model.Slot.ASSOCIATED_SLOT, Model.SlotID.ASSOCIATED_SLOT);
        _associatedFacetSlot = createSystemSlot(Model.Slot.ASSOCIATED_FACET, Model.SlotID.ASSOCIATED_FACET);

        _valueTypeSlot = createSystemSlot(Model.Slot.VALUE_TYPE, _valueTypeFacet, Model.SlotID.VALUE_TYPE);
        _maximumCardinalitySlot =
            createSystemSlot(
                Model.Slot.MAXIMUM_CARDINALITY,
                _maximumCardinalityFacet,
                Model.SlotID.MAXIMUM_CARDINALITY);
        _minimumCardinalitySlot =
            createSystemSlot(
                Model.Slot.MINIMUM_CARDINALITY,
                _minimumCardinalityFacet,
                Model.SlotID.MINIMUM_CARDINALITY);

        _directSuperclassesSlot = createSystemSlot(Model.Slot.DIRECT_SUPERCLASSES, Model.SlotID.DIRECT_SUPERCLASSES);
        _directSubclassesSlot = createSystemSlot(Model.Slot.DIRECT_SUBCLASSES, Model.SlotID.DIRECT_SUBCLASSES);
        _directSuperslotsSlot = createSystemSlot(Model.Slot.DIRECT_SUPERSLOTS, Model.SlotID.DIRECT_SUPERSLOTS);
        _directSubslotsSlot = createSystemSlot(Model.Slot.DIRECT_SUBSLOTS, Model.SlotID.DIRECT_SUBSLOTS);
        _directTemplateSlotsSlot =
            createSystemSlot(Model.Slot.DIRECT_TEMPLATE_SLOTS, Model.SlotID.DIRECT_TEMPLATE_SLOTS);

        _defaultValuesSlot = createSystemSlot(Model.Slot.DEFAULTS, _defaultValuesFacet, Model.SlotID.DEFAULTS);
        _valuesSlot = createSystemSlot(Model.Slot.VALUES, _valuesFacet, Model.SlotID.VALUES);
        _roleSlot = createSystemSlot(Model.Slot.ROLE, Model.SlotID.ROLE);
        setDefaultValues(_roleSlot, makeCollection(RoleConstraint.CONCRETE));
        _constraintsSlot = createSystemSlot(Model.Slot.CONSTRAINTS, _constraintsFacet, Model.SlotID.CONSTRAINTS);
        _inverseSlot = createSystemSlot(Model.Slot.INVERSE, _inverseFacet, Model.SlotID.INVERSE);
        _documentationSlot =
            createSystemSlot(Model.Slot.DOCUMENTATION, _documentationFacet, Model.SlotID.DOCUMENTATION);
        _numericMinimumSlot =
            createSystemSlot(Model.Slot.NUMERIC_MINIMUM, _numericMinimumFacet, Model.SlotID.NUMERIC_MINIMUM);
        _numericMaximumSlot =
            createSystemSlot(Model.Slot.NUMERIC_MAXIMUM, _numericMaximumFacet, Model.SlotID.NUMERIC_MAXIMUM);

        // add slots to standard-slot
        standardSlot.addDirectTemplateSlot(_nameSlot);
        standardSlot.addDirectTemplateSlot(_directTypeSlot);
        standardSlot.addDirectTemplateSlot(_defaultValuesSlot);
        standardSlot.addDirectTemplateSlot(_valuesSlot);
        standardSlot.addDirectTemplateSlot(_constraintsSlot);
        standardSlot.addDirectTemplateSlot(_valueTypeSlot);
        standardSlot.addDirectTemplateSlot(_minimumCardinalitySlot);
        standardSlot.addDirectTemplateSlot(_maximumCardinalitySlot);
        standardSlot.addDirectTemplateSlot(_documentationSlot);
        standardSlot.addDirectTemplateSlot(_numericMaximumSlot);
        standardSlot.addDirectTemplateSlot(_numericMinimumSlot);
        standardSlot.addDirectTemplateSlot(_inverseSlot);
        standardSlot.addDirectTemplateSlot(_directSubslotsSlot);
        standardSlot.addDirectTemplateSlot(_directSuperslotsSlot);
        standardSlot.addDirectTemplateSlot(_associatedFacetSlot);

        _valueTypeSlot.setDefaultValues(makeCollection(ValueType.STRING.toString()));
        _maximumCardinalitySlot.setDefaultValues(makeCollection(new Integer(1)));

        // configure the facets
        standardFacet.addDirectTemplateSlot(_nameSlot);
        standardFacet.addDirectTemplateSlot(_directTypeSlot);
        standardFacet.addDirectTemplateSlot(_associatedSlotSlot);
        standardFacet.addDirectTemplateSlot(_documentationSlot);

        // create the classes
        Cls standardCls = createSystemCls(Model.Cls.STANDARD_CLASS, Model.ClsID.STANDARD_CLASS);
        standardCls.setDirectType(standardCls);
        _defaultClsMetaCls = standardCls;

        _rootCls = createSystemCls(Model.Cls.THING, Model.ClsID.THING);
        _rootSlotMetaCls = createSystemCls(Model.Cls.SLOT, Model.ClsID.SLOT);
        Cls constraintCls = createSystemCls(Model.Cls.CONSTRAINT, Model.ClsID.CONSTRAINT);

        // set direct types
        standardSlot.setDirectType(standardCls);
        standardFacet.setDirectType(standardCls);

        // add slots to standard-class
        standardCls.addDirectTemplateSlot(_nameSlot);
        standardCls.addDirectTemplateSlot(_documentationSlot);
        standardCls.addDirectTemplateSlot(_roleSlot);
        standardCls.addDirectTemplateSlot(_directTemplateSlotsSlot);
        standardCls.addDirectTemplateSlot(_directTypeSlot);
        standardCls.addDirectTemplateSlot(_directInstancesSlot);
        standardCls.addDirectTemplateSlot(_directSuperclassesSlot);
        standardCls.addDirectTemplateSlot(_directSubclassesSlot);
        standardCls.addDirectTemplateSlot(_constraintsSlot);

        standardCls.setTemplateSlotDefaultValues(_roleSlot, makeCollection(RoleConstraint.CONCRETE));

        // configure slots
        setupSlot(_nameSlot, false, ValueType.STRING);
        setupSlot(_roleSlot, false, ValueType.SYMBOL, RoleConstraint.getValues());
        setupSlot(_directTypeSlot, false, ValueType.CLS, _rootCls);
        setupSlot(_minimumCardinalitySlot, false, ValueType.INTEGER);
        setupSlot(_maximumCardinalitySlot, false, ValueType.INTEGER);
        setupSlot(_numericMinimumSlot, false, ValueType.FLOAT);
        setupSlot(_numericMaximumSlot, false, ValueType.FLOAT);
        setupSlot(_directTypeSlot, false, ValueType.CLS, _rootCls);
        // setupSlot(_valueTypeSlot, false, ValueType.SYMBOL, ValueTypeConstraint.getValues());
        setupSlot(_valueTypeSlot, true, ValueType.ANY);
        setupSlot(_directInstancesSlot, true, ValueType.INSTANCE, _rootCls);
        setupSlot(_directSuperclassesSlot, true, ValueType.CLS, _rootCls);
        setupSlot(_directSubclassesSlot, true, ValueType.CLS, _rootCls);
        setupSlot(_directSuperslotsSlot, true, ValueType.INSTANCE, _rootSlotMetaCls);
        setupSlot(_directSubslotsSlot, true, ValueType.INSTANCE, _rootSlotMetaCls);
        setupSlot(_directTemplateSlotsSlot, true, ValueType.INSTANCE, _rootSlotMetaCls);
        setupSlot(_defaultValuesSlot, true, ValueType.ANY);
        setupSlot(_constraintsSlot, true, ValueType.INSTANCE, constraintCls);
        setupSlot(_documentationSlot, true, ValueType.STRING);
        setupSlot(_inverseSlot, false, ValueType.INSTANCE, _rootSlotMetaCls);
        setupSlot(_associatedSlotSlot, false, ValueType.INSTANCE, _rootSlotMetaCls);

        // create the rest of the classes
        _rootClsMetaCls = createSystemCls(Model.Cls.CLASS, true, Model.ClsID.CLASS);
        _rootFacetMetaCls = createSystemCls(Model.Cls.FACET, true, Model.ClsID.FACET);
        Cls systemCls = createSystemCls(Model.Cls.SYSTEM_CLASS, true, Model.ClsID.SYSTEM_CLASS);

        setupSlot(_associatedFacetSlot, false, ValueType.INSTANCE, _rootFacetMetaCls);
        _directSuperslotsSlot.setInverseSlot(_directSubslotsSlot);
        _associatedSlotSlot.setInverseSlot(_associatedFacetSlot);

        //
        _rootSlotMetaCls.setAbstract(true);
        standardCls.setAbstract(false);
        standardSlot.setAbstract(false);
        standardFacet.setAbstract(false);
        constraintCls.setAbstract(true);
        _rootCls.setAbstract(true);

        // setup inheritance hierarchy
        systemCls.addDirectSuperclass(_rootCls);
        _rootClsMetaCls.addDirectSuperclass(systemCls);
        standardCls.addDirectSuperclass(_rootClsMetaCls);
        _rootSlotMetaCls.addDirectSuperclass(systemCls);
        standardSlot.addDirectSuperclass(_rootSlotMetaCls);
        _rootFacetMetaCls.addDirectSuperclass(systemCls);
        standardFacet.addDirectSuperclass(_rootFacetMetaCls);
        constraintCls.addDirectSuperclass(systemCls);

        setTemplateSlotAllowedParent(standardSlot, _directTypeSlot, _rootSlotMetaCls);
        setTemplateSlotAllowedParent(standardFacet, _directTypeSlot, _rootFacetMetaCls);
        setTemplateSlotAllowedParent(standardCls, _directTypeSlot, _rootClsMetaCls);

        createPALFrames();
        createAnnotationFrames();
        createRelationFrames();
    }

    private DefaultSlot createSystemSlot(String name, Cls metaCls, FrameID frameID) {
        DefaultSlot slot = (DefaultSlot) newSlot(name, metaCls, frameID, true);
        slot.setEditable(false);
        slot.setSystem(true);
        slot.setIncluded(true);
        return slot;
    }

    private DefaultSlot createSystemSlot(String name, Facet facet, FrameID id) {
        DefaultSlot slot = createSystemSlot(name, id);
        slot.setAssociatedFacet(facet);
        facet.setAssociatedSlot(slot);
        return slot;
    }

    private DefaultSlot createSystemSlot(String name, FrameID frameID) {
        return createSystemSlot(name, _defaultSlotMetaCls, frameID);
    }

    /**
     * @deprecated
     */
    public synchronized String createUniqueFrameName(String name) {
        String uniqueName;
        if (name == null) {
            String padding;
            boolean isFirst = true;
            do {
                if (_nextFrameNumber < 10) {
                    padding = "0000";
                } else if (_nextFrameNumber < 100) {
                    padding = "000";
                } else if (_nextFrameNumber < 1000) {
                    padding = "00";
                } else if (_nextFrameNumber < 10000) {
                    padding = "0";
                } else {
                    padding = "";
                }
                uniqueName = _frameNamePrefix + padding + _nextFrameNumber;
                _nextFrameNumber += (isFirst) ? 1 : 100;
                isFirst = false;
                // Log.trace("trying name: " + uniqueName, this, "createUniqueFrameName");
            } while (containsFrame(uniqueName));
        } else {
            uniqueName = name;
            int number = 1;
            while (containsFrame(uniqueName)) {
                uniqueName = name + "_" + number;
                ++number;
            }
            if (number != 1) {
                Log.warning("duplicate name changed to: " + uniqueName, this, "createUniqueFrameName", name);
            }
        }
        return uniqueName;
    }

    public synchronized void deleteCls(Cls cls) {
        checkArgs(CLS, cls);
        journal("deleteClass", "name", cls.getName());
        if (getInstanceCount(cls) == 0) {
            internalDeleteCls(cls);
        } else {
            throw new RuntimeException("Cannot delete class " + cls.getBrowserText() + " because it has instances");
        }
    }

    private void deleteDirectInstances(Cls cls) {
        Iterator i = new ArrayList(getDirectInstances(cls)).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            // internalDeleteInstance(instance);
            deleteInstance(instance);
        }
    }

    public synchronized void deleteFacet(Facet facet) {
        checkArgs(FACET, facet);
        String facetName = facet.getName();
        journal("deleteFacet", "name", facetName);
        Collection refs = _frameManager.getReferences(facet, 0);
        _frameManager.deleteFacet(facet);
        postReferencesRemoved(refs, facet);
        postKnowledgeBaseEvent(KnowledgeBaseEvent.FACET_DELETED, facet, facetName);
        markAsDeleted(facet);
    }

    public synchronized void deleteFrame(Frame frame) {
        Assert.assertNotNull("frame", frame);
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

    public synchronized void deleteInstance(Instance instance) {
        deleteFrame(instance);
    }

    public synchronized void deleteSimpleInstance(SimpleInstance instance) {
        checkArgs(INSTANCE, instance);
        String instanceName = instance.getName();
        journal("deleteSimpleInstance", "name", instanceName);
        Collection refs = _frameManager.getReferences(instance, 0);
        _frameManager.deleteSimpleInstance(instance);
        postReferencesRemoved(refs, instance);
        postKnowledgeBaseEvent(KnowledgeBaseEvent.INSTANCE_DELETED, instance, instanceName);
        markAsDeleted(instance);
    }

    public synchronized void deleteSlot(Slot slot) {
        checkArgs(SLOT, slot);
        String slotName = slot.getName(); // needs to be cached for event
        journal("deleteSlot", "name", slotName);
        Collection refs = _frameManager.getReferences(slot, 0);
        _frameManager.deleteSlot(slot);
        postReferencesRemoved(refs, slot);
        postKnowledgeBaseEvent(KnowledgeBaseEvent.SLOT_DELETED, slot, slotName);
        markAsDeleted(slot);
    }

    public synchronized void dispose() {
        _frameManager.dispose();
        _frameManager = null;

        _clientInformation = null;
        _frameManager = null;
        _frameListeners = null;
        _clsListeners = null;
        _slotListeners = null;
        _facetListeners = null;
        _instanceListeners = null;
        _knowledgeBaseListeners = null;
    }

    public synchronized boolean endTransaction(boolean doCommit) {
        return _frameManager.endTransaction(doCommit);
    }

    public synchronized Collection getAllowedClses(Slot slot) {
        checkArgs(SLOT, slot);
        Collection c = getValueTypeValues(slot);
        return ValueTypeConstraint.getAllowedClses(c);
    }

    public synchronized Collection getAllowedParents(Slot slot) {
        checkArgs(SLOT, slot);
        Collection c = getValueTypeValues(slot);
        return ValueTypeConstraint.getAllowedParents(c);
    }

    public synchronized Collection getAllowedValues(Slot slot) {
        checkArgs(SLOT, slot);
        Collection c = getValueTypeValues(slot);
        return ValueTypeConstraint.getAllowedValues(c);
    }

    public synchronized boolean getAllowsMultipleValues(Slot slot) {
        checkArgs(SLOT, slot);
        Integer i = (Integer) internalGetOwnSlotValue(slot, _maximumCardinalitySlot);
        return (i == null) ? true : i.intValue() > 1;
    }

    public synchronized Facet getAssociatedFacet(Slot slot) {
        checkArgs(SLOT, slot);
        return (Facet) internalGetOwnSlotValue(slot, _associatedFacetSlot);
    }

    public synchronized Slot getAssociatedSlot(Facet facet) {
        checkArgs(FACET, facet);
        return (Slot) internalGetOwnSlotValue(facet, _associatedSlotSlot);
    }

    private static Slot getFirstBrowserSlot(Cls cls) {
        BrowserSlotPattern pattern = cls.getBrowserSlotPattern();
        return (pattern == null) ? null : pattern.getFirstSlot();
    }

    public synchronized String getBrowserText(Instance instance) {
        // checkArgs(INSTANCE, instance);
        String browserText = null;
        if (instance.getFrameID() == null) {
            browserText = "<deleted frame>";
        } else {
            Cls type = instance.getDirectType();
            Slot slot = (type == null) ? _nameSlot : getFirstBrowserSlot(type);
            Object value = (slot == null) ? (Object) null : internalGetOwnSlotValue(instance, slot);
            if (value == null) {
                browserText = getName(instance);
                if (browserText == null) {
                    browserText = "<newly deleted frame>";
                }
            } else if (value instanceof Instance) {
                Instance browserValue = (Instance) value;
                browserText = getBrowserText(browserValue);
            } else {
                browserText = value.toString();
            }
            Assert.assertNotNull("browser text", browserText);
        }
        return browserText;
    }

    public synchronized String getBuildString() {
        return _buildString;
    }

    public synchronized Object getClientInformation(Object key) {
        return _clientInformation.get(key);
    }

    public synchronized Cls getCls(String name) {
        Cls cls = null;
        Frame frame = getFrame(name);
        if (frame instanceof Cls) {
            cls = (Cls) frame;
        } else if (frame != null) {
            // Log.error("frame is not a class: " + frame.getClass().getName(), this, "getCls", name);
        }
        return cls;
    }

    public synchronized int getClsCount() {
        return _frameManager.getClsCount();
    }

    public synchronized Collection getClses() {
        return getInstances(_rootClsMetaCls);
    }

    public synchronized Collection getClsNameMatches(String s, int maxMatches) {
        Collection frames = getFrameNameMatches(s, maxMatches);
        Collection clses = new ArrayList();
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (frame instanceof Cls) {
                clses.add(frame);
            }
        }
        return clses;
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

    public synchronized Collection getDefaultValues(Slot slot) {
        checkArgs(SLOT, slot);
        return _frameManager.getDefaultValues(slot);
    }

    public synchronized Facet getDefaultValuesFacet() {
        return _defaultValuesFacet;
    }

    public synchronized Slot getDefaultValuesSlot() {
        return _defaultValuesSlot;
    }

    public synchronized int getDirectInstanceCount(Cls type) {
        checkArgs(CLS, type);
        return _frameManager.getDirectInstanceCount(type);
    }

    public synchronized Collection getDirectInstances(Cls type) {
        checkArgs(CLS, type);
        return _frameManager.getDirectInstances(type);
    }

    public synchronized Slot getDirectInstancesSlot() {
        return _directInstancesSlot;
    }

    public synchronized int getDirectSubclassCount(Cls parent) {
        checkArgs(CLS, parent);
        return _frameManager.getDirectSubclassCount(parent);
    }

    public synchronized Collection getDirectSubclasses(Cls parent) {
        checkArgs(CLS, parent);
        return _frameManager.getDirectSubclasses(parent);
    }

    public synchronized Slot getDirectSubclassesSlot() {
        return _directSubclassesSlot;
    }

    public synchronized int getDirectSubslotCount(Slot slot) {
        checkArgs(SLOT, slot);
        return _directSubslotsSlot == null ? 0 : getOwnSlotValueCount(slot, _directSubslotsSlot);
    }

    public synchronized Collection getDirectSubslots(Slot slot) {
        checkArgs(SLOT, slot);
        return internalGetOwnSlotValues(slot, _directSubslotsSlot);
    }

    public synchronized int getDirectSuperclassCount(Cls cls) {
        checkArgs(CLS, cls);
        return _frameManager.getDirectSuperclassCount(cls);
    }

    public synchronized Collection getDirectSuperclasses(Cls cls) {
        checkArgs(CLS, cls);
        return _frameManager.getDirectSuperclasses(cls);
    }

    public synchronized Slot getDirectSuperclassesSlot() {
        return _directSuperclassesSlot;
    }

    public synchronized Slot getDirectSuperslotsSlot() {
        return _directSuperslotsSlot;
    }

    public synchronized int getDirectSuperslotCount(Slot slot) {
        checkArgs(SLOT, slot);
        return _directSuperslotsSlot == null ? 0 : getOwnSlotValueCount(slot, _directSuperslotsSlot);
    }

    public synchronized Collection getDirectSuperslots(Slot slot) {
        return internalGetOwnSlotValues(slot, _directSuperslotsSlot);
    }

    public synchronized List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        return _frameManager.getDirectTemplateFacetValues(cls, slot, facet);
    }

    public synchronized Collection getDirectTemplateSlots(Cls cls) {
        checkArgs(CLS, cls);
        return _frameManager.getDirectTemplateSlots(cls);
    }

    public synchronized Slot getDirectTemplateSlotsSlot() {
        return _directTemplateSlotsSlot;
    }

    public synchronized List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.getDirectTemplateSlotValues(cls, slot);
    }

    public synchronized Cls getDirectType(Instance instance) {
        checkArgs(INSTANCE, instance);
        return _frameManager.getDirectType(instance);
    }

    public synchronized Collection getDirectTypes(Instance instance) {
        return CollectionUtilities.createCollection(getDirectType(instance));
    }

    public synchronized Slot getDirectTypeSlot() {
        return _directTypeSlot;
    }

    public synchronized Collection getDocumentation(Frame frame) {
        checkArgs(FRAME, frame);
        return internalGetOwnSlotValues(frame, _documentationSlot);
    }

    public synchronized Facet getFacet(String name) {
        Facet facet = null;
        Frame frame = getFrame(name);
        if (frame instanceof Facet) {
            facet = (Facet) frame;
        }
        return facet;
    }

    public synchronized int getFacetCount() {
        return _frameManager.getFacetCount();
    }

    public synchronized Collection getFacets() {
        return getInstances(_rootFacetMetaCls);
    }

    public synchronized Frame getFrame(FrameID id) {
        return _frameManager.getFrame(id);
    }

    public synchronized Frame getFrame(String name) {
        return _frameManager.getFrame(name);
    }

    public synchronized int getFrameCount() {
        return _frameManager.getFrameCount();
    }

    public synchronized String getFrameCreationTimestamp(Frame frame) {
        checkArgs(FRAME, frame);
        return (String) internalGetOwnSlotValue(frame, getSlot(Model.Slot.CREATION_TIMESTAMP));
    }

    public synchronized String getFrameCreator(Frame frame) {
        checkArgs(FRAME, frame);
        return (String) internalGetOwnSlotValue(frame, getSlot(Model.Slot.CREATOR));
    }

    public synchronized String getFrameLastModificationTimestamp(Frame frame) {
        checkArgs(FRAME, frame);
        return (String) internalGetOwnSlotValue(frame, getSlot(Model.Slot.MODIFICATION_TIMESTAMP));
    }

    public synchronized String getFrameLastModifier(Frame frame) {
        checkArgs(FRAME, frame);
        return (String) internalGetOwnSlotValue(frame, getSlot(Model.Slot.MODIFIER));
    }

    public synchronized Collection getFrameNameMatches(String s, int maxMatches) {
        if (!s.endsWith("*")) {
            s += "*";
        }
        return getMatchingFrames(_nameSlot, null, false, s, maxMatches);
    }

    public String getFrameNamePrefix() {
        return _frameNamePrefix;
    }

    public synchronized Collection getFrames() {
        return _frameManager.getFrames();
    }

    public synchronized Instance getInstance(String name) {
        return (Instance) getFrame(name);
    }

    public synchronized int getInstanceCount(Cls type) {
        checkArgs(CLS, type);
        return _frameManager.getInstanceCount(type);
    }

    public synchronized Collection getInstances() {
        return getFrames();
    }

    /**
     * @deprecated
     */
    public synchronized Collection getInstances(Cls type) {
        checkArgs(CLS, type);
        return _frameManager.getInstances(type);
    }

    public synchronized String getInvalidOwnSlotValuesText(Frame frame, Slot slot, Collection values) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE_COLLECTION, values);
        String result = null;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result == null && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.getInvalidValuesText(frame, slot, values);
        }
        return result;
    }

    public synchronized String getInvalidOwnSlotValueText(Frame frame, Slot slot, Object value) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE, value);
        String result = null;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result == null && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.getInvalidValueText(frame, slot, value);
        }
        return result;
    }

    public synchronized Slot getInverseSlot(Slot slot) {
        checkArgs(SLOT, slot);
        return (_inverseSlot == null) ? (Slot) null : (Slot) internalGetOwnSlotValue(slot, _inverseSlot);
    }

    private Class getJavaClass(String clsName) {
        Class javaClass = null;
        if (tryToLoad(clsName)) {
            Iterator i = _javaLoadPackages.iterator();
            while (i.hasNext() && javaClass == null) {
                String packageName = (String) i.next();
                String fullClsName = packageName + "." + clsName;
                javaClass = SystemUtilities.forName(fullClsName);
            }
            if (javaClass == null) {
                setFailedToLoadCls(clsName);
            }
        }
        return javaClass;
    }

    public synchronized KnowledgeBaseFactory getKnowledgeBaseFactory() {
        return _knowledgeBaseFactory;
    }

    public synchronized Collection getMatchingFrames(
        Slot slot,
        Facet facet,
        boolean isTemplate,
        String s,
        int nMatches) {
        checkArgs(SLOT, slot, FACET_OR_NULL, facet);
        return _frameManager.getMatchingFrames(slot, facet, isTemplate, s, nMatches);
    }

    public synchronized Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object value) {
        checkArgs(SLOT, slot, FACET_OR_NULL, facet);
        return _frameManager.getFramesWithValue(slot, facet, isTemplate, value);
    }

    public synchronized int getMaximumCardinality(Slot slot) {
        checkArgs(SLOT, slot);
        Integer i = (Integer) internalGetOwnSlotValue(slot, _maximumCardinalitySlot);
        return (i == null) ? MAXIMUM_CARDINALITY_UNBOUNDED : i.intValue();
    }

    public synchronized Number getMaximumValue(Slot slot) {
        checkArgs(SLOT, slot);
        return (Number) internalGetOwnSlotValue(slot, _numericMaximumSlot);
    }

    public synchronized int getMinimumCardinality(Slot slot) {
        checkArgs(SLOT, slot);
        Integer i = (Integer) internalGetOwnSlotValue(slot, _minimumCardinalitySlot);
        return (i == null) ? 0 : i.intValue();
    }

    public synchronized Number getMinimumValue(Slot slot) {
        checkArgs(SLOT, slot);
        return (Number) internalGetOwnSlotValue(slot, _numericMinimumSlot);
    }

    public synchronized String getName() {
        return _kbName;
    }

    public synchronized String getName(Frame frame) {
        // we special case this method because it is often used as part of "toString" for debugging.
        if (frame.getFrameID() == null) {
            return "<deleted frame>";
        }
        return _frameManager.getName(frame);
    }

    public synchronized Slot getNameSlot() {
        return _nameSlot;
    }

    public synchronized int getNextFrameNumber() {
        return _nextFrameNumber;
    }

    public synchronized boolean getOwnSlotAllowsMultipleValues(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        boolean allowsMultipleValues;
        Cls cls = getDirectType((Instance) frame);
        if (cls == null) {
            Log.getLogger().severe("null type: " + frame.getName());
            allowsMultipleValues = true;
        } else {
            allowsMultipleValues = getTemplateSlotAllowsMultipleValues(cls, slot);
        }
        return allowsMultipleValues;
    }

    public synchronized Collection getOwnSlotAndSubslotValues(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        return _frameManager.getOwnSlotAndSubslotValues(frame, slot);
    }

    public synchronized Collection getOwnSlotDefaultValues(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        Cls cls = ((Instance) frame).getDirectType();
        return getTemplateSlotDefaultValues(cls, slot);
    }

    public synchronized Collection getOwnSlotFacets(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        return getTemplateFacets(((Instance) frame).getDirectType(), slot);
    }

    public synchronized Collection getOwnSlotFacetValues(Frame frame, Slot slot, Facet facet) {
        checkArgs(FRAME, frame, SLOT, slot, FACET, facet);
        return _frameManager.getOwnSlotFacetValues(frame, slot, facet);
    }

    public synchronized Collection getOwnSlots(Frame frame) {
        checkArgs(FRAME, frame);
        return _frameManager.getOwnSlots(frame);
    }

    public synchronized Object getOwnSlotValue(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        return internalGetOwnSlotValue(frame, slot);
    }

    public synchronized int getOwnSlotValueCount(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        return _frameManager.getOwnSlotValueCount(frame, slot);
    }

    public synchronized List getDirectOwnSlotValues(Frame frame, Slot slot) {
        return (List) getOwnSlotValues(frame, slot);
    }

    public synchronized Collection getOwnSlotValues(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        return internalGetOwnSlotValues(frame, slot);
    }

    public synchronized ValueType getOwnSlotValueType(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        ValueType valueType;
        Cls cls = getDirectType((Instance) frame);
        if (cls == null) {
            Log.getLogger().severe("null type: " + frame.getName());
            valueType = ValueType.ANY;
        } else {
            valueType = getTemplateSlotValueType(cls, slot);
        }
        return valueType;
    }

    private Class getPrimitiveOwnSlotType(Frame frame, Slot slot) {

        return getOwnSlotValueType(frame, slot).getJavaType();
    }

    public synchronized Project getProject() {
        return _project;
    }

    public synchronized Collection getReachableSimpleInstances(Collection roots) {
        checkArgs(CLS_COLLECTION, roots);
        Collection reachableInstances = new HashSet();
        Iterator i = roots.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            addReachableSimpleInstances(instance, reachableInstances);
        }
        return reachableInstances;
    }

    public synchronized Collection getReferences(Object o, int maxReferences) {
        checkArgs(VALUE, o);
        return _frameManager.getReferences(o, maxReferences);
    }

    public Collection getMatchingReferences(String s, int maxReferences) {
        return getReferences(s, maxReferences);
    }

    public synchronized Cls getRootCls() {
        return _rootCls;
    }

    public synchronized Collection getRootClses() {
        return CollectionUtilities.createCollection(_rootCls);
    }

    public synchronized Cls getRootClsMetaCls() {
        return _rootClsMetaCls;
    }

    public synchronized Cls getRootFacetMetaCls() {
        return _rootFacetMetaCls;
    }

    public synchronized Cls getRootSlotMetaCls() {
        return _rootSlotMetaCls;
    }

    public synchronized Collection getRootSlots() {
        List result = new ArrayList();
        Iterator i = getSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (slot.getDirectSuperslots().isEmpty()) {
                result.add(slot);
            }
        }
        return result;
    }

    public synchronized Slot getSlot(String name) {
        Slot slot = null;
        Frame frame = getFrame(name);
        if (frame instanceof Slot) {
            slot = (Slot) frame;
        } else {
            // Log.warning(frame.getClass().getName() + " not a slot", this, "getSlot", name);
        }
        return slot;
    }

    public synchronized int getSlotCount() {
        return _frameManager.getSlotCount();
    }

    public synchronized Collection getSlots() {
        return getInstances(_rootSlotMetaCls);
    }

    public synchronized String getSlotValueLastModificationTimestamp(Frame frame, Slot slot, boolean isTemplate) {
        checkArgs(FRAME, frame, SLOT, slot);
        Collection c = getOwnSlotFacetValues(frame, slot, getFacet(Model.Facet.MODIFICATION_TIMESTAMP));
        return (c == null) ? (String) null : (String) CollectionUtilities.getFirstItem(c);
    }

    public synchronized String getSlotValueLastModifier(Frame frame, Slot slot, boolean isTemplate) {
        checkArgs(FRAME, frame, SLOT, slot);
        Collection c = getOwnSlotFacetValues(frame, slot, getFacet(Model.Facet.MODIFIER));
        return (c == null) ? (String) null : (String) CollectionUtilities.getFirstItem(c);
    }

    public synchronized Storage getStorage() {
        return _frameManager.getStorage();
    }

    public synchronized int getSubclassCount(Cls parent) {
        checkArgs(CLS, parent);
        return _frameManager.getSubclassCount(parent);
    }

    /**
     * @deprecated
     */
    public synchronized Collection getSubclasses(Cls parent) {
        checkArgs(CLS, parent);
        return _frameManager.getSubclasses(parent);
    }

    public synchronized Collection getSubslots(Slot slot) {
        checkArgs(SLOT, slot);
        Set subslots = new HashSet();
        getSubslots(slot, subslots);
        return subslots;
    }

    private void getSubslots(Slot slot, Set subslots) {
        Iterator i = slot.getDirectSubslots().iterator();
        while (i.hasNext()) {
            Slot subslot = (Slot) i.next();
            boolean changed = subslots.add(subslot);
            if (changed) {
                getSubslots(subslot, subslots);
            }
        }
    }

    private void getSuperslots(Slot slot, Set superslots) {
        Iterator i = slot.getDirectSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            boolean changed = superslots.add(superslot);
            if (changed) {
                getSuperslots(superslot, superslots);
            }
        }
    }

    public synchronized int getSuperclassCount(Cls child) {
        checkArgs(CLS, child);
        return _frameManager.getSuperclassCount(child);
    }

    public synchronized Collection getSuperclasses(Cls child) {
        checkArgs(CLS, child);
        return _frameManager.getSuperclasses(child);
    }

    public synchronized Collection getSuperslots(Slot slot) {
        checkArgs(SLOT, slot);
        Set superslots = new HashSet();
        getSuperslots(slot, superslots);
        return superslots;
    }

    public synchronized Collection getTemplateFacets(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.getTemplateFacets(cls, slot);
    }

    public synchronized Object getTemplateFacetValue(Cls cls, Slot slot, Facet facet) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        return _frameManager.getTemplateFacetValue(cls, slot, facet);
    }

    public synchronized Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        return _frameManager.getTemplateFacetValues(cls, slot, facet);
    }

    public synchronized Collection getTemplateSlotAllowedClses(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return ValueTypeConstraint.getAllowedClses(getValueTypeValues(cls, slot));
    }

    public synchronized Collection getTemplateSlotAllowedParents(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return ValueTypeConstraint.getAllowedClses(getValueTypeValues(cls, slot));
    }

    public synchronized Collection getTemplateSlotAllowedValues(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return ValueTypeConstraint.getAllowedValues(getValueTypeValues(cls, slot));
    }

    public synchronized boolean getTemplateSlotAllowsMultipleValues(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        Integer i = (Integer) getTemplateFacetValue(cls, slot, _maximumCardinalityFacet);
        return (i == null) ? true : i.intValue() > 1;
    }

    public synchronized Collection getDirectDomain(Slot slot) {
        checkArgs(SLOT, slot);
        Collection clses = new ArrayList();
        Iterator i = getReferences(slot, 0).iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            if (equals(ref.getSlot(), _directTemplateSlotsSlot) && ref.getFacet() == null && !ref.isTemplate()) {
                clses.add(ref.getFrame());
            }
        }
        return clses;
    }

    public synchronized Collection getDomain(Slot slot) {
        // HACK
        return getDirectDomain(slot);
    }

    public synchronized Collection getTemplateSlotDefaultValues(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.getTemplateSlotDefaultValues(cls, slot);
    }

    public synchronized Collection getTemplateSlotDocumentation(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return getTemplateFacetValues(cls, slot, _documentationFacet);
    }

    public synchronized int getTemplateSlotMaximumCardinality(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        Integer i = (Integer) getTemplateFacetValue(cls, slot, _maximumCardinalityFacet);
        return (i == null) ? MAXIMUM_CARDINALITY_UNBOUNDED : i.intValue();
    }

    public synchronized Number getTemplateSlotMaximumValue(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return (Number) getTemplateFacetValue(cls, slot, _numericMaximumFacet);
    }

    public synchronized int getTemplateSlotMinimumCardinality(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        Integer i = (Integer) getTemplateFacetValue(cls, slot, _minimumCardinalityFacet);
        return (i == null) ? 0 : i.intValue();
    }

    public synchronized Number getTemplateSlotMinimumValue(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return (Number) getTemplateFacetValue(cls, slot, _numericMinimumFacet);
    }

    public synchronized Collection getTemplateSlots(Cls cls) {
        checkArgs(CLS, cls);
        return _frameManager.getTemplateSlots(cls);
    }

    public synchronized Object getTemplateSlotValue(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.getTemplateSlotValue(cls, slot);
    }

    public synchronized Collection getTemplateSlotValues(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.getTemplateSlotValues(cls, slot);
    }

    public synchronized ValueType getTemplateSlotValueType(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return ValueTypeConstraint.getType(getValueTypeValues(cls, slot));
    }

    public synchronized Collection getUnreachableSimpleInstances(Collection roots) {
        checkArgs(CLS_COLLECTION, roots);
        Set set = new HashSet(getFrames());
        set.removeAll(getClses());
        set.removeAll(getSlots());
        set.removeAll(getFacets());
        set.removeAll(getReachableSimpleInstances(roots));
        return set;
    }

    public synchronized String getUserName() {
        if (_userName == null) {
            _userName = SystemUtilities.getUserName();
        }
        return _userName;
    }

    public synchronized Collection getValues(Slot slot) {
        checkArgs(SLOT, slot);
        return _frameManager.getValues(slot);
    }

    public synchronized Facet getValuesFacet() {
        return _valuesFacet;
    }

    public synchronized Slot getValuesSlot() {
        return _valuesSlot;
    }

    public synchronized ValueType getValueType(Slot slot) {
        checkArgs(SLOT, slot);
        Collection c = getValueTypeValues(slot);
        return ValueTypeConstraint.getType(c);
    }

    private Collection getValueTypeValues(Cls cls, Slot slot) {
        return _frameManager.getTemplateFacetValues(cls, slot, _valueTypeFacet);
    }

    private Collection getValueTypeValues(Slot slot) {
        return internalGetOwnSlotValues(slot, _valueTypeSlot);
    }

    public synchronized String getVersionString() {
        return _versionString;
    }

    public synchronized String getVersionString(String s) {
        return _versionString;
    }

    public synchronized boolean hasChanged() {
        return _hasChanged;
    }

    public synchronized boolean hasDirectlyOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        return _frameManager.hasDirectlyOverriddenTemplateFacet(cls, slot, facet);
    }

    public synchronized boolean hasDirectlyOverriddenTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.hasDirectlyOverriddenTemplateSlot(cls, slot);
    }

    public synchronized boolean hasDirectSubclass(Cls parent, Cls child) {
        checkArgs(CLS, parent, CLS, child);
        return _frameManager.hasDirectSubclass(parent, child);
    }

    public synchronized boolean hasDirectSuperclass(Cls child, Cls parent) {
        checkArgs(CLS, child, CLS, parent);
        return _frameManager.hasDirectSuperclass(child, parent);
    }

    public synchronized boolean hasDirectSuperslot(Slot slot, Slot superslot) {
        checkArgs(SLOT, slot, SLOT, superslot);
        return _frameManager.hasDirectSuperslot(slot, superslot);
    }

    public synchronized boolean hasDirectTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.hasDirectTemplateSlot(cls, slot);
    }

    public synchronized boolean hasDirectType(Instance instance, Cls type) {
        checkArgs(INSTANCE, instance, CLS, type);
        return _frameManager.hasDirectType(instance, type);
    }

    public synchronized boolean hasInheritedTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.hasInheritedTemplateSlot(cls, slot);
    }

    public synchronized boolean hasOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        return _frameManager.hasOverriddenTemplateFacet(cls, slot, facet);
    }

    public synchronized boolean hasOverriddenTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.hasOverriddenTemplateSlot(cls, slot);
    }

    public synchronized boolean hasOwnSlot(Frame frame, Slot slot) {
        checkArgs(FRAME, frame, SLOT, slot);
        return _frameManager.hasOwnSlot(frame, slot);
    }

    public synchronized boolean hasSlotValueAtSomeFrame(Slot slot) {
        checkArgs(SLOT, slot);
        boolean result = _frameManager.hasValueAtSomeFrame(slot, null, false);
        if (!result) {
            result = _frameManager.hasValueAtSomeFrame(slot, _defaultValuesFacet, true);
        }
        if (!result) {
            result = _frameManager.hasValueAtSomeFrame(slot, _valuesFacet, true);
        }
        return result;
    }

    public synchronized boolean hasSubclass(Cls parent, Cls child) {
        checkArgs(CLS, parent, CLS, child);
        return _frameManager.hasSubclass(parent, child);
    }

    public synchronized boolean hasSuperclass(Cls child, Cls superclass) {
        checkArgs(CLS, child, CLS, superclass);
        return _frameManager.hasSuperclass(child, superclass);
    }

    public synchronized boolean hasTemplateFacet(Cls cls, Slot slot, Facet facet) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        return _frameManager.hasTemplateFacet(cls, slot, facet);
    }

    public synchronized boolean hasTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        return _frameManager.hasTemplateSlot(cls, slot);
    }

    public synchronized boolean hasType(Instance instance, Cls type) {
        checkArgs(INSTANCE, instance, CLS, type);
        return _frameManager.hasType(instance, type);
    }

    private void initializeOwnSlots(Instance instance, Cls directType) {
        if (directType != null) {
            Iterator i = getTemplateSlots(directType).iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                Collection values = getTemplateSlotDefaultValues(directType, slot);
                if (!values.isEmpty()) {
                    internalSetOwnSlotValues(instance, slot, values);
                }
            }
        }
    }

    private void internalAddOwnSlotValue(Frame frame, Slot slot, Object value) {
        _frameManager.addOwnSlotValue(frame, slot, value);
        addInverseOwnSlotValue(frame, slot, value);
        postOwnSlotValueChanged(frame, slot);
    }

    private void internalAddOwnSlotValue(Frame frame, Slot slot, Object value, int index) {
        _frameManager.addOwnSlotValue(frame, slot, value, index);
        addInverseOwnSlotValue(frame, slot, value);
        postOwnSlotValueChanged(frame, slot);
    }

    private void internalDeleteCls(Cls cls) {
        String clsName = cls.getName();
        removeDirectSubclasses(cls);
        deleteDirectInstances(cls);
        if (equals(cls, _defaultClsMetaCls)) {
            Cls parent = (Cls) CollectionUtilities.getFirstItem(cls.getSuperclasses());
            setDefaultClsMetaCls(parent);
        } else if (equals(cls, _defaultSlotMetaCls)) {
            Cls parent = (Cls) CollectionUtilities.getFirstItem(cls.getSuperclasses());
            setDefaultSlotMetaCls(parent);
        } else if (equals(cls, _defaultFacetMetaCls)) {
            Cls parent = (Cls) CollectionUtilities.getFirstItem(cls.getSuperclasses());
            setDefaultFacetMetaCls(parent);
        }
        Collection refs = new ArrayList(_frameManager.getReferences(cls, 0));
        _frameManager.deleteCls(cls);
        postReferencesRemoved(refs, cls);
        postKnowledgeBaseEvent(KnowledgeBaseEvent.CLS_DELETED, cls, clsName);
        markAsDeleted(cls);
    }

    private Object internalGetOwnSlotValue(Frame frame, Slot slot) {
        Assert.assertNotNull("slot", slot);
        return _frameManager.getOwnSlotValue(frame, slot);
    }

    private Collection internalGetOwnSlotValues(Frame frame, Slot slot) {
        Assert.assertNotNull("slot", slot);
        return _frameManager.getOwnSlotValues(frame, slot);
    }

    private void internalRemoveDirectSuperclass(Cls child, Cls parent) {
        _frameManager.removeDirectSuperclass(child, parent);
        postClsEvent(child, ClsEvent.DIRECT_SUPERCLASS_REMOVED, parent);
        postClsEvent(parent, ClsEvent.DIRECT_SUBCLASS_REMOVED, child);
    }

    private void internalSetOwnSlotValue(Frame frame, Slot slot, Object value) {
        if (equals(slot, _inverseSlot)) {
            setInverseSlot((Slot) frame, (Slot) value);
        } else if (equals(slot, _directSuperclassesSlot)) {
            removeDirectSuperclasses((Cls) frame);
            addDirectSuperclass((Cls) frame, (Cls) value);
        } else {
            removeInverseOwnSlotValues(frame, slot);
            _frameManager.setOwnSlotValue(frame, slot, value);
            addInverseOwnSlotValue(frame, slot, value);
        }
        postOwnSlotValueChanged(frame, slot);
    }

    private void internalSetOwnSlotValues(Frame frame, Slot slot, Collection values) {
        if (equals(slot, _inverseSlot)) {
            setInverseSlot((Slot) frame, (Slot) CollectionUtilities.getFirstItem(values));
        } else if (equals(slot, _valueTypeSlot)) {
            setValueTypeOwnSlotValues((Slot) frame, values);
        } else if (equals(slot, _directSuperclassesSlot)) {
            removeDirectSuperclasses((Cls) frame);
            addDirectSuperclasses((Cls) frame, values);
        } else {
            removeInverseOwnSlotValues(frame, slot);
            _frameManager.setOwnSlotValues(frame, slot, values);
            addInverseOwnSlotValues(frame, slot, values);
        }
        postOwnSlotValueChanged(frame, slot);
    }

    private void internalSetTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        internalSetTemplateFacetValues(cls, slot, facet, CollectionUtilities.createCollection(value));
    }

    private void internalSetTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        if (equals(facet, _valueTypeFacet)) {
            setValueTypeFacetValues(cls, slot, values);
        } else {
            _frameManager.setTemplateFacetValues(cls, slot, facet, values);
        }
        postTemplateFacetValueChanged(cls, slot, facet);
    }

    public synchronized boolean isAbstract(Cls cls) {
        checkArgs(CLS, cls);
        return RoleConstraint.ABSTRACT.equals(internalGetOwnSlotValue(cls, _roleSlot));
    }

    public synchronized boolean isAutoUpdatingFacetValues() {
        return _project != null && _project.getUpdateModificationSlots();
    }

    public synchronized boolean isClsMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        return equals(cls, _rootClsMetaCls) || hasSuperclass(cls, _rootClsMetaCls);
    }

    public synchronized boolean isDefaultClsMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        return equals(cls, _defaultClsMetaCls);
    }

    public synchronized boolean isDefaultFacetMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        return equals(_defaultFacetMetaCls, cls);
    }

    public synchronized boolean isDefaultSlotMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        return equals(cls, _defaultSlotMetaCls);
    }

    public synchronized boolean isFacetMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        return equals(cls, _rootFacetMetaCls) || hasSuperclass(cls, _rootFacetMetaCls);
    }

    public synchronized boolean isLoading() {
        return _isLoading;
    }

    public synchronized boolean isMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        Collection superclasses = getSuperclasses(cls);
        superclasses.add(cls);
        return superclasses.contains(_rootClsMetaCls)
            || superclasses.contains(_rootSlotMetaCls)
            || superclasses.contains(_rootFacetMetaCls);
    }

    private boolean isSingleValued(Frame frame, Slot slot) {
        return equals(slot, _valueTypeSlot) ? false : !getOwnSlotAllowsMultipleValues(frame, slot);
    }

    public synchronized boolean isSlotMetaCls(Cls cls) {
        checkArgs(CLS, cls);
        return equals(cls, _rootSlotMetaCls) || (_rootSlotMetaCls != null && hasSuperclass(cls, _rootSlotMetaCls));
    }

    public synchronized boolean isValidOwnSlotValue(Frame frame, Slot slot, Object value) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE, value);
        boolean result = true;
        Iterator i = getOwnSlotFacets(frame, slot).iterator();
        while (result && i.hasNext()) {
            Facet facet = (Facet) i.next();
            result = facet.isValidValue(frame, slot, value);
        }
        return result;
    }

    private boolean isValueInheritedBySubslots(Slot slot) {
        return !(
            equals(slot, _nameSlot)
                || equals(slot, _directTypeSlot)
                || equals(slot, _directSuperslotsSlot)
                || equals(slot, _directSubslotsSlot));
    }

    private void journal(String method, String name1, Object arg1) {
        if (writeToJournal()) {
            OldJournal.enter(this, method, name1, arg1);
        }
    }

    private void journal(String method, String name1, Object arg1, String arg2Name, Object arg2) {
        if (writeToJournal()) {
            OldJournal.enter(this, method, name1, arg1, arg2Name, arg2);
        }
    }

    private void journal(
        String method,
        String name1,
        Object arg1,
        String name2,
        Object arg2,
        String name3,
        Object arg3) {
        if (writeToJournal()) {
            OldJournal.enter(this, method, name1, arg1, name2, arg2, name3, arg3);
        }
    }

    private void journal(
        String method,
        String name1,
        Object arg1,
        String name2,
        Object arg2,
        String name3,
        Object arg3,
        String name4,
        Object arg4) {
        if (writeToJournal()) {
            OldJournal.enter(this, method, name1, arg1, name2, arg2, name3, arg3, name4, arg4);
        }
    }

    private void journal(
        String method,
        String name1,
        Object arg1,
        String name2,
        Object arg2,
        String name3,
        Object arg3,
        String name4,
        Object arg4,
        String name5,
        Object arg5) {

        if (writeToJournal()) {
            OldJournal.enter(this, method, name1, arg1, name2, arg2, name3, arg3, name4, arg4, name5, arg5);
        }
    }

    private void load() {
        _frameManager = makeFrameManager();
        _isLoading = true;
        setEventsEnabled(false);
        createSystemFrames();
        _isLoading = false;
    }

    protected FrameManager makeFrameManager() {
        return new FrameManager(this);
    }

    private Collection makeCollection(Object o) {
        return CollectionUtilities.createCollection(o);
    }

    /** create an instance of the correct java class but do not put it in the kb */
    public synchronized Instance makeSimpleInstance(String clsName, FrameID id) {
        journal("makeSimpleInstance", "clsName", clsName, "frameid", id);
        Instance instance = null;
        if (clsName != null) {
            Class javaClass = getJavaClass(clsName);
            if (javaClass != null) {
                instance = createJavaClassInstance(javaClass, id);
                if (instance == null) {
                    setFailedToLoadCls(clsName);
                }
            }
        }
        if (instance == null) {
            instance = new DefaultSimpleInstance(this, id);
        }
        return instance;
    }

    private void markAsDeleted(Frame frame) {
        checkForDanglingListeners(frame);
        frame.markDeleted(true);
    }

    public synchronized void moveDirectSubclass(Cls parent, Cls child, Cls afterCls) {
        checkArgs(CLS, parent, CLS, child, CLS, afterCls);
        journal("moveDirectSubclass", "parent", parent, "child", child, "afterCls", afterCls);
        _frameManager.moveDirectSubclass(parent, child, afterCls);
        postClsEvent(parent, ClsEvent.DIRECT_SUBCLASS_MOVED, child);
    }

    public synchronized void moveDirectSuperclass(Cls child, Cls parent, Cls afterCls) {
        checkArgs(CLS, child, CLS, parent, CLS, afterCls);
        journal("moveDirectSuperclass", "child", child, "parent", parent, "afterCls", afterCls);
        _frameManager.moveDirectSuperclass(child, parent, afterCls);
        // postClsEvent(ClsEvent.DIRECT_SUPERCLASSES_MOVED, child, parent, index);
    }

    public synchronized void moveDirectTemplateSlot(Cls cls, Slot slot, int toIndex) {
        // do nothing
    }

    public synchronized void moveOwnSlotValue(Frame frame, Slot slot, int fromIndex, int toIndex) {
        checkArgs(FRAME, frame, SLOT, slot);
        journal(
            "moveOwnSlotValue",
            "frame",
            frame,
            "slot",
            slot,
            "from",
            new Integer(fromIndex),
            "to",
            new Integer(toIndex));
        _frameManager.moveOwnSlotValue(frame, slot, fromIndex, toIndex);
        postOwnSlotValueChanged(frame, slot);
    }

    public synchronized void moveTemplateFacetValue(Cls cls, Slot slot, Facet facet, int fromIndex, int toIndex) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet);
        journal(
            "moveTemplateFacetValue",
            "cls",
            cls,
            "slot",
            slot,
            "facet",
            facet,
            "from",
            new Integer(fromIndex),
            "to",
            new Integer(toIndex));
        _frameManager.moveTemplateFacetValue(cls, slot, facet, fromIndex, toIndex);
        postTemplateFacetValueChanged(cls, slot, facet);
    }

    private FrameID getValidFrameID(FrameID id) {
        if (id == null) {
            throw new UnsupportedOperationException("Legacy databases cannot be modified in Protege 2.0");
        }
        return id;
    }

    private Cls newCls(String name, Cls metaCls, FrameID id, boolean isNew) {
        id = getValidFrameID(id);
        Cls cls = new DefaultCls(this, id);
        addInstance(cls, name, metaCls, isNew);
        return cls;
    }

    private Facet newFacet(String name, Cls metaCls, FrameID id, boolean isNew) {
        id = getValidFrameID(id);
        Facet facet = new DefaultFacet(this, id);
        addInstance(facet, name, metaCls, isNew);
        return facet;
    }

    public synchronized Instance newInstance(String name, Cls cls, FrameID id, boolean isNew) {
        checkArgs(PRIMITIVE, null, CONCRETE_CLS_OR_NULL, cls);
        journal("newInstance", "name", name, "cls", cls, "id", id, "isNew", new Boolean(isNew));
        Instance instance;
        if (cls == null) {
            instance = newSimpleInstance(name, cls, id, isNew);
        } else {
            Collection superclasses = getSuperclasses(cls);
            superclasses.add(cls);
            if (superclasses.contains(_rootClsMetaCls)) {
                instance = newCls(name, cls, id, isNew);
            } else if (superclasses.contains(_rootSlotMetaCls)) {
                instance = newSlot(name, cls, id, isNew);
            } else if (superclasses.contains(_rootFacetMetaCls)) {
                instance = newFacet(name, cls, id, isNew);
            } else {
                instance = newSimpleInstance(name, cls, id, isNew);
            }
        }
        return instance;
    }

    private Instance newSimpleInstance(String name, Cls cls, FrameID id, boolean isNew) {
        // Journal.enter(this, "new-simple-instance", "name", name, "class", cls, "id", id);
        String clsName = (cls == null) ? (String) null : cls.getName();
        id = getValidFrameID(id);
        Instance instance = makeSimpleInstance(clsName, id);
        addInstance(instance, name, cls, isNew);
        return instance;
    }

    private Slot newSlot(String name, Cls metaCls, FrameID id, boolean isNew) {
        id = getValidFrameID(id);
        Slot slot = new DefaultSlot(this, id);
        addInstance(slot, name, metaCls, isNew);
        return slot;
    }

    public synchronized void notifyVisibilityChanged(Frame frame) {
        checkArgs(FRAME, frame);
        postFrameEvent(FrameEvent.VISIBILITY_CHANGED, frame, null);
    }

    protected void postBrowserTextChanged(Frame frame) {
        postFrameEvent(frame, FrameEvent.BROWSER_TEXT_CHANGED, null, null);
    }

    private void postClsEvent(Cls cls, int type, Object arg1) {
        postClsEvent(cls, type, arg1, null);
    }

    private void postClsEvent(Cls cls, int type, Object arg1, Object arg2) {
        _clsListeners.postEvent(cls, type, arg1, arg2);
        postClsEventToSubclasses(cls, type, arg1, arg2);
        _knowledgeBaseListeners.postEvent(this, type, cls, arg1, arg2);
        setChanged(true);
    }

    private void postClsEventToSubclasses(Cls cls, int type, Object arg1, Object arg2) {
        if (propagateEventToSubclases(type)) {
            Iterator i = new ArrayList(_clsListeners.getSources()).iterator();
            while (i.hasNext()) {
                Cls potentialChild = (Cls) i.next();
                if (hasSuperclass(potentialChild, cls)) {
                    _clsListeners.postEvent(potentialChild, type, arg1, arg2);
                }
            }
        }
    }

    private void postFrameEvent(int type, Frame frame, Object arg) {
        postFrameEvent(frame, type, arg, null);
        postKnowledgeBaseEvent(type, frame, arg);
    }

    private void postFrameEvent(Frame frame, int type, Object arg1, Object arg2) {
        _frameListeners.postEvent(frame, type, arg1, arg2);
        setChanged(true);
    }

    /* package */
    void postSimpleInstanceCreatedEvents(Instance instance, Cls directType) {
        postInstanceCreatedEvents(KnowledgeBaseEvent.INSTANCE_CREATED, instance, directType);
    }

    private void postInstanceCreatedEvents(int type, Instance instance, Cls directType) {
        // Log.enter(this, "postInstanceCreatedEvents", instance);
        postKnowledgeBaseEvent(type, instance);
        if (type != KnowledgeBaseEvent.INSTANCE_CREATED) {
            postKnowledgeBaseEvent(KnowledgeBaseEvent.INSTANCE_CREATED, instance);
        }
        if (directType != null) {
            postClsEvent(directType, ClsEvent.DIRECT_INSTANCE_ADDED, instance);
        }
    }

    private void postInstanceEvent(Instance instance, int type, Object arg) {
        _instanceListeners.postEvent(instance, type, arg);
        postKnowledgeBaseEvent(type, instance, arg);
    }

    private void postKnowledgeBaseEvent(int type, Frame frame) {
        postKnowledgeBaseEvent(type, frame, null);
    }

    private void postKnowledgeBaseEvent(int type, Frame frame, Object arg1) {
        postKnowledgeBaseEvent(type, frame, arg1, null);
    }

    private void postKnowledgeBaseEvent(int type, Frame frame, Object arg1, Object arg2) {
        _knowledgeBaseListeners.postEvent(this, type, frame, arg1, arg2);
        setChanged(true);
    }

    protected void postOwnSlotValueChanged(Frame frame, Slot slot) {
        if (_frameListeners.hasListeners(frame)) {
            postFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, slot);
            if (equals(slot, getFirstBrowserSlot(((Instance) frame).getDirectType()))) {
                postBrowserTextChanged(frame);
            }
        }
        if (frame instanceof Slot) {
            Slot slotFrame = (Slot) frame;
            Facet facet = slot.getAssociatedFacet();
            if (facet != null) {
                Iterator i = slotFrame.getDirectDomain().iterator();
                while (i.hasNext()) {
                    Cls cls = (Cls) i.next();
                    postTemplateFacetValueChanged(cls, slotFrame, facet);
                }
            }
        }
    }

    private void postReferencesRemoved(Collection refs, Instance instance) {
        Iterator i = refs.iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            postRemoveReferenceEvents(instance, ref);
        }
    }

    private void postRemoveReferenceEvents(Instance instance, Reference ref) {
        Frame frame = ref.getFrame();
        Slot slot = ref.getSlot();
        Facet facet = ref.getFacet();
        boolean isTemplate = ref.isTemplate();
        if (facet == null) {
            if (isTemplate) {
                postClsEvent((Cls) frame, ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED, slot);
            } else {
                postFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, slot);
                if (frame instanceof Cls) {
                    postSpecialClsRemoveEvents((Cls) frame, slot, instance);
                }
            }
        } else {
            postClsEvent((Cls) frame, ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, slot, facet);
        }
    }

    private void postSpecialClsRemoveEvents(Cls cls, Slot slot, Instance instance) {
        if (equals(slot, _directSuperclassesSlot)) {
            postClsEvent(cls, ClsEvent.DIRECT_SUPERCLASS_REMOVED, instance);
        } else if (equals(slot, _directSubclassesSlot)) {
            postClsEvent(cls, ClsEvent.DIRECT_SUBCLASS_REMOVED, instance);
        } else if (equals(slot, _directInstancesSlot)) {
            postClsEvent(cls, ClsEvent.DIRECT_INSTANCE_REMOVED, instance);
        } else if (equals(slot, _directTemplateSlotsSlot)) {
            postClsEvent(cls, ClsEvent.TEMPLATE_SLOT_REMOVED, instance);
        }
    }

    private void postTemplateFacetValueChanged(Cls cls, Slot slot, Facet facet) {
        // Log.enter(this, "postTemplateFacetValueChanged", cls, slot, facet);
        postClsEvent(cls, ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, slot, facet);
    }

    private void postTemplateSlotValueChanged(Cls cls, Slot slot) {
        postClsEvent(cls, ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED, slot);
    }

    private static boolean propagateEventToSubclases(int type) {
        return type == ClsEvent.TEMPLATE_SLOT_ADDED
            || type == ClsEvent.TEMPLATE_SLOT_REMOVED
            || type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED
            || type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED
            || type == ClsEvent.TEMPLATE_FACET_ADDED
            || type == ClsEvent.TEMPLATE_FACET_REMOVED;
    }

    private void removeBadTypeValues(Slot slot, Cls cls) {
        _frameManager.removeValues(slot, null, false, cls);
        _frameManager.removeValues(slot, _defaultValuesFacet, true, cls);
        _frameManager.removeValues(slot, _valuesFacet, true, cls);
    }

    public synchronized void removeClsListener(Cls cls, ClsListener listener) {
        checkArgs(FRAME_OR_DELETED_FRAME, cls, LISTENER, listener);
        _clsListeners.remove(cls, listener);
    }

    private void removeDirectSubclasses(Cls cls) {
        Iterator i = new ArrayList(getDirectSubclasses(cls)).iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            if (subclass.getDirectSuperclassCount() == 1) {
                internalDeleteCls(subclass);
            } else {
                internalRemoveDirectSuperclass(subclass, cls);
            }
        }
    }

    public synchronized void removeDirectSuperclass(Cls child, Cls parent) {
        checkArgs(CLS, child, CLS, parent);
        journal("removeDirectSuperclass", "child", child, "parent", parent);
        internalRemoveDirectSuperclass(child, parent);
    }

    private void removeDirectSuperclasses(Cls child) {
        Iterator i = new ArrayList(child.getDirectSuperclasses()).iterator();
        while (i.hasNext()) {
            Cls parent = (Cls) i.next();
            removeDirectSuperclass(child, parent);
        }
    }

    public synchronized void removeDirectSuperslot(Slot slot, Slot superSlot) {
        checkArgs(SLOT, slot, SLOT, superSlot);
        journal("removeDirectSuperslot", "slot", slot, "superSlot", superSlot);
        _frameManager.removeOwnSlotValue(slot, _directSuperslotsSlot, superSlot);
    }

    public synchronized void removeDirectTemplateSlot(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        journal("removeDirectTemplateSlot", "cls", cls, "slot", slot);
        _frameManager.removeDirectTemplateSlot(cls, slot);
        postClsEvent(cls, ClsEvent.TEMPLATE_SLOT_REMOVED, slot);
    }

    public synchronized void removeFacetListener(Facet facet, FacetListener listener) {
        checkArgs(FRAME_OR_DELETED_FRAME, facet, LISTENER, listener);
        _facetListeners.remove(facet, listener);
    }

    public synchronized void removeFrameListener(Frame frame, FrameListener listener) {
        checkArgs(FRAME_OR_DELETED_FRAME, frame, LISTENER, listener);
        _frameListeners.remove(frame, listener);
    }

    public synchronized void removeInstanceListener(Instance instance, InstanceListener listener) {
        checkArgs(FRAME_OR_DELETED_FRAME, instance, LISTENER, listener);
        _instanceListeners.remove(instance, listener);
    }

    private void removeInverseOwnSlotValue(Frame frame, Slot slot, Object value) {
        if (!_isLoading) {
            Slot inverseSlot = slot.getInverseSlot();
            if (inverseSlot != null && value instanceof Instance) {
                Instance instance = (Instance) value;
                removeSingleOwnSlotValue(instance, inverseSlot, frame);
                postFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, instance, inverseSlot);
            }
        }
    }

    private void removeInverseOwnSlotValues(Frame frame, Slot slot) {
        if (!_isLoading) {
            Slot inverseSlot = slot.getInverseSlot();
            if (inverseSlot != null) {
                Collection values = new ArrayList(frame.getOwnSlotValues(slot));
                Iterator i = values.iterator();
                while (i.hasNext()) {
                    Instance value = (Instance) i.next();
                    removeSingleOwnSlotValue(value, inverseSlot, frame);
                    postFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, value, inverseSlot);
                }
            }
        }
    }

    private void removeInverseSlot(Slot slot) {
        Slot inverseSlot = getInverseSlot(slot);
        if (inverseSlot != null) {
            _frameManager.setOwnSlotValue(slot, _inverseSlot, null);
            _frameManager.setOwnSlotValue(inverseSlot, _inverseSlot, null);
            postOwnSlotValueChanged(slot, _inverseSlot);
            postOwnSlotValueChanged(inverseSlot, _inverseSlot);
        }
    }

    public synchronized void removeJavaLoadPackage(String packageName) {
        checkArgs(STRING, packageName);
        _javaLoadPackages.remove(packageName);
    }

    public synchronized void removeKnowledgeBaseListener(KnowledgeBaseListener listener) {
        checkArgs(LISTENER, listener);
        _knowledgeBaseListeners.remove(this, listener);
    }

    /** remove all occurances of value */
    public synchronized void removeOwnSlotValue(Frame frame, Slot slot, Object value) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE, value);
        journal("removeOwnSlotValue", "frame", frame, "slot", slot, "value", value);
        // should actually remove inverses for all occurances of value
        removeInverseOwnSlotValue(frame, slot, value);
        _frameManager.removeOwnSlotValue(frame, slot, value);
    }

    private void removeSingleOwnSlotValue(Frame frame, Slot slot, Object value) {
        // Log.enter(this, "removeSingleOwnSlotValue", frame, slot, value);
        Collection values = new ArrayList(_frameManager.getOwnSlotValues(frame, slot));
        boolean changed = values.remove(value);
        if (!changed) {
            Log.getLogger().warning("value not found");
        }
        _frameManager.setOwnSlotValues(frame, slot, values);
    }

    public synchronized void removeSlotListener(Slot slot, SlotListener listener) {
        checkArgs(FRAME_OR_DELETED_FRAME, slot, LISTENER, listener);
        _slotListeners.remove(slot, listener);
    }

    public synchronized void removeTemplateFacetOverrides(Cls cls, Slot slot) {
        checkArgs(CLS, cls, SLOT, slot);
        journal("removeTemplateFacetOverrides", "cls", cls, "slot", slot);
        _frameManager.removeTemplateFacetValueOverrides(cls, slot);
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            postClsEvent(cls, ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, slot, facet);
        }
    }

    public synchronized void setAbstract(Cls cls, boolean isAbstract) {
        checkArgs(CLS, cls);
        journal("setAbstract", "cls", cls, "isAbstract", new Boolean(isAbstract));
        internalSetOwnSlotValue(cls, _roleSlot, (isAbstract) ? RoleConstraint.ABSTRACT : RoleConstraint.CONCRETE);
    }

    public synchronized void setAllowedClses(Slot slot, Collection clses) {
        checkArgs(SLOT, slot, CLS_COLLECTION, clses);
        journal("setAllowedClses", "slot", slot, " clses", clses);
        setValueTypeValues(slot, ValueType.INSTANCE, clses);
    }

    public synchronized void setAllowedParents(Slot slot, Collection parents) {
        checkArgs(SLOT, slot, CLS_COLLECTION, parents);
        journal("setAllowedParents", "slot", slot, " parents", parents);
        setValueTypeValues(slot, ValueType.CLS, parents);
    }

    public synchronized void setAllowedValues(Slot slot, Collection values) {
        checkArgs(SLOT, slot, STRING_COLLECTION, values);
        journal("setAllowedValues", "slot", slot, " values", values);
        setValueTypeValues(slot, ValueType.SYMBOL, values);
    }

    public synchronized void setAllowsMultipleValues(Slot slot, boolean b) {
        checkArgs(SLOT, slot);
        journal("setAllowsMultipleValues", "slot", slot, "value", new Boolean(b));
        Integer i = (b) ? (Integer) null : new Integer(1);
        internalSetOwnSlotValue(slot, _maximumCardinalitySlot, i);
    }

    public synchronized void setAssociatedFacet(Slot slot, Facet facet) {
        checkArgs(SLOT, slot, FACET, facet);
        journal("setAssociatedFacet", "slot", slot, "facet", facet);
        internalSetOwnSlotValue(slot, _associatedFacetSlot, facet);
    }

    public synchronized void setAssociatedSlot(Facet facet, Slot slot) {
        checkArgs(FACET, facet, SLOT, slot);
        journal("setAssociatedSlot", "facet", facet, "slot", slot);
        internalSetOwnSlotValue(facet, _associatedSlotSlot, slot);
    }

    public synchronized void setAutoUpdateFacetValues(boolean b) {
        _project.setUpdateModificationSlots(b);
    }

    public synchronized void setBuildString(String s) {
        checkArgs(STRING, s);
        _buildString = s;
    }

    public synchronized void setChanged(boolean b) {
        if (_frameListeners.isPostingEnabled()) {
            // Log.trace("changed", this, "setChanged", new Boolean(b));
        }
        _hasChanged = b;
    }

    public synchronized Object setClientInformation(Object key, Object value) {
        // checkArgs(OBJECT, key, OBJECT_OR_NULL, value);
        return _clientInformation.put(key, value);
    }

    public synchronized void setDefaultClsMetaCls(Cls cls) {
        checkArgs(CONCRETE_CLS_OR_NULL, cls);
        journal("setDefaultClsMetaCls", "cls", cls);
        Cls oldMetaCls = _defaultClsMetaCls;
        _defaultClsMetaCls = cls;
        postKnowledgeBaseEvent(KnowledgeBaseEvent.DEFAULT_CLS_METACLASS_CHANGED, cls, oldMetaCls);
    }

    public synchronized void setDefaultFacetMetaCls(Cls cls) {
        checkArgs(CONCRETE_CLS_OR_NULL, cls);
        journal("setDefaultFacetMetaCls", "cls", cls);
        Cls oldMetaCls = _defaultFacetMetaCls;
        _defaultFacetMetaCls = cls;
        postKnowledgeBaseEvent(KnowledgeBaseEvent.DEFAULT_FACET_METACLASS_CHANGED, cls, oldMetaCls);
    }

    public synchronized void setDefaultSlotMetaCls(Cls cls) {
        checkArgs(CONCRETE_CLS_OR_NULL, cls);
        journal("setDefaultSlotMetaCls", "cls", cls);
        Cls oldMetaCls = _defaultSlotMetaCls;
        _defaultSlotMetaCls = cls;
        postKnowledgeBaseEvent(KnowledgeBaseEvent.DEFAULT_SLOT_METACLASS_CHANGED, cls, oldMetaCls);
    }

    public synchronized void setDefaultValues(Slot slot, Collection values) {
        checkArgs(SLOT, slot, VALUE_COLLECTION, values);
        journal("setDefaultValues", "frame", slot, "values", values);
        _frameManager.setDefaultValues(slot, values);
    }

    public synchronized Instance setDirectType(Instance instance, Cls type) {
        checkArgs(INSTANCE, instance, CLS, type);
        journal("setDirectType", "instance", instance, "type", type);
        Cls oldType = instance.getDirectType();
        Class javaClass = getJavaClass(type.getName());
        if (javaClass == null) {
            _frameManager.setDirectType(instance, type);
        } else {
            // Log.trace("swaping instance", this, "setDirectType", instance, type);
            Instance newInstance = createJavaClassInstance(javaClass, null);
            _frameManager.swapInstance(instance, newInstance);
            _frameManager.setDirectType(newInstance, type);
            instance = newInstance;
        }
        postInstanceEvent(instance, InstanceEvent.DIRECT_TYPE_ADDED, type);
        postInstanceEvent(instance, InstanceEvent.DIRECT_TYPE_REMOVED, oldType);
        if (oldType != null) {
            postClsEvent(oldType, ClsEvent.DIRECT_INSTANCE_REMOVED, instance);
        }
        postClsEvent(type, ClsEvent.DIRECT_INSTANCE_ADDED, instance);
        return instance;
    }

    private void setDirectType(Collection instances, Cls metaCls) {
        Iterator i = instances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!equals(instance.getDirectType(), metaCls)) {
                setDirectType(instance, metaCls);
            }
        }
    }

    public synchronized void setDirectTypeOfSubclasses(Cls cls, Cls metaCls) {
        checkArgs(CLS, cls, CLS, metaCls);
        setDirectType(cls.getSubclasses(), metaCls);
    }

    public synchronized void setDirectTypeOfSubslots(Slot slot, Cls cls) {
        checkArgs(SLOT, slot, CLS, cls);
        setDirectType(slot.getSubslots(), cls);
    }

    public synchronized void setDocumentation(Frame frame, String doc) {
        checkArgs(FRAME, frame, STRING_OR_NULL, doc);
        journal("setDocumentation", "frame", frame, "doc", doc);
        internalSetOwnSlotValue(frame, _documentationSlot, doc);
    }

    public synchronized void setDocumentation(Frame frame, Collection docs) {
        checkArgs(FRAME, frame, STRING_COLLECTION, docs);
        journal("setDocumentation", "frame", frame, "docs", docs);
        internalSetOwnSlotValues(frame, _documentationSlot, docs);
    }

    /**
     * @deprecated
     */
    public synchronized boolean setEventsEnabled(boolean enabled) {
        // Log.enter(this, "setEventsEnabled", new Boolean(enabled));
        boolean wasEnabled = _frameListeners.setPostingEnabled(enabled);
        _clsListeners.setPostingEnabled(enabled);
        _slotListeners.setPostingEnabled(enabled);
        _facetListeners.setPostingEnabled(enabled);
        _instanceListeners.setPostingEnabled(enabled);
        _knowledgeBaseListeners.setPostingEnabled(enabled);
        return wasEnabled;
    }

    private void setFailedToLoadCls(String clsName) {
        // Log.enter(this, "setFailedToLoadCls", cls);
        _failedClsLoads.add(clsName);
    }

    public void setFrameNamePrefix(String prefix) {
        _frameNamePrefix = prefix;
    }

    public synchronized void setInverseSlot(Slot slot, Slot inverseSlot) {
        checkArgs(SLOT, slot, SLOT_OR_NULL, inverseSlot);
        journal("setInverseSlot", "slot", slot, "inverseSlot", inverseSlot);
        removeInverseSlot(slot);
        if (inverseSlot != null) {
            removeInverseSlot(inverseSlot);
            addInverseSlotRelationship(slot, inverseSlot);
        }
    }

    public synchronized void setLoading(boolean loading) {
        _isLoading = loading;
    }

    public synchronized void setMaximumCardinality(Slot slot, int max) {
        checkArgs(SLOT, slot);
        journal("setMaximumCardinality", "slot", slot, "max", new Integer(max));
        Integer i = (max == MAXIMUM_CARDINALITY_UNBOUNDED) ? null : new Integer(max);
        internalSetOwnSlotValue(slot, _maximumCardinalitySlot, i);
    }

    public synchronized void setMaximumValue(Slot slot, Number min) {
        checkArgs(SLOT, slot);
        journal("setMaximumValue", "slot", slot, "min", min);
        internalSetOwnSlotValue(slot, _numericMaximumSlot, min);
    }

    public synchronized void setMinimumCardinality(Slot slot, int min) {
        checkArgs(SLOT, slot);
        journal("setMaximumValue", "slot", slot, "min", new Integer(min));
        Integer i = (min == 0) ? (Integer) null : new Integer(min);
        internalSetOwnSlotValue(slot, _minimumCardinalitySlot, i);
    }

    public synchronized void setMinimumValue(Slot slot, Number min) {
        checkArgs(SLOT, slot);
        journal("setMinimumValue", "slot", slot, "min", min);
        internalSetOwnSlotValue(slot, _numericMinimumSlot, min);
    }

    public synchronized void setName(String name) {
        _kbName = name;
        updateFrameNamePrefix();
    }

    public synchronized void setNextFrameNumber(int i) {
        _nextFrameNumber = i;
    }

    public synchronized void setOwnSlotValue(Frame frame, Slot slot, Object value) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE_OR_NULL, value);
        journal("setOwnSlotValue", "frame", frame, "slot", slot, "value", value);
        if (value != null) {
            if (_slotValueFacetChecking) {
                checkAllFacets(frame, slot, Collections.singleton(value));
            } else {
                checkPrimitiveType(frame, slot, value);
            }
        }
        internalSetOwnSlotValue(frame, slot, value);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        setOwnSlotValues(frame, slot, values);
    }

    public synchronized void setOwnSlotValues(Frame frame, Slot slot, Collection values) {
        checkArgs(FRAME, frame, SLOT, slot, VALUE_COLLECTION, values);
        journal("setOwnSlotValues", "frame", frame, "slot", slot, "values", values);
        if (_slotValueFacetChecking) {
            checkAllFacets(frame, slot, values);
        } else {
            checkPrimitiveType(frame, slot, values);
        }
        internalSetOwnSlotValues(frame, slot, values);
    }

    public synchronized void setProject(Project project) {
        _project = project;
    }

    /**
     * Checks every call that changes an own slot value that the new value
     * is consistent with all facets.  This checking is disabled by default.  It can significately slow
     * down the system but is useful for tracking down bugs in code that calls the api (both system
     * and user code).
     */
    public synchronized void setSlotValueFacetChecking(boolean b) {
        _slotValueFacetChecking = b;
    }

    public synchronized boolean getSlotValueFacetChecking() {
        return _slotValueFacetChecking;
    }

    public synchronized void setStorage(Storage storage) {
        _frameManager.setStorage(storage);
    }

    private void setSuperslots(Slot slot, Collection superslots) {
        if (!superslots.isEmpty()) {
            internalSetOwnSlotValues(slot, _directSuperslotsSlot, superslots);
            Slot prototypeSuperslot = (Slot) CollectionUtilities.getFirstItem(superslots);
            Iterator i = prototypeSuperslot.getOwnSlots().iterator();
            while (i.hasNext()) {
                Slot ownSlot = (Slot) i.next();
                if (isValueInheritedBySubslots(ownSlot)) {
                    internalSetOwnSlotValues(slot, ownSlot, prototypeSuperslot.getOwnSlotValues(ownSlot));
                }
            }
        }
    }

    public synchronized void setTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet, VALUE_OR_NULL, value);
        journal("setTemplateFacetValue", "cls", cls, "slot", slot, "facet", facet, "value", value);
        internalSetTemplateFacetValue(cls, slot, facet, value);
    }

    public synchronized void setTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        checkArgs(CLS, cls, SLOT, slot, FACET, facet, VALUE_COLLECTION, values);
        journal("setTemplateFacetValues", "cls", cls, "slot", slot, "facet", facet, "values", values);
        internalSetTemplateFacetValues(cls, slot, facet, values);
    }

    public synchronized void setTemplateSlotAllowedClses(Cls cls, Slot slot, Collection clses) {
        checkArgs(CLS, cls, SLOT, slot, CLS_COLLECTION, clses);
        journal("setTemplateSlotAllowedClses", "cls", cls, "slot", slot, "clses", clses);
        checkPrimitiveType(Cls.class, clses);
        setValueTypeValues(cls, slot, ValueType.INSTANCE, clses);
    }

    private void setTemplateSlotAllowedParent(Cls cls, Slot slot, Cls allowedParent) {
        Assert.assertNotNull("cls", cls);
        Assert.assertNotNull("slot", slot);
        Assert.assertNotNull("allowed parent", allowedParent);
        setTemplateSlotAllowedParents(cls, slot, Collections.singleton(allowedParent));
    }

    public synchronized void setTemplateSlotAllowedParents(Cls cls, Slot slot, Collection clses) {
        checkArgs(CLS, cls, SLOT, slot, CLS_COLLECTION, clses);
        journal("setTemplateSlotAllowedParents", "cls", cls, "slot", slot, "clses", clses);
        checkPrimitiveType(Cls.class, clses);
        setValueTypeValues(cls, slot, ValueType.CLS, clses);
    }

    public synchronized void setTemplateSlotAllowedValues(Cls cls, Slot slot, Collection values) {
        checkArgs(CLS, cls, SLOT, slot, STRING_COLLECTION, values);
        journal("setTemplateSlotAllowedValues", "cls", cls, "slot", slot, "values", values);
        checkPrimitiveType(String.class, values);
        setValueTypeValues(cls, slot, ValueType.SYMBOL, values);
    }

    public synchronized void setTemplateSlotAllowsMultipleValues(Cls cls, Slot slot, boolean allowed) {
        checkArgs(CLS, cls, SLOT, slot);
        journal("setTemplateSlotAllowsMultipleValues", "cls", cls, "slot", slot, "allowed", new Boolean(allowed));
        Integer value = (allowed) ? (Integer) null : new Integer(1);
        internalSetTemplateFacetValue(cls, slot, _maximumCardinalityFacet, value);
    }

    public synchronized void setTemplateSlotDefaultValues(Cls cls, Slot slot, Collection values) {
        checkArgs(CLS, cls, SLOT, slot, VALUE_COLLECTION, values);
        journal("setTemplateSlotDefaultValues", "cls", cls, "slot", slot, "values", values);
        _frameManager.setTemplateSlotDefaultValues(cls, slot, values);
        postClsEvent(cls, ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, slot, _defaultValuesFacet);
    }

    public synchronized void setTemplateSlotDocumentation(Cls cls, Slot slot, String doc) {
        checkArgs(CLS, cls, SLOT, slot, VALUE_OR_NULL, doc);
        journal("setTemplateSlotDocumentation", "cls", cls, "slot", slot, "doc", doc);
        internalSetTemplateFacetValue(cls, slot, _documentationFacet, doc);
    }

    public synchronized void setTemplateSlotDocumentation(Cls cls, Slot slot, Collection doc) {
        checkArgs(CLS, cls, SLOT, slot, STRING_COLLECTION, doc);
        journal("setTemplateSlotDocumentation", "cls", cls, "slot", slot, "doc", doc);
        checkPrimitiveType(String.class, doc);
        internalSetTemplateFacetValues(cls, slot, _documentationFacet, doc);
    }

    public synchronized void setTemplateSlotMaximumCardinality(Cls cls, Slot slot, int max) {
        // temporary hack
        if (max == 0) {
            Log.getLogger().warning("max card=0");
        }
        checkArgs(CLS, cls, SLOT, slot);
        journal("setTemplateSlotMaximumCardinality", "cls", cls, "slot", slot, "max", new Integer(max));
        Integer value = (max == MAXIMUM_CARDINALITY_UNBOUNDED) ? null : new Integer(max);
        internalSetTemplateFacetValue(cls, slot, _maximumCardinalityFacet, value);
    }

    public synchronized void setTemplateSlotMaximumValue(Cls cls, Slot slot, Number value) {
        checkArgs(CLS, cls, SLOT, slot);
        journal("setTemplateSlotMaximumValue", "cls", cls, "slot", slot, "value", value);
        internalSetTemplateFacetValue(cls, slot, _numericMaximumFacet, value);
    }

    public synchronized void setTemplateSlotMinimumCardinality(Cls cls, Slot slot, int min) {
        checkArgs(CLS, cls, SLOT, slot);
        journal("setTemplateSlotMinimumCardinality", "cls", cls, "slot", slot, "min", new Integer(min));
        Integer value = (min == 0) ? null : new Integer(min);
        internalSetTemplateFacetValue(cls, slot, _minimumCardinalityFacet, value);
    }

    public synchronized void setTemplateSlotMinimumValue(Cls cls, Slot slot, Number value) {
        checkArgs(CLS, cls, SLOT, slot);
        journal("setTemplateSlotMinimumValue", "cls", cls, "slot", slot, "value", value);
        internalSetTemplateFacetValue(cls, slot, _numericMinimumFacet, value);
    }

    public synchronized void setTemplateSlotValue(Cls cls, Slot slot, Object value) {
        checkArgs(CLS, cls, SLOT, slot, VALUE_OR_NULL, value);
        journal("setTemplateSlotValue", "cls", cls, "slot", slot, "value", value);
        _frameManager.setTemplateSlotValue(cls, slot, value);
        postTemplateSlotValueChanged(cls, slot);
    }

    public synchronized void setTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        checkArgs(CLS, cls, SLOT, slot, VALUE_COLLECTION, values);
        journal("setTemplateSlotValues", "cls", cls, "slot", slot, "values", values);
        _frameManager.setTemplateSlotValues(cls, slot, values);
        postTemplateSlotValueChanged(cls, slot);
    }

    public synchronized void setTemplateSlotValueType(Cls cls, Slot slot, ValueType type) {
        checkArgs(CLS, cls, SLOT, slot, VALUE_TYPE, type);
        journal("setTemplateSlotValueType", "cls", cls, "slot", slot, "type", type);
        setValueTypeValues(cls, slot, type, null);
    }

    private void setupSlot(Slot slot, boolean allowsMultiple) {
        slot.setAllowsMultipleValues(allowsMultiple);
    }

    private void setupSlot(Slot slot, boolean allowsMultiple, ValueType type) {
        setupSlot(slot, allowsMultiple);
        slot.setValueType(type);
    }

    private void setupSlot(Slot slot, boolean allowsMultiple, ValueType type, Object value) {
        setupSlot(slot, allowsMultiple);
        Collection values = CollectionUtilities.createCollection(value);
        if (equals(type, ValueType.INSTANCE)) {
            slot.setAllowedClses(values);
        } else if (equals(type, ValueType.CLS)) {
            slot.setAllowedParents(values);
        } else {
            Log.getLogger().warning("unexpected type: " + type);
        }
    }

    private void setupSlot(Slot slot, boolean allowsMultiple, ValueType type, Collection values) {
        setupSlot(slot, allowsMultiple);
        slot.setAllowedValues(values);
    }

    public synchronized void setUserName(String name) {
        _userName = name;
    }

    /**
     * @deprecated Instead use either #setSlotValueFacetChecking
     */
    public synchronized void setValueChecking(boolean b) {
        setSlotValueFacetChecking(b);
    }

    public synchronized boolean getValueChecking() {
        return _slotValueFacetChecking;
    }

    public synchronized void setValues(Slot slot, Collection values) {
        checkArgs(SLOT, slot, VALUE_COLLECTION, values);
        journal("setValues", "slot", slot, "values", values);
        _frameManager.setValues(slot, values);
    }

    public synchronized void setValueType(Slot slot, ValueType valueType) {
        checkArgs(SLOT, slot, VALUE_TYPE, valueType);
        journal("setValueType", "slot", slot, "valueType", valueType);
        setValueTypeValues(slot, valueType, null);
    }

    public synchronized void setValueTypeFacetValues(Cls cls, Slot slot, Collection values) {
        checkArgs(CLS, cls, SLOT, slot, VALUE_COLLECTION, values);
        journal("setValueTypeFacetValues", "cls", cls, "slot", slot, "values", values);
        ValueType oldType = cls.getTemplateSlotValueType(slot);
        _frameManager.setTemplateFacetValues(cls, slot, _valueTypeFacet, values);
        if (!_isLoading) {
            ValueType newType = cls.getTemplateSlotValueType(slot);
            if (!equals(oldType, newType)) {
                removeBadTypeValues(slot, cls);
            }
        }
    }

    public synchronized void setValueTypeOwnSlotValues(Slot slot, Collection values) {
        checkArgs(SLOT, slot, VALUE_COLLECTION, values);
        journal("setValueTypeOwnSlotValues", "slot", slot, "values", values);
        ValueType oldType = null;
        if (!_isLoading) {
            oldType = getValueType(slot);
        }
        _frameManager.setOwnSlotValues(slot, _valueTypeSlot, values);
        if (!_isLoading) {
            ValueType newType = getValueType(slot);
            if (!equals(oldType, newType)) {
                removeBadTypeValues(slot, null);
                setOwnSlotValue(slot, _defaultValuesSlot, null);
                setOwnSlotValue(slot, _valuesSlot, null);
            }
        }
    }

    private void setValueTypeValues(Cls cls, Slot slot, ValueType type, Collection values) {
        Collection valueTypeValues = ValueTypeConstraint.getValues(type, values);
        internalSetTemplateFacetValues(cls, slot, _valueTypeFacet, valueTypeValues);
    }

    private void setValueTypeValues(Slot slot, ValueType type, Collection c) {
        Collection values = ValueTypeConstraint.getValues(type, c);
        internalSetOwnSlotValues(slot, _valueTypeSlot, values);
    }

    public synchronized void setVersionString(String s) {
        _versionString = s;
    }

    public synchronized String toString() {
        return "KB(" + getName() + ")";
    }

    private boolean tryToLoad(String clsName) {
        return clsName != null && !_javaLoadPackages.isEmpty() && !_failedClsLoads.contains(clsName);
    }

    private void updateFrameNamePrefix() {
        _frameNamePrefix = _kbName + "_";
        // If the project has no real name yet then we generate a random prefix so that
        // it is unlikely the instance names will collide if projects are ever included.
        if (_kbName.equals("KB") || _kbName.equals("ProjectKB")) {
            _frameNamePrefix += String.valueOf(Math.random()).substring(2, 6) + "_";
        }
    }

    private boolean writeToJournal() {
        // This is a hack.  We don't want to write project kb changes to the journal but
        // this is a kludgy way to tell if we are the project kb.
        return _project != null;
    }

    protected FrameManager getFrameManager() {
        return _frameManager;
    }

    public Collection getCurrentUsers() {
        return CollectionUtilities.createCollection(getUserName());
    }

    /**
     * @deprecated
     */
    public synchronized boolean getEventsEnabled() {
        return _frameListeners.isPostingEnabled();
    }

    public void addDirectType(Instance instance, Cls type) {
        throw new UnsupportedOperationException();
    }

    public void removeDirectType(Instance instance, Cls type) {
        throw new UnsupportedOperationException();
    }
    public CommandManager getCommandManager() {
        return null;
        // throw new UnsupportedOperationException();
    }
    public boolean setUndoEnabled(boolean b) {
        // do nothing
        // throw new UnsupportedOperationException();
        return false;
    }
    public void addClsListener(ClsListener listener) {
    }
    public void removeClsListener(ClsListener listener) {
    }
    public void addFacetListener(FacetListener listener) {
    }
    public void removeFacetListener(FacetListener listener) {
    }
    public void addFrameListener(FrameListener listener) {
    }
    public void removeFrameListener(FrameListener listener) {
    }
    public void addInstanceListener(InstanceListener listener) {
    }
    public void removeInstanceListener(InstanceListener listener) {
    }
    public void addSlotListener(SlotListener listener) {
    }
    public void removeSlotListener(SlotListener listener) {
    }

    public boolean isValidFrameName(String s, Frame frame) {
        return true;
    }

    public String getInvalidFrameNameDescription(String s, Frame frame) {
        throw new UnsupportedOperationException();
    }
    public void setFrameNameValidator(FrameNameValidator validator) {
        throw new UnsupportedOperationException();
    }

    public boolean getGenerateEventsEnabled() {
        throw new UnsupportedOperationException();
    }

    public boolean setGenerateEventsEnabled(boolean b) {
        return setEventsEnabled(b);
        // throw new UnsupportedOperationException();
    }

    public boolean getDispatchEventsEnabled() {
        throw new UnsupportedOperationException();
    }

    public boolean setDispatchEventsEnabled(boolean b) {
        throw new UnsupportedOperationException();
    }
    public void setPollForEvents(boolean b) {
        throw new UnsupportedOperationException();
    }
    public void setFrameFactory(FrameFactory f) {
        throw new UnsupportedOperationException();
    }
    public FrameStore getFrameStore(Class c) {
        throw new UnsupportedOperationException();
    }
    public void setDirectBrowserSlot(Cls cls, Slot slot) {
        // throw new UnsupportedOperationException();
    }
    public void insertFrameStore(FrameStore newFrameStoreClass, FrameStore afterFrameStoreClass) {
        throw new UnsupportedOperationException();
    }
    public void insertFrameStore(FrameStore newFrameStoreClass) {
        throw new UnsupportedOperationException();
    }
    public boolean setFrameStoreEnabled(Class frameStoreClass, boolean enable) {
        throw new UnsupportedOperationException();
    }

    public void insertFrameStore(FrameStore newFrameStore, int position) {
        throw new UnsupportedOperationException();
    }

    public List getFrameStores() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getFrameFactory()
     */
    public FrameFactory getFrameFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getSystemFrames()
     */
    public SystemFrames getSystemFrames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#clearAllListeners()
     */
    public void clearAllListeners() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getHeadFrameStore()
     */
    public FrameStore getHeadFrameStore() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getFrameCounts()
     */
    public FrameCounts getFrameCounts() {
        return getProject().getFrameCounts();
    }

    public int getSimpleInstanceCount() {
        return _frameManager.getSimpleInstanceCount();
    }

    public boolean hasSuperslot(Slot slot, Slot superslot) {
        throw new UnsupportedOperationException();
    }
    public void moveDirectSubslot(Slot slot, Slot subslot, Slot afterSlot) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#createCls(edu.stanford.smi.protege.model.FrameID, java.lang.String, java.util.Collection, java.util.Collection, boolean)
     */
    public Cls createCls(FrameID id, String name, Collection parents, Collection metaClses, boolean isNew) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#createFacet(edu.stanford.smi.protege.model.FrameID, java.lang.String, java.util.Collection, boolean)
     */
    public Facet createFacet(FrameID id, String name, Collection metaClses, boolean isNew) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#createSlot(edu.stanford.smi.protege.model.FrameID, java.lang.String, java.util.Collection, java.util.Collection, boolean)
     */
    public Slot createSlot(FrameID id, String name, Collection metaClses, Collection superslots, boolean isNew) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#createInstance(edu.stanford.smi.protege.model.FrameID, java.lang.String, edu.stanford.smi.protege.model.Cls, boolean)
     */
    public Instance createInstance(FrameID id, String name, Cls directType, boolean initializeDefaults) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#setJournalingEnabled(boolean)
     */
    public boolean setJournalingEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#isJournalingEnabled()
     */
    public boolean isJournalingEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#setDisplaySlotPattern(edu.stanford.smi.protege.model.Cls, java.lang.String)
     */
    public void setDirectBrowserSlotPattern(Cls cls, BrowserSlotPattern p) {
        setDirectBrowserSlot(cls, p.getFirstSlot());
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getDisplaySlotPattern(edu.stanford.smi.protege.model.Cls)
     */
    public BrowserSlotPattern getDirectBrowserSlotPattern(Cls cls) {
        // TODO Auto-generated method stub
        return getProject().getDirectBrowserSlotPattern(cls);
    }

    public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        return Collections.EMPTY_SET;
    }

    public boolean isCallCachingEnabled() {
        return false;
    }

    public boolean setCallCachingEnabled(boolean enabled) {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#setArgumentCheckingEnabled(boolean)
     */
    public boolean setArgumentCheckingEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#setChangeMonitorEnabled(boolean)
     */
    public boolean setChangeMonitorEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#setCleanDispatchEnabled(boolean)
     */
    public boolean setCleanDispatchEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#setFacetCheckingEnabled(boolean)
     */
    public boolean setFacetCheckingEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#getDirectOwnSlotValue(edu.stanford.smi.protege.model.Frame, edu.stanford.smi.protege.model.Slot)
     */
    public Object getDirectOwnSlotValue(Frame frame, Slot slot) {
        return getOwnSlotValue(frame, slot);
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#removeFrameStore(edu.stanford.smi.protege.model.framestore.FrameStore)
     */
    public void removeFrameStore(FrameStore frameStore) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#startJournaling(java.lang.String, java.net.URI)
     */
    public void startJournaling(URI uri) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.model.KnowledgeBase#stopJournaling()
     */
    public void stopJournaling() {
        // TODO Auto-generated method stub

    }
    public synchronized Collection getDirectlyOverriddenTemplateSlots(Cls cls) {
        return Collections.EMPTY_SET;
    }

    public synchronized Collection getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        return Collections.EMPTY_SET;
    }

    public boolean setGenerateDeletingFrameEventsEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        return false;
    }

    public Collection getOverriddenTemplateFacets(Cls cls, Slot slot) {
        return Collections.EMPTY_SET;
    }

    public SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes, boolean initializeDefaults) {
        Cls type = (Cls) CollectionUtilities.getFirstItem(directTypes);
        return (SimpleInstance) createInstance(id, name, type, initializeDefaults);
    }
    
    public Instance setDirectTypes(Instance instance, Collection types) {
        Cls type = (Cls) CollectionUtilities.getFirstItem(types);
        return setDirectType(instance, type);
    }

    public Instance createInstance(FrameID id, String name, Collection directTypes, boolean initializeDefaults) {
        Cls type = (Cls) CollectionUtilities.getFirstItem(directTypes);
        return createInstance(id, name, type, initializeDefaults);
    }

}
