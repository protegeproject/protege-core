package edu.stanford.smi.protege.util;

import java.util.logging.Level;


/**
 * Default implementation of the {@link ErrorHandler} interface.
 * 
 * <p>
 * The default behaviour is:
 * <li> <b>warning</b> - do nothing, just log the warning </li>
 * <li> <b>error</b> - do nothing, just log the error </li>
 * <li> <b>fatal error</b> - rethrow the exception </li>
 * </p>
 * 
 * @see ErrorHandler
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 * @param <X>
 */
public class DefaultErrorHandler<X extends Throwable> implements ErrorHandler<X> {

	public void warning(X e) throws X {
		Log.getLogger().log(Level.WARNING, "Warning:  " + e.getMessage(), e);		
	}

	public void error(X e) throws X {
		Log.getLogger().log(Level.WARNING, "Error: " + e.getMessage(), e);		
	}

	public void fatalError(X e) throws X {
		throw e;		
	}
	
	
}
