package edu.stanford.smi.protege.model;
//ESCA*JAVA0130

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.framestore.*;

/**
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultKnowledgeBase_SimpleTest extends SimpleTestCase {

    public void testCreateKB() {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase();
        Cls thing1 = kb.getRootCls();
        Cls thing2 = kb.getCls(Model.Cls.THING);
        assertEquals(thing1, thing2);
    }

    public void testCreateCls() {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase();
        Cls cls = kb.createCls(null, kb.getRootClses());
        Cls cls2 = kb.getCls(cls.getName());
        assertEquals(cls, cls2);
    }

    public void testCreateInstanceWithNoType() {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase();
        Instance instance = kb.createInstance(null, (Cls) null);
        assertNotNull(instance);
        String name = instance.getName();
        Instance instance2 = kb.getInstance(name);
        assertEquals(instance, instance2);
    }

    public void testGetClses() {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase();
        Cls cls = kb.createCls(null, kb.getRootClses());
        assertTrue(kb.getClses().contains(cls));
        assertTrue(kb.getClses().contains(kb.getRootCls()));
    }

    public void testGetValueType() {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase();
        Slot slot = kb.createSlot(null);
        Cls cls = kb.createCls(null, kb.getRootClses());
        assertEquals(false, cls.isAbstract());
        assertEquals(ValueType.STRING, slot.getValueType());
        slot.setValueType(ValueType.INSTANCE);
        assertEquals(ValueType.INSTANCE, slot.getValueType());
        cls.setAbstract(true);
        assertEquals(true, cls.isAbstract());
        assertEqualsSet(makeList(), slot.getDirectDomain());
        assertEqualsSet(makeList(), cls.getDirectTemplateSlots());
        cls.addDirectTemplateSlot(slot);
        assertEqualsSet(makeList(slot), cls.getDirectTemplateSlots());
        assertEqualsSet(makeList(cls), slot.getDirectDomain());
    }

    public void testDeleteCls() {
        final Boolean[] fired = new Boolean[] { Boolean.FALSE };
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase();
        Cls cls = kb.createCls(null, kb.getRootClses());
        kb.addKnowledgeBaseListener(new KnowledgeBaseAdapter() {
            public void clsDeleted(KnowledgeBaseEvent event) {
                assertTrue(event.getCls() != null);
                fired[0] = Boolean.TRUE;
            }
        });
        assertTrue(fired[0] == Boolean.FALSE);
        kb.deleteCls(cls);
        assertTrue(fired[0] == Boolean.TRUE);
    }
}