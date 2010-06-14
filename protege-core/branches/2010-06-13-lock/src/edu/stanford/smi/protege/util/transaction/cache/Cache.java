package edu.stanford.smi.protege.util.transaction.cache;




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
 * Some usage cases follow.  First, in many cases the cache will be a cache of frame values.  In 
 * this case the caller will hold a weak hash map from Frames to a Cache from Slot-Facet-isTemplate objects
 * to a list of values.  
 * 
 * Another example is the client who receives Cache updates from the server. The client will receive a
 * cache update from the server only if either the update was made by the client itself or if the session 
 * executing the update is not in a transaction.  Thus for this case the session type is a boolean indicating
 * if the cache update came from the client itself.
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
     * value.  It can be null in which case the isValid() method should
     * be called to determine if this is just a missing cache element or 
     * actually the correct value.  This call is required to never return null
     * so that the code sequence 
     * <pre>
     *    CacheResult result = cache.readCache(session, var);
     *    if (result.isValid()) {
     *       return result.getValue();
     *    else {
     *       go to the source to  find the right value
     *    } 
     * </pre>
     * works and is the recommended approach.
     * 
     * @param var a variable
     * @return the value in the cache for the variable var.  Never returns null.
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
     * This notifies the cache that this cache is now invalid either because the object that 
     * this cache represents is deleted or for any other reason.  If this change is outside a transaction or 
     * gets committed, the cache enters the invalid state.  Once in the invalidated  
     * state all attempts to get a value return a value null value. 
     * 
     * @param session
     */
    void invalidate(S session);
    
    /**
     * This indicates  that the cache has entered the deleted state or is otherwise invalid.  
     * A cache cannot recover from this state.  
     */
    boolean isInvalid();
    
    /**
     * Try to avoid this.  If this is invoked and you are at READ_COMMITTED or above, the
     * cache can repeatedly get flushed on commit and rollback transactions until everything 
     * is closed out.
     */
    void flush();

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
    
    void abortCompleteCache();
    
    boolean isCacheComplete();

    /**
     * The session has entered a transaction.  Nesting is counted.
     * 
     * @param session
     */
    void beginTransaction(S session);

    /**
     * A transaction is committed.
     * 
     * @param session
     */
    void commitTransaction(S session);
    
    /**
     * A transaction is rolled back.
     * 
     * @param session
     */
    void rollbackTransaction(S session);
    
    /**
     * Returns the transaction nesting for a session.  This is really just a convenience
     * method for the implementation.
     */
    int getTransactionNesting(S session);
    
    /**
     * This method returns a unique id for each cache.
     * This is to give the entire aspect stack of a cache to make logging and 
     * debugging easier
     */
    int getCacheId();
}
