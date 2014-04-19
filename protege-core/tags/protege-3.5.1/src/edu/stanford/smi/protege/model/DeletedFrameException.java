package edu.stanford.smi.protege.model;
//ESCA*JAVA0051

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

    private static final long serialVersionUID = -1201162153303219322L;

    public DeletedFrameException(String s) {
        super(s);
    }
}
