package edu.stanford.smi.protege.event;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Dispatcher to send knowledge base events to their listeners.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class KnowledgeBaseEventDispatcher implements EventDispatcher {

    public void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3) {
        // Log.enter(this, "postEvent");
        KnowledgeBaseEvent event = new KnowledgeBaseEvent((KnowledgeBase) source, type, (Frame) arg1, arg2, arg3);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            KnowledgeBaseListener listener = (KnowledgeBaseListener) i.next();
            switch (type) {
                case KnowledgeBaseEvent.CLS_CREATED :
                    listener.clsCreated(event);
                    break;
                case KnowledgeBaseEvent.CLS_DELETED :
                    listener.clsDeleted(event);
                    break;
                case KnowledgeBaseEvent.SLOT_CREATED :
                    listener.slotCreated(event);
                    break;
                case KnowledgeBaseEvent.SLOT_DELETED :
                    listener.slotDeleted(event);
                    break;
                case KnowledgeBaseEvent.FACET_CREATED :
                    listener.facetCreated(event);
                    break;
                case KnowledgeBaseEvent.FACET_DELETED :
                    listener.facetDeleted(event);
                    break;
                case KnowledgeBaseEvent.INSTANCE_CREATED :
                    listener.instanceCreated(event);
                    break;
                case KnowledgeBaseEvent.INSTANCE_DELETED :
                    listener.instanceDeleted(event);
                    break;
                case KnowledgeBaseEvent.FRAME_NAME_CHANGED :
                    listener.frameNameChanged(event);
                    break;
                case KnowledgeBaseEvent.DEFAULT_CLS_METACLASS_CHANGED :
                    listener.defaultClsMetaClsChanged(event);
                    break;
                case KnowledgeBaseEvent.DEFAULT_SLOT_METACLASS_CHANGED :
                    listener.defaultSlotMetaClsChanged(event);
                    break;
                case KnowledgeBaseEvent.DEFAULT_FACET_METACLASS_CHANGED :
                    listener.defaultFacetMetaClsChanged(event);
                    break;
                default :
                    // Assert.unreachable(type);
                    // Log.trace("dispatch frame kb event", this, "postKnowledgeBaseEvent", event, listener);
            }
        }
    }
}
