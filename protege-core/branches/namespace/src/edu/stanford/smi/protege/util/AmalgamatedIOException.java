package edu.stanford.smi.protege.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class AmalgamatedIOException extends IOException {
    private static final long serialVersionUID = -974893021619055417L;
    
    @SuppressWarnings("unchecked")
    private Collection errors = new ArrayList();
    
    @SuppressWarnings("unchecked")
    public  AmalgamatedIOException(Collection errors) {
        super();
        this.errors = errors;
    }
    
    
    @SuppressWarnings("unchecked")
    public Collection getErrorList() {
        return Collections.unmodifiableCollection(errors);
    }
    

}
