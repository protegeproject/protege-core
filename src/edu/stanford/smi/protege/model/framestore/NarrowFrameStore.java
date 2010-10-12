package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public interface NarrowFrameStore {

  /**
   * The Narrow Frame store has a standard set/get name interface.
   * One of the purposes of this interface is to allow the MergingNarrowFrameStore
   * match names of the narrow frame stores of including and included
   * projects.  For this purpose the name of the narrow frame store
   * is the string representation of the uri for the project.
   *
   * @return the name of this narrow frame store.
   */
    String getName();

    /**
     * The Narrow Frame store has a standard set/get name interface.
     * One of the purposes of this interface is to allow the MergingNarrowFrameStore
     * match names of the narrow frame stores of including and included
     * projects.  For this purpose the name of the narrow frame store
     * is the string representation of the uri for the project.
     *
     * @param name - the name of the Narrow Frame Store.
     */
    void setName(String name);

    NarrowFrameStore getDelegate();

    int getFrameCount();

    int getClsCount();

    int getSlotCount();

    int getFacetCount();

    int getSimpleInstanceCount();

    Set<Frame> getFrames();

    Frame getFrame(FrameID id);

    /**
     * Obtains the values of a slot/facet to a frame.  It consists of a list of Strings, Integers
     * Floats and Frames.
     *
     * This call does one of several things.  If facet == null then we are looking at a slot
     * value.  In this case, if isTemplate is true, then the frame is a class, the slot is
     * a template slot and the value is a default facet value.  If facet != null then we are
     * looking at a facet value.  In this case, if isTemplate is true then the frame is a
     * class, the slot is a template slot and the value is a default facet value.
     *
     * @param frame - the frame
     * @param slot the slot
     * @param facet the facet.  If this is non-null then we are looking at a facet value.
     * @param isTemplate to be determined
     * @return a list of the values of the slot of the frame.
     */
    List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    /**
     * Retrieves the list of values associated with the (frame, slot, facet, isTemplate)
     * combination and moves the item at the position from to the position to.  Indexing is
     * done starting from 0.
     *
     * @param frame the frame (as used in getValues)
     * @param slot the slot (as used in getValues)
     * @param facet the facet (as used in getValues)
     * @param isTemplate whether it is a template (as used in getValues)
     * @param from the starting position of a value
     * @param to the position of the value after this call
     */
    void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to);

    void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value);

    Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate);

    /**
     * @see KnowledgeBase.getMatchingFrames
     *
     * @param slot the slot
     * @param facet the facet
     * @param isTemplate whether we are looking at template values
     * @param value the regexp to use for matching
     * @param maxMatches the max number of matches (-1 for get all)
     * @return
     */
    Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches);

    Set<Reference> getReferences(Object value);

    Set<Reference> getMatchingReferences(String value, int maxMatches);

  /**
   * The executeQuery method allows for complex queries.  It is asynchronous
   * so that in server-client mode the server knowledge base lock will not be
   * held for an excessive amount of time.
   *
   * The contract specifies that the implementor must call one of the
   * QueryCallback methods in a separate thread.  This makes it possible
   * for the caller to know how to retrieve the results in a synchronous way
   * without worrying about deadlock.
   *
   * @param Query  the query to be executed.
   * @param QueryCallback the callback that receives the results of the query.
   */
    void executeQuery(Query query, QueryCallback callback);

    void deleteFrame(Frame frame);

    void close();

    Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    /**
     * A complete hack to work around problems with the java packages feature
     */
    void replaceFrame(Frame frame);

    boolean beginTransaction(String name);

    boolean commitTransaction();

    boolean rollbackTransaction();

    /**
     * Retrieves a transaction status monitor for transactions.  If this call returns null
     * then it means that transactions are not supported.
     *
     * @return A TransactionMonitor object that tracks the status of transactions.
     */
    TransactionMonitor getTransactionStatusMonitor();

    void reinitialize();

    /**
     * Replace all references of the frame original with the frame replacement.
     *
     * This (somewhat expensive) routine is used when the user wants to change the name
     * of a frame.  The result of this call is that the original frame is deleted and
     * the replacement frame takes over in each position where the original frame occured.
     * When the name of a frame is being changed, the caller will create a new frame (the
     * replacement) with the new name and will then delete the original frame.
     *
     * @param original the frame in the database being replaced
     * @param replacement the replacement frame that does not exist in the database before the call.
     */
    void replaceFrame(Frame original, Frame replacement);


    boolean setCaching(RemoteSession session, boolean doCache);
}
