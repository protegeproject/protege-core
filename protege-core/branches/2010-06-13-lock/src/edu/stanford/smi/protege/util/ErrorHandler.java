package edu.stanford.smi.protege.util;


/**
 * 
 * Basic interface for Protege error handlers.
 * 
 * <p>
 * NOTE: Protege does not use this interface for basic error handling, but we plan to do add this feature in the near future.
 * However, there are certain components in Protege that already make use of this interface.
 * </p>
 * 
 * If an application needs to implement customized error handling, 
 * then it can implement this interface and register an instance of it with the component that already supports 
 * this type of error handling by calling the <code> setErrorHandler </code> method.  
 * The component will then report all errors and warnings through this interface. 
 * 
 * <p>
 * The components that use this type of error handling should use this interface instead of throwing an exception.
 * It is up to the application to decide whether to throw an exception for different types of errors and warnings or not.
 * </p> 
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 * @param <E> - the throwable class that is used as an argument and thrown exception by all methods of this interface
 * (e.g. <code>ErrorHandler&lt;ConversionException&gt;</code>) 
 */
public interface ErrorHandler<E extends Throwable> {

	/** 
	 * Receive notification of a warning.
	 * <p>
	 *  Components will use this method to report conditions that are not errors or fatal errors
	 *  The default behavior is to take no action. The component should continue to provide normal operation events
	 *  after invoking this method: it should still be possible for the application to continue normal operation.
	 *  </p>
	 * @param e - The warning information encapsulated in an exception of type <code>{@link E}</code>.
	 * @throws E - The same type of exception as the method argument
	 */
	public void warning(E e) throws E;
	
	/** 
	 * Receive notification of a recoverable error.
	 * <p>
	 *  Components will use this method to report conditions that are not fatal errors.
	 *  The default behavior is to take no action. The component should continue to provide normal operation events
	 *  after invoking this method: it should still be possible for the application to continue normal operation.
	 *  If the component cannot continue normal operation is should throw a fatal error.
	 *  </p>
	 * @param e - The error information encapsulated in an exception of type <code>{@link E}</code>.
	 * @throws E - The same type of exception as the method argument
	 */
	public void error(E e) throws E;
	
	/** 
	 * Receive notification of an unrecoverable error.
	 * <p>
	 * Components will use this method to report conditions that are fatal errors.
	 * The default behavior is to rethrow the exception.
	 * The application should not assume that the component will continue normal operation after a fatal error.
	 * The application should continue (if at all) only for the sake of collecting addition error messages.
	 *  
	 *  </p>
	 * @param e - The error information encapsulated in an exception of type <code>{@link E}</code>.
	 * @throws E - The same type of exception as the method argument
	 */
	public void fatalError(E e) throws E;

}
