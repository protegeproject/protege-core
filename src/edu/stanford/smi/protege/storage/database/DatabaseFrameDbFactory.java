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

	private static Class<? extends DatabaseFrameDb>	implementation;
	private static Class<? extends DatabaseFrameDb>	fallback;

	static {
		fallback = DefaultDatabaseFrameDb.class;

		String className = System
				.getProperty( "edu.stanford.smi.protege.storage.database.DatabaseFrameDb.class" );

		if( className == null ) {
			implementation = fallback;
		}
		else {
			try {
				Class<?> clazz = Class.forName( className );
				implementation = clazz.asSubclass( DatabaseFrameDb.class );
			} catch( ClassNotFoundException e ) {
				log.warning( className + " not found, using default DatabaseFrameDb" );
				implementation = fallback;
			} catch( ClassCastException e ) {
				log.warning( className
						+ " does not implement DatabaseFrameDb, using default DatabaseFrameDb" );
				implementation = fallback;
			}
		}
	}

	public static void setDefaultImplementation(Class<? extends DatabaseFrameDb> clazz) {
		implementation = clazz;
	}

	public static DatabaseFrameDb createDatabaseFrameDb() {
		try {
			return implementation.newInstance();
		} catch( InstantiationException e ) {
			log.log( Level.SEVERE, "Failed to instantiate DatabaseFrameDb", e );
			return null;
		} catch( IllegalAccessException e ) {
			log.log( Level.SEVERE, "Illegal access exception while creating DatabaseFrameDb", e );
			return null;
		}
	}
}
