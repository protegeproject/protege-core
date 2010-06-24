package edu.stanford.smi.protege.util.transaction.cache;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;

/*
 * It is almost impossible to even get  mild coverage through these tests.
 */

public class Cache_Test extends TestCase {
    
    public static final String SESSION1 = "Timothy Redmond";
    public static final String SESSION2 = "Jennifer Vendetti";
    public static final String SESSION3 = "Tania Tudorache";
    public static Set<String> SESSIONS = new HashSet<String>();
    static {
        SESSIONS.add(SESSION1);
        SESSIONS.add(SESSION2);
        SESSIONS.add(SESSION3);
    }
    
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
            
            result = cache.readCache(SESSION1, VAR2);
            assertTrue(!result.isValid());
            
            result = cache.readCache(SESSION2, VAR2);
            assertTrue(!result.isValid());
            
            cache.modifyCache(SESSION3, VAR1, VAL3);
            result = cache.readCache(SESSION2, VAR1);
            assertTrue(result.isValid());
            assertTrue(result.getResult().equals(VAL3));
        }
    }
    
    public void testNesting() {
        CacheResult<String> result;
        
        for (TransactionIsolationLevel level : TransactionIsolationLevel.values()) {
            Cache<String, String, String> cache = CacheFactory.createEmptyCache(level);
            assertTrue(cache.getTransactionNesting(SESSION1) == 0);
            assertTrue(cache.getTransactionNesting(SESSION2) == 0);
            assertTrue(cache.getTransactionNesting(SESSION3) == 0);
            
            cache.beginTransaction(SESSION1);
            assertTrue(cache.getTransactionNesting(SESSION1) == 1);
            assertTrue(cache.getTransactionNesting(SESSION2) == 0);
            assertTrue(cache.getTransactionNesting(SESSION3) == 0);
            
            cache.modifyCache(SESSION1, VAR1, VAL1);
            assertTrue(cache.readCache(SESSION1, VAR1).isValid());
            assertTrue(cache.readCache(SESSION1, VAR1).getResult().equals(VAL1));
            
            cache.beginTransaction(SESSION2);
            assertTrue(cache.getTransactionNesting(SESSION1) == 1);
            assertTrue(cache.getTransactionNesting(SESSION2) == 1);
            assertTrue(cache.getTransactionNesting(SESSION3) == 0);
            
            cache.modifyCache(SESSION1, VAR2, VAL2);
            assertTrue(cache.readCache(SESSION1, VAR2).isValid());
            assertTrue(cache.readCache(SESSION1, VAR2).getResult().equals(VAL2));
            
            cache.beginTransaction(SESSION1);
            assertTrue(cache.getTransactionNesting(SESSION1) == 2);
            assertTrue(cache.getTransactionNesting(SESSION2) == 1);
            assertTrue(cache.getTransactionNesting(SESSION3) == 0);
            
            cache.beginTransaction(SESSION3);
            assertTrue(cache.getTransactionNesting(SESSION1) == 2);
            assertTrue(cache.getTransactionNesting(SESSION2) == 1);
            assertTrue(cache.getTransactionNesting(SESSION3) == 1);
            
            cache.commitTransaction(SESSION2);
            assertTrue(cache.getTransactionNesting(SESSION1) == 2);
            assertTrue(cache.getTransactionNesting(SESSION2) == 0);
            assertTrue(cache.getTransactionNesting(SESSION3) == 1);
            
            cache.rollbackTransaction(SESSION1);
            assertTrue(cache.getTransactionNesting(SESSION1) == 1);
            assertTrue(cache.getTransactionNesting(SESSION2) == 0);
            assertTrue(cache.getTransactionNesting(SESSION3) == 1);
            
            cache.commitTransaction(SESSION3);
            assertTrue(cache.getTransactionNesting(SESSION1) == 1);
            assertTrue(cache.getTransactionNesting(SESSION2) == 0);
            assertTrue(cache.getTransactionNesting(SESSION3) == 0);
            
            cache.rollbackTransaction(SESSION1);
            assertTrue(cache.getTransactionNesting(SESSION1) == 0);
            assertTrue(cache.getTransactionNesting(SESSION2) == 0);
            assertTrue(cache.getTransactionNesting(SESSION3) == 0);
            
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
        assertTrue(result.getResult().equals(VAL3));
        
        cache.modifyCache(SESSION2, VAR1, VAL2);
        result=cache.readCache(SESSION3, VAR1);
        assertTrue(result.isValid());
        assertTrue(result.getResult().equals(VAL2));
        
        cache.rollbackTransaction(SESSION2);
        
        result = cache.readCache(SESSION1, VAR1);
        assertTrue(!result.isValid() || result.getResult().equals(VAL1));
        
        result = cache.readCache(SESSION1, VAR3);
        assertTrue(result.isValid());
        assertTrue(result.getResult().equals(VAL3));
        
        cache.beginTransaction(SESSION1);
        
        cache.modifyCache(SESSION1, VAR3, VAL2);
        
        result = cache.readCache(SESSION2, VAR3);
        assertTrue(result.isValid() && result.getResult().equals(VAL2));
        
        cache.commitTransaction(SESSION1);
        
        result = cache.readCache(SESSION3, VAR3);
        assertTrue(result.isValid() && result.getResult().equals(VAL2));
    }
    
    public void testReadCommitted01() {
        itestReadCommitted01(true);
        itestReadCommitted01(false);
    }
    
    public void itestReadCommitted01(boolean commit) {
        for (TransactionIsolationLevel level : TransactionIsolationLevel.values()) {
            if (level.compareTo(TransactionIsolationLevel.READ_COMMITTED) < 0) {
                continue;
            }
            Cache<String, String, String> cache = CacheFactory.createEmptyCache(level);
            CacheResult<String> result;
            
            cache.updateCache(SESSION1, VAR1, VAL1);
            cache.updateCache(SESSION2, VAR2, VAL2);
            cache.updateCache(SESSION3, VAR3, VAL3);
            
            cache.beginTransaction(SESSION3);
            cache.modifyCache(SESSION3, VAR1, VAL3);
            
            result = cache.readCache(SESSION1, VAR1);
            assertTrue(result.isValid() && result.getResult().equals(VAL1));
            
            result  = cache.readCache(SESSION3, VAR1);
            assertTrue(result.isValid() && result.getResult().equals(VAL3));
            
            result = cache.readCache(SESSION1, VAR2);
            assertTrue(result.isValid() && result.getResult().equals(VAL2));
            
            cache.modifyCache(SESSION2, VAR2, VAL1);
            
            result = cache.readCache(SESSION1, VAR2);
            assertTrue(result.isValid() && result.getResult().equals(VAL1));
            
            result = cache.readCache(SESSION2, VAR2);
            assertTrue(result.isValid() && result.getResult().equals(VAL1)); 
            
            cache.modifyCache(SESSION3, VAR2);
            
            result = cache.readCache(SESSION2, VAR2);
            assertTrue(result.isValid() && result.getResult().equals(VAL1)); 
            
            result = cache.readCache(SESSION3, VAR2);
            assertTrue(!result.isValid());
            
            if (commit) {
                cache.commitTransaction(SESSION3);
                for (String session : SESSIONS) {
                    result = cache.readCache(session, VAR1);
                    assertTrue(result.isValid() && result.getResult().equals(VAL3));
                    
                    result = cache.readCache(session, VAR2);
                    assertTrue(!result.isValid());
                    
                    result = cache.readCache(session, VAR3);
                    assertTrue(result.isValid() && result.getResult().equals(VAL3));
                }
            }
            else {
                cache.rollbackTransaction(SESSION3);
                for (String session : SESSIONS) {
                    result = cache.readCache(session, VAR1);
                    assertTrue(!result.isValid() || result.getResult().equals(VAL1));
                    
                    result = cache.readCache(session, VAR2);
                    assertTrue(!result.isValid() || result.getResult().equals(VAL1));
                    
                    result = cache.readCache(session, VAR3);
                    assertTrue(result.isValid() && result.getResult().equals(VAL3));
                }
            }
            
            cache.modifyCache(SESSION1, VAR1, VAL1);
            cache.modifyCache(SESSION1, VAR2, VAL2);
            
            cache.beginTransaction(SESSION2);
            cache.modifyCache(SESSION2, VAR1, VAL2);
            cache.modifyCache(SESSION2, VAR2);
            cache.modifyCache(SESSION1, VAR1);
            cache.modifyCache(SESSION1, VAR2,VAL3);
            if (commit) {
                cache.commitTransaction(SESSION2);
                for (String session : SESSIONS) {
                    result = cache.readCache(session, VAR1);
                    assertTrue(result.isValid() && result.getResult().equals(VAL2));
                    
                    result = cache.readCache(session, VAR2);
                    assertTrue(!result.isValid());
                }
            }
            else {
                cache.rollbackTransaction(SESSION2);
                for (String session :  SESSIONS) {
                    result = cache.readCache(session, VAR1);
                    assertTrue(!result.isValid());
                    
                    result = cache.readCache(session, VAR2);
                    assertTrue(result.isValid() && result.getResult().equals(VAL3));
                }
            }
            
        }
    }

}
