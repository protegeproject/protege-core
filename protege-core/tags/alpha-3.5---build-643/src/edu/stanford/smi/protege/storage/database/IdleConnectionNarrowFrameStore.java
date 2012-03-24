package edu.stanford.smi.protege.storage.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class IdleConnectionNarrowFrameStore implements NarrowFrameStore {
    private static Logger logger = Log.getLogger(IdleConnectionNarrowFrameStore.class);
    private final NarrowFrameStore delegate;
    private final AbstractDatabaseFrameDb databaseNfs;

    public IdleConnectionNarrowFrameStore(ValueCachingNarrowFrameStore delegate) {
        this.delegate = delegate;
        databaseNfs = (AbstractDatabaseFrameDb) delegate.getFrameDb();
    }

    private void setIdle() {
        try {
            databaseNfs.getCurrentConnection().setIdle(true);
        }
        catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }


    public void addValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Collection values) {
        try {
            delegate.addValues(frame, slot, facet, isTemplate, values);
        }
        finally {
            setIdle();
        }
    }

    public boolean beginTransaction(String name) {
        try {
            return delegate.beginTransaction(name);
        }
        finally {
            setIdle();
        }
    }

    public void close() {
        delegate.close();
    }

    public boolean commitTransaction() {
        try {
            return delegate.commitTransaction();
        }
        finally {
            setIdle();
        }
    }

    public void deleteFrame(Frame frame) {
        try {
            delegate.deleteFrame(frame);
        }
        finally {
            setIdle();
        }

    }

    public void executeQuery(Query query, QueryCallback callback) {
        try {
            delegate.executeQuery(query, callback);
        }
        finally {
            setIdle();
        }
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate) {
        try {
            return delegate.getClosure(frame, slot, facet, isTemplate);
        }
        finally {
            setIdle();
        }
    }

    public int getClsCount() {
        try {
            return delegate.getClsCount();
        }
        finally {
            setIdle();
        }
    }

    public NarrowFrameStore getDelegate() {
        return delegate;
    }

    public int getFacetCount() {
        try {
            return delegate.getFacetCount();
        }
        finally {
            setIdle();
        }
    }

    public Frame getFrame(FrameID id) {
        try {
            return delegate.getFrame(id);
        }
        finally {
            setIdle();
        }
    }

    public int getFrameCount() {
        try {
            return delegate.getFrameCount();
        }
        finally {
            setIdle();
        }
    }

    public Set<Frame> getFrames() {
        try {
            return delegate.getFrames();
        }
        finally {
            setIdle();
        }
    }

    public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
                                Object value) {
        try {
            return delegate.getFrames(slot, facet, isTemplate, value);
        }
        finally {
            setIdle();
        }
    }

    public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet,
                                            boolean isTemplate) {
        try {
            return delegate.getFramesWithAnyValue(slot, facet, isTemplate);
        }
        finally {
            setIdle();
        }
    }

    public Set<Frame> getMatchingFrames(Slot slot, Facet facet,
                                        boolean isTemplate, String value,
                                        int maxMatches) {
        try {
            return delegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
        }
        finally {
            setIdle();
        }
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        try {
            return delegate.getMatchingReferences(value, maxMatches);
        }
        finally {
            setIdle();
        }
    }

    public String getName() {
        try {
            return delegate.getName();
        }
        finally {
            setIdle();
        }
    }

    public Set<Reference> getReferences(Object value) {
        try {
            return delegate.getReferences(value);
        }
        finally {
            setIdle();
        }
    }

    public int getSimpleInstanceCount() {
        try {
            return delegate.getSimpleInstanceCount();
        }
        finally {
            setIdle();
        }
    }

    public int getSlotCount() {
        try {
            return delegate.getSlotCount();
        }
        finally {
            setIdle();
        }
    }

    public TransactionMonitor getTransactionStatusMonitor() {
        try {
            return delegate.getTransactionStatusMonitor();
        }
        finally {
            setIdle();
        }
    }

    public List getValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate) {
        try {
            return delegate.getValues(frame, slot, facet, isTemplate);
        }
        finally {
            setIdle();
        }
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet,
                              boolean isTemplate) {
        try {
            return delegate.getValuesCount(frame, slot, facet, isTemplate);
        }
        finally {
            setIdle();
        }
    }

    public void moveValue(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, int from, int to) {
        try {
            delegate.moveValue(frame, slot, facet, isTemplate, from, to);
        }
        finally {
            setIdle();
        }
    }

    public void reinitialize() {
        try {
            delegate.reinitialize();
        }
        finally {
            setIdle();
        }
    }

    public boolean setCaching(RemoteSession session, boolean doCache) {
        return delegate.setCaching(session, doCache);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet,
                            boolean isTemplate, Object value) {
        try {
            delegate.removeValue(frame, slot, facet, isTemplate, value);
        }
        finally {
            setIdle();
        }
    }

    public void replaceFrame(Frame frame) {
        try {
            delegate.replaceFrame(frame);
        }
        finally {
            setIdle();
        }
    }

    public void replaceFrame(Frame original, Frame replacement) {
        try {
            delegate.replaceFrame(original, replacement);
        }
        finally {
            setIdle();
        }
    }

    public boolean rollbackTransaction() {
        try {
            return delegate.rollbackTransaction();
        }
        finally {
            setIdle();
        }
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Collection values) {
        try {
            delegate.setValues(frame, slot, facet, isTemplate, values);
        }
        finally {
            setIdle();
        }
    }

}
