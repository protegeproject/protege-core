package edu.stanford.smi.protege.exception;


/**
 * This exception represents errors accessing the backing store that 
 * are not related to problems with the ontology.  These exceptions encapsulate
 * file io exceptions, sql exceptions and remote exceptions.
 * 
 * @author tredmond
 *
 */
public class ProtegeIOException extends ProtegeException {
    private static final long serialVersionUID = 7431568981527965048L;

    public ProtegeIOException() {
        super();
    }

    public ProtegeIOException(String msg) {
        super(msg);
    }

    public ProtegeIOException(String msg, Throwable t) {
        super(msg, t);
    }

    public ProtegeIOException(Throwable t) {
        super(t);
    }

}
