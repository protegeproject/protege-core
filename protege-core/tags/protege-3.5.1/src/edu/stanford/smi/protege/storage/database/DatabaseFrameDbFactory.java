package edu.stanford.smi.protege.storage.database;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

/**
 * <p>
 * Title: DatabaseFrameDbFactory
 * </p>
 * <p>
 * Description: Factory to create instances of {@link DatabaseFrameDb}
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class DatabaseFrameDbFactory {

	final private static Logger						log	= Log
																.getLogger( DatabaseFrameDbFactory.class );



	public static DatabaseFrameDb createDatabaseFrameDb(Class<? extends DatabaseFrameDb> clz) {
		try {
			return clz.newInstance();
		} catch( InstantiationException e ) {
			log.log( Level.SEVERE, "Failed to instantiate DatabaseFrameDb", e );
			return null;
		} catch( IllegalAccessException e ) {
			log.log( Level.SEVERE, "Illegal access exception while creating DatabaseFrameDb", e );
			return null;
		}
	}
	
	public static Class<? extends DatabaseFrameDb> getFrameDbClass(String className) {
	    Class<? extends DatabaseFrameDb> dbFrameDbClass = DefaultDatabaseFrameDb.class;
	    if( className != null ) {
	        try {
	            Class<?> clazz = Class.forName( className );
	            dbFrameDbClass = clazz.asSubclass( DatabaseFrameDb.class );
	        } catch( ClassNotFoundException e ) {
	            log.warning( className + " not found, using default DatabaseFrameDb" );
	        } catch( ClassCastException e ) {
	            log.warning( className
	                         + " does not implement DatabaseFrameDb, using default DatabaseFrameDb" );
	        }
	    }
	    return dbFrameDbClass;
	}
}
