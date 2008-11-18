package edu.stanford.smi.protege.util.transaction.cache;

public class CacheResult<R> {
    private R result;
    private boolean isValid;
    
    public CacheResult(R result, boolean isValid) {
        super();
        this.result = result;
        this.isValid = isValid;
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

}
