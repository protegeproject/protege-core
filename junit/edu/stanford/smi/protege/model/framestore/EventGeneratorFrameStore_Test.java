package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.AbstractEvent;

public class EventGeneratorFrameStore_Test extends FrameStore_Test {
    private KnowledgeBase _kb;

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

    public void testCreateClsEvent() {
        Cls rootCls = _kb.getRootCls();
        Cls stdCls = _kb.getCls(Model.Cls.STANDARD_CLASS);
        Slot subclassesSlot = _kb.getSlot(Model.Slot.DIRECT_SUBCLASSES);
        Slot instancesSlot = _kb.getSlot(Model.Slot.DIRECT_INSTANCES);
        Cls cls = createCls();
        List<EventObject> events = getTestFrameStore().getEvents();
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
        List<EventObject> events = getTestFrameStore().getEvents();
        ClsEvent testEvent1 = new ClsEvent(cls, ClsEvent.DIRECT_SUPERCLASS_ADDED, superclass);
        ClsEvent testEvent2 = new ClsEvent(superclass, ClsEvent.DIRECT_SUBCLASS_ADDED, cls);
        assertEquals("kb event", testEvent1, events.get(0));
        assertEquals("kb event", testEvent2, events.get(1));
    }

    public void testDeleteClsEvents() {
        Cls cls = createCls();
        KnowledgeBaseEvent testEvent1 = new KnowledgeBaseEvent(_kb, KnowledgeBaseEvent.CLS_DELETED, cls, cls.getName());
        getTestFrameStore().getEvents();
        getTestFrameStore().deleteCls(cls);
        List<EventObject> events = getTestFrameStore().getEvents();
        assertTrue(containsSimilarEvent(events, testEvent1));
    }
}
