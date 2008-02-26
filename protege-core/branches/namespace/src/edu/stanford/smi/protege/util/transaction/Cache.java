package edu.stanford.smi.protege.util.transaction;

import java.util.Iterator;
import java.util.Map;


/**
 * This interface represents a simple cache mechanism for storing cached  
 * values either globally or on a per session basis.  This cache mechanism is 
 * caching the values (of type R) of a variable (of type V).
 *
 * There are three issues that make the interpretation of this interface rather
 * complex.  The first issue is the easier one.  When we do a cache lookup and 
 * get a non-null value, it is clear that the cache has the result that we want
 * to use.  But in some occasions it is important to also know that the cache is
 * complete in the sense that if we get a null value it is also the right answer.
 * To handle this we have created an interval between Cache.startCompleteCache
 * and Cache.finishCompleteCache were all entries added form a complete list of 
 * possible values in the cache.  We allow for the possibility that values will  be
 * invalidated between Cache.startCompleteCache and Cache.finishCompleteCache and even
 * that they will be invalidated after and in that case we keep track of which items
 * may be incomplete and which items are not.
 * 
 * The second issue is a bruiser!  Transactions!! Oh man!!! This adds a lot of complexity.
 * This means that different sessions might see different values.  A session in a transaction
 * may see values that are not visible to other sessions.  In addition, the behavior depends
 * on the transaction isolation level.  If the level is Read committed  or higher, changes made
 * by a session in a transaction are not visible to other sessions until the transaction is 
 * committed.  This means that we need to distinguish between values read and values written and 
 * we need to keep track of who is doing the read or write.  If the level is repeatable read or 
 * higher then the cache must not return a value for read even if the right value is known because
 * the underlying mechanism needs to be informed of the read.
 * 
 * The third issue is that we want it to be possible to garbage collect.  This requires a little
 * care because we have to be aware if the cache is complete.
 * 
 * @author tredmond
 *
 * @param <S> the session type
 * @param <V> the variable type
 * @param <R> the result set type
 */
public interface Cache<S, V, R> {

    /**
     * If  the variable var is cached then this returns the cached
     * value.  It can be null in which case the isCached() method should
     * be called to determine if this is just a missing cache element or 
     * actually the correct value.
     * 
     * @param var a variable
     * @return the value in the cache for the variable var.
     */
    CacheResult<R> readCache(S session, V var);

    /**
     * This call indicates that a read was scheduled but no useful value
     * was retrieved.  This can happen between a startCompleteCache and a 
     * finishCompleteCache.  The process filling the cache is supposed to 
     * read all possible variables that might have a value.  But the process
     * either doesn't want to read the var (too expensive) or was not able to.
     * This allows the cache to understand that it does not have a value at this
     * var.
     * 
     * @param session the session making the change
     * @param var
     * @param value
     */
    void updateCache(S session, V var);

    /**
     * The caller has received an update that the new value of var is value.
     * This as viewed as a value read.  The session is important for two reasons.
     * First, if the transaction isolation level is repeatable read, a session must
     * not use the cache on the first read during a transaction even if the correct
     * value is known.  The underlying mechanism must be invoked so that it can record 
     * that a read has occurred. Second the session is important to determine if the value
     * being read might be visible only in the single 
     * 
     * @param session the session making the change
     * @param var
     * @param value
     */
    void updateCache(S session, V var, R value);

    /**
     * This notifies the cache, that the caller has made a change to the 
     * value of the var.  It is important that the caller always calls one
     * of the modifyCache calls when making changes to cached values.
     * 
     * @param session the session making the change
     * @param var the variable being modified.
     */
    void modifyCache(S session, V var);

    /**
     * This notifies the cache that the caller has made a change to the 
     * value of var and that the caller knows the new value is value.
     * 
     * @param session the session making the change
     * @param var the variable being changed
     * @param value the new value for the variable
     */
    void modifyCache(S session, V var, R value);
    
    /**
     * Creates an iterator over the entry set.  It is assumed that
     * calls to remove on the iterator or setValue on the Entry 
     * are done as a result of changes made by the user in session.
     * 
     * @param session
     * @return iterator
     */
    Iterator<Map.Entry<V, R>> iterator(S session);

    /**
     * The startCompleteCache and the finishCompleteCache are part of a protocol.  In the 
     * interval between the startCompleteCache and the finishCompleteCache, the values in the
     * cache - with the exception of any entries that have been explicitly invalidated - are 
     * a complete list of all the real values.
     */
    void startCompleteCache();

    /**
     * The startCompleteCache and the finishCompleteCache are part of a protocol.  In the 
     * interval between the startCompleteCache and the finishCompleteCache, the values in the
     * cache - with the exception of any entries that have been explicitly invalidated - are 
     * a complete list of all the real values.
     */
    void finishCompleteCache();

    /**
     * The session has entered a transaction.  Nesting is counted.
     * 
     * @param session
     */
    void beginTransaction(S session);

    /**
     * The session has left a transaction.  Nesting is counted.
     * 
     * @param session
     */
    void endTransaction(S session);
}
