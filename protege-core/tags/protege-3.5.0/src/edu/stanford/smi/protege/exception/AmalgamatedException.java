package edu.stanford.smi.protege.exception;

import java.util.Collection;
import java.util.Collections;

public interface AmalgamatedException {
    
    void addError(Object error);
    
    @SuppressWarnings("unchecked")
    void addErrors(Collection errors);
    
    @SuppressWarnings("unchecked")
    Collection getErrorList();

}
