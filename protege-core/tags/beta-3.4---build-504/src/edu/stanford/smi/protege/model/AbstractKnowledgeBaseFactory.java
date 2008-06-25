package edu.stanford.smi.protege.model;

//ESCA*JAVA0130

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;

import edu.stanford.smi.protege.model.framestore.InMemoryFrameDb;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.URIUtilities;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractKnowledgeBaseFactory implements KnowledgeBaseFactory2 {

    protected BufferedReader createReader(KnowledgeBase kb, String name, Collection errors) {
        URI projectUri = kb.getProject().getLoadingURI();
        URI inputUri;
        if (projectUri == null) {
            inputUri = new File(name).toURI();
        } else {
            name = encode(name);
            inputUri = URIUtilities.resolve(projectUri, name);
        }
        BufferedReader reader = URIUtilities.createBufferedReader(inputUri);
        if (reader == null) {
        	String message = "Unable to open " + inputUri + " for " + name; 
            errors.add(new MessageError(message));
            Log.getLogger().severe(message);
        }
        return reader;
    }

    private static String encode(String name) {
        StringBuffer encodedName = new StringBuffer();
        name = FileUtilities.urlEncode(name);
        int start = 0;
        int index = 0;
        while ((index = name.indexOf('+', start)) != -1) {
            encodedName.append(name.substring(start, index));
            encodedName.append("%20");
            start = index + 1;
        }
        encodedName.append(name.substring(start));
        return encodedName.toString();
    }

    public String getProjectFilePath() {
        return null;
    }

    public void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        loadKnowledgeBase(kb, sources, false, errors);
    }

    public NarrowFrameStore createNarrowFrameStore(String name) {
        return new InMemoryFrameDb(name);
    }

    public void includeKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        loadKnowledgeBase(kb, sources, true, errors);
    }

    protected abstract void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources, boolean isInclude,
            Collection error);

    public String toString() {
        return StringUtilities.getClassName(this);
    }

    protected File createTempFile(KnowledgeBase kb, String fileName) throws IOException {
        File tmpfile = null;
        if (fileName != null) {
            Project p = kb.getProject();
            File file;
            if (p == null) {
                file = new File(fileName);
            } else {
                URI projectUri = kb.getProject().getProjectURI();
                File projectFile = new File(projectUri);
                String path = FileUtilities.replaceFileName(projectFile.getPath(), fileName);
                file = new File(path);
            }
            tmpfile = FileUtilities.createTempFile(file);
        }
        return tmpfile;
    }

    protected void makeTempFilePermanent(File file) throws IOException {
        FileUtilities.makeTempFilePermanent(file);
    }

    protected BufferedWriter createWriter(File file) {
        return FileUtilities.createBufferedWriter(file);
    }

    protected void close(Writer writer) {
        FileUtilities.close(writer);
    }

    protected void close(Reader reader) {
        FileUtilities.close(reader);
    }

    protected URI getSourceUri(String name, PropertyList sources, KnowledgeBase kb) {
        return null;
    }

    public void prepareToSaveInFormat(KnowledgeBase kb, KnowledgeBaseFactory factory, Collection errors) {
        // Log.enter(this, "prepareToSaveInFormat", kb, factory);
        // do nothing
    }

}