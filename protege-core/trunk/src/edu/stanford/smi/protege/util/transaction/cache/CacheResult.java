package edu.stanford.smi.protege.util.transaction.cache;

import java.io.Serializable;
import java.util.Collection;

public class CacheResult<R> implements Serializable {
    private static final long serialVersionUID = -8134642352504821785L;
    private R result;
    private boolean isValid;
    
    public CacheResult(R result, boolean isValid) {
        super();
        this.result = result;
        this.isValid = isValid;
    }
    
    public static <R> CacheResult<R> getInvalid() {
    	return new CacheResult<R>(null, false);
    }

    /**
     * @return the result
     */
    public R getResult() {
        return result;
    }

    /**
     * @return the isValid
     */
    public boolean isValid() {
        return isValid;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CacheResult)) {
            return false;
        }
        CacheResult other = (CacheResult) obj;
        if (other.isValid() != isValid) {
            return false;
        }
        if (result == null) {
            return other.getResult() == null;
        }
        return result.equals(other.getResult());
    }
    
    @Override
    public int hashCode() {
        return result.hashCode() + (isValid ? 42 : 0);
    }
    
    public String toString() {
    	if (isValid()) {
        	StringBuffer sb = new StringBuffer("[");
        	sb.append("Valid Result ");
        	if (getResult() == null) {
        	    sb.append("null");
        	}
        	else {
        	    sb.append(" of type ");
        	    // It is tempting to just print getResult() here.  I think though that I remember
        	    // this causes problems in the logs when printing getResult() involves going back 
        	    // to the cache...
        	    sb.append(getResult().getClass());
        	    if (getResult() instanceof Collection<?>) {
        	        sb.append(" (size=");
        	        sb.append(((Collection<?>) getResult()).size());
        	        sb.append(")");
        	    }
        	}
    		sb.append("]");
    		return sb.toString();
    	}
    	else {
    		return "[Invalid Result]";
    	}
    }

}
