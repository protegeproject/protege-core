package edu.stanford.smi.protege.storage.jdbc;

import java.sql.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Interface to a database.  All SQL commands are hidden away in the implementation of
 * this interface.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface DatabaseManager extends DatabaseConstants, Disposable {

    void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value)
             throws SQLException;

    public boolean beginTransaction();

    public boolean endTransaction(boolean doCommit);

    Collection getAllFrameIDs()
             throws SQLException;

    int getFrameCount(int type) throws SQLException;

    // FrameInfo getFrameInfo(FrameID id) throws SQLException;
    /**
     *  returns a collection of DBReference objects
     *
     * @return                   The DBReferences value
     * @param  o                 Description of Parameter
     * @exception  SQLException  Description of Exception
     */
    Collection getDBReferences(Object o, int maxReferences)
             throws SQLException;

    String getDriverName();

    Collection getFrameIDs(FrameID slot, FrameID facet, boolean isTemplate, String value) throws SQLException;

    /**
     *  returns a collection of FrameIDs
     */
    Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object value) throws SQLException;

    /**
     *  returns a collection of FrameIDs
     */
    Collection getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) throws SQLException;

    int getFrameType(FrameID id) throws SQLException;

    Collection getMatchingFrameIDs(FrameID slot, FrameID facet, boolean isTemplate,
            String matchString, int maxMatches) throws SQLException;

    String getTableName();

    int getValueCount(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate)
             throws SQLException;

    /**
     *  returns a collection of Objects. A frame is returned as a frameID
     */
    List getValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate)
             throws SQLException;

    boolean hasValueAtSomeFrame(Slot slot, Facet facet, boolean isTemplate) throws SQLException;

    void removeAllReferences(Frame frame)
             throws SQLException;

    void removeAllValues(Frame frame, Slot slot, Facet facet, boolean isTemplate)
             throws SQLException;

    void removeFramesWithValue(Slot slot, Facet facet, boolean isTemplate) throws SQLException;

    void removeSingleValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) throws SQLException;

    void saveKnowledgeBase(KnowledgeBase kb)
             throws SQLException;

    void setTracing(boolean b);

    void setValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value)
             throws SQLException;

    void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values)
             throws SQLException;

    public boolean supportsTransactions();
}
