package edu.stanford.smi.protege.model;

import java.util.Collection;

import edu.stanford.smi.protege.util.PropertyList;

/**
 * A factory for creating a KnowledgeBase implementation. The KB implementation is typically DefaultKnowledgeBase with,
 * perhaps, some "standard" frames loaded.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface KnowledgeBaseFactory {
    String FACTORY_CLASS_NAME = "factory_class_name";

    KnowledgeBase createKnowledgeBase(Collection errors);

    /**
     * Note that this method should really take a URI for a param rather than a string. I maintain the string for
     * backwards compatibility
     * 
     * @param projectURIString
     * @param sources
     */
    KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String projectURIString, PropertyList sources);

    String getDescription();

    String getProjectFilePath();

    void includeKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors);

    boolean isComplete(PropertyList sources);

    void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors);

    void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors);
}