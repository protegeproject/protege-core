package edu.stanford.smi.protege.util;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public final class HashUtils {
    private static final int MULTIPLIER = 37;

    public static int getHash(Object o1, Object o2) {
        int hash = hash(o1);
        hash = combine(hash, hash(o2));
        return hash;
    }
    
    public static int getHash(int i1, int i2) {
      int hash = combine(i1, i2);
      return hash;
    }

    public static int getHash(Object o1, Object[] oArray) {
        int hash = hash(o1);
        hash = combine(hash, hash(oArray));
        return hash;
    }

    public static int getHash(Object o1, Object o2, boolean b) {
        int hash = hash(o1);
        hash = combine(hash, hash(o2));
        hash = combine(hash, hash(b));
        return hash;
    }

    public static int getHash(Object o1, Object o2, Object o3) {
        int hash = hash(o1);
        hash = combine(hash, hash(o2));
        hash = combine(hash, hash(o3));
        return hash;
    }

    public static int getHash(Object o1, Object o2, Object o3, boolean b) {
        int hash = hash(o1);
        hash = combine(hash, hash(o2));
        hash = combine(hash, hash(o3));
        hash = combine(hash, hash(b));
        return hash;
    }

    private static int combine(int hash1, int hash2) {
        return MULTIPLIER * hash1 + hash2;
    }

    private static int hash(Object o1) {
        return (o1 == null) ? 0 : o1.hashCode();
    }

    private static int hash(boolean b) {
        return b ? 1 : 104729;
    }

    private static int hash(Object[] array) {
        int hash = 0;
        for (int i = 0; i < array.length; ++i) {
            Object o = array[i];
            hash = combine(hash, hash(o));
        }
        return hash;
    }
}
