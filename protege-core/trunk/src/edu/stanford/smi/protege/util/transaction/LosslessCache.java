package edu.stanford.smi.protege.util.transaction;


/**
 * This cache is the same as the Cache interface except it indicates
 * that the cache cannot forget data.  This is important for the session 
 * caches because we use these caches to determine if a value for a variable is 
 * only seen within the session's transaction or whether it is a value that is
 * seen by all.
 * 
 * @author tredmond
 *
 */
public interface LosslessCache<V,R> extends Cache<V,R> {

}
