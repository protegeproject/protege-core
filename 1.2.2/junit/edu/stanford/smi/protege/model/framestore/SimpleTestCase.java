package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * @author Ray Fergerson
 * 
 * Description of this class
 */
public class SimpleTestCase extends TestCase {

    protected static List makeList() {
        return new ArrayList();
    }

    protected static List makeList(Object o) {
        List list = makeList();
        list.add(o);
        return list;
    }

    protected static List makeList(Object o1, Object o2) {
        List list = makeList(o1);
        list.add(o2);
        return list;
    }

    protected static List makeList(Object o1, Object o2, Object o3) {
        List list = makeList(o1, o2);
        list.add(o3);
        return list;
    }

    protected static List makeList(Object o1, Object o2, Object o3, Object o4) {
        List list = makeList(o1, o2, o3);
        list.add(o4);
        return list;
    }

    protected static List makeList(Object o1, Object o2, Object o3, Object o4, Object o5) {
        List list = makeList(o1, o2, o3, o4);
        list.add(o5);
        return list;
    }

    protected static List makeList(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        List list = makeList(o1, o2, o3, o4, o5);
        list.add(o6);
        return list;
    }

    protected static void assertEqualsList(Collection c1, Collection c2) {
        assertEqualsList("", c1, c2);
    }

    protected static void assertEqualsList(String name, Collection c1, Collection c2) {
        assertEquals(name + " size", c1.size(), c2.size());
        Iterator i1 = c1.iterator();
        Iterator i2 = c2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            Object o1 = i1.next();
            Object o2 = i2.next();
            assertEquals(name + " objects", o1, o2);
        }
    }

    protected static void assertEqualsSet(Collection c1, Collection c2) {
        assertEqualsSet("", c1, c2);
    }

    protected static void assertEqualsSet(String name, Collection c1, Collection c2) {
        assertEquals(name + " size", c1.size(), c2.size());
        Set s = new HashSet(c1);
        s.removeAll(c2);
        assertEquals(name + " contents", 0, s.size());
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }
}