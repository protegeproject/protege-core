package edu.stanford.smi.protege.exception;

/**
 * A basic exception class for all protege exceptions.  It is expected that
 * programs will not declare throwing this exception but will specify subclasses
 * instead.
 * 
 * @author tredmond
 *
 */

public class ProtegeException extends RuntimeException {

    private static final long serialVersionUID = 1309127978902548057L;

    public ProtegeException() {
        super();
    }

    public ProtegeException(String msg) {
        super(msg);
    }

    public ProtegeException(String msg, Throwable t) {
        super(msg, t);
    }

    public ProtegeException(Throwable t) {
        super(t);
    }
}
