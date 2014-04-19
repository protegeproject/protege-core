package edu.stanford.smi.protege.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;



public class AmalgamatedLoadException extends OntologyLoadException implements AmalgamatedException {
    private static final long serialVersionUID = -974893021619055417L;
    
    @SuppressWarnings("unchecked")
    private Collection errors = new ArrayList();
    
    @SuppressWarnings("unchecked")
    public  AmalgamatedLoadException(Collection errors) {
        super();
        this.errors = errors;
    }
    
    @SuppressWarnings("unchecked")
    public void addError(Object error) {
        errors.add(error);
    }
    
    @SuppressWarnings("unchecked")
    public void addErrors(Collection errors) {
        this.errors.addAll(errors);
    }
    
    
    @SuppressWarnings("unchecked")
    public Collection getErrorList() {
        return Collections.unmodifiableCollection(errors);
    }
    

}
