package edu.stanford.smi.protege.util.transaction.cache;

import java.io.Serializable;

public class CacheResult<R> implements Serializable {
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
        	    sb.append(getResult().getClass());
        	}
    		sb.append("]");
    		return sb.toString();
    	}
    	else {
    		return "[Invalid Result]";
    	}
    }

}
