package edu.stanford.smi.protege.event;

/**
 * Adapter for the listener interface for Knowledge base events.  Subclass this class if you want only to catch a few
 * knowledge base events.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class KnowledgeBaseAdapter implements KnowledgeBaseListener {

    public void clsCreated(KnowledgeBaseEvent event) {
    }

    public void clsDeleted(KnowledgeBaseEvent event) {
    }

    public void defaultClsMetaClsChanged(KnowledgeBaseEvent event) {
    }

    public void defaultFacetMetaClsChanged(KnowledgeBaseEvent event) {
    }

    public void defaultSlotMetaClsChanged(KnowledgeBaseEvent event) {
    }

    public void facetCreated(KnowledgeBaseEvent event) {
    }

    public void facetDeleted(KnowledgeBaseEvent event) {
    }

    /*
     * @deprecated Override replaceFrame instead
     */
    @Deprecated
    public void frameNameChanged(KnowledgeBaseEvent event) {
        frameReplaced(event);
    }
    
    public void frameReplaced(KnowledgeBaseEvent event) {
    }

    public void instanceCreated(KnowledgeBaseEvent event) {
    }

    public void instanceDeleted(KnowledgeBaseEvent event) {
    }

    public void slotCreated(KnowledgeBaseEvent event) {
    }

    public void slotDeleted(KnowledgeBaseEvent event) {
    }
}
