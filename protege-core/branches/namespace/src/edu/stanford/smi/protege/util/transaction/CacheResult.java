package edu.stanford.smi.protege.util.transaction;

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

}
