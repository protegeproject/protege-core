package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Storage backend to maintain a knowledge base in Clips 6.0 file format.  Eventually this file format will be retired
 * in favor of an XML + XML Schema format.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsKnowledgeBaseFactory extends AbstractKnowledgeBaseFactory {
    public static final String DESCRIPTION = Text.getProgramName() + " Files (.pont and .pins)";
    private static final String CLSES_FILE_NAME = "classes_file_name";
    private static final String INSTANCES_FILE_NAME = "instances_file_name";

    public KnowledgeBase createKnowledgeBase(Collection errors) {
        KnowledgeBase kb = new DefaultKnowledgeBase(this);
        return kb;
    }

    public KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String project, PropertyList list) {
        return new FileSourcesPanel(project, list);
    }

    public static String getClsesSourceFile(PropertyList sources) {
        return sources.getString(CLSES_FILE_NAME);
    }

    public static URI getClsesSourceRelativeURI(PropertyList sources) {
        return URIUtilities.createURI(getClsesSourceFile(sources));
    }

    public Reader getClsesSourceReader(KnowledgeBase kb, PropertyList sources, Collection errors) {
        String name = getClsesSourceFile(sources);
        return (name == null) ? null : createReader(kb, name, errors);
    }

    public Reader getInstancesSourceReader(KnowledgeBase kb, PropertyList sources, Collection errors) {
        String name = getInstancesSourceFile(sources);
        return (name == null) ? null : createReader(kb, name, errors);
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public static String getInstancesSourceFile(PropertyList sources) {
        return sources.getString(INSTANCES_FILE_NAME);
    }

    public static URI getInstancesSourceRelativeURI(PropertyList sources) {
        return URIUtilities.createURI(getInstancesSourceFile(sources));
    }

    public boolean isComplete(PropertyList sources) {
        // If either file is not set we now generate it from the project name.
        // return getClsesSourceFile(sources) != null && getInstancesSourceFile(sources) != null;
        return true;
    }

    protected void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources, boolean isInclude, Collection errors) {
        Reader clsesReader = getClsesSourceReader(kb, sources, errors);
        Reader instancesReader = getInstancesSourceReader(kb, sources, errors);
        loadKnowledgeBase(kb, clsesReader, instancesReader, isInclude, errors);
        close(clsesReader);
        close(instancesReader);
    }

    //ESCA-JAVA0130 
    public void loadKnowledgeBase(KnowledgeBase kb, Reader clsesReader, Reader instancesReader, boolean isInclude,
            Collection errors) {
        // Log.enter(this, "loadKnowledgeBase", kb, new Boolean(isInclude));
        boolean cachingEnabled = kb.setCallCachingEnabled(false);
        boolean eventsEnabled = kb.setGenerateEventsEnabled(false);
        if (clsesReader != null) {
            Parser delegate = new Parser(clsesReader);
            delegate.loadClses(kb, isInclude, errors);
        }
        if (instancesReader != null) {
            Parser delegate = new Parser(instancesReader);
            delegate.loadInstances(kb, isInclude, errors);
        }
        kb.setGenerateEventsEnabled(eventsEnabled);
        kb.setCallCachingEnabled(cachingEnabled);
    }

    public void loadKnowledgeBase(KnowledgeBase kb, String clsesName, String instancesName, boolean isInclude,
            Collection errors) {
        Reader clsesReader = createReader(kb, clsesName, errors);
        Reader instancesReader = createReader(kb, instancesName, errors);
        loadKnowledgeBase(kb, clsesReader, instancesReader, isInclude, errors);
        FileUtilities.close(clsesReader);
        FileUtilities.close(instancesReader);
    }

    public KnowledgeBase loadKnowledgeBase(Reader clsesReader, Reader instancesReader, Collection errors) {
        KnowledgeBase kb = createKnowledgeBase(errors);
        loadKnowledgeBase(kb, clsesReader, instancesReader, false, errors);
        return kb;
    }

    public KnowledgeBase loadKnowledgeBase(String clsesFileName, String instancesFileName, Collection errors) {
        KnowledgeBase kb = createKnowledgeBase(errors);
        loadKnowledgeBase(kb, clsesFileName, instancesFileName, false, errors);
        return kb;
    }

    public void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        boolean changed = false;
        String projectBaseName = kb.getProject().getProjectName();
        String clsesName = getClsesSourceFile(sources);
        if (clsesName == null) {
            clsesName = projectBaseName + ".pont";
            changed = true;
        }
        String instancesName = getInstancesSourceFile(sources);
        if (instancesName == null) {
            instancesName = projectBaseName + ".pins";
            changed = true;
        }
        if (changed) {
            setSourceFiles(sources, clsesName, instancesName);
        }
        saveKnowledgeBase(kb, clsesName, instancesName, errors);
    }

    //ESCA-JAVA0130 
    public void saveKnowledgeBase(KnowledgeBase kb, Writer clsesWriter, Writer instancesWriter, Collection errors) {
        if (clsesWriter != null) {
            ClsStorer clsStorer = new ClsStorer(clsesWriter);
            clsStorer.storeClses(kb, errors);
        }
        if (instancesWriter != null) {
            InstanceStorer instanceStorer = new InstanceStorer(instancesWriter);
            instanceStorer.storeInstances(kb, errors);
        }
    }

    public void saveKnowledgeBase(KnowledgeBase kb, String clsesFileName, String instancesFileName, Collection errors) {
        Writer pontWriter = null;
        Writer pinsWriter = null;
        try {
            File pontFile = createTempFile(kb, clsesFileName);
            File pinsFile = createTempFile(kb, instancesFileName);
            pontWriter = createWriter(pontFile);
            pinsWriter = createWriter(pinsFile);
            saveKnowledgeBase(kb, pontWriter, pinsWriter, errors);
            close(pontWriter);
            close(pinsWriter);
            if (errors.isEmpty()) {
                makeTempFilePermanent(pontFile);
                makeTempFilePermanent(pinsFile);
            }
        } catch (Exception e) {
        	String message = "Error saving project. Classes file name: " + clsesFileName + ". Instances file name: " + instancesFileName;
            errors.add(new MessageError(e, message));
            Log.getLogger().log(Level.SEVERE, message, e);  
            close(pontWriter);
            close(pinsWriter);
        }
    }

    public static void setSourceFiles(PropertyList sources, String clsesFileName, String instancesFileName) {
        setProperty(sources, CLSES_FILE_NAME, clsesFileName);
        setProperty(sources, INSTANCES_FILE_NAME, instancesFileName);
    }

    public static void setSourceURIs(PropertyList sources, URI relativeClsesURI, URI relativeInstancesURI) {
        Assert.assertTrue("Classes uri", relativeClsesURI == null || !relativeClsesURI.isAbsolute());
        Assert.assertTrue("Instances uri", relativeInstancesURI == null || !relativeInstancesURI.isAbsolute());
        setSourceFiles(sources, toString(relativeClsesURI), toString(relativeInstancesURI));
    }

    private static String toString(URI uri) {
        return (uri == null) ? null : uri.toString();
    }

    private static void setProperty(PropertyList sources, String propertyName, String value) {
        sources.setString(propertyName, value);
    }
}
