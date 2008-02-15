package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

public abstract class TransactionFrameStore extends ModificationFrameStore {
    private boolean _inExternalTransaction;

    protected TransactionFrameStore() {
    }

    public boolean beginTransaction(String name) {
        _inExternalTransaction = true;
        return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        _inExternalTransaction = false;
        return getDelegate().commitTransaction();
    }

    private void localBeginTransaction() {
        if (!_inExternalTransaction) {
            // getDelegate().beginTransaction();
        }
    }

    private void localEndTransaction() {
        if (!_inExternalTransaction) {
            // getDelegate().endTransaction(true);
        }
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        try {
            localBeginTransaction();
            getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        } finally {
            localEndTransaction();
        }
    }
}