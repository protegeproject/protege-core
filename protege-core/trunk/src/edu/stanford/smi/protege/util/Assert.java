package edu.stanford.smi.protege.util;

//ESCA*JAVA0150

/**
 * A utility class for checking assertions.  This class predates the buildin "assert" language construct.  This
 * class is now implemented in terms of the "assert" language construct.
 *
 * Modeled on the JUnit Assert class.
 *
 * @author    Ray Fergerson (fergerson@smi.stanford.edu)
 */
public class Assert {

    public static void assertEquals(float x1, float x2, float delta) {
        boolean equal = Math.abs(x1 - x2) < delta;
        if (!equal) {
            throw new AssertionError("|" + x1 + " - " + x2 + "| > " + delta);
        }
    }

    public static void assertEquals(String description, float x1, float x2, float delta) {
        boolean equal = Math.abs(x1 - x2) < delta;
        if (!equal) {
            throw new AssertionError(description + ": |" + x1 + " - " + x2 + "| > " + delta);
        }
    }

    public static void assertEquals(double x1, double x2, double delta) {
        boolean equal = Math.abs(x1 - x2) < delta;
        if (!equal) {
            throw new AssertionError("|" + x1 + " - " + x2 + "| > " + delta);
        }
    }

    public static void assertEquals(String description, double x1, double x2, double delta) {
        boolean equal = Math.abs(x1 - x2) < delta;
        if (!equal) {
            throw new AssertionError(description + ": |" + x1 + " - " + x2 + "| > " + delta);
        }
    }

    public static void assertEquals(int i1, int i2) {
        if (i1 != i2) {
            throw new AssertionError(i1 + " != " + i2);
        }
    }

    public static void assertEquals(String description, int i1, int i2) {
        if (i1 != i2) {
            throw new AssertionError(description + ": " + i1 + " != " + i2);
        }
    }

    public static void assertEquals(long i1, long i2) {
        if (i1 != i2) {
            throw new AssertionError(i1 + " != " + i2);
        }
    }

    public static void assertEquals(String description, long i1, long i2) {
        if (i1 != i2) {
            throw new AssertionError(description + ": " + i1 + " != " + i2);
        }
    }

    public static void assertEquals(Object o1, Object o2) {
        boolean equal = (o1 == null) ? o2 == null : o1.equals(o2);
        if (!equal) {
            throw new AssertionError(o1 + " != " + o2);
        }
    }

    public static void assertEquals(String description, Object o1, Object o2) {
        boolean equal = (o1 == null) ? o2 == null : o1.equals(o2);
        if (!equal) {
            throw new AssertionError(description + ": " + o1 + " != " + o2);
        }
    }

    public static void assertSame(Object o1, Object o2) {
        if (o1 != o2) {
            throw new AssertionError(o1 + " not the same object as " + o2);
        }
    }

    public static void assertSame(String description, Object o1, Object o2) {
        if (o1 != o2) {
            throw new AssertionError(description + ": " + o1 + " not the same object as " + o2);
        }
    }

    public static void assertNotNull(Object o) {
        if (o == null) {
            throw new AssertionError("Object is null");
        }
    }

    public static void assertNotNull(String description, Object o) {
        if (o == null) {
            throw new AssertionError(description);
        }
    }

    public static void assertNull(Object o) {
        if (o != null) {
            throw new AssertionError(o + " is not null");
        }
    }

    public static void assertNull(String description, Object o) {
        if (o != null) {
            throw new AssertionError(description + ": " + o + " is not null");
        }
    }

    public static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Condition is false");
        }
    }

    public static void assertTrue(String description, boolean condition) {
        if (!condition) {
            throw new AssertionError(description);
        }
    }

    public static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Condition is true");
        }
    }

    public static void assertFalse(String description, boolean condition) {
        if (condition) {
            throw new AssertionError(description);
        }
    }

    public static void fail(String description) {
        throw new AssertionError(description);
    }
}
