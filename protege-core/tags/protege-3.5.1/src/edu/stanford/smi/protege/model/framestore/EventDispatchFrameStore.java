package edu.stanford.smi.protege.model.framestore;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FacetEvent;
import edu.stanford.smi.protege.event.FacetListener;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.InstanceEvent;
import edu.stanford.smi.protege.event.InstanceListener;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.ProjectEvent;
import edu.stanford.smi.protege.event.ServerProjectEvent;
import edu.stanford.smi.protege.event.ServerProjectListener;
import edu.stanford.smi.protege.event.ServerProjectNotificationEvent;
import edu.stanford.smi.protege.event.ServerProjectSessionClosedEvent;
import edu.stanford.smi.protege.event.ServerProjectStatusChangeEvent;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.event.SlotListener;
import edu.stanford.smi.protege.event.TransactionEvent;
import edu.stanford.smi.protege.event.TransactionListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.ArrayListMultiMap;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.Log;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class EventDispatchFrameStore extends ModificationFrameStore {
    private static transient Logger log = Log.getLogger(EventDispatchFrameStore.class);
    //ESCA-JAVA0077 
    public static final int DELAY_MSEC = 5 * 1000;
    private Map<Class<?>, Map<Object, Collection<EventListener>>> _listeners 
    			= new HashMap<Class<?>, Map<Object, Collection<EventListener>>>();
    private Thread _eventThread;
    private KnowledgeBase kb;
    
    private boolean passThrough = false;
    private List<AbstractEvent> savedEvents = new ArrayList<AbstractEvent>();
    private ArrayListMultiMap<RemoteSession, AbstractEvent> transactedEvents = new ArrayListMultiMap<RemoteSession, AbstractEvent>();

    public EventDispatchFrameStore(KnowledgeBase kb) {
    	this.kb = kb;
    }
    

    
    public void reinitialize() {
	    // do nothing. In particular we do not clear the listeners. Dispatch can
	    // be turned off and then back on and
	    // the listeners to not get lost.
	}

	@Override
	public void close() {
	    super.close();
	    stopEventThread();
        _listeners = null;
	}

    /*------------------------------------------------------------------------------
     * Event Logic
     */
	public void setPassThrough(boolean passThrough) {
		this.passThrough = passThrough;
	}

	private List<AbstractEvent> getDispatchableEvents() {
		List<AbstractEvent> results = new ArrayList<AbstractEvent>();
		for (AbstractEvent event : getDelegate().getEvents()) {
			if (passThrough) {
				savedEvents.add(event);
			}
			if (event.isHiddenByTransaction() && !isMyEvent(event)) {
				RemoteSession session = event.getSession();
				transactedEvents.addValue(session, event);
				if (log.isLoggable(Level.FINER)) {
				    log.finer("Dispatch for " + event + " deferred until " + session + "s transaction closes.");
				}
				continue;
			}
			if (event instanceof TransactionEvent && 
			        ((TransactionEvent) event).getEventType() == TransactionEvent.TRANSACTION_END) {
				RemoteSession session = event.getSession();
				List<AbstractEvent> deferred = transactedEvents.removeKey(session);
				if (deferred != null && ((TransactionEvent) event).isCommitted()) {
				    if (log.isLoggable(Level.FINER)) {
				        log.finer("Committing " + deferred.size() + " events for session " + session);
				    }
				    results.addAll(deferred);
				}
			}
			results.add(event);
			if (log.isLoggable(Level.FINER)) {
			    log.finer("Event being dispatched now: " + event);
			}
		}
		return results;
	}
	
	private boolean isMyEvent(AbstractEvent event) {
	    RemoteSession mySession = ServerFrameStore.getCurrentSession();
	    RemoteSession eventSession = event.getSession();
	    if (mySession == null) {
	        return eventSession == null;
	    }
	    else {
	        return mySession.equals(eventSession);
	    }
	}

	/**
	 * When pass through is set for the event dispatch frame store it is the callers responsibility to avoid 
	 * deadlock situations.  Deadlock really shouldn't be an issue for the server  but this would
	 * be important if there was ever another caller.
	 * 
	 * @see edu.stanford.smi.protege.model.framestore.ModificationFrameStore#getEvents()
	 */
	@Override
	public List<AbstractEvent> getEvents() {
		if (passThrough) {
			dispatchEvents();
			List<AbstractEvent> results = savedEvents;
			savedEvents = new ArrayList<AbstractEvent>();
			return results;
		}
		else {
			return new ArrayList<AbstractEvent>();
		}
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
    
    private void startEventThread() {
        _eventThread = new Thread("EventDispatchFrameStoreHandler.startEventThread") {
          @Override
		public void run() {
            while (true) {
              try {
                synchronized (kb) {
                  if (_eventThread != this) {
                    return;
                  }
                }
                flushEvents();
                synchronized (kb) {
                  kb.wait(DELAY_MSEC);
                }
              } catch (Exception e) {
                Log.getLogger().warning(e.toString());
                log.log(Level.FINE, "Exception caught", e);
              }
            }
          }
        };
        _eventThread.setPriority(Thread.MIN_PRIORITY);
        _eventThread.setDaemon(true);
        _eventThread.start();
    }
    
    private void stopEventThread() {
        _eventThread = null;
        synchronized (kb){
            kb.notifyAll();
        }
    }
    
    public void flushEvents() throws InterruptedException {
        try {
            if (EventQueue.isDispatchThread()) {
                dispatchEvents();
            }
            else {  // careful - deadlock territory...
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        dispatchEvents();
                    }
                });
            }
        } catch (InvocationTargetException ite) {
            Log.getLogger().warning("Exception caught " + ite);
        }
    }


   
    private void dispatchEvents() {
        dispatchEvents(false);
    }
    
    @SuppressWarnings("unchecked")
    private void dispatchEvents(boolean ignoreExceptions) {
    	try {
    	synchronized (kb) {
    		Collection<AbstractEvent> events = getDispatchableEvents();
    		if (!events.isEmpty()) {
    			dispatchEvents(events, ignoreExceptions);
    		}
    	}
    	}
    	catch (Throwable t) {
            do {
            	//TODO: watch the ConnectionException - maybe it is thrown for transitory connection problems
                if (t instanceof ServerSessionLost || t instanceof ConnectException) {
                    log.warning("Knowledge base has been disconnected from the server");
                    kb.getProject().postProjectEvent(ProjectEvent.SERVER_SESSION_LOST);
                    return;
                }
            } while ((t = t.getCause()) != null);
    	}
    }



    /*
     * We ignore exceptions in the multiuser server client because they are an almost unavoidable side-effect of event
     * processing in some cases
     */
    private void dispatchEvents(Collection<AbstractEvent> events, boolean ignoreExceptions) {
        // Log.trace("found events: " + events, this, "dispatchEvents");
        Iterator<AbstractEvent> i = events.iterator();
        while (i.hasNext()) {
            AbstractEvent event = i.next();
            try {
                dispatchEvent(event);
            } catch (Throwable e) {
                if (!ignoreExceptions) {
                  if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Exception caught", e);
                  }
                  Log.getLogger().warning("Exception caught " + e.toString());
                  Log.getLogger().warning("use fine logging for more details");
                }
            }
        }
        for (Object event : events) {
        	if (event instanceof KnowledgeBaseEvent) {
        		KnowledgeBaseEvent kbEvent = (KnowledgeBaseEvent) event;
        		if (kbEvent.getEventType() == KnowledgeBaseEvent.FRAME_REPLACED) {
        			replaceListeners(kbEvent.getFrame(), kbEvent.getNewFrame());
        		}
        	}
        }
    }



    private void dispatchEvent(AbstractEvent event) {
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
        } else if (event instanceof TransactionEvent) {
            dispatchTransactionEvent((TransactionEvent) event);
        } else if (event instanceof ServerProjectEvent) {
            dispatchServerEvent((ServerProjectEvent) event);
        } else {
            throw new RuntimeException("unknown event type: " + event);
        }
    }
    
    private void dispatchServerEvent(ServerProjectEvent event) {
        for (EventListener l : getListeners(ServerProjectListener.class, event.getSource())) {
            ServerProjectListener listener = (ServerProjectListener)  l;
            if  (event instanceof ServerProjectNotificationEvent) {
                listener.projectNotificationReceived((ServerProjectNotificationEvent) event);
            }
            else if (event instanceof ServerProjectStatusChangeEvent) {
                listener.projectStatusChanged((ServerProjectStatusChangeEvent) event);
            } else if (event instanceof ServerProjectSessionClosedEvent) {
            	/*
            	 * The event is dispatch to all clients with this project.
            	 * Each client is responsible for checking whether sessionToKill
            	 * is his own session.
            	 */
            	listener.beforeProjectSessionClosed((ServerProjectSessionClosedEvent) event);
            }
        }
    }
    
    private void dispatchTransactionEvent(TransactionEvent event) {
        Iterator i = getListeners(TransactionListener.class, event.getSource()).iterator();
        while (i.hasNext()) {
            TransactionListener listener = (TransactionListener) i.next();
            switch (event.getEventType()) {
                case TransactionEvent.TRANSACTION_BEGIN:
                    listener.transactionBegin(event);
                    break;
                case TransactionEvent.TRANSACTION_END:
                    listener.transactionEnded(event);
                    break;
                default:
                    Log.getLogger().warning("bad event: " + event);
                    break;
            }
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
                case KnowledgeBaseEvent.FRAME_REPLACED:
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
            Iterator<Cls> i = getListenedToSubclasses(ClsListener.class, event.getCls()).iterator();
            while (i.hasNext()) {
                Cls cls = i.next();
                ClsEvent subclassEvent = new ClsEvent(cls, event.getEventType(), event.getArgument1(), event
                        .getArgument2());
                dispatchClsEvent(subclassEvent);
            }
        }
    }

    private static boolean doDispatchToSubclasses(ClsEvent event) {
        int type = event.getEventType();
        return type == ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED || type == ClsEvent.TEMPLATE_FACET_VALUE_CHANGED
                || type == ClsEvent.TEMPLATE_SLOT_ADDED || type == ClsEvent.TEMPLATE_SLOT_REMOVED;
    }

    private void dispatchFrameEventAsClsFacetEvent(FrameEvent event) {
        Facet facet = getFacet(event);
        if (facet != null) {
            Slot slot = (Slot) event.getFrame();
            Iterator i = slot.getDirectDomain().iterator();
            if (i.hasNext()) {
                Cls cls = (Cls) i.next();
                // Log.getLogger().info("** dispatch frame event as cls facet event: " + event + " " + facet);
                ClsEvent clsEvent = new ClsEvent(cls, ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, slot, facet);
                dispatchClsEvent(clsEvent);
                dispatchClsEventToSubclasses(clsEvent);
            }
        }
    }

    private static Facet getFacet(FrameEvent event) {
        Facet facet = null;
        if (event.getFrame() instanceof Slot && event.getEventType() == FrameEvent.OWN_SLOT_VALUE_CHANGED) {
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
                case FrameEvent.REPLACE_FRAME:
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
                    break;
            }
        }
    }
    
    @SuppressWarnings("unchecked")

    
    /*-----------------------------------------------------------------------------------------
     * Listener Logic
     */
    
    public Collection<EventListener> getListeners(Class<?> c, Object o) {
        Collection<EventListener> allListeners = null;
        Map<Object, Collection<EventListener>> listeners = _listeners.get(c);
        if (listeners != null) {
            Collection<EventListener> objectListeners = listeners.get(o);
            allListeners = addListeners(objectListeners, allListeners);
            Collection<EventListener> globalListeners = listeners.get(null);
            allListeners = addListeners(globalListeners, allListeners);
        }
        if (allListeners == null) {
        	return Collections.emptyList();
        }
        else {
        	return allListeners;
        }
    }

    private static Collection<EventListener> addListeners(Collection<EventListener> listenersToAdd, 
    									   			      Collection<EventListener> listeners) {
        if (listenersToAdd != null) {
            if (listeners == null) {
                listeners = new ArrayList<EventListener>(listenersToAdd);
            } else {
                listeners.addAll(listenersToAdd);
            }
        }
        return listeners;
    }

    public void clearListeners() {
	    _listeners.clear();
	}

	private void removeListeners(Class<?> listenerType, Frame frame) {
        Map<Object, Collection<EventListener>> listeners = _listeners.get(listenerType);
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
        Collection<Instance> relevantInstances = getListenedToInstances(FrameListener.class, cls);
        Iterator<Instance> i = relevantInstances.iterator();
        while (i.hasNext()) {
            Frame frame = i.next();
            FrameEvent event = new FrameEvent(frame, FrameEvent.BROWSER_TEXT_CHANGED);
            dispatchFrameEvent(event);
        }
    }

    private Collection<Cls> getListenedToSubclasses(Class<?> c, Cls cls) {
        Set<Cls> listenedToSubclasses = new HashSet<Cls>();
        Map<Object, Collection<EventListener>> listeners = _listeners.get(c);
        if (listeners != null) {
            Set<Object> listenedToObjects = listeners.keySet();
            Iterator<Object> i = listenedToObjects.iterator();
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

    private Collection<Instance> getListenedToInstances(Class<?> c, Cls cls) {
        Set<Instance> listenedToInstances = new HashSet<Instance>();
        Map<Object, Collection<EventListener>> listeners = _listeners.get(c);
        if (listeners != null) {
            Set<Object> listenedToObjects = listeners.keySet();
            Iterator<Object> i = listenedToObjects.iterator();
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

    public void addListener(Class<?> c, Object o, EventListener listener) {
        // Log.enter(this, "addListener", c, o, listener);
        Map<Object, Collection<EventListener>> listeners = _listeners.get(c);
        if (listeners == null) {
            listeners = new HashMap<Object, Collection<EventListener>>();
            _listeners.put(c, listeners);
        }
        Collection<EventListener> objectListeners = listeners.get(o);
        if (objectListeners == null) {
            objectListeners = new ArrayList<EventListener>();
            listeners.put(o, objectListeners);
        }
        objectListeners.add(listener);
    }

    public void removeListener(Class<?> c, Object o, EventListener listener) {
        Map<Object, Collection<EventListener>> listeners = _listeners.get(c);
        if (listeners != null) {
            Collection<EventListener> objectListeners = listeners.get(o);
            if (objectListeners != null) {
                objectListeners.remove(listener);
            }
        }
    }


    /*------------------------------------------------------------------------
	 * Basic Frame Store responsibilities.
     */
    
    
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
        boolean succeeded = getDelegate().beginTransaction(name);
        dispatchEvents();
        return succeeded;
    }

    public boolean commitTransaction() {
        boolean succeeded = getDelegate().commitTransaction();
        dispatchEvents();
        return succeeded;
    }

    public Cls createCls(FrameID id, Collection types, Collection superclasses, boolean loadDefaults) {
        Cls cls = getDelegate().createCls(id, types, superclasses, loadDefaults);
        dispatchEvents();
        return cls;
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        Facet facet = getDelegate().createFacet(id, directTypes, loadDefaultValues);
        dispatchEvents();
        return facet;
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection types, boolean loadDefaultValues) {
        SimpleInstance simpleInstance = getDelegate().createSimpleInstance(id, types, loadDefaultValues);
        dispatchEvents();
        return simpleInstance;
    }

    public Slot createSlot(FrameID id, Collection types, Collection superslots, boolean loadDefaults) {
        Slot slot = getDelegate().createSlot(id, types, superslots, loadDefaults);
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

    public void moveDirectType(Instance instance, Cls type, int index) {
        getDelegate().moveDirectType(instance, type, index);
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




    public void replaceFrame(Frame original, Frame replacement) {
      getDelegate().replaceFrame(original, replacement);
      dispatchEvents();
    }
    
    private void replaceListeners(Frame original, Frame replacement) {
    	for (Map<Object,  Collection<EventListener>> map : _listeners.values()) {
    		Collection<EventListener> listeners = map.remove(original);
    		if (listeners != null) {
    			Collection<EventListener> existingListeners = map.get(replacement);
    			if (existingListeners != null) {
    				listeners.addAll(existingListeners);
    			}
    			map.put(replacement, listeners);
    		}
    	}
    }
    
    public void printListenersByHashCode(int hash) {
    	System.out.println("Printing out FrameListeners that have hash code " + hash);
    	for (java.util.Map.Entry<Object, Collection<EventListener>> entry : _listeners.get(FrameListener.class).entrySet()) {
    		Object o  = entry.getKey();
    		Collection<EventListener> listeners = entry.getValue();
    		for (EventListener listener :  listeners) {
    			if (listener.hashCode() == hash) {
    			   System.out.println("Found listener " + listener + " at " + o);
    			}
    		}
    	}    	
    }
    
    public void dumpListeners(Level level) {
    	if (!log.isLoggable(level)) {
    		return;
    	}
    	log.log(level, "-----------------------printing listeners======================");
    	for (Map<Object,  Collection<EventListener>> map : _listeners.values()) {
    		for (Entry<Object, Collection<EventListener>> entry : map.entrySet()) {
    			log.log(level, "listeners for object " + entry.getKey());
    			for (EventListener listener : entry.getValue()) {
    				log.log(level, "\t" + listener);
    			}
    		}
    	}
    }
    
}