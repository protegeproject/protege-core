package edu.stanford.smi.protege.storage.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Storage backend to maintain a knowledge base in XML format.  
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class XMLKnowledgeBaseFactory extends AbstractKnowledgeBaseFactory {
    // ublic static final String DESCRIPTION = Text.getProgramName() + " Files (.xml)";
    public static final String DESCRIPTION = "Experimental XML File (.xml)";
    private static final String SOURCE_FILE_NAME = "source_file_name";

    public KnowledgeBase createKnowledgeBase(Collection errors) {
        KnowledgeBase kb = new DefaultKnowledgeBase(this);
        return kb;
    }

    public KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String project, PropertyList list) {
        return new FileSourcesPanel(project, list);
    }

    public static String getSourceFile(PropertyList sources) {
        return sources.getString(SOURCE_FILE_NAME);
    }

    public static URI getSourceRelativeURI(PropertyList sources) {
        return URIUtilities.createURI(getSourceFile(sources));
    }

    public BufferedReader getSourceReader(KnowledgeBase kb, PropertyList sources, Collection errors) {
        String name = getSourceFile(sources);
        return (name == null) ? null : createReader(kb, name, errors);
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public boolean isComplete(PropertyList sources) {
        // If either file is not set we now generate it from the project name.
        // return getClsesSourceFile(sources) != null && getInstancesSourceFile(sources) != null;
        return true;
    }

    protected void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources, boolean isInclude, Collection errors) {
        BufferedReader reader = getSourceReader(kb, sources, errors);
        loadKnowledgeBase(kb, reader, isInclude, errors);
        close(reader);
    }

    public static void loadKnowledgeBase(KnowledgeBase kb, BufferedReader reader, boolean isInclude, Collection errors) {
        boolean cachingEnabled = kb.setCallCachingEnabled(false);
        boolean eventsEnabled = kb.setGenerateEventsEnabled(false);
        new XMLLoader(kb, reader, isInclude, errors).load();
        kb.setGenerateEventsEnabled(eventsEnabled);
        kb.setCallCachingEnabled(cachingEnabled);
    }

    public void loadKnowledgeBase(KnowledgeBase kb, String sourceName, boolean isInclude, Collection errors) {
        BufferedReader reader = createReader(kb, sourceName, errors);
        loadKnowledgeBase(kb, reader, isInclude, errors);
        FileUtilities.close(reader);
    }

    public KnowledgeBase loadKnowledgeBase(BufferedReader reader, Collection errors) {
        KnowledgeBase kb = createKnowledgeBase(errors);
        loadKnowledgeBase(kb, reader, false, errors);
        return kb;
    }

    public KnowledgeBase loadKnowledgeBase(String filename, Collection errors) {
        KnowledgeBase kb = createKnowledgeBase(errors);
        loadKnowledgeBase(kb, filename, false, errors);
        return kb;
    }

    public void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        String projectBaseName = kb.getProject().getProjectName();
        String sourceName = getSourceFile(sources);
        if (sourceName == null) {
            sourceName = projectBaseName + ".xml";
            setSourceFile(sources, sourceName);
        }
        saveKnowledgeBase(kb, sourceName, errors);
    }

    public static void saveKnowledgeBase(KnowledgeBase kb, Writer writer, Collection errors) {
        new XMLStorer(kb, writer, errors).store();
    }

    public void saveKnowledgeBase(KnowledgeBase kb, String filename, Collection errors) {
        Writer writer = null;
        try {
            File file = createTempFile(kb, filename);
            writer = createWriter(file);
            saveKnowledgeBase(kb, writer, errors);
            close(writer);
            if (errors.isEmpty()) {
                makeTempFilePermanent(file);
            }
        } catch (Exception e) {
        	String message = "Errors saving file " + filename;
            errors.add(new MessageError(e, message));
            Log.getLogger().log(Level.SEVERE, message, e);
            close(writer);
        }
    }

    public static void setSourceFile(PropertyList sources, String filename) {
        setProperty(sources, SOURCE_FILE_NAME, filename);
    }

    public static void setSourceURIs(PropertyList sources, URI relativeURI) {
        setSourceFile(sources, toString(relativeURI));
    }

    private static String toString(URI uri) {
        return (uri == null) ? null : uri.toString();
    }

    private static void setProperty(PropertyList sources, String propertyName, String value) {
        sources.setString(propertyName, value);
    }
}
