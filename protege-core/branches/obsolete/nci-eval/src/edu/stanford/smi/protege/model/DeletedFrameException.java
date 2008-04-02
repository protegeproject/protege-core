package edu.stanford.smi.protege.model;

/**
 * Exception thrown when the client calls a method on default knowledge base (directly or indirectly) and passes 
 * in a parameter which is or contains a frame that has been deleted.  For example:
 * 
 * <pre><code>
 * KnowledgeBase kb = ...
 * Slot slot = ...
 * Instance instance = kb.getInstance("foo");
 * kb.deleteFrame(instance);
 * ...
 * i.getOwnSlotValue(slot);     // <-- throws exception
 * </code></pre>
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DeletedFrameException extends IllegalArgumentException {

    public DeletedFrameException(String s) {
        super(s);
    }
}
