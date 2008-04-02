package edu.stanford.smi.protege.event;

import java.util.*;

/**
 * Interface for notification of knowledge-base events The frame that has been
 * added or removed from the knowledge-base is available through the
 * KnowledgeBaseEvent.getFrame() call.  Note that this interface does not get events that occur when the slot
 * values change, for example.  To get events that occur on an individual instance you need to attach a listener
 * to that instance.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface KnowledgeBaseListener extends EventListener {

    void clsCreated(KnowledgeBaseEvent event);

    void clsDeleted(KnowledgeBaseEvent event);

    void defaultClsMetaClsChanged(KnowledgeBaseEvent event);

    void defaultFacetMetaClsChanged(KnowledgeBaseEvent event);

    void defaultSlotMetaClsChanged(KnowledgeBaseEvent event);

    void facetCreated(KnowledgeBaseEvent event);

    void facetDeleted(KnowledgeBaseEvent event);

    void frameNameChanged(KnowledgeBaseEvent event);

    void instanceCreated(KnowledgeBaseEvent event);

    void instanceDeleted(KnowledgeBaseEvent event);

    void slotCreated(KnowledgeBaseEvent event);

    void slotDeleted(KnowledgeBaseEvent event);
}
