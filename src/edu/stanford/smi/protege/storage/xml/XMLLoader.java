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

    private static final boolean isValidating = true;
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    public XMLLoader(KnowledgeBase kb, BufferedReader reader, boolean isInclude, Collection errors) {
        this.kb = kb;
        this.reader = reader;
        this.errors = errors;

        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
    }

    public void load() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            if (isValidating) {
                factory.setValidating(true);
            }
            SAXParser saxParser = factory.newSAXParser();
            if (isValidating) {
                saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            }
            saxParser.parse(new InputSource(reader), new XMLHandler(kb, errors));
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }
}
