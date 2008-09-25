package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.AbstractEvent;

public class EventGeneratorFrameStore_Test extends FrameStore_Test {
    private KnowledgeBase _kb;
    private int nameCounter = 0;

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        _kb = kb;
        FrameStore fs = new EventGeneratorFrameStore(kb);
        fs.setDelegate(new InMemoryFrameStore(kb));
        return fs;
    }

    public void tearDown() throws Exception {
        super.tearDown();
        _kb = null;
    }
    
    private boolean containsSimilarEvent(List<EventObject> events, AbstractEvent event) {
      for (EventObject ev : events) {
        if (ev instanceof AbstractEvent) {
          event.setTimeStamp(((AbstractEvent) ev).getTimeStamp());
        }
        if (ev.equals(event)) {
          return  true;
        }
      }
      return false;
    }
    
    private boolean isSimilarEvent(EventObject event1, AbstractEvent event2) {
        if (event1 instanceof AbstractEvent) {
            event2.setTimeStamp(((AbstractEvent) event1).getTimeStamp());
        }
        return event1.equals(event2);
    }

    
    public String createName() {
      return "frameName" + nameCounter++;
    }

    @SuppressWarnings("unchecked")
    public void testCreateClsEvent() {
        Cls rootCls = _kb.getRootCls();
        Cls stdCls = _kb.getCls(Model.Cls.STANDARD_CLASS);
        Slot subclassesSlot = _kb.getSlot(Model.Slot.DIRECT_SUBCLASSES);
        Slot instancesSlot = _kb.getSlot(Model.Slot.DIRECT_INSTANCES);
        Cls cls = createCls();
        List<EventObject> events = (List) getTestFrameStore().getEvents();
        assertTrue(containsSimilarEvent(events, new KnowledgeBaseEvent(_kb, KnowledgeBaseEvent.CLS_CREATED, cls)));
        assertTrue(containsSimilarEvent(events, new ClsEvent(rootCls, ClsEvent.DIRECT_SUBCLASS_ADDED, cls)));
        assertTrue(containsSimilarEvent(events, new FrameEvent(rootCls, FrameEvent.OWN_SLOT_VALUE_CHANGED, subclassesSlot)));
        assertTrue(containsSimilarEvent(events, new ClsEvent(stdCls, ClsEvent.DIRECT_INSTANCE_ADDED, cls)));
        assertTrue(containsSimilarEvent(events, new FrameEvent(stdCls, FrameEvent.OWN_SLOT_VALUE_CHANGED, instancesSlot, null)));
    }

    public void testAddSuperclassEvent() {
        Cls cls = createCls();
        Cls superclass = createCls();
        getTestFrameStore().getEvents();
        getTestFrameStore().addDirectSuperclass(cls, superclass);
        List<AbstractEvent> events = getTestFrameStore().getEvents();
        ClsEvent testEvent1 = new ClsEvent(cls, ClsEvent.DIRECT_SUPERCLASS_ADDED, superclass);
        ClsEvent testEvent2 = new ClsEvent(superclass, ClsEvent.DIRECT_SUBCLASS_ADDED, cls);
        assertTrue("kb event", isSimilarEvent(events.get(0), testEvent1));
        assertTrue("kb event", isSimilarEvent(events.get(1), testEvent2));
    }

    @SuppressWarnings("unchecked")
	public void testDeleteClsEvents() {
        Cls cls = createCls();
        KnowledgeBaseEvent testEvent1 = new KnowledgeBaseEvent(_kb, KnowledgeBaseEvent.CLS_DELETED, cls, cls.getName());
        getTestFrameStore().getEvents();
        getTestFrameStore().deleteCls(cls);
        List<EventObject> events = (List) getTestFrameStore().getEvents();
        assertTrue(containsSimilarEvent(events, testEvent1));
    }
    
    public void testProperlyConfigured() {
        List errors = new ArrayList();
        Project p = new Project("junit/pprj/metaproject.pprj", errors);
        assertTrue(errors.isEmpty());
        assertFalse(p.isMultiUserClient());
        assertFalse(p.isMultiUserServer());
        FrameStoreManager fsm = p.getKnowledgeBase().getFrameStoreManager();
        assertNotNull(fsm.getFrameStoreFromClass(EventGeneratorFrameStore.class));
        assertNull(fsm.getFrameStoreFromClass(EventSinkFrameStore.class));
        p.dispose();
    }
}
