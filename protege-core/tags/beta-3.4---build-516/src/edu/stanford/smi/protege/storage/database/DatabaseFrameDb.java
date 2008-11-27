package edu.stanford.smi.protege.storage.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public interface DatabaseFrameDb extends NarrowFrameStore {

    public void initialize(FrameFactory factory,
                           String driver,
                           String url, String user, String pass, String table,
                           boolean isInclude);
    
    public String getTableName();

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    public Map<Sft,List> getFrameValues(Frame frame);

    public void overwriteKB(KnowledgeBase kb,
                                boolean saveFrames) throws SQLException;

    public TransactionMonitor getTransactionStatusMonitor();
}