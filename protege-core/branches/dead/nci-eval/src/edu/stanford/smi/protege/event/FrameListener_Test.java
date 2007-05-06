package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameListener_Test extends APITestCase {

    public void testFrameNameChangedEvent() {
        clearEvents();
        FrameListener listener = new FrameAdapter() {
            public void nameChanged(FrameEvent event) {
                recordEventFired(event);
            }
            public void ownSlotValueChanged(FrameEvent event) {
                recordEventFired(event);
            }
            public void browserTextChanged(FrameEvent event) {
                recordEventFired(event);
            }
        };
        Frame frame = createCls();
        frame.addFrameListener(listener);
        frame.setName("foo");
        assertEventFired(FrameEvent.NAME_CHANGED);
        assertEventFired(FrameEvent.OWN_SLOT_VALUE_CHANGED);
        assertEventFired(FrameEvent.BROWSER_TEXT_CHANGED);
    }

    public void testOwnSlotValueChangedEvent() {
        clearEvents();
        FrameListener listener = new FrameAdapter() {
            public void ownSlotValueChanged(FrameEvent event) {
                recordEventFired(event);
            }
        };
        Cls cls = createCls();
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        Instance instance = createInstance(cls);
        instance.addFrameListener(listener);
        instance.setOwnSlotValue(slot, "foo");
        assertEventFired(FrameEvent.OWN_SLOT_VALUE_CHANGED);
        clearEvents();
        instance.setOwnSlotValues(slot, makeList("a", "b"));
        FrameEvent event = (FrameEvent) getEventFired(FrameEvent.OWN_SLOT_VALUE_CHANGED);
        assertEqualsList(event.getOldValues(), makeList("foo"));
    }

}
