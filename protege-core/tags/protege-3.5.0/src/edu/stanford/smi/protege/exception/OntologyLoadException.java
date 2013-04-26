package edu.stanford.smi.protege.exception;


public class OntologyLoadException extends Exception {

    private static final long serialVersionUID = 2464503050573445720L;

    private String message;

    private String suggestion;

    public OntologyLoadException() {
    	this(null, null, null);
    }

    public OntologyLoadException(Throwable t) {
    	this(t, null, null);
	}
    
    public OntologyLoadException(Throwable t, String message) {
    	this(t, message, null);
    }
    
    public OntologyLoadException(Throwable t, String message, String suggestion) {
        super(t);
        this.message = message;
        this.suggestion = suggestion;
    }


    public String toString() {
        String str = message;
        if (suggestion != null) {
            str += "\nSuggestion: " + suggestion;
        }
        return str;
    }
}
