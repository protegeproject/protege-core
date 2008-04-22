package edu.stanford.smi.protege.model;

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.test.*;

/**
 * Unit tests for PropertyMapUtil
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PropertyMapUtil_Test extends APITestCase {
    protected Instance _mapInstance;

    public static class TestClass {
        String _value;

        public TestClass(String s) {
            _value = s;
        }

        public String toString() {
            return _value;
        }

        public boolean equals(Object o) {
            TestClass tco = (TestClass) o;
            return _value.equals(tco._value);
        }

        public int hashCode() {
            return _value.hashCode();
        }
    }

    public void setUp() throws Exception {
        super.setUp();
        KnowledgeBase projectKB = getProject().getSources().getWrappedInstance().getKnowledgeBase();
        Cls mapCls = projectKB.getCls("Map");
        _mapInstance = projectKB.createInstance(null, mapCls);
    }

    public void testClassObject() {
        Map map = new HashMap();
        map.put("foo", PropertyMapUtil_Test.class);
        map.put(PropertyMapUtil.class, "bar");
        map.put(Project.class, Model.class);

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());
        assertSame("string to class", PropertyMapUtil_Test.class, map.get("foo"));
        assertEquals("class to string", "bar", map.get(PropertyMapUtil.class));
        assertSame("class to class", Model.class, map.get(Project.class));
    }

    public void testFrame() {
        Frame frame1 = createFrame();
        Frame frame2 = createFrame();
        Frame frame3 = createFrame();
        Frame frame4 = createFrame();
        Map map = new HashMap();
        map.put(frame1, "foo");
        map.put("bar", frame2);
        map.put(frame3, frame4);

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());
        assertSame("frame to frame", frame4, map.get(frame3));
        assertEquals("string to frame", "foo", map.get(frame1));
        assertSame("frame to string", frame2, map.get("bar"));
    }

    public void testInteger() {
        Map map = new HashMap();
        map.put(new Integer(1), "foo1");
        map.put("foo2", new Integer(2));
        map.put(new Integer(3), new Integer(4));

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());

        assertEquals("int to string", "foo1", map.get(new Integer(1)));
        assertEquals("string to int", new Integer(2), map.get("foo2"));
        assertEquals("int to int", new Integer(4), map.get(new Integer(3)));
    }

    public void testJavaClass() {
        Object o1 = new TestClass("a");
        Object o2 = new TestClass("b");
        Object o3 = new TestClass("c");
        Object o4 = new TestClass("d");
        Map map = new HashMap();
        map.put(o1, "foo");
        map.put("bar", o2);
        map.put(o3, o4);

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());
        assertEquals("object to string", "foo", map.get(o1));
        assertEquals("string to object", o2, map.get("bar"));
        assertEquals("object to object", o4, map.get(o3));
    }

    public void testMap() {
        Map map = new HashMap();
        Map subMap1 = new HashMap();
        subMap1.put("foo", "bar");
        map.put("submap1", subMap1);

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());

        subMap1 = (Map) map.get("submap1");
        assertNotNull("submap", subMap1);
        assertEquals("bar", subMap1.get("foo"));
    }

    public void testRectangle() {
        Rectangle r1 = new Rectangle(1, 2, 3, 4);
        Rectangle r2 = new Rectangle(5, 6, 7, 8);
        Rectangle r3 = new Rectangle(10, 20, 30, 40);
        Rectangle r4 = new Rectangle(50, 60, 70, 80);

        Map map = new HashMap();
        map.put(r1, "foo1");
        map.put("foo2", r2);
        map.put(r3, r4);

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());

        assertEquals("rect to string", "foo1", map.get(r1));
        assertEquals("string to rect", r2, map.get("foo2"));
        assertEquals("rect to rect", r4, map.get(r3));
    }

    public void testString() {
        Map map = new HashMap();
        map.put("foo", "bar");

        PropertyMapUtil.store(map, _mapInstance);
        map = PropertyMapUtil.load(_mapInstance, getDomainKB());
        assertEquals("string to string", "bar", map.get("foo"));
    }
}
