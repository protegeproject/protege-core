package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public abstract class CollectingErrorHandler<E extends Throwable> implements ErrorHandler<E> {
    
    private Collection<E> errors = new ArrayList<E>();
    private Collection<E> warnings = new ArrayList<E>();
    

    public void warning(E e) throws E {
        warnings.add(e);
    }

    public void error(E e) throws E {
        errors.add(e);
    }

    public void fatalError(E e) throws E {
        Log.getLogger().log(Level.SEVERE, "Exception found " + e, e);
        throw e;
    }

    public void addErrors(Collection collectedErrors) {
        for (Object o : collectedErrors) {
            if (o instanceof Throwable) {
                errors.add(convertThrowable((Throwable) o));
            }
            else if (o instanceof MessageError && ((MessageError) o).getException() != null) {
                warnings.add(convertThrowable(((MessageError) o).getException()));
            }
            else {
                Log.getLogger().warning("Error found - " + o);
                Log.getLogger().warning("Badly formatted error message");
            }
        }
    }
    
    public Collection getErrors() {
        Collection collectedErrors = new ArrayList();
        for (E warning : warnings) {
            collectedErrors.add(new MessageError(warning));
        }
        for (E error : errors) {
            collectedErrors.add(error);
        }
        return collectedErrors;
    }
    
    public abstract E convertThrowable(Throwable t);

}
