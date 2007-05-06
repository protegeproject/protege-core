package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class EventDispatchFrameStore extends ModificationFrameStore {
    private Map _listeners = new HashMap();
    private Thread _eventThread;
    private static final int DELAY_MSEC = 1000 * 5;

    public EventDispatchFrameStore() {
        // Log.enter(this, "EventDispatchFrameStore");
    }

    public void reinitialize() {
        // do nothing. In particular we do not clear the listeners. Dispatch can
        // be turned off and then back on and
        // the listeners to not get lost.
    }

    public void clearListeners() {
        _listeners.clear();
    }

    public void close() {
        super.close();
        _listeners = null;
        stopEventThread();
    }

    private void dispatchEvents() {
        dispatchEvents(false);
    }

    /*
     * We ignore exceptions in the multiuser server client because they are an almost unavoidable side-effect of event
     * processing in some cases
     */
    private void dispatchEvents(Collection events, boolean ignoreExceptions) {
        // Log.trace("found events: " + events, this, "dispatchEvents");
        Iterator i = events.iterator();
        while (i.hasNext()) {
            EventObject event = (EventObject) i.next();
            try {
                dispatchEvent(event);
            } catch (Exception e) {
                if (!ignoreExceptions) {
                    Log.getLogger().warning(e.toString());
                }
            }
        }
    }

    private void dispatchEvents(boolean ignoreExceptions) {
        Collection events = getDelegate().getEvents();
        if (!events.isEmpty()) {
            dispatchEvents(events, ignoreExceptions);
        }
    }

    private void dispatchEvent(EventObject event) {
        if (event instanceof KnowledgeBaseEvent) {
            dispatchKbEvent((KnowledgeBaseEvent) event);
        } else if (event instanceof ClsEvent) {
            dispatchClsEvent((ClsEvent) event);
            dispatchClsEventToSubclasses((ClsEvent) event);
        } else if (event instanceof SlotEvent) {
            dispatchSlotEvent((SlotEvent) event);
        } else if (event instanceof FacetEvent) {
            dispatchFacetEvent((FacetEvent) event);
        } else if (event instanceof InstanceEvent) {
            dispatchInstanceEvent((InstanceEvent) event);
        } else if (event instanceof FrameEvent) {
            dispatchFrameEvent((FrameEvent) event);
            dispatchFrameEventAsClsFacetEvent((FrameEvent) event);
        } else {
            throw new RuntimeException("unknown event type: " + event);
        }
    }

    private void dispatchKbEvent(KnowledgeBaseEvent event) {
        // Log.enter(this, "dispatchEvents", event);
        Iterator i = getListeners(KnowledgeBaseListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            KnowledgeBaseListener listener = (KnowledgeBaseListener) i.next();
            switch (event.getEventType()) {
            case KnowledgeBaseEvent.CLS_CREATED:
                // Log.trace("dispatched to " + listener, this,
                // "dispatchKbEvent", event);
                listener.clsCreated(event);
                break;
            case KnowledgeBaseEvent.CLS_DELETED:
                // Log.trace("delete dispatched to " + listener, this,
                // "dispatchKbEvent", event);
                listener.clsDeleted(event);
                break;
            case KnowledgeBaseEvent.SLOT_CREATED:
                listener.slotCreated(event);
                break;
            case KnowledgeBaseEvent.SLOT_DELETED:
                listener.slotDeleted(event);
                break;
            case KnowledgeBaseEvent.FACET_CREATED:
                listener.facetCreated(event);
                break;
            case KnowledgeBaseEvent.FACET_DELETED:
                listener.facetDeleted(event);
                break;
            case KnowledgeBaseEvent.INSTANCE_CREATED:
                listener.instanceCreated(event);
                break;
            case KnowledgeBaseEvent.INSTANCE_DELETED:
                listener.instanceDeleted(event);
                break;
            case KnowledgeBaseEvent.FRAME_NAME_CHANGED:
                listener.frameNameChanged(event);
                break;
            case KnowledgeBaseEvent.DEFAULT_CLS_METACLASS_CHANGED:
                listener.defaultClsMetaClsChanged(event);
                break;
            case KnowledgeBaseEvent.DEFAULT_SLOT_METACLASS_CHANGED:
                listener.defaultSlotMetaClsChanged(event);
                break;
            case KnowledgeBaseEvent.DEFAULT_FACET_METACLASS_CHANGED:
                listener.defaultFacetMetaClsChanged(event);
                break;
            default:
                Log.getLogger().warning("bad event: " + event);
                break;
            }
        }
    }

    private void dispatchClsEvent(ClsEvent event) {
        Iterator i = getListeners(ClsListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            ClsListener listener = (ClsListener) i.next();
            switch (event.getEventType()) {
            case ClsEvent.DIRECT_SUPERCLASS_ADDED:
                listener.directSuperclassAdded(event);
                break;
            case ClsEvent.DIRECT_SUPERCLASS_REMOVED:
                listener.directSuperclassRemoved(event);
                break;
            case ClsEvent.DIRECT_SUBCLASS_ADDED:
                listener.directSubclassAdded(event);
                break;
            case ClsEvent.DIRECT_SUBCLASS_REMOVED:
                listener.directSubclassRemoved(event);
                break;
            case ClsEvent.DIRECT_INSTANCE_ADDED:
                listener.directInstanceAdded(event);
                break;
            case ClsEvent.DIRECT_INSTANCE_REMOVED:
                listener.directInstanceRemoved(event);
                break;
            case ClsEvent.DIRECT_SUBCLASS_MOVED:
                listener.directSubclassMoved(event);
                break;
            case ClsEvent.TEMPLATE_SLOT_ADDED:
                listener.templateSlotAdded(event);
                break;
            case ClsEvent.TEMPLATE_SLOT_REMOVED:
                listener.templateSlotRemoved(event);
                break;
            case ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED:
                listener.templateSlotValueChanged(event);
                break;
            case ClsEvent.TEMPLATE_FACET_ADDED:
                listener.templateFacetAdded(event);
                break;
            case ClsEvent.TEMPLATE_FACET_REMOVED:
                listener.templateFacetRemoved(event);
                break;
            case ClsEvent.TEMPLATE_FACET_VALUE_CHANGED:
                listener.templateFacetValueChanged(event);
                break;
            default:
                Log.getLogger().severe("bad event: " + event);
                break;
            }
        }
    }

    private void dispatchClsEventToSubclasses(ClsEvent event) {
        if (doDispatchToSubclasses(event)) {
            Iterator i = getListenedToSubclasses(ClsListener.class, event.getCls()).iterator();
            while (i.hasNext()) {
                Cls cls = (Cls) i.next();
                ClsEvent subclassEvent = new ClsEvent(cls, event.getEventType(), event
                        .getArgument1(), event.getArgument2());
                dispatchClsEvent(subclassEvent);
            }
        }
    }

    private static boolean doDispatchToSubclasses(ClsEvent event) {
        int type = event.getEventType();
        return type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED
                || type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED
                || type == ClsEvent.TEMPLATE_SLOT_ADDED || type == ClsEvent.TEMPLATE_SLOT_REMOVED;
    }

    private void dispatchFrameEventAsClsFacetEvent(FrameEvent event) {
        Facet facet = getFacet(event);
        if (facet != null) {
            Slot slot = (Slot) event.getFrame();
            Iterator i = slot.getDirectDomain().iterator();
            if (i.hasNext()) {
                Cls cls = (Cls) i.next();
                ClsEvent clsEvent = new ClsEvent(cls, ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, slot,
                        facet);
                dispatchClsEvent(clsEvent);
                dispatchClsEventToSubclasses(clsEvent);
            }
        }
    }

    private static Facet getFacet(FrameEvent event) {
        Facet facet = null;
        if (event.getFrame() instanceof Slot
                && event.getEventType() == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
            Slot slot = event.getSlot();
            facet = slot.getAssociatedFacet();
        }
        return facet;
    }

    private void dispatchSlotEvent(SlotEvent event) {
        Iterator i = getListeners(SlotListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            SlotListener listener = (SlotListener) i.next();
            int type = event.getEventType();
            switch (type) {
            case SlotEvent.TEMPLATE_SLOT_CLS_ADDED:
                listener.templateSlotClsAdded(event);
                break;
            case SlotEvent.TEMPLATE_SLOT_CLS_REMOVED:
                listener.templateSlotClsRemoved(event);
                break;
            case SlotEvent.DIRECT_SUBSLOT_ADDED:
                listener.directSubslotAdded(event);
                break;
            case SlotEvent.DIRECT_SUBSLOT_REMOVED:
                listener.directSubslotRemoved(event);
                break;
            case SlotEvent.DIRECT_SUBSLOT_MOVED:
                listener.directSubslotMoved(event);
                break;
            case SlotEvent.DIRECT_SUPERSLOT_ADDED:
                listener.directSuperslotAdded(event);
                break;
            case SlotEvent.DIRECT_SUPERSLOT_REMOVED:
                listener.directSuperslotRemoved(event);
                break;
            default:
                Assert.fail("bad type: " + type);
                break;
            }
        }
    }

    private void dispatchFacetEvent(FacetEvent event) {
        Iterator i = getListeners(FacetListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            FacetListener listener = (FacetListener) i.next();
            int type = event.getEventType();
            switch (type) {
            case FacetEvent.FRAME_SLOT_REFERENCE_ADDED:
                listener.frameSlotReferenceAdded(event);
                break;
            case FacetEvent.FRAME_SLOT_REFERENCE_REMOVED:
                listener.frameSlotReferenceRemoved(event);
                break;
            default:
                Assert.fail("bad type: " + type + " " + listener);
                break;
            }
        }
    }

    private void dispatchInstanceEvent(InstanceEvent event) {
        Iterator i = getListeners(InstanceListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            InstanceListener listener = (InstanceListener) i.next();
            int type = event.getEventType();
            switch (type) {
            case InstanceEvent.DIRECT_TYPE_ADDED:
                listener.directTypeAdded(event);
                break;
            case InstanceEvent.DIRECT_TYPE_REMOVED:
                listener.directTypeRemoved(event);
                break;
            default:
                Assert.fail("bad type: " + type + " " + listener);
                break;
            }
        }
    }

    private void dispatchFrameEvent(FrameEvent event) {
        Iterator i = getListeners(FrameListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            FrameListener listener = (FrameListener) i.next();
            switch (event.getEventType()) {
            case FrameEvent.NAME_CHANGED:
                listener.nameChanged(event);
                break;
            case FrameEvent.VISIBILITY_CHANGED:
                listener.visibilityChanged(event);
                break;
            case FrameEvent.BROWSER_TEXT_CHANGED:
                listener.browserTextChanged(event);
                break;
            case FrameEvent.OWN_SLOT_ADDED:
                listener.ownSlotAdded(event);
                break;
            case FrameEvent.OWN_SLOT_REMOVED:
                listener.ownSlotRemoved(event);
                break;
            case FrameEvent.OWN_FACET_ADDED:
                listener.ownFacetAdded(event);
                break;
            case FrameEvent.OWN_FACET_REMOVED:
                listener.ownFacetRemoved(event);
                break;
            case FrameEvent.OWN_SLOT_VALUE_CHANGED:
                listener.ownSlotValueChanged(event);
                break;
            case FrameEvent.OWN_FACET_VALUE_CHANGED:
                listener.ownFacetValueChanged(event);
                break;
            case FrameEvent.DELETED:
                listener.deleted(event);
                break;
            default:
                Log.getLogger().severe("bad event: " + event);
            }
        }
    }

    public Collection getListeners(Class c, Object o) {
        Collection allListeners = null;
        Map listeners = (Map) _listeners.get(c);
        if (listeners != null) {
            Collection objectListeners = (Collection) listeners.get(o);
            allListeners = addListeners(objectListeners, allListeners);
            Collection globalListeners = (Collection) listeners.get(null);
            allListeners = addListeners(globalListeners, allListeners);
        }
        return allListeners == null ? Collections.EMPTY_LIST : allListeners;
    }

    private Collection addListeners(Collection listenersToAdd, Collection listeners) {
        if (listenersToAdd != null) {
            if (listeners == null) {
                listeners = new ArrayList(listenersToAdd);
            } else {
                listeners.addAll(listenersToAdd);
            }
        }
        return listeners;
    }

    private void removeListeners(Class listenerType, Frame frame) {
        Map listeners = (Map) _listeners.get(listenerType);
        if (listeners != null) {
            listeners.remove(frame);
        }
    }

    private void removeClsListeners(Cls cls) {
        removeListeners(ClsListener.class, cls);
        removeInstanceListeners(cls);
    }

    private void removeSlotListeners(Slot slot) {
        removeListeners(SlotListener.class, slot);
        removeInstanceListeners(slot);
    }

    private void removeFacetListeners(Facet facet) {
        removeListeners(FacetListener.class, facet);
        removeInstanceListeners(facet);
    }

    private void removeSimpleInstanceListeners(SimpleInstance simpleInstance) {
        removeInstanceListeners(simpleInstance);
    }

    private void removeInstanceListeners(Instance instance) {
        removeListeners(InstanceListener.class, instance);
        removeFrameListeners(instance);
    }

    private void removeFrameListeners(Frame frame) {
        removeListeners(FrameListener.class, frame);
    }

    public void notifyInstancesOfBrowserTextChange(Cls cls) {
        Collection relevantInstances = getListenedToInstances(FrameListener.class, cls);
        Iterator i = relevantInstances.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            FrameEvent event = new FrameEvent(frame, FrameEvent.BROWSER_TEXT_CHANGED);
            dispatchFrameEvent(event);
        }
    }

    private Collection getListenedToSubclasses(Class c, Cls cls) {
        Set listenedToSubclasses = new HashSet();
        Map listeners = (Map) _listeners.get(c);
        if (listeners != null) {
            Set listenedToObjects = listeners.keySet();
            Iterator i = listenedToObjects.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof Cls) {
                    Cls listenedToCls = (Cls) o;
                    if (getSuperclasses(listenedToCls).contains(cls)) {
                        listenedToSubclasses.add(listenedToCls);
                    }
                }
            }
        }
        return listenedToSubclasses;
    }

    private Collection getListenedToInstances(Class c, Cls cls) {
        Set listenedToInstances = new HashSet();
        Map listeners = (Map) _listeners.get(c);
        if (listeners != null) {
            Set listenedToObjects = listeners.keySet();
            Iterator i = listenedToObjects.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof Instance) {
                    Instance instance = (Instance) o;
                    if (instance.hasType(cls)) {
                        listenedToInstances.add(instance);
                    }
                }
            }
        }
        return listenedToInstances;
    }

    public void addListener(Class c, Object o, EventListener listener) {
        // Log.enter(this, "addListener", c, o, listener);
        Map listeners = (Map) _listeners.get(c);
        if (listeners == null) {
            listeners = new HashMap();
            _listeners.put(c, listeners);
        }
        Collection objectListeners = (Collection) listeners.get(o);
        if (objectListeners == null) {
            objectListeners = new ArrayList();
            listeners.put(o, objectListeners);
        }
        objectListeners.add(listener);
    }

    public void removeListener(Class c, Object o, EventListener listener) {
        Map listeners = (Map) _listeners.get(c);
        if (listeners != null) {
            Collection objectListeners = (Collection) listeners.get(o);
            if (objectListeners != null) {
                objectListeners.remove(listener);
            }
        }
    }

    // --------------------------------------
    public void addDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().addDirectSuperclass(cls, superclass);
        dispatchEvents();
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().addDirectSuperslot(slot, superslot);
        dispatchEvents();
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().addDirectTemplateSlot(cls, slot);
        dispatchEvents();
    }

    public void addDirectType(Instance instance, Cls type) {
        getDelegate().addDirectType(instance, type);
        dispatchEvents();
    }

    public boolean beginTransaction(String name) {
        return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        boolean succeeded = getDelegate().commitTransaction();
        dispatchEvents();
        return succeeded;
    }

    public Cls createCls(FrameID id, String name, Collection types, Collection superclasses,
            boolean loadDefaults) {
        Cls cls = getDelegate().createCls(id, name, types, superclasses, loadDefaults);
        dispatchEvents();
        return cls;
    }

    public Facet createFacet(FrameID id, String name, Collection directTypes,
            boolean loadDefaultValues) {
        Facet facet = getDelegate().createFacet(id, name, directTypes, loadDefaultValues);
        dispatchEvents();
        return facet;
    }

    public SimpleInstance createSimpleInstance(FrameID id, String name, Collection types,
            boolean loadDefaultValues) {
        SimpleInstance simpleInstance = getDelegate().createSimpleInstance(id, name, types,
                loadDefaultValues);
        dispatchEvents();
        return simpleInstance;
    }

    public Slot createSlot(FrameID id, String name, Collection types, Collection superslots,
            boolean loadDefaults) {
        Slot slot = getDelegate().createSlot(id, name, types, superslots, loadDefaults);
        dispatchEvents();
        return slot;
    }

    public void deleteCls(Cls cls) {
        getDelegate().deleteCls(cls);
        removeClsListeners(cls);
        dispatchEvents();
    }

    public void deleteFacet(Facet facet) {
        getDelegate().deleteFacet(facet);
        removeFacetListeners(facet);
        dispatchEvents();
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        getDelegate().deleteSimpleInstance(simpleInstance);
        removeSimpleInstanceListeners(simpleInstance);
        dispatchEvents();
    }

    public void deleteSlot(Slot slot) {
        getDelegate().deleteSlot(slot);
        removeSlotListeners(slot);
        dispatchEvents();
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        dispatchEvents();
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
        dispatchEvents();
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
        dispatchEvents();
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        dispatchEvents();
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().removeDirectSuperclass(cls, superclass);
        dispatchEvents();
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().removeDirectSuperslot(slot, superslot);
        dispatchEvents();
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        dispatchEvents();
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
        dispatchEvents();
    }

    public void removeDirectType(Instance instance, Cls type) {
        getDelegate().removeDirectType(instance, type);
        dispatchEvents();
    }

    public boolean rollbackTransaction() {
        boolean succeeded = getDelegate().rollbackTransaction();
        dispatchEvents();
        return succeeded;
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        dispatchEvents();
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        dispatchEvents();
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        dispatchEvents();
    }

    public void setFrameName(Frame frame, String name) {
        getDelegate().setFrameName(frame, name);
        dispatchEvents();
    }

    private void startEventThread() {
        _eventThread = new Thread("EventDispatchFrameStoreHandler.startEventThread") {
            public void run() {
                while (_eventThread == this) {
                    try {
                        pollForEvents();
                        Thread.sleep(DELAY_MSEC);
                    } catch (Exception e) {
                        Log.getLogger().warning(e.toString());
                    }
                }
            }
        };
        _eventThread.setPriority(Thread.MIN_PRIORITY);
        _eventThread.setDaemon(true);
        _eventThread.start();
    }

    private void pollForEvents() throws InvocationTargetException, InterruptedException {
        final Collection events = getDelegate().getEvents();
        if (!events.isEmpty()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if (getDelegate() != null) {
                        dispatchEvents(events, true);
                    }
                }
            });
        }
    }

    private void stopEventThread() {
        _eventThread = null;
    }

    public void setPollForEvents(boolean b) {
        // Log.enter(this, "setPollForEvents", Boolean.valueOf(b));
        if (b) {
            if (_eventThread == null) {
                startEventThread();
            } else {
                throw new IllegalStateException("Already polling");
            }
        } else {
            stopEventThread();
        }
    }
}