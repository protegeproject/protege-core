package edu.stanford.smi.protege.util.transaction.cache;

import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import junit.framework.TestCase;

public class Cache_Test extends TestCase {
    
    public static final String SESSION1 = "Timothy Redmond";
    public static final String SESSION2 = "Jennifer Vendetti";
    public static final String SESSION3 = "Tania Tudorache";
    
    public static final String VAR1 = "x";
    public static final String VAR2 = "y";
    public static final String VAR3 = "z";
    
    public static final String VAL1 = "Claudia";
    public static final String VAL2 = "natalia";
    public static final String VAL3 = "troglodyte";
    
    public void testCommon() {
        CacheResult<String> result;
        for (TransactionIsolationLevel level : TransactionIsolationLevel.values()) {
            Cache<String, String, String> cache = CacheFactory.createEmptyCache(level);
            cache.modifyCache(SESSION1, VAR1, VAL1);
            result = cache.readCache(SESSION1, VAR1);
            assertTrue(result.isValid());
            assertTrue(result.getResult().equals(VAL1));
            
            cache.modifyCache(SESSION2, VAR2);
            
            result = cache.readCache(SESSION3, VAR1);
            assertTrue(result.isValid());
            assertTrue(result.getResult().equals(VAL1));
            
            result = cache.readCache(SESSION2, VAR2);
            assertTrue(!result.isValid());
            
            cache.modifyCache(SESSION3, VAR1);
            result = cache.readCache(SESSION2, VAR1);
            assertTrue(!result.isValid());
        }
    }
    
    public void testReadUncommitted() {
        CacheResult<String> result;
        Cache<String, String, String> cache = CacheFactory.createEmptyCache(TransactionIsolationLevel.READ_UNCOMMITTED);
        cache.updateCache(SESSION1, VAR1, VAL1);
        cache.updateCache(SESSION2, VAR2, VAL2);
        cache.modifyCache(SESSION3, VAR3, VAL3);
        
        cache.beginTransaction(SESSION2);
        
        result = cache.readCache(SESSION2, VAR3);
        assertTrue(result.isValid());
    }

}
