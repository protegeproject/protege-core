package edu.stanford.smi.protege.storage.xml;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class XMLLoader {
    private KnowledgeBase kb;
    private Collection errors;
    private BufferedReader reader;

    public XMLLoader(KnowledgeBase kb, BufferedReader reader, boolean isInclude, Collection errors) {
        this.kb = kb;
        this.reader = reader;
        this.errors = errors;
    }

    public void load() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(reader), new XMLHandler(kb, errors));
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }
}
