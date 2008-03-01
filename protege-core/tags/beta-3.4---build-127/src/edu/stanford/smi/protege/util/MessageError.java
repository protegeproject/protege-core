package edu.stanford.smi.protege.util;

/**
 * Class used for reporting errors at project load time. An instance of this class
 * contains an exception and a string message explaining the error.
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class MessageError {
	private Exception exception;
	private String message;
	
	public MessageError(String message) {
		this(null, message);
	}
	
	public MessageError(Exception ex) {
		this(ex,null);
	}
	
	public MessageError(Exception ex, String message) {
		this.exception = ex;
		this.message = message;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
