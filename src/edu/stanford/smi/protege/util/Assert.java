package edu.stanford.smi.protege.util;

/**
 * A utility class for checking assertions.  This class predates the buildin "assert" language construct.  This
 * class is now implemented in terms of the "assert" language construct.
 *
 * Modeled on the JUnit Assert class.
 *
 * @author    Ray Fergerson (fergerson@smi.stanford.edu)
 */
public class Assert {

    public static void assertEquals(String description, float x1, float x2, float delta) {
        doAssert(Math.abs(x1 - x2) < delta, description + ", x1=" + x1 + ", x2=" + x2 + ", delta=" + delta);
    }

    public static void assertEquals(String description, double x1, double x2, double delta) {
        doAssert(Math.abs(x1 - x2) < delta, description + ", x1=" + x1 + ", x2=" + x2 + ", delta=" + delta);
    }

    public static void assertEquals(String description, int i1, int i2) {
        doAssert(i1 == i2, description + ", i1=" + i1 + ", i2=" + i2);
    }

    public static void assertEquals(String description, long i1, long i2) {
        doAssert(i1 == i2, description + ", l1=" + i1 + ", l2=" + i2);
    }

    public static void assertEquals(String description, Object o1, Object o2) {
        boolean equal = (o1 == null) ? o2 == null : o1.equals(o2);
        doAssert(equal, description + ", o1=" + o1 + ", o2=" + o2);
    }

    public static void assertFalse(String description, boolean condition) {
        doAssert(!condition, description);
    }

    public static void assertNotNull(String description, Object o) {
        doAssert(o != null, description);
    }

    public static void assertNull(String description, Object o) {
        doAssert(o == null, description + ", o=" + o);
    }

    public static void assertSame(String description, Object o1, Object o2) {
        doAssert(o1 == o2, description + ", o1=" + o1 + ", o2=" + o2);
    }

    public static void assertTrue(String description, boolean condition) {
        doAssert(condition, description);
    }

    public static void fail(String description) {
        doAssert(false, description);
    }

    private static void doAssert(boolean test, String description) {
        if (!test) {
            throw new AssertionError(description);
        }
    }
}
