package edu.stanford.smi.protege.model;

/**
 * Exception thrown when a frame is passed in as a parameter to a Knowledge base method and that frame is not a member
 * of the knowledge base.  This can occur when use code loads two projects and mistakenly takes a frame from one and 
 * tries to use it with the other.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MissingFrameException extends IllegalArgumentException {

    private static final long serialVersionUID = 3298554587414692662L;

    /**
     * DeletedFrameException constructor comment.
     * @param s java.lang.String
     */
    public MissingFrameException(String s) {
        super(s);
    }
}
