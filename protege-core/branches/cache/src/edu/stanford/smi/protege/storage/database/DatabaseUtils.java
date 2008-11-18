package edu.stanford.smi.protege.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Assert;

public class DatabaseUtils {
	public static final String NULL_FRAME_ID_STRING = "";
    /*
     * These constants are store in the database as a performance hack. When a frame is read out of the db we need to
     * make a java object from it. To do this we need to know if it is a class, a slot, or a simple instance. We could
     * figure this out by making a bunch of additional db calls, but instead we just store a number that effectively
     * maps it directly to a Java class. For slot/facet "values" we always store everything as a string. Thus we need
     * some way to reliably map back to the original Java class. These integers also server this purpose.
     */
    private static final int VALUE_TYPE_INTEGER = 1;
    private static final int VALUE_TYPE_FLOAT = 2;
    private static final int VALUE_TYPE_STRING = 3;
    private static final int VALUE_TYPE_BOOLEAN = 4;
    /*
     * The Frame type values start at 5.  This allow me to check or search for frames by
     * checking if the value type is greater than BASE_FRAME_TYPE_VALUE.
     */
    public static final int BASE_FRAME_TYPE_VALUE = 5;

    private static final char SINGLE_QUOTE = '\'';

    public static String getValue(FrameID id) {
      return id == null ? "" : id.getName();
    }

    public static int getStringValueType() {
        return VALUE_TYPE_STRING;
    }

    private static void setId(PreparedStatement stmt, int index, Frame frame) throws SQLException {
        setId(stmt, index, getId(frame));
    }

    private static FrameID getId(Frame frame) {
        return (frame == null) ? null : frame.getFrameID();
    }

    private static void setId(PreparedStatement stmt, int index, FrameID id) throws SQLException {
        stmt.setString(index, getValue(id));
    }

    private static String getFrameIDValueString(FrameID id) {
        return (id == null) ? NULL_FRAME_ID_STRING : id.getName();
    }

    public static void setValueType(PreparedStatement stmt, int index, Object o, FrameFactory factory)
            throws SQLException {
        setValueType(stmt, index, valueType(o, factory));
    }

    public static void setValueType(PreparedStatement stmt, int index, int type) throws SQLException {
        /*
         * The setByte below is correct but the JdbcOdbc bridge fails on this call even though the underlying data type
         * is a byte. Thus we use setInt.
         */
        // stmt.setByte(index, (byte) type);
        stmt.setInt(index, (short) type);
    }

    public static int valueType(Object value, FrameFactory factory) {
        int type;
        if (value instanceof String) {
            type = VALUE_TYPE_STRING;
        } else if (value instanceof Integer) {
            type = VALUE_TYPE_INTEGER;
        } else if (value instanceof Float) {
            type = VALUE_TYPE_FLOAT;
        } else if (value instanceof Boolean) {
            type = VALUE_TYPE_BOOLEAN;
        } else {
            type = factory.getJavaClassId((Frame) value);
        }
        return type;
    }
    
    public static void setFrameId(PreparedStatement stmt, int index, FrameID frameId) throws SQLException {
    	setId(stmt, index, frameId);
    }

    public static void setFrame(PreparedStatement stmt, int index, Frame frame) throws SQLException {
        setId(stmt, index, frame);
    }

    public static void setFrame(PreparedStatement stmt, int frameIndex, int typeIndex, Frame frame, FrameFactory factory)
            throws SQLException {
        setId(stmt, frameIndex, frame);
        setValueType(stmt, typeIndex, frame, factory);
    }

    public static void setSlot(PreparedStatement stmt, int index, Slot slot) throws SQLException {
        setId(stmt, index, slot);
    }

    public static void setFacet(PreparedStatement stmt, int index, Facet facet) throws SQLException {
        setId(stmt, index, facet);
    }

    public static void setIsTemplate(PreparedStatement stmt, int index, boolean isTemplate) throws SQLException {
        stmt.setBoolean(index, isTemplate);
    }

    public static int getIsTemplateValue(boolean isTemplate) {
        return isTemplate ? 1 : 0;
    }

    public static void setValueIndex(PreparedStatement stmt, int index, int valueIndex) throws SQLException {
        stmt.setInt(index, valueIndex);
    }

    public static void setShortValue(PreparedStatement stmt, int valueIndex, int valueTypeIndex, Object object,
            FrameFactory factory) throws SQLException {
        stmt.setString(valueIndex, toString(object));
        setValueType(stmt, valueTypeIndex, object, factory);
    }

    private static String toString(Object o) {
        Assert.assertNotNull("object", o);
        String s;
        if (o instanceof Frame) {
            s = getFrameIDValueString(((Frame) o).getFrameID());
        } else {
            s = o.toString();
        }
        return s;
    }

    public static void setShortMatchValue(PreparedStatement stmt, int valueIndex, int valueTypeIndex, String value,
            char escapeChar) throws SQLException {
        stmt.setString(valueIndex, getMatchString(value, escapeChar));
        setValueType(stmt, valueTypeIndex, VALUE_TYPE_STRING);
    }

    private static void setNullShortValue(PreparedStatement stmt, int valueIndex, int valueTypeIndex)
            throws SQLException {
        if (isJdbcOdbcBridge(stmt)) {
            /*
             * Under some conditions that I can't figure out, the standard setNull command fails when using the JdbcOdbc
             * driver. The problem can be seen when dumping the newspaper example to Ms Access. Since I cannot figure
             * out the problem or a workaround we instead set the value to the empty string rather than null and then we
             * convert it back to null on input.
             */
            stmt.setString(valueIndex, "");
        } else {
            stmt.setNull(valueIndex, Types.VARCHAR);
        }
        setValueType(stmt, valueTypeIndex, VALUE_TYPE_STRING);
    }

    private static boolean isJdbcOdbcBridge(Statement stmt) {
        return stmt.getClass().getName().startsWith("sun.jdbc.odbc.JdbcOdbcPreparedStatement");
    }

    public static void setLongValue(PreparedStatement stmt, int valueIndex, Object object) throws SQLException {
        stmt.setString(valueIndex, toString(object));
        // setValueType(stmt, valueTypeIndex, VALUE_TYPE_STRING);

    }

    static boolean first = true;

    public static void setNullLongValue(PreparedStatement stmt, int index) throws SQLException {
        stmt.setNull(index, Types.LONGVARCHAR);
    }

    public static void setValue(PreparedStatement stmt, int shortValueIndex, int longValueIndex, int valueTypeIndex,
            Object o, int sizeLimit, FrameFactory factory) throws SQLException {
        if (isShortValue(o, sizeLimit)) {
            setShortValue(stmt, shortValueIndex, valueTypeIndex, o, factory);
            setNullLongValue(stmt, longValueIndex);
        } else {
            setNullShortValue(stmt, shortValueIndex, valueTypeIndex);
            setLongValue(stmt, longValueIndex, o);
        }
    }

    public static String getMatchString(String input, char escapeChar) {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            c = Character.toUpperCase(c);
            if (escapeChar != 0 && (c == '%' || c == '_' || c == escapeChar)) {
                // escape any special character
                output.append(escapeChar);
                output.append(c);
            } else if (c == SINGLE_QUOTE) {
                // double single quotes
                output.append(SINGLE_QUOTE);
                output.append(SINGLE_QUOTE);
            } else if (c == '*') {
                output.append('%');
            } else {
                output.append(Character.toLowerCase(c));
            }
        }
        return output.toString();
    }

    private static boolean isShortValue(Object o, int sizeLimit) {
        boolean isShortValue;
        if (o instanceof String) {
            isShortValue = ((String) o).length() <= sizeLimit;
        } else {
            isShortValue = true;
        }
        return isShortValue;
    }

    public static Frame getFrame(ResultSet rs, 
                                 int frameIndex, int typeIndex, 
                                 FrameFactory factory,
                                 boolean isInclude) throws SQLException {
        FrameID id = getFrameId(rs, frameIndex);
        int type = rs.getInt(typeIndex);
        return getFrame(id, type, factory, isInclude);
    }

    private static Frame getFrame(FrameID id, int type, FrameFactory factory,
                                  boolean isInclude) {
        if (id == null) {
          return null;
        }
        Frame frame = factory.createFrameFromClassId(type, id);
        if (isInclude) {
          frame.setIncluded(true);
        }
        return frame;
    }

    private static FrameID getFrameId(ResultSet rs, int index) throws SQLException {
        String value = rs.getString(index);
        if (value.equals("")) {
          return null;
        }
        return new FrameID(value);
    }

    public static Slot getSlot(ResultSet rs, int index, 
                               FrameFactory factory,
                               boolean isInclude) throws SQLException {
        Collection types = Collections.EMPTY_LIST;
        FrameID id = getFrameId(rs, index);
        Slot slot = factory.createSlot(id, types);
        if (isInclude) {
          slot.setIncluded(isInclude);
        }
        return slot;
    }

    public static Facet getFacet(ResultSet rs, int index, 
                                 FrameFactory factory,
                                 boolean isInclude) throws SQLException {
        Collection types = Collections.EMPTY_LIST;
        Facet facet;
        FrameID id = getFrameId(rs, index);
        if (id == null) {
            facet = null;
        } else {
            facet = factory.createFacet(id, types);
            if (isInclude) {
              facet.setIncluded(true);
            }
        }
        return facet;
    }

    public static Object getShortValue(ResultSet rs, 
                                       int valueIndex, int valueTypeIndex, 
                                       FrameFactory factory, int projectId,
                                       boolean isInclude)
            throws SQLException {
        Object value;
        int type = rs.getByte(valueTypeIndex);
        String valueString = rs.getString(valueIndex);
        switch (type) {
            case VALUE_TYPE_INTEGER:
                value = Integer.valueOf(valueString);
                break;
            case VALUE_TYPE_FLOAT:
                value = Float.valueOf(valueString);
                break;
            case VALUE_TYPE_BOOLEAN:
                value = Boolean.valueOf(valueString);
                break;
            case VALUE_TYPE_STRING:
                value = valueString;
                // Inverse of hack for JDK 1.4 JdbcOdbcBridge bug
                if (valueString != null && valueString.length() == 0) {
                    value = null;
                }
                break;
            default:
                FrameID id = new FrameID(valueString);
                value = getFrame(id, type, factory, isInclude);
                break;
        }
        return value;
    }

    public static Object getLongValue(ResultSet rs, int valueIndex) throws SQLException {
        return rs.getString(valueIndex);
    }

    public static int getIndex(ResultSet rs, int index) throws SQLException {
        return rs.getInt(index);
    }

    public static boolean getIsTemplate(ResultSet rs, int index) throws SQLException {
        return rs.getBoolean(index);
    }

    public static void setFrameType(PreparedStatement stmt, int index, int type) throws SQLException {
        setValueType(stmt, index, type);
    }

}