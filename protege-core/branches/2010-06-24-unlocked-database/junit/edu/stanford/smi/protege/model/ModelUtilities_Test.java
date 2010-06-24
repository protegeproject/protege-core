package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.test.*;

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
}
