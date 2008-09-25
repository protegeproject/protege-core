package com.clarkparsia.protege3.storage.database;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.ReferenceImpl;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.storage.database.AbstractDatabaseFrameDb;
import edu.stanford.smi.protege.storage.database.DatabaseFrameDb;
import edu.stanford.smi.protege.storage.database.DatabaseUtils;
import edu.stanford.smi.protege.storage.database.RobustConnection;
import edu.stanford.smi.protege.util.Log;

/**
 * <p>
 * Title: CPDatabaseFrameDb
 * </p>
 * <p>
 * Description: Implementation of {@link DatabaseFrameDb}
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
public class CPDatabaseFrameDb extends AbstractDatabaseFrameDb {

	private static DateFormat		df						= new SimpleDateFormat(
																	"yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
	private static final String		FRAME_ID_COLUMN, FRAME_NAME_COLUMN, FRAME_TYPE_COLUMN;

	public static final long		MAX_BATCH_VALUE_INSERT	= 50000;

	private static final String		SCHEMA_PROPERTIES_FILE	= "database-config.properties";

	private static final Properties	schemaProperties;
	private static final String		VALUE_FRAME_COLUMN, VALUE_SLOT_COLUMN, VALUE_FACET_COLUMN,
			VALUE_IS_TEMPLATE_COLUMN, VALUE_TYPE_COLUMN, VALUE_INDEX_COLUMN,
			VALUE_VALUE_FRAME_COLUMN, VALUE_VALUE_SHORT_COLUMN, VALUE_VALUE_LONG_COLUMN;

	static {
		schemaProperties = new Properties();
		try {
			schemaProperties.load( CPDatabaseFrameDb.class
					.getResourceAsStream( SCHEMA_PROPERTIES_FILE ) );
		} catch( IOException e ) {
			throw new RuntimeException( e );
		}

		FRAME_ID_COLUMN = schemaProperties.getProperty( "FRAME_ID_COLUMN", "FRAME_ID" );
		FRAME_TYPE_COLUMN = schemaProperties.getProperty( "FRAME_TYPE_COLUMN", "FRAME_TYPE" );
		FRAME_NAME_COLUMN = schemaProperties.getProperty( "FRAME_NAME_COLUMN", "FRAME_NAME" );

		VALUE_FRAME_COLUMN = schemaProperties.getProperty( "VALUE_FRAME_COLUMN", "FRAME" );
		VALUE_SLOT_COLUMN = schemaProperties.getProperty( "VALUE_SLOT_COLUMN", "SLOT" );
		VALUE_FACET_COLUMN = schemaProperties.getProperty( "VALUE_FACET_COLUMN", "FACET" );
		VALUE_IS_TEMPLATE_COLUMN = schemaProperties.getProperty( "VALUE_IS_TEMPLATE_COLUMN",
				"IS_TEMPLATE" );
		VALUE_INDEX_COLUMN = schemaProperties.getProperty( "VALUE_INDEX_COLUMN", "VALUE_INDEX" );
		VALUE_TYPE_COLUMN = schemaProperties.getProperty( "VALUE_TYPE_COLUMN", "VALUE_TYPE" );
		VALUE_VALUE_FRAME_COLUMN = schemaProperties.getProperty( "VALUE_VALUE_FRAME_COLUMN",
				"VALUE_FRAME" );
		VALUE_VALUE_SHORT_COLUMN = schemaProperties.getProperty( "VALUE_VALUE_SHORT_COLUMN",

		"VALUE_SHORT" );
		VALUE_VALUE_LONG_COLUMN = schemaProperties.getProperty( "VALUE_VALUE_LONG_COLUMN",
				"VALUE_LONG" );
	}

	private static String replaceSQLVariantStrings(RobustConnection connection, String sql) {
		Pattern p;
		Matcher m;

		p = Pattern.compile( "@SQL_LIKE_ESCAPE_CLAUSE@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getEscapeClause() );

		return sql;

	}

	private static String replaceDBSchemaStrings(String table, String sql) {

		Pattern p;
		Matcher m;

		p = Pattern.compile( "@TABLE_PREFIX@" );
		m = p.matcher( sql );
		sql = m.replaceAll( table );

		p = Pattern.compile( "@FRAME_ID_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( FRAME_ID_COLUMN );

		p = Pattern.compile( "@FRAME_NAME_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( FRAME_NAME_COLUMN );

		p = Pattern.compile( "@FRAME_TYPE_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( FRAME_TYPE_COLUMN );

		p = Pattern.compile( "@VALUE_FRAME_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_FRAME_COLUMN );

		p = Pattern.compile( "@VALUE_SLOT_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_SLOT_COLUMN );

		p = Pattern.compile( "@VALUE_FACET_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_FACET_COLUMN );

		p = Pattern.compile( "@VALUE_IS_TEMPLATE_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_IS_TEMPLATE_COLUMN );

		p = Pattern.compile( "@VALUE_INDEX_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_INDEX_COLUMN );

		p = Pattern.compile( "@VALUE_TYPE_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_TYPE_COLUMN );

		p = Pattern.compile( "@VALUE_VALUE_FRAME_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_VALUE_FRAME_COLUMN );

		p = Pattern.compile( "@VALUE_VALUE_SHORT_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_VALUE_SHORT_COLUMN );

		p = Pattern.compile( "@VALUE_VALUE_LONG_COLUMN@" );
		m = p.matcher( sql );
		sql = m.replaceAll( VALUE_VALUE_LONG_COLUMN );

		return sql;
	}

	private static String replaceDBTypeStrings(RobustConnection connection, String sql) {

		Pattern p;
		Matcher m;

		p = Pattern.compile( "@DBTYPE_INTEGER@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getIntegerTypeName() );

		p = Pattern.compile( "@DBTYPE_FRAMENAME@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getFrameNameType() );

		p = Pattern.compile( "@DBTYPE_SMALLINT@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getSmallIntTypeName() );

		p = Pattern.compile( "@DBTYPE_SHORTVALUE@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getShortValueType() );

		p = Pattern.compile( "@DBTYPE_LONGVALUE@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getLongvarcharTypeName() );

		p = Pattern.compile( "@DBTYPE_BIT@" );
		m = p.matcher( sql );
		sql = m.replaceAll( connection.getBitTypeName() );

		return sql;
	}

	private static void setValueParameters(RobustConnection connection, PreparedStatement stmt,
			int shortIndex, int longIndex, Object o) throws SQLException {
		final String value = o.toString();
		if( isShortValue( connection, value ) ) {
			stmt.setString( shortIndex, value );
			stmt.setNull( longIndex, Types.LONGVARCHAR );
		}
		else {
			stmt.setNull( shortIndex, Types.VARCHAR );
			stmt.setString( longIndex, value );
		}
	}

	private static boolean isShortValue(RobustConnection connection, String value) {
		return (value.length() <= connection.getMaxVarcharSize());
	}

	private int								maxFrameId				= 0;
	private Map<String, PreparedStatement>	preparedStatementMap	= new HashMap<String, PreparedStatement>();

	public CPDatabaseFrameDb() {
		super();
	}

	public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {

		try {
			final RobustConnection connection = getCurrentConnection();
			int index;

			int oldMaxFrame = maxFrameId;

			final int frId = getDatabaseId( connection, frame, true, null );
			final int slId = getDatabaseId( connection, slot, true, null );
			int faId = 0;

			if( oldMaxFrame == maxFrameId ) {
				PreparedStatement maxSelect;
				if( facet == null ) {
					maxSelect = getPreparedStatement( connection,
							"SELECT_MAX_NULL_FACET_VALUE_INDEX_SQL" );
					maxSelect.setInt( 1, frId );
					maxSelect.setInt( 2, slId );
					maxSelect.setBoolean( 3, isTemplate );

				}
				else {
					faId = getDatabaseId( connection, facet, true, null );
					maxSelect = getPreparedStatement( connection,
							"SELECT_MAX_FACET_VALUE_INDEX_SQL" );
					maxSelect.setInt( 1, frId );
					maxSelect.setInt( 2, slId );
					maxSelect.setInt( 3, faId );
					maxSelect.setBoolean( 4, isTemplate );
				}
				ResultSet maxRset = executeQuery( maxSelect );
				if( maxRset.next() )
					index = maxRset.getInt( 1 ) + 1;
				else
					index = 0;
			}
			else
				index = 0;

			addValues( connection, frId, slId, faId, isTemplate, values, index );
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	private void addValues(RobustConnection connection, int frameId, int slotId, int facetId,
			boolean isTemplate, Collection values, int index) throws SQLException {
		PreparedStatement insStmt = getPreparedStatement( connection, (facetId == 0)
			? "INSERT_NULL_FACET_VALUE_SQL"
			: "INSERT_FACET_VALUE_SQL" );
		for( Object o : values ) {
			if( isNullValue( o ) ) {
				Log.getLogger().warning( "Skipping addition of null value" );
			}
			else {
				completeInsertStatement( connection, insStmt, frameId, slotId, facetId, isTemplate,
						index++, o, null );
				insStmt.addBatch();
			}
		}
		insStmt.executeBatch();
	}

	private void completeInsertStatement(RobustConnection connection, PreparedStatement insert,
			int frameId, int slotId, int facetId, boolean isTemplate, int index, Object value,
			Map<Frame, Integer> frameIdCache) throws SQLException {

		final int facetSpace = (facetId == 0)
			? 0
			: 1;
		final int type = DatabaseUtils.valueType( value, _frameFactory );
		insert.setInt( 1, frameId );
		insert.setInt( 2, slotId );
		if( facetSpace == 1 )
			insert.setInt( 3, facetId );
		insert.setBoolean( 3 + facetSpace, isTemplate );
		insert.setInt( 4 + facetSpace, index );
		if( type >= DatabaseUtils.BASE_FRAME_TYPE_VALUE ) {
			final int vId = getDatabaseId( connection, (Frame) value, true, frameIdCache );
			insert.setInt( 5 + facetSpace, (short) DatabaseUtils.BASE_FRAME_TYPE_VALUE );
			insert.setInt( 6 + facetSpace, vId );
			insert.setString( 7 + facetSpace, null );
			insert.setString( 8 + facetSpace, null );
		}
		else {
			insert.setInt( 5 + facetSpace, (short) type );
			insert.setNull( 6 + facetSpace, Types.INTEGER );
			setValueParameters( getCurrentConnection(), insert, 7 + facetSpace, 8 + facetSpace,
					value );
		}
	}

	public void deleteFrame(Frame frame) {

		try {
			final RobustConnection connection = getCurrentConnection();

			int fId = getDatabaseId( connection, frame, false, null );
			if( fId < 0 )
				throw new IllegalStateException();

			PreparedStatement delete;
			delete = getPreparedStatement( connection, "DELETE_FACET_VALUE_BY_FRAME_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_NULL_FACET_VALUE_BY_FRAME_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_FACET_VALUE_BY_SLOT_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_NULL_FACET_VALUE_BY_SLOT_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_FACET_VALUE_BY_FACET_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_FACET_VALUE_BY_VALUE_FRAME_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_NULL_FACET_VALUE_BY_VALUE_FRAME_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

			delete = getPreparedStatement( connection, "DELETE_FRAME_SQL" );
			delete.setInt( 1, fId );
			executeUpdate( delete );

		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
		// FIXME
		System.err.println( "CPDatabaseFrameDB: getClosure" );
		throw new UnsupportedOperationException( "getClosure" );
	}

	public int getClsCount() {
		// FIXME
		System.err.println( "CPDatabaseFrameDB: getClsCount" );
		throw new UnsupportedOperationException( "getClsCount" );
	}

	/**
	 * Get database integer identifier for a frame, optionally adding if not
	 * present
	 * 
	 * @param insert
	 *            {@code true} if frame should be added if missing {@code false}
	 *            else
	 * @return -1 is missing and {@code insert = false}, database identifier
	 *         else
	 */
	private int getDatabaseId(RobustConnection connection, Frame frame, boolean insert,
			Map<Frame, Integer> cache) throws SQLException {

		Integer id = (cache == null)
			? null
			: cache.get( frame );

		if( id == null ) {
			PreparedStatement select = getPreparedStatement( connection, "SELECT_FRAME_ID_SQL" );
			select.setString( 1, frame.getFrameID().getName() );
			select.setInt( 2, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
			ResultSet idRset = executeQuery( select );
			if( idRset.next() )
				id = idRset.getInt( 1 );
			else {
				if( !insert )
					return -1;

				if( maxFrameId == 0 ) {
					PreparedStatement maxSelect = getPreparedStatement( connection,
							"SELECT_MAX_FRAME_ID_SQL" );
					ResultSet maxRset = executeQuery( maxSelect );
					if( maxRset.next() )
						maxFrameId = maxRset.getInt( 1 );
				}
				id = ++maxFrameId;

				PreparedStatement insStmt = getPreparedStatement( connection, "INSERT_FRAME_SQL" );
				insStmt.setInt( 1, id );
				insStmt.setInt( 2, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
				insStmt.setString( 3, frame.getFrameID().getName() );
				executeUpdate( insStmt );
			}
			if( cache != null )
				cache.put( frame, id );
		}

		return id.intValue();
	}

	public NarrowFrameStore getDelegate() {
		return null;
	}

	public int getFacetCount() {
		// FIXME
		System.err.println( "CPDatabaseFrameDB: getFacetCount" );
		throw new UnsupportedOperationException( "getFacetCount" );
	}

	public Frame getFrame(FrameID id) {

		if( id == null )
			throw new NullPointerException();

		try {
			Frame ret;
			PreparedStatement select = getPreparedStatement( getCurrentConnection(),
					"SELECT_FRAME_BY_NAME_SQL" );
			select.setString( 1, id.getName() );

			ResultSet rs = executeQuery( select );
			try {
				if( rs.next() ) {
					ret = createFrame( rs.getByte( 1 ), id.getName() );
					if( rs.next() )
						throw new IllegalStateException( "Multiple matches for frame not expected" );
				}
				else
					ret = null;
			} finally {
				rs.close();
			}

			return ret;

		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public int getFrameCount() {

		try {
			PreparedStatement select = getPreparedStatement( getCurrentConnection(),
					"SELECT_FRAME_COUNT_SQL" );
			select.setString( 1, Model.SlotID.NAME.getName() );
			ResultSet rs = executeQuery( select );
			try {
				if( rs.next() ) {
					int ret = rs.getInt( 1 );
					if( rs.next() )
						throw new IllegalStateException(
								"Counting frames not expected to return multiple rows" );
					return ret;
				}
				else
					throw new IllegalStateException();
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public Set<Frame> getFrames() {
		Set<Frame> ret = new HashSet<Frame>();

		try {
			PreparedStatement select = getPreparedStatement( getCurrentConnection(),
					"SELECT_FRAMES_SQL" );
			select.setString( 1, Model.SlotID.NAME.getName() );
			ResultSet rs = executeQuery( select );
			try {
				while( rs.next() ) {
					ret.add( createFrame( rs.getByte( 2 ), rs.getString( 1 ) ) );
				}
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
		return ret;
	}

	public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {

		Set<Frame> ret = new HashSet<Frame>();

		int valueType = DatabaseUtils.valueType( value, _frameFactory );
		PreparedStatement select;
		try {
			final RobustConnection connection = getCurrentConnection();

			if( valueType < DatabaseUtils.BASE_FRAME_TYPE_VALUE ) {
				final String valueString = value.toString();
				if( facet == null ) {
					select = getPreparedStatement( connection, isShortValue( connection,
							valueString )
						? "SELECT_FRAME_BY_NULL_FACET_SHORT_REFERENCE_SQL"
						: "SELECT_FRAME_BY_NULL_FACET_LONG_REFERENCE_SQL" );
					select.setString( 1, slot.getFrameID().getName() );
					select.setBoolean( 2, isTemplate );
					select.setInt( 3, (short) valueType );
					select.setString( 4, valueString );
				}
				else {
					select = getPreparedStatement( connection, isShortValue( connection,
							valueString )
						? "SELECT_FRAME_BY_FACET_SHORT_REFERENCE_SQL"
						: "SELECT_FRAME_BY_FACET_LONG_REFERENCE_SQL" );
					select.setString( 1, slot.getFrameID().getName() );
					select.setString( 2, facet.getFrameID().getName() );
					select.setBoolean( 3, isTemplate );
					select.setInt( 4, (short) valueType );
					select.setString( 5, valueString );
				}
			}
			else {
				if( facet == null ) {
					select = getPreparedStatement( connection,
							"SELECT_FRAME_BY_NULL_FACET_FRAME_REFERENCE_SQL" );
					select.setString( 1, slot.getFrameID().getName() );
					select.setBoolean( 2, isTemplate );
					select.setInt( 3, (short) valueType );
					select.setString( 4, ((Frame) value).getFrameID().getName() );
				}
				else {
					select = getPreparedStatement( connection,
							"SELECT_FRAME_BY_FACET_FRAME_REFERENCE_SQL" );
					select.setString( 1, slot.getFrameID().getName() );
					select.setString( 2, facet.getFrameID().getName() );
					select.setBoolean( 3, isTemplate );
					select.setInt( 4, (short) valueType );
					select.setString( 5, ((Frame) value).getFrameID().getName() );
				}
			}

			ResultSet rs = executeQuery( select );
			try {
				while( rs.next() ) {
					ret.add( createFrame( rs.getByte( 2 ), rs.getString( 1 ) ) );
				}
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

		return ret;
	}

	public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
		Set<Frame> ret = new HashSet<Frame>();

		PreparedStatement select;
		try {
			final RobustConnection connection = getCurrentConnection();

			if( facet == null ) {
				select = getPreparedStatement( connection,
						"SELECT_FRAME_BY_NULL_FACET_ANY_REFERENCE_SQL" );
				select.setString( 1, slot.getFrameID().getName() );
				select.setBoolean( 2, isTemplate );
			}
			else {
				select = getPreparedStatement( connection,
						"SELECT_FRAME_BY_FACET_ANY_REFERENCE_SQL" );
				select.setString( 1, slot.getFrameID().getName() );
				select.setString( 2, facet.getFrameID().getName() );
				select.setBoolean( 3, isTemplate );
			}

			ResultSet rs = executeQuery( select );
			try {
				while( rs.next() ) {
					ret.add( createFrame( rs.getByte( 2 ), rs.getString( 1 ) ) );
				}
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

		return ret;
	}

	public Map<Sft, List> getFrameValues(Frame frame) {

		try {
			Map<Sft, List> ret = new HashMap<Sft, List>();

			final String name = frame.getFrameID().getName();
			final short frameType = (short) DatabaseUtils.valueType( frame, _frameFactory );

			PreparedStatement select = getPreparedStatement( getCurrentConnection(),
					"SELECT_VALUES_BY_FRAME_SQL" );
			select.setString( 1, name );
			select.setInt( 2, frameType );
			select.setString( 3, name );
			select.setInt( 4, frameType );

			ResultSet rs = executeQuery( select );
			try {
				Slot sl = null;
				Facet ft = null;
				boolean isT = false;
				boolean lastIsT = false;
				List<Object> values = null;
				Sft key = null;
				String slStr = null;
				String lastSlStr = null;
				String ftStr = null;
				String lastFtStr = null;
				boolean newSft;

				while( rs.next() ) {

					newSft = false;

					slStr = rs.getString( 1 );
					if( !slStr.equals( lastSlStr ) ) {
						newSft = true;
						sl = createSlot( rs.getString( 1 ) );
					}

					ftStr = rs.getString( 2 );
					if( ftStr != null ) {
						if( !ftStr.equals( lastFtStr ) ) {
							newSft = true;
							ft = createFacet( ftStr );
						}
					}
					else {
						if( lastFtStr != null )
							newSft = true;
						ftStr = null;
						ft = null;
					}

					isT = rs.getBoolean( 3 );
					if( isT != lastIsT )
						newSft = true;

					if( newSft ) {
						if( key != null )
							ret.put( key, values );

						key = new Sft( sl, ft, isT );
						values = new ArrayList<Object>();

						lastSlStr = slStr;
						lastFtStr = ftStr;
						lastIsT = isT;
					}
					values.add( getValueFromResult( rs, 4, 5, 6, 7, 8 ) );
				}

				if( key != null )
					ret.put( key, values );

			} finally {
				rs.close();
			}

			return ret;
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		} catch( RuntimeException e ) {
			System.err.println( "Caught runtime exception: " + e.getMessage() );
			e.printStackTrace( System.err );
			throw e;
		}
	}

	/*
	 * From DatabaseUtils.getMatchString, but without the single quote doubling
	 */
	private static String getMatchString(String input, char escapeChar) {
		StringBuffer output = new StringBuffer();
		for( int i = 0; i < input.length(); ++i ) {
			char c = input.charAt( i );
			if( escapeChar != 0 && (c == '%' || c == '_' || c == escapeChar) ) {
				// escape any special character
				output.append( escapeChar );
				output.append( c );
			}
			else if( c == '*' ) {
				output.append( '%' );
			}
			else {
				output.append( Character.toLowerCase( c ) );
			}
		}
		return output.toString();
	}

	public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value,
			int maxMatches) {

		Set<Frame> ret = new HashSet<Frame>();

		PreparedStatement select;
		try {
			final RobustConnection connection = getCurrentConnection();
			final String modifiedValue = getMatchString( value, connection.getEscapeCharacter() );

			if( facet == null ) {
				select = getPreparedStatement( connection,
						"SELECT_FRAME_BY_NULL_FACET_STRING_MATCH_SQL" );
				select.setString( 1, slot.getFrameID().getName() );
				select.setBoolean( 2, isTemplate );
				select.setInt( 3, DatabaseUtils.getStringValueType() );
				select.setString( 4, modifiedValue );
				select.setString( 5, modifiedValue );
			}
			else {
				select = getPreparedStatement( connection, "SELECT_FRAME_BY_FACET_STRING_MATCH_SQL" );
				select.setString( 1, slot.getFrameID().getName() );
				select.setString( 2, facet.getFrameID().getName() );
				select.setBoolean( 3, isTemplate );
				select.setInt( 4, DatabaseUtils.getStringValueType() );
				select.setString( 5, modifiedValue );
				select.setString( 6, modifiedValue );
			}

			ResultSet rs = executeQuery( select );
			try {
				while( rs.next() && ((maxMatches--) != 0) ) {
					ret.add( createFrame( rs.getByte( 2 ), rs.getString( 1 ) ) );
				}
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

		return ret;
	}

	public Set<Reference> getMatchingReferences(String value, int maxMatches) {

		Set<Reference> references = new HashSet<Reference>();

		PreparedStatement select;
		try {
			final RobustConnection connection = getCurrentConnection();
			final String modifiedValue = getMatchString( value, connection.getEscapeCharacter() );
			select = getPreparedStatement( connection, "SELECT_REFERENCES_STRING_MATCH_SQL" );
			select.setInt( 1, (short) DatabaseUtils.getStringValueType() );
			select.setString( 2, modifiedValue );
			select.setString( 3, modifiedValue );
			select.setInt( 4, (short) DatabaseUtils.getStringValueType() );
			select.setString( 5, modifiedValue );
			select.setString( 6, modifiedValue );

			ResultSet rs = executeQuery( select );
			try {
				while( rs.next() && ((maxMatches--) != 0) ) {
					Frame frame = createFrame( rs.getByte( 2 ), rs.getString( 1 ) );
					Slot slot = createSlot( rs.getString( 3 ) );
					final String facetStr = rs.getString( 4 );
					Facet facet = (facetStr == null)
						? null
						: createFacet( facetStr );
					boolean isTemplate = rs.getBoolean( 5 );
					references.add( new ReferenceImpl( frame, slot, facet, isTemplate ) );
				}
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

		return references;
	}

	private PreparedStatement getPreparedStatement(RobustConnection connection, String key)
			throws SQLException {

		PreparedStatement stmt = preparedStatementMap.get( key );

		if( stmt == null ) {
			stmt = connection.getPreparedStatement( getStringStatement( connection, key ) );
			preparedStatementMap.put( key, stmt );
		}

		return stmt;
	}

	public Set<Reference> getReferences(Object value) {

		int type = DatabaseUtils.valueType( value, _frameFactory );
		Set<Reference> references = new HashSet<Reference>();

		PreparedStatement select;
		try {
			final RobustConnection connection = getCurrentConnection();
			if( type < DatabaseUtils.BASE_FRAME_TYPE_VALUE ) {
				final String valueString = value.toString();
				select = getPreparedStatement( connection, isShortValue( connection, valueString )
					? "SELECT_REFERENCES_SHORT_SQL"
					: "SELECT_REFERENCES_LONG_SQL" );
				select.setInt( 1, (short) type );
				select.setString( 2, valueString );
				select.setInt( 3, (short) type );
				select.setString( 4, valueString );
			}
			else {
				select = getPreparedStatement( connection, "SELECT_REFERENCES_FRAME_SQL" );
				final String name = ((Frame) value).getFrameID().getName();
				select.setInt( 1, (short) type );
				select.setString( 2, name );
				select.setInt( 3, (short) type );
				select.setString( 4, name );
			}

			ResultSet rs = executeQuery( select );
			try {
				while( rs.next() ) {
					Frame frame = createFrame( rs.getByte( 2 ), rs.getString( 1 ) );
					Slot slot = createSlot( rs.getString( 3 ) );
					final String facetStr = rs.getString( 4 );
					Facet facet = (facetStr == null)
						? null
						: createFacet( facetStr );
					boolean isTemplate = rs.getBoolean( 5 );
					references.add( new ReferenceImpl( frame, slot, facet, isTemplate ) );
				}
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

		return references;
	}

	private Frame createFrame(byte type, String name) {
		if( type < DatabaseUtils.BASE_FRAME_TYPE_VALUE )
			throw new IllegalStateException();

		Frame ret = _frameFactory.createFrameFromClassId( type, new FrameID( name ) );
		ret.setIncluded( _isInclude );

		return ret;
	}

	private Facet createFacet(String name) {
		Facet ret = _frameFactory.createFacet( new FrameID( name ), Collections.emptyList() );
		ret.setIncluded( _isInclude );

		return ret;
	}

	private Slot createSlot(String name) {
		Slot ret = _frameFactory.createSlot( new FrameID( name ), Collections.emptyList() );
		ret.setIncluded( _isInclude );

		return ret;
	}

	public int getSimpleInstanceCount() {
		// FIXME
		System.err.println( "CPDatabaseFrameDB: getSimpleInstanceCount" );
		throw new UnsupportedOperationException( "getSimpleInstanceCount" );
	}

	public int getSlotCount() {
		// FIXME
		System.err.println( "CPDatabaseFrameDB: getSlotCount" );
		throw new UnsupportedOperationException( "getSlotCount" );
	}

	private String getStringStatement(RobustConnection connection, String key) {
		String sql = schemaProperties.getProperty( key );
		if( sql == null )
			throw new IllegalStateException( "Missing SQL data manipulation statement for key: "
					+ key );

		return replaceSQLVariantStrings( connection, replaceDBTypeStrings( connection,
				replaceDBSchemaStrings( _table, sql ) ) );
	}

	private String[] getStringStatementArray(RobustConnection connection, String key)
			throws SQLException {

		String nStr = schemaProperties.getProperty( key + "_NUM" );
		if( nStr == null )
			throw new IllegalStateException( "Missing count value for key: " + key );

		final int n = Integer.valueOf( nStr );
		String[] ret = new String[n];
		for( int i = 0; i < n; i++ )
			ret[i] = getStringStatement( connection, key + "_" + i );

		return ret;
	}

	private Object getValueFromResult(ResultSet rs, int typeIndex, int frameTypeIndex,
			int frameNameIndex, int shortIndex, int longIndex) throws SQLException {
		Object value;

		int type = rs.getByte( typeIndex );
		if( type < DatabaseUtils.BASE_FRAME_TYPE_VALUE ) {
			value = DatabaseUtils.getShortValue( rs, shortIndex, typeIndex, _frameFactory, 0,
					_isInclude );
			if( value == null )
				value = rs.getString( longIndex );
		}
		else
			value = createFrame( rs.getByte( frameTypeIndex ), rs.getString( frameNameIndex ) );

		return value;
	}

	public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {

		PreparedStatement select = null;
		try {
			if( facet == null ) {
				select = getPreparedStatement( getCurrentConnection(),
						"SELECT_VALUES_BY_FRAME_SLOT_NULL_FACET_TEMPLATE_SQL" );
				select.setString( 1, frame.getFrameID().getName() );
				select.setInt( 2, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
				select.setString( 3, slot.getFrameID().getName() );
				select.setBoolean( 4, isTemplate );
			}
			else {
				select = getPreparedStatement( getCurrentConnection(),
						"SELECT_VALUES_BY_FRAME_SLOT_FACET_TEMPLATE_SQL" );
				select.setString( 1, frame.getFrameID().getName() );
				select.setInt( 2, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
				select.setString( 3, slot.getFrameID().getName() );
				select.setString( 4, facet.getFrameID().getName() );
				select.setBoolean( 5, isTemplate );
			}

			ResultSet rs = executeQuery( select );
			try {
				List<Object> values = new ArrayList<Object>();
				while( rs.next() ) {
					values.add( getValueFromResult( rs, 1, 2, 3, 4, 5 ) );
				}
				return values;
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			System.err.println( e.getMessage() + ": " + select.toString() );
			throw createRuntimeException( e );
		} catch( RuntimeException e ) {
			System.err.println( e.getMessage() );
			e.printStackTrace();
			throw e;
		}
	}

	public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
		try {
			PreparedStatement select;

			if( facet == null ) {
				select = getPreparedStatement( getCurrentConnection(),
						"SELECT_VALUE_COUNT_BY_FRAME_SLOT_NULL_FACET_TEMPLATE_SQL" );
				select.setString( 1, frame.getFrameID().getName() );
				select.setInt( 2, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
				select.setString( 3, slot.getFrameID().getName() );
				select.setBoolean( 4, isTemplate );
			}
			else {
				select = getPreparedStatement( getCurrentConnection(),
						"SELECT_VALUE_COUNT_BY_FRAME_SLOT_FACET_TEMPLATE_SQL" );
				select.setString( 1, frame.getFrameID().getName() );
				select.setInt( 2, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
				select.setString( 3, slot.getFrameID().getName() );
				select.setString( 4, slot.getFrameID().getName() );
				select.setBoolean( 5, isTemplate );
			}

			ResultSet rs = executeQuery( select );
			try {
				if( rs.next() ) {
					int ret = rs.getInt( 1 );
					if( rs.next() )
						throw new IllegalStateException(
								"Counting frames not expected to return multiple rows" );
					return ret;
				}
				else
					throw new IllegalStateException();
			} finally {
				rs.close();
			}
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	@Override
	public void initialize(FrameFactory factory, String driver, String url, String user,
			String pass, String table, boolean isInclude) {
		if( table.endsWith( "_FRAME" ) )
			table = table.substring( 0, table.length() - 6 );
		else if( table.endsWith( "_SLOT_VALUE" ) )
			table = table.substring( 0, table.length() - 11 );
		else if( table.endsWith( "_FACET_VALUE" ) )
			table = table.substring( 0, table.length() - 12 );
		super.initialize( factory, driver, url, user, pass, table, isInclude );
		preparedStatementMap.clear();
	}

	public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
		// FIXME
		System.err.println( "CPDatabaseFrameDB: moveValue" );
		throw new UnsupportedOperationException( "moveValue" );
	}

	public void overwriteKB(KnowledgeBase kb, boolean saveFrames) throws SQLException {

		final RobustConnection connection = getCurrentConnection();
		connection.closeStatements();

		// 1) Drop tables if exist
		String[] drops = getStringStatementArray( connection, "DROP_TABLES_SQL" );
		for( String dropSql : drops ) {
			try {
				executeUpdate( dropSql );
			} catch( SQLException e ) {
				// FIXME Only ignore if this is a table does not exist exception
			}
		}

		// 2) Create tables
		try {
			String[] creates = getStringStatementArray( connection, "CREATE_TABLES_SQL" );
			for( String createSql : creates ) {
				executeUpdate( createSql );
			}
		} catch( SQLException e ) {
			Logger.getLogger( "Failed to create database tables: " + e.getMessage() );
			throw e;
		}

		// 3) Create indices (pre-load)
		try {
			String[] creates = getStringStatementArray( connection, "CREATE_INDICES_PRELOAD_SQL" );
			for( String createSql : creates ) {
				executeUpdate( createSql );
			}
		} catch( SQLException e ) {
			Logger.getLogger( "Failed to create database index: " + e.getMessage() );
			throw e;
		}

		maxFrameId = 0;

		// 4) Save any frames
		if( saveFrames ) {
			final boolean callCachingBefore = kb.setCallCachingEnabled( false );
			try {
				connection.setAutoCommit( false );
				saveFrames( kb, connection );
				System.err.print( df.format( new Date() ) + " committing ... " );
				connection.commit();
				System.err.println( " complete " + df.format( new Date() ) );
			} finally {
				connection.setAutoCommit( true );
				kb.setCallCachingEnabled( callCachingBefore );
			}
		}

		// 5) Create indices (post-load)
		try {
			String[] creates = getStringStatementArray( connection, "CREATE_INDICES_POSTLOAD_SQL" );
			for( String createSql : creates ) {
				System.err.print( df.format( new Date() ) + " creating index ... " );
				executeUpdate( createSql );
				System.err.println( " complete " + df.format( new Date() ) );
			}
		} catch( SQLException e ) {
			Logger.getLogger( "Failed to create database index: " + e.getMessage() );
			throw e;
		}
	}

	public void reinitialize() {
	}

	public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
		try {
			final RobustConnection connection = getCurrentConnection();

			final int frId = getDatabaseId( connection, frame, false, null );
			final int slId = getDatabaseId( connection, slot, false, null );
			final int faId = (facet == null)
				? 0
				: getDatabaseId( connection, facet, true, null );
			final int facetSpace = (facet == null)
				? 0
				: 1;

			if( frId < 0 )
				throw new IllegalStateException();

			if( slId < 0 )
				throw new IllegalStateException();

			if( faId < 0 )
				throw new IllegalStateException();

			PreparedStatement delete;

			if( value instanceof Frame ) {
				Frame valueFrame = (Frame) value;
				final int vfId = getDatabaseId( connection, valueFrame, false, null );
				if( vfId < 0 )
					throw new IllegalStateException();

				delete = getPreparedStatement( connection, (facet == null)
					? "DELETE_VALUE_BY_NULL_FACET_REFERENCES_FRAME_SQL"
					: "DELETE_VALUE_BY_FACET_REFERENCES_FRAME_SQL" );
				delete.setInt( 4 + facetSpace, (short) DatabaseUtils.BASE_FRAME_TYPE_VALUE );
				delete.setInt( 5 + facetSpace, vfId );
			}
			else {
				final String valueString = value.toString();
				if( facet == null )
					delete = getPreparedStatement( connection, isShortValue( connection,
							valueString )
						? "DELETE_VALUE_BY_NULL_FACET_REFERENCES_SHORT_SQL"
						: "DELETE_VALUE_BY_NULL_FACET_REFERENCES_LONG_SQL" );
				else
					delete = getPreparedStatement( connection, isShortValue( connection,
							valueString )
						? "DELETE_VALUE_BY_FACET_REFERENCES_SHORT_SQL"
						: "DELETE_VALUE_BY_FACET_REFERENCES_LONG_SQL" );
				delete.setInt( 4 + facetSpace, (short) DatabaseUtils.valueType( value,
						_frameFactory ) );
				delete.setString( 5 + facetSpace, valueString );
			}

			delete.setInt( 1, frId );
			delete.setInt( 2, slId );
			if( facet != null )
				delete.setInt( 3, faId );
			delete.setBoolean( 3 + facetSpace, isTemplate );

			executeUpdate( delete );

		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public void replaceFrame(Frame frame) {
		try {
			PreparedStatement update = getPreparedStatement( getCurrentConnection(),
					"UPDATE_FRAME_TYPE_SQL" );
			update.setInt( 1, (short) DatabaseUtils.valueType( frame, _frameFactory ) );
			update.setString( 2, frame.getFrameID().getName() );
			executeUpdate( update );
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public void replaceFrame(Frame original, Frame replacement) {
		try {
			PreparedStatement update = getPreparedStatement( getCurrentConnection(),
					"UPDATE_FRAME_NAME_SQL" );
			update.setString( 1, replacement.getFrameID().getName() );
			update.setString( 2, original.getFrameID().getName() );
			executeUpdate( update );
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	private void saveFrames(KnowledgeBase kb, RobustConnection connection) throws SQLException {

		Map<Frame, Integer> frameIdCache = new HashMap<Frame, Integer>();
		MergingNarrowFrameStore mnfs = MergingNarrowFrameStore.get( kb );
		NarrowFrameStore nfs = null;
		if( mnfs != null )
			nfs = mnfs.getActiveFrameStore();

		final boolean useNfs = (nfs != null);

		final PreparedStatement insFacetStmt = getPreparedStatement( connection,
				"INSERT_FACET_VALUE_SQL" );
		final PreparedStatement insNullFacetStmt = getPreparedStatement( connection,
				"INSERT_NULL_FACET_VALUE_SQL" );

		long nCountSinceExecute = 0;

		for( Frame frame : useNfs
			? nfs.getFrames()
			: kb.getFrames() ) {

			int fId = -1;
			final boolean systemFrame = frame.isSystem();

			// 1) Save direct own slot values
			for( Slot s : frame.getOwnSlots() ) {
				int sId = -1;
				final boolean systemSlot = s.isSystem();

				int index = 0;
				for( Object o : useNfs
					? nfs.getValues( frame, s, null, /* isTemplate = */false )
					: frame.getDirectOwnSlotValues( s ) ) {

					if( systemFrame && systemSlot && o instanceof Frame ) {
						if( ((Frame) o).isSystem() )
							continue;
					}

					if( fId < 0 )
						fId = getDatabaseId( connection, frame, true, frameIdCache );
					if( sId < 0 )
						sId = getDatabaseId( connection, s, true, frameIdCache );

					completeInsertStatement( connection, insNullFacetStmt, fId, sId, 0, false,
							index++, o, frameIdCache );
					insNullFacetStmt.addBatch();
					nCountSinceExecute++;
				}
			}

			// 2) Save direct template slot values
			if( frame instanceof Cls ) {
				Cls cls = (Cls) frame;
				for( Slot s : cls.getTemplateSlots() ) {
					int sId = -1;
					final boolean systemSlot = s.isSystem();

					int index = 0;
					for( Object o : useNfs
						? nfs.getValues( cls, s, null, /* isTemplate = */true )
						: cls.getDirectTemplateSlotValues( s ) ) {

						if( systemFrame && systemSlot && o instanceof Frame ) {
							if( ((Frame) o).isSystem() )
								continue;
						}

						if( fId < 0 )
							fId = getDatabaseId( connection, frame, true, frameIdCache );
						if( sId < 0 )
							sId = getDatabaseId( connection, s, true, frameIdCache );

						completeInsertStatement( connection, insNullFacetStmt, fId, sId, 0, true,
								index++, o, frameIdCache );
						insNullFacetStmt.addBatch();
						nCountSinceExecute++;
					}

					// 3) Save direct template facet values
					for( Facet facet : cls.getTemplateFacets( s ) ) {
						int facetId = -1;
						final boolean systemFacet = facet.isSystem();

						int facetIndex = 0;
						for( Object o : useNfs
							? nfs.getValues( cls, s, facet, true )
							: cls.getDirectTemplateFacetValues( s, facet ) ) {

							if( systemFrame && systemSlot && systemFacet && o instanceof Frame ) {
								if( ((Frame) o).isSystem() )
									continue;
							}

							if( fId < 0 )
								fId = getDatabaseId( connection, frame, true, frameIdCache );
							if( sId < 0 )
								sId = getDatabaseId( connection, s, true, frameIdCache );
							if( facetId < 0 )
								facetId = getDatabaseId( connection, facet, true, frameIdCache );

							completeInsertStatement( connection, insFacetStmt, fId, sId, facetId,
									true, facetIndex++, o, frameIdCache );
							insFacetStmt.addBatch();
							nCountSinceExecute++;
						}
					}
				}
			}

			if( nCountSinceExecute > MAX_BATCH_VALUE_INSERT ) {
				System.err.print( df.format( new Date() ) + " inserting " + nCountSinceExecute
						+ " statements ..." );
				insFacetStmt.executeBatch();
				insNullFacetStmt.executeBatch();
				System.err.println( " complete." );
				nCountSinceExecute = 0;
			}
		}
		insFacetStmt.executeBatch();
		insNullFacetStmt.executeBatch();
	}

	public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {

		try {
			final RobustConnection connection = getCurrentConnection();

			final int frId = getDatabaseId( connection, frame, true, null );
			final int slId = getDatabaseId( connection, slot, true, null );
			final int faId = (facet == null)
				? 0
				: getDatabaseId( connection, facet, true, null );
			final int facetSpace = (facet == null)
				? 0
				: 1;

			PreparedStatement delete = getPreparedStatement( connection, (facet == null)
				? "DELETE_NULL_FACET_VALUE_SQL"
				: "DELETE_FACET_VALUE_SQL" );
			delete.setInt( 1, frId );
			delete.setInt( 2, slId );
			if( facet != null )
				delete.setInt( 3, faId );
			delete.setBoolean( 3 + facetSpace, isTemplate );
			executeUpdate( delete );

			addValues( connection, frId, slId, faId, isTemplate, values, 0 );
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

	}
}
