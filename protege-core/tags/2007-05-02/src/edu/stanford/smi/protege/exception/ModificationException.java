package edu.stanford.smi.protege.exception;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ModificationException extends ProtegeIOException {
  private static final long serialVersionUID = 8125114376426533208L;

    public ModificationException(String s) {
        super(s);
    }
}
