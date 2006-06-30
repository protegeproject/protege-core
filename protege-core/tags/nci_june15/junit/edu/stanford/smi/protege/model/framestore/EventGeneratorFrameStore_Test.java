package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
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

    public void testCreateClsEvent() {
        Cls rootCls = _kb.getRootCls();
        Cls stdCls = _kb.getCls(Model.Cls.STANDARD_CLASS);
        Slot subclassesSlot = _kb.getSlot(Model.Slot.DIRECT_SUBCLASSES);
        Slot instancesSlot = _kb.getSlot(Model.Slot.DIRECT_INSTANCES);
        Cls cls = createCls();
        List<AbstractEvent> events = getTestFrameStore().getEvents();
        assertTrue(events.contains(new KnowledgeBaseEvent(_kb, KnowledgeBaseEvent.CLS_CREATED, cls)));
        assertTrue(events.contains(new ClsEvent(rootCls, ClsEvent.DIRECT_SUBCLASS_ADDED, cls)));
        assertTrue(events.contains(new FrameEvent(rootCls, FrameEvent.OWN_SLOT_VALUE_CHANGED, subclassesSlot)));
        assertTrue(events.contains(new ClsEvent(stdCls, ClsEvent.DIRECT_INSTANCE_ADDED, cls)));
        assertTrue(events.contains(new FrameEvent(stdCls, FrameEvent.OWN_SLOT_VALUE_CHANGED, instancesSlot, null)));
    }

    public void testAddSuperclassEvent() {
        Cls cls = createCls();
        Cls superclass = createCls();
        getTestFrameStore().getEvents();
        getTestFrameStore().addDirectSuperclass(cls, superclass);
        List<AbstractEvent> events = getTestFrameStore().getEvents();
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
        Collection events = getTestFrameStore().getEvents();
        assertTrue(events.contains(testEvent1));
    }
}