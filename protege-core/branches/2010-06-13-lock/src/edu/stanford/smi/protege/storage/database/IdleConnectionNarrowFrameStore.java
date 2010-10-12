package edu.stanford.smi.protege.storage.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class IdleConnectionNarrowFrameStore implements NarrowFrameStore {
    private static Logger logger = Log.getLogger(IdleConnectionNarrowFrameStore.class);
    private DatabaseFrameDb delegate;

    public IdleConnectionNarrowFrameStore(DatabaseFrameDb delegate) {
        this.delegate = delegate;
    }

    private void referenceConnection() {
    	try {
			delegate.getCurrentConnection().reference();
		} catch (SQLException e) {
            throw new RuntimeException(e);
		}
    }
    
    private void dereferenceConnection() {
        try {
            delegate.getCurrentConnection().dereference();
        }
        catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
    public String getName() {
		return delegate.getName();
	}


	public void setName(String name) {
	    delegate.setName(name);
	}

	public NarrowFrameStore getDelegate() {
	    return delegate;
	}

	public Map<Sft,List> getFrameValues(Frame frame) {
    	try {
    		referenceConnection();
    		return delegate.getFrameValues(frame);
    	}
    	finally {
    		dereferenceConnection();
    	}
    }
    
    public void addValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Collection values) {
        try {
            referenceConnection();
            delegate.addValues(frame, slot, facet, isTemplate, values);
        }
        finally {
            dereferenceConnection();
        }
    }

    public boolean beginTransaction(String name) {
        try {
            referenceConnection();
            return delegate.beginTransaction(name);
        }
        finally {
            dereferenceConnection();
        }
    }

    public void close() {
        delegate.close();
    }

    public boolean commitTransaction() {
        try {
            referenceConnection();
            return delegate.commitTransaction();
        }
        finally {
            dereferenceConnection();
        }
    }

    public void deleteFrame(Frame frame) {
        try {
            referenceConnection();
            delegate.deleteFrame(frame);
        }
        finally {
            dereferenceConnection();
        }

    }

    public void executeQuery(Query query, QueryCallback callback) {
        try {
            referenceConnection();
            delegate.executeQuery(query, callback);
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate) {
        try {
            referenceConnection();
            return delegate.getClosure(frame, slot, facet, isTemplate);
        }
        finally {
            dereferenceConnection();
        }
    }

    public int getClsCount() {
        try {
            referenceConnection();
            return delegate.getClsCount();
        }
        finally {
            dereferenceConnection();
        }
    }

    public int getFacetCount() {
        try {
            referenceConnection();
            return delegate.getFacetCount();
        }
        finally {
            dereferenceConnection();
        }
    }

    public Frame getFrame(FrameID id) {
        try {
            referenceConnection();
            return delegate.getFrame(id);
        }
        finally {
            dereferenceConnection();
        }
    }

    public int getFrameCount() {
        try {
            referenceConnection();
            return delegate.getFrameCount();
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set<Frame> getFrames() {
        try {
            referenceConnection();
            return delegate.getFrames();
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
                                Object value) {
        try {
            referenceConnection();
            return delegate.getFrames(slot, facet, isTemplate, value);
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet,
                                            boolean isTemplate) {
        try {
            referenceConnection();
            return delegate.getFramesWithAnyValue(slot, facet, isTemplate);
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set<Frame> getMatchingFrames(Slot slot, Facet facet,
                                        boolean isTemplate, String value,
                                        int maxMatches) {
        try {
            referenceConnection();
            return delegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        try {
            referenceConnection();
            return delegate.getMatchingReferences(value, maxMatches);
        }
        finally {
            dereferenceConnection();
        }
    }

    public Set<Reference> getReferences(Object value) {
        try {
            referenceConnection();
            return delegate.getReferences(value);
        }
        finally {
            dereferenceConnection();
        }
    }

    public int getSimpleInstanceCount() {
        try {
            referenceConnection();
            return delegate.getSimpleInstanceCount();
        }
        finally {
            dereferenceConnection();
        }
    }

    public int getSlotCount() {
        try {
            referenceConnection();
            return delegate.getSlotCount();
        }
        finally {
            dereferenceConnection();
        }
    }

    public TransactionMonitor getTransactionStatusMonitor() {
        try {
            referenceConnection();
            return delegate.getTransactionStatusMonitor();
        }
        finally {
            dereferenceConnection();
        }
    }

    public List getValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate) {
        try {
            referenceConnection();
            return delegate.getValues(frame, slot, facet, isTemplate);
        }
        finally {
            dereferenceConnection();
        }
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet,
                              boolean isTemplate) {
        try {
            referenceConnection();
            return delegate.getValuesCount(frame, slot, facet, isTemplate);
        }
        finally {
            dereferenceConnection();
        }
    }

    public void moveValue(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, int from, int to) {
        try {
            referenceConnection();
            delegate.moveValue(frame, slot, facet, isTemplate, from, to);
        }
        finally {
            dereferenceConnection();
        }
    }

    public void reinitialize() {
        try {
            referenceConnection();
            delegate.reinitialize();
        }
        finally {
            dereferenceConnection();
        }
    }

    public boolean setCaching(RemoteSession session, boolean doCache) {
        return delegate.setCaching(session, doCache);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet,
                            boolean isTemplate, Object value) {
        try {
            referenceConnection();
            delegate.removeValue(frame, slot, facet, isTemplate, value);
        }
        finally {
            dereferenceConnection();
        }
    }

    public void replaceFrame(Frame frame) {
        try {
            referenceConnection();
            delegate.replaceFrame(frame);
        }
        finally {
            dereferenceConnection();
        }
    }

    public void replaceFrame(Frame original, Frame replacement) {
        try {
            referenceConnection();
            delegate.replaceFrame(original, replacement);
        }
        finally {
            dereferenceConnection();
        }
    }

    public boolean rollbackTransaction() {
        try {
            referenceConnection();
            return delegate.rollbackTransaction();
        }
        finally {
            dereferenceConnection();
        }
    }

    public void setValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Collection values) {
        try {
            referenceConnection();
            delegate.setValues(frame, slot, facet, isTemplate, values);
        }
        finally {
            dereferenceConnection();
        }
    }

}
