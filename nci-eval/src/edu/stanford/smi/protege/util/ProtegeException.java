package edu.stanford.smi.protege.util;

public class ProtegeException extends RuntimeException {
    public ProtegeException(Throwable t) {
        super(t.toString());
    }
}