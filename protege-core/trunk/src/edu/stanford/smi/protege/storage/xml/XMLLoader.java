package edu.stanford.smi.protege.storage.xml;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class XMLLoader {
    private KnowledgeBase kb;
    private BufferedReader reader;
    private Collection errors;
    public static final String VALIDATE = "http://xml.org/sax/features/validation";

    public XMLLoader(KnowledgeBase kb, BufferedReader reader, boolean isInclude, Collection errors) {
        this.kb = kb;
        this.reader = reader;
        this.errors = errors;
        System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
    }

    public void load() {
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(new MyContentHandler(kb));
            parser.setErrorHandler(new MyErrorHandler(errors));
            parser.setFeature(VALIDATE, true);
            parser.parse(new InputSource(reader));
        } catch (SAXException e) {
            Log.getLogger().severe(Log.toString(e));
        } catch (IOException e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }

}

class MyErrorHandler implements ErrorHandler {
    private Collection errors;

    public MyErrorHandler(Collection errors) {
        this.errors = errors;
    }

    public void error(SAXParseException exception) throws SAXException {
        handle(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        handle(exception);
    }

    public void warning(SAXParseException exception) throws SAXException {
        handle(exception);
    }

    private void handle(Exception e) {
        errors.add(e);
        Log.getLogger().severe(Log.toString(e));
    }

}

class MyContentHandler implements ContentHandler {
    private int indentLevel;
    private KnowledgeBase kb;

    public MyContentHandler(KnowledgeBase kb) {
        this.kb = kb;
        Log.getLogger().info("Reading " + this.kb);
    }

    private void output(String s, int delta) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < indentLevel; ++i) {
            buffer.append("    ");
        }
        buffer.append(s);
        Log.getLogger().info(buffer.toString());
        indentLevel += delta;
    }

    public void setDocumentLocator(Locator locator) {
        output("document locator: " + locator, 0);
    }

    public void startDocument() throws SAXException {
        output("start document", 1);
    }

    public void endDocument() throws SAXException {
        output("end document", -1);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        output("start prefix mapping: " + prefix + " " + uri, +1);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        output("end prefix mapping: " + prefix, -1);
    }

    private LinkedList openElements = new LinkedList();

    private Element getCurrentElement() {
        return (Element) openElements.get(openElements.size() - 1);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (!qName.equals("kb")) {
            Element element = new Element(qName, atts);
            if (openElements.isEmpty()) {
                openElements.add(element);
            } else {
                getCurrentElement().addElement(element);
                openElements.add(element);
            }
        }
        // output("start element: " + uri + " " + localName + " " + qName + " " + atts, +1);

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!qName.equals("kb")) {
            Element lastElement = (Element) openElements.remove(openElements.size() - 1);
            if (openElements.isEmpty()) {
                createFrame(lastElement);
            }
        }
        // output("end element: " + uri + " " + localName + " " + qName, -1);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        // output("character: " + new String(ch, start, length), 0);
        getCurrentElement().addCharacters(new String(ch, start, length));
    }

    private Collection getElementClsValues(Element root, String tag) {
        Collection clses = new ArrayList();
        Iterator i = root.getSubelementValues(tag).iterator();
        while (i.hasNext()) {
            String clsName = (String) i.next();
            clses.add(getCls(clsName));
        }
        return clses;
    }

    private Collection getElementSlotValues(Element root, String tag) {
        Collection slots = new ArrayList();
        Iterator i = root.getSubelementValues(tag).iterator();
        while (i.hasNext()) {
            String slotName = (String) i.next();
            slots.add(getSlot(slotName));
        }
        return slots;
    }

    private Cls getCls(String name) {
        Cls cls = kb.getCls(name);
        if (cls == null) {
            cls = kb.createCls(null, name, kb.getRootClses(), Collections.EMPTY_LIST, false);
        }
        return cls;
    }

    private Slot getSlot(String name) {
        Slot slot = kb.getSlot(name);
        if (slot == null) {
            slot = kb.createSlot(name, null, Collections.EMPTY_LIST, false);
        }
        return slot;
    }

    private void createFrame(Element root) {
        String kind = root.getName();
        String name = root.getSubelementValue("name");
        Collection types = getElementClsValues(root, "type");
        // Log.getLogger().info("create: " + name + ", types=" + types);

        Frame frame;
        if (kind.equals("class")) {
            frame = createCls(name, types, root);
        } else if (kind.equals("slot")) {
            frame = createSlot(name, types, root);
        } else if (kind.equals("facet")) {
            frame = createFacet(name, types, root);
        } else {
            frame = createSimpleInstance(name, types, root);
        }

        addOwnSlotValues(frame, root);
    }

    private void addOwnSlotValues(Frame frame, Element root) {
        Iterator i = root.getSubelements("own_slot_value").iterator();
        while (i.hasNext()) {
            Element node = (Element) i.next();
            addOwnSlotValue(frame, node);
        }
    }

    private void addOwnSlotValue(Frame frame, Element node) {
        Slot slot = null;
        Collection values = new ArrayList();
        Iterator i = node.getSubelements().iterator();
        while (i.hasNext()) {
            Element element = (Element) i.next();
            String value = element.getValue();
            if (element.getName().equals("slot_reference")) {
                slot = getSlot(value);
            } else {
                Object slotValue;
                String typeOfValue = element.getAttributeValue("type");
                if (typeOfValue.equals("integer")) {
                    slotValue = new Integer(value);
                } else if (typeOfValue.equals("float")) {
                    slotValue = new Float(value);
                } else if (typeOfValue.equals("string")) {
                    slotValue = value;
                } else if (typeOfValue.equals("class")) {
                    slotValue = getCls(value);
                } else if (typeOfValue.equals("slot")) {
                    slotValue = getSlot(value);
                } else {
                    slotValue = null;
                    Log.getLogger().warning("Bad slot value: " + value);
                }
                if (slotValue != null) {
                    values.add(slotValue);
                }
            }
        }
        frame.setOwnSlotValues(slot, values);
    }

    private Cls createCls(String name, Collection types, Element root) {
        Collection superclasses = getElementClsValues(root, "superclass");
        Cls cls = kb.getCls(name);
        if (cls == null) {
            cls = kb.createCls(null, name, superclasses, types, false);
        } else {
            cls.setDirectTypes(types);
        }
        Collection slots = getElementSlotValues(root, "template_slot");
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            cls.addDirectTemplateSlot(slot);
        }
        return cls;
    }

    private Slot createSlot(String name, Collection types, Element root) {
        Cls type = (Cls) CollectionUtilities.getFirstItem(types);
        Slot slot = kb.getSlot(name);
        if (slot == null) {
            slot = kb.createSlot(name, type, Collections.EMPTY_LIST, false);
        } else {
            slot.setDirectType(type);
        }
        return slot;
    }

    private Facet createFacet(String name, Collection types, Element root) {
        Cls type = (Cls) CollectionUtilities.getFirstItem(types);
        Facet facet = kb.getFacet(name);
        if (facet == null) {
            facet = kb.createFacet(name, type, false);
        } else {
            facet.setDirectType(type);
        }
        return facet;
    }

    private SimpleInstance createSimpleInstance(String name, Collection types, Element root) {
        SimpleInstance simpleInstance = (SimpleInstance) kb.getInstance(name);
        if (simpleInstance == null) {
            simpleInstance = kb.createSimpleInstance(null, name, types, false);
        } else {
            simpleInstance.setDirectTypes(types);
        }
        return simpleInstance;
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        //output("ignorable whitespace: " + new String(ch, start, length) + " " + start + " " + length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        output("processing instruction: " + target + " " + data, 0);
    }

    public void skippedEntity(String name) throws SAXException {
        output("skipped entity: " + name, 0);
    }

}

class Element {
    private String name;
    private Attributes attributes;
    private String value;
    private List subelements;

    Element(String name, Attributes attributes) {
        this.name = name;
        this.attributes = new AttributesImpl(attributes);
    }

    public void addCharacters(String s) {
        if (value == null) {
            value = s;
        } else {
            value += s;
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void addElement(Element element) {
        if (subelements == null) {
            subelements = new ArrayList();
        }
        subelements.add(element);
    }

    public String getAttributeValue(String type) {
        return attributes.getValue(type);
    }

    public Element getSubelement(int index) {
        return (Element) subelements.get(index);
    }

    public Collection getSubelements() {
        return subelements;
    }

    public String getSubelementValue(String tag) {
        return (String) CollectionUtilities.getFirstItem(getSubelementValues(tag));
    }

    public Collection getSubelementValues(String tag) {
        Collection values = new ArrayList();
        Iterator i = subelements.iterator();
        while (i.hasNext()) {
            Element element = (Element) i.next();
            if (element.getName().equals(tag)) {
                values.add(element.getValue());
            }
        }
        return values;
    }

    public Collection getSubelements(String tag) {
        Collection elements = new ArrayList();
        Iterator i = subelements.iterator();
        while (i.hasNext()) {
            Element element = (Element) i.next();
            if (element.getName().equals(tag)) {
                elements.add(element);
            }
        }
        return elements;

    }

}
