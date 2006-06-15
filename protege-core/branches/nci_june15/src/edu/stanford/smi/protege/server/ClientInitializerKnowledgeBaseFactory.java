package edu.stanford.smi.protege.server;

import java.util.Set;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;


/**
 * This interface provides a KnowledgeBaseFactory a chance to perform
 * initialization on the client side of a server client relationship.
 * 
 * Unlike other Project implementations, the Remote client project does not
 * call either factory.createNarrowFrameStore,
 * factory.loadKnowledgeBase, or factory.includeKnowledgeBase.  This
 * means that the factory loses several opportunities to initialize
 * the knowledgebase.  In particular, the OWL factories need these
 * opportunities or several of their datastructures will not be set up
 * correctly.  This interface (a bit of a kludge) is here to
 * provide the factory component with an opportunity to do this initialization.
 *
 */
public interface ClientInitializerKnowledgeBaseFactory {

  void initializeClientKnowledgeBase(FrameStore fs,
                                     NarrowFrameStore systemNfs,
                                     NarrowFrameStore nfs, 
                                     KnowledgeBase kb);

}