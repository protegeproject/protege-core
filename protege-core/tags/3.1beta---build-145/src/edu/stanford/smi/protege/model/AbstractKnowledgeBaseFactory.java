package edu.stanford.smi.protege.model;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractKnowledgeBaseFactory implements KnowledgeBaseFactory2 {

    protected Reader createReader(KnowledgeBase kb, String name, Collection errors) {
        URI projectUri = kb.getProject().getLoadingURI();
        URI inputUri;
        if (projectUri == null) {
            inputUri = new File(name).toURI();
        } else {
            name = encode(name);
            inputUri = URIUtilities.resolve(projectUri, name);
        }
        Reader reader = URIUtilities.createBufferedReader(inputUri);
        if (reader == null) {
            errors.add("Unable to open " + inputUri + " for " + name);
        }
        return reader;
    }

    private String encode(String name) {
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
        URI uri = kb.getProject().getLoadingURI();
        if (uri != null) {
            getMergingFrameStore(kb).setActiveFrameStoreName(uri.toString());
        }
        loadKnowledgeBase(kb, sources, false, errors);
    }

    protected NarrowFrameStore createNarrowFrameStore(String name) {
        return new InMemoryFrameDb(name);
    }

    public void includeKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        MergingNarrowFrameStore frameStore = getMergingFrameStore(kb);
        URI child = kb.getProject().getLoadingURI();
        frameStore.addActiveFrameStoreChild(createNarrowFrameStore(child.toString()));
        NarrowFrameStore parent = frameStore.activateFrameStore(child.toString());
        loadKnowledgeBase(kb, sources, true, errors);
        frameStore.activateFrameStore(parent);
    }

    private MergingNarrowFrameStore getMergingFrameStore(KnowledgeBase kb) {
        SimpleFrameStore simpleFrameStore = (SimpleFrameStore) ((DefaultKnowledgeBase) kb)
                .getTerminalFrameStore();
        MergingNarrowFrameStore fs = (MergingNarrowFrameStore) simpleFrameStore.getHelper()
                .getDelegate();
        return fs;
    }

    protected abstract void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources,
            boolean isInclude, Collection error);

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

    protected Writer createWriter(File file) {
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

    public void prepareToSaveInFormat(KnowledgeBase kb, KnowledgeBaseFactory factory,
            Collection errors) {
        // Log.enter(this, "prepareToSaveInFormat", kb, factory);
        // do nothing
    }

}