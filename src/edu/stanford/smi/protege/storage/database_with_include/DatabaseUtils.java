package edu.stanford.smi.protege.storage.database_with_include;

import java.sql.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

class DatabaseUtils {
    private static final int VALUE_TYPE_INTEGER = 1;
    private static final int VALUE_TYPE_FLOAT = 2;
    private static final int VALUE_TYPE_STRING = 3;
    private static final int VALUE_TYPE_BOOLEAN = 4;
    private static final int VALUE_TYPE_SIMPLE_INSTANCE = 5;
    private static final int VALUE_TYPE_CLASS = 6;
    private static final int VALUE_TYPE_SLOT = 7;
    private static final int VALUE_TYPE_FACET = 8;

    private static final char ESCAPE_CHAR = '\\';
    private static final char SINGLE_QUOTE = '\'';

    public static String getEscapeClause() {
        return "{escape '" + ESCAPE_CHAR + "'}";
    }

    public static int getStringValueType() {
        return VALUE_TYPE_STRING;
    }

    private static void setId(PreparedStatement stmt, int index, int projectIndex, Frame frame, FrameIDHelper helper)
        throws SQLException {
        setId(stmt, index, projectIndex, getId(frame), helper);
    }

    private static FrameID getId(Frame frame) {
        return (frame == null) ? null : frame.getFrameID();
    }

    private static void setId(PreparedStatement stmt, int index, int projectIndex, FrameID id, FrameIDHelper helper)
        throws SQLException {
        int localId;
        int projectId;
        if (id == null) {
            localId = FrameID.NULL_FRAME_ID_VALUE;
            projectId = 0;
        } else {
            localId = id.getLocalPart();
            projectId = helper.getLocalProjectPart(id);
        }
        stmt.setInt(index, localId);
        stmt.setInt(projectIndex, projectId);
    }

    private static void setValueType(PreparedStatement stmt, int index, Object o) throws SQLException {
        setValueType(stmt, index, o.getClass());
    }

    private static void setValueType(PreparedStatement stmt, int index, Class clas) throws SQLException {
        setValueType(stmt, index, valueType(clas));
    }

    private static void setValueType(PreparedStatement stmt, int index, int type) throws SQLException {
        /* The setByte below is correct but the JdbcOdbc bridge fails on this call even though the underlying data
         * type is a byte.  Thus we use setInt.
         */
        // stmt.setByte(index, (byte) type);
        stmt.setInt(index, (short) type);
    }

    private static int valueType(Class clas) {
        Assert.assertNotNull("Class", clas);
        int type;
        if (String.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_STRING;
        } else if (Integer.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_INTEGER;
        } else if (Float.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_FLOAT;
        } else if (Boolean.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_BOOLEAN;
        } else if (Cls.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_CLASS;
        } else if (Slot.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_SLOT;
        } else if (Facet.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_FACET;
        } else if (SimpleInstance.class.isAssignableFrom(clas)) {
            type = VALUE_TYPE_SIMPLE_INSTANCE;
        } else {
            throw new IllegalArgumentException("bad value: " + clas);
        }
        return type;
    }

    public static void setFrame(PreparedStatement stmt, int index, int projectIndex, Frame frame, FrameIDHelper helper)
        throws SQLException {
        setId(stmt, index, projectIndex, frame, helper);
    }

    public static void setFrame(
        PreparedStatement stmt,
        int frameIndex,
        int projectIndex,
        int typeIndex,
        Frame frame,
        FrameIDHelper helper)
        throws SQLException {
        setId(stmt, frameIndex, projectIndex, frame, helper);
        setValueType(stmt, typeIndex, frame);
    }

    public static void setSlot(PreparedStatement stmt, int index, int projectIndex, Slot slot, FrameIDHelper helper)
        throws SQLException {
        setId(stmt, index, projectIndex, slot, helper);
    }

    public static void setFacet(PreparedStatement stmt, int index, int projectIndex, Facet facet, FrameIDHelper helper)
        throws SQLException {
        setId(stmt, index, projectIndex, facet, helper);
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

    public static void setShortValue(
        PreparedStatement stmt,
        int valueIndex,
        int valueTypeIndex,
        Object object,
        FrameIDHelper helper)
        throws SQLException {
        stmt.setString(valueIndex, toString(object, helper));
        setValueType(stmt, valueTypeIndex, object);
    }

    private static String toString(Object o, FrameIDHelper helper) {
        Assert.assertNotNull("object", o);
        String s;
        if (o instanceof Frame) {
            FrameID id = ((Frame) o).getFrameID();
            s = helper.createString(id);
        } else {
            s = o.toString();
        }
        return s;
    }

    public static void setShortMatchValue(
        PreparedStatement stmt,
        int valueIndex,
        int valueTypeIndex,
        String value,
        boolean supportsEscape)
        throws SQLException {
        stmt.setString(valueIndex, getMatchString(value, supportsEscape));
        setValueType(stmt, valueTypeIndex, VALUE_TYPE_STRING);
    }

    private static void setNullShortValue(PreparedStatement stmt, int valueIndex, int valueTypeIndex)
        throws SQLException {
        stmt.setNull(valueIndex, Types.VARCHAR);
        setValueType(stmt, valueTypeIndex, VALUE_TYPE_STRING);
    }

    public static void setLongValue(
        PreparedStatement stmt,
        int valueIndex,
        int valueTypeIndex,
        Object object,
        FrameIDHelper helper)
        throws SQLException {
        stmt.setString(valueIndex, toString(object, helper));
        setValueType(stmt, valueTypeIndex, VALUE_TYPE_STRING);

    }

    public static void setNullLongValue(PreparedStatement stmt, int index) throws SQLException {
        stmt.setNull(index, Types.LONGVARCHAR);
    }

    public static void setValue(
        PreparedStatement stmt,
        int shortValueIndex,
        int longValueIndex,
        int valueTypeIndex,
        Object o,
        int sizeLimit,
        FrameIDHelper helper)
        throws SQLException {
        if (isShortValue(o, sizeLimit)) {
            setShortValue(stmt, shortValueIndex, valueTypeIndex, o, helper);
            setNullLongValue(stmt, longValueIndex);
        } else {
            setNullShortValue(stmt, shortValueIndex, valueTypeIndex);
            setLongValue(stmt, longValueIndex, valueTypeIndex, o, helper);
        }
    }

    public static String getMatchString(String input, boolean supportsEscape) {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            c = Character.toUpperCase(c);
            if (supportsEscape && (c == '%' || c == '_' || c == ESCAPE_CHAR)) {
                // escape any special character
                output.append(ESCAPE_CHAR);
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

    public static Frame getFrame(
        ResultSet rs,
        int frameIndex,
        int projectIndex,
        int typeIndex,
        FrameFactory factory,
        FrameIDHelper helper)
        throws SQLException {
        FrameID id = getFrameId(rs, frameIndex, projectIndex, helper);
        int type = rs.getInt(typeIndex);
        return getFrame(id, type, factory);
    }

    private static Frame getFrame(FrameID id, int type, FrameFactory factory) {
        /*
         * It is too much work (and probably also too slow) to get the type information so we just punt on the
         * "java packages" feature with the database backend for now. 
         */
        Collection types = Collections.EMPTY_LIST;
        Frame frame;
        switch (type) {
            case VALUE_TYPE_CLASS :
                frame = factory.createCls(id, types);
                break;
            case VALUE_TYPE_SLOT :
                frame = factory.createSlot(id, types);
                break;
            case VALUE_TYPE_FACET :
                frame = factory.createFacet(id, types);
                break;
            case VALUE_TYPE_SIMPLE_INSTANCE :
                frame = factory.createSimpleInstance(id, types);
                break;
            default :
                throw new RuntimeException("bad value type");
        }
        return frame;
    }

    private static FrameID getFrameId(ResultSet rs, int index, int projectIndex, FrameIDHelper helper)
        throws SQLException {
        int value = rs.getInt(index);
        int project = rs.getInt(projectIndex);
        return helper.createFrameID(project, value);
    }

    public static Slot getSlot(ResultSet rs, int index, int projectIndex, FrameFactory factory, FrameIDHelper helper)
        throws SQLException {
        Collection types = Collections.EMPTY_LIST;
        FrameID id = getFrameId(rs, index, projectIndex, helper);
        return factory.createSlot(id, types);
    }

    public static Facet getFacet(ResultSet rs, int index, int projectIndex, FrameFactory factory, FrameIDHelper helper)
        throws SQLException {
        Collection types = Collections.EMPTY_LIST;
        Facet facet;
        FrameID id = getFrameId(rs, index, projectIndex, helper);
        if (id == null) {
            facet = null;
        } else {
            facet = factory.createFacet(id, types);
        }
        return facet;
    }

    public static Object getShortValue(
        ResultSet rs,
        int valueIndex,
        int valueTypeIndex,
        FrameFactory factory,
        FrameIDHelper helper)
        throws SQLException {
        Object value;
        int type = rs.getByte(valueTypeIndex);
        String valueString = rs.getString(valueIndex);
        switch (type) {
            case VALUE_TYPE_INTEGER :
                value = Integer.valueOf(valueString);
                break;
            case VALUE_TYPE_FLOAT :
                value = Float.valueOf(valueString);
                break;
            case VALUE_TYPE_BOOLEAN :
                value = Boolean.valueOf(valueString);
                break;
            case VALUE_TYPE_STRING :
                value = valueString;
                break;
            default :
                FrameID id = helper.createFrameID(valueString);
                value = getFrame(id, type, factory);
                break;
        }
        // Log.trace("value =" + valueString, DatabaseUtils.class, "getShortValue");
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

    public static void setFrameType(PreparedStatement stmt, int index, Class frameClass) throws SQLException {
        setValueType(stmt, index, frameClass);
    }

}