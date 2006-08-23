package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class BrowserSlotPattern_Test extends APITestCase {

    public void testSlotsInitializer() {
        Slot slot = createSlot();
        List slots = makeList(slot);
        BrowserSlotPattern pattern = new BrowserSlotPattern(slots);
        assertEqualsList(slots, pattern.getSlots());
    }

    public void testStringAndSlotInitializer() {
        Slot slot = createSlot();
        List elements = makeList("ABC ", slot, " DEF");
        BrowserSlotPattern browserTextPattern = new BrowserSlotPattern(elements);
        List retrievedElements = browserTextPattern.getElements();
        assertEquals(elements, retrievedElements);
        assertEqualsList(makeList(slot), browserTextPattern.getSlots());
    }

}
