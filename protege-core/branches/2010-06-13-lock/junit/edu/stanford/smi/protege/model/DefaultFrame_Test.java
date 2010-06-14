package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultFrame_Test extends APITestCase {
    public void testSortWithEmptyBrowserText() {
        Cls cls = createCls();
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        cls.setDirectBrowserSlotPattern(new BrowserSlotPattern(slot));
        Instance instance1 = createInstance(cls);
        Instance instance2 = createInstance(cls);
        List<Instance> list = new ArrayList<Instance>();
        list.add(instance1);
        list.add(instance2);
        instance2.setOwnSlotValue(slot, ":foo");
        instance1.setOwnSlotValue(slot, "");
        Collections.sort(list);
    }

}
