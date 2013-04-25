package edu.stanford.smi.protege.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.test.APITestCase;

/**
 * @author Holger Knublauch  <holger@smi.stanford.edu>
 */
public class ModelUtilities_Test extends APITestCase {

    /**
     * :THING
     *   Top
     *     Hidden
     *       TestCls
     *     Visible
     *       TestCls
     * ------------------------------------------------------
     * Path should be: TestCls, VisibleParent, Top, :THING
     */
    public void testGetPathToRootWithHiddenSuperclass() {
        Cls topCls = createCls();
        Cls hiddenCls = createSubCls(topCls);
        hiddenCls.setVisible(false);
        Cls visibleCls = createSubCls(topCls);
        Cls testCls = createSubCls(hiddenCls);
        testCls.addDirectSuperclass(visibleCls);
        List list = ModelUtilities.getPathToRoot(testCls);
        Iterator it = list.iterator();
        assertEquals(getDomainKB().getRootCls(), it.next());
        assertEquals(topCls, it.next());
        assertEquals(visibleCls, it.next());
        assertEquals(testCls, it.next());
        assertFalse(it.hasNext());
    }

    /**
     * :THING
     *  |
     *  +- R    p: n
     *     |
     *     +- Z    p: h
     *     |  |
     *     |  +- T    p: k
     *     |  |
     *     +--+- C    p: e
     *     |     |
     *     |     +- B    p: e
     *     |     |
     *     |     +- A    p: e
     *     |        |
     *     +--------+- Y    p: {n, ny, tk}
     *                 |
     *                 +- X    p: {o, h}
     * ------------------------------------------------------
     * Result should be: {tk=[y], h=[x, z], n=[y, r], ny=[y], o=[x], e=[c, a]}
     */
    public void testGetPropertyValuesOnAllSuperclasses() {
    	//create class hierarchy
        Cls r = createCls();
        Cls z = createSubCls(r);
        Cls t = createSubCls(z);
        Cls c = createSubCls(z);
        c.addDirectSuperclass(r);
        Cls b = createSubCls(c);
        Cls a = createSubCls(c);
        Cls y = createSubCls(a);
        y.addDirectSuperclass(r);
        Cls x = createSubCls(y);

        //create property values
    	Slot p = createSlot();
		r.addOwnSlotValue(p, "n");
        z.addOwnSlotValue(p, "h");
        t.addOwnSlotValue(p, "k");
        c.addOwnSlotValue(p, "e");
        b.addOwnSlotValue(p, "e");
        a.addOwnSlotValue(p, "e");
        y.addOwnSlotValue(p, "n");
        y.addOwnSlotValue(p, "ny");
        y.addOwnSlotValue(p, "tk");
        x.addOwnSlotValue(p, "o");
        x.addOwnSlotValue(p, "h");

        //test method
        Map<Object, List<Instance>> valuesMap = ModelUtilities.getPropertyValuesOnAllSuperclasses(x, p);
        //System.out.println(valuesMap);

        Set<Object> values = valuesMap.keySet();
        //System.out.println(values);

        String[] expectedValues = new String[] {"n", "h", "e", "ny", "tk", "o"};
        assertEquals(expectedValues.length, values.size());
        for (String expValue : expectedValues) {
			assertTrue(values.contains(expValue));
		}

        List<Instance> occList;
        occList = valuesMap.get("n");
        assertEquals(2, occList.size());
        assertTrue(occList.contains(r));
        assertTrue(occList.contains(y));

        occList = valuesMap.get("h");
        assertEquals(2, occList.size());
        assertTrue(occList.contains(z));
        assertTrue(occList.contains(x));

        occList = valuesMap.get("e");
        assertEquals(2, occList.size());
        assertTrue(occList.contains(a));
        assertTrue(occList.contains(c));

        occList = valuesMap.get("ny");
        assertEquals(1, occList.size());
        assertTrue(occList.contains(y));

        occList = valuesMap.get("tk");
        assertEquals(1, occList.size());
        assertTrue(occList.contains(y));

        occList = valuesMap.get("o");
        assertEquals(1, occList.size());
        assertTrue(occList.contains(x));

    }


}
