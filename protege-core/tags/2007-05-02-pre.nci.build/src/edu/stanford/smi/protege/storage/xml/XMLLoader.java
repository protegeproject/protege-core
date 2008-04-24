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
    private boolean isIncluded;

    public XMLLoader(KnowledgeBase kb, BufferedReader reader, boolean isIncluded, Collection errors) {
        this.kb = kb;
        this.reader = reader;
        //ESCA-JAVA0256 
        this.errors = errors;
        this.isIncluded = isIncluded;
    }

    public void load() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(reader), new XMLHandler(kb, isIncluded, errors));
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }
}
