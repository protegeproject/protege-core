package edu.stanford.smi.protege.model;

import java.util.Collection;

import edu.stanford.smi.protege.util.PropertyList;

/**
 * This interface will replace the KnowledgeBaseFactory in a later release.  But for now I don't
 * know the impact on the existing plugins and will not change this.
 * 
 * TODO remove the existing includeKnowledgeBase and loadKnowledgeBase and insert the KnowledgeBase2 
 *      stuff from Projects.java into an Abstract class (AbstractKnowledgeBaseFactory) version of these
 *      calls.  The question is which plugins inherit from KnowledgeBaseFactory but not 
 *      from KnowledgeBaseFactory2?
 * 
 * @author tredmond
 *
 */

public interface KnowledgeBaseFactoryNew extends KnowledgeBaseFactory {
  void includeKnowledgeBase(KnowledgeBase kb, 
                            PropertyList sources, 
                            String name,
                            Collection subordinateUris,
                            Collection errors);
  
  void loadKnowledgeBase(KnowledgeBase kb, 
                         PropertyList sources, 
                         Collection subordinateUris,
                         Collection errors);
}
