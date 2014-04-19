package edu.stanford.smi.protege.storage.xml;

import java.util.*;
import java.util.logging.Level;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

public class XMLHandler extends DefaultHandler {
    private KnowledgeBase kb;
    private boolean isIncluded;
    private Collection errors;

    public XMLHandler(KnowledgeBase kb, boolean isIncluded, Collection errors) {
        this.kb = kb;
        //ESCA-JAVA0256 
        this.errors = errors;
        this.isIncluded = isIncluded;
        // Log.getLogger().info("Reading " + this.kb);
    }

    public void error(SAXParseException exception) {
        handle(exception);
    }

    public void fatalError(SAXParseException exception) {
        handle(exception);
    }

    public void warning(SAXParseException exception) {
        handle(exception);
    }

    private void handle(Exception e) {
    	String message = "Error at parsing ";
    	
    	if (e instanceof SAXParseException) 
    		message = message + "token ar line " + ((SAXParseException)e).getLineNumber() + 
    			" column " + ((SAXParseException)e).getColumnNumber();
    	else message = message + "XML file";
    	
    	errors.add(new MessageError(e, message));        
        Log.getLogger().log(Level.SEVERE, message, e);
    }

    private LinkedList openElements = new LinkedList();

    private Element getCurrentElement() {
        return openElements.isEmpty() ? null : (Element) openElements.get(openElements.size() - 1);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) {
        if (!qName.equals(XMLString.ElementName.KNOWLEDGE_BASE)) {
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

    public void endElement(String uri, String localName, String qName) {
        if (!qName.equals(XMLString.ElementName.KNOWLEDGE_BASE)) {
            Element lastElement = (Element) openElements.remove(openElements.size() - 1);
            if (openElements.isEmpty()) {
                createFrame(lastElement);
            }
        }
        // output("end element: " + uri + " " + localName + " " + qName, -1);
    }

    public void characters(char[] ch, int start, int length) {
        // output("character: " + new String(ch, start, length), 0);
        Element currentElement = getCurrentElement();
        if (currentElement != null) {
            currentElement.addCharacters(new String(ch, start, length));
        }
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
            cls = kb.createCls(new FrameID(name), Collections.EMPTY_LIST, Collections.EMPTY_LIST, false);
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

    private Facet getFacet(String name) {
        Facet facet = kb.getFacet(name);
        if (facet == null) {
            facet = kb.createFacet(name, null, false);
        }
        return facet;
    }

    private SimpleInstance getSimpleInstance(String name) {
        SimpleInstance simpleInstance = kb.getSimpleInstance(name);
        if (simpleInstance == null) {
            simpleInstance = kb.createSimpleInstance(new FrameID(name), Collections.EMPTY_LIST, false);
        }
        return simpleInstance;
    }

    private void createFrame(Element root) {
        String kind = root.getName();
        String name = root.getSubelementValue(XMLString.ElementName.NAME);
        Collection types = getElementClsValues(root, XMLString.ElementName.TYPE);
        // Log.getLogger().info("create: " + name + ", types=" + types);

        Frame frame;
        if (kind.equals(XMLString.ElementName.CLASS)) {
            frame = createCls(name, types, root);
        } else if (kind.equals(XMLString.ElementName.SLOT)) {
            frame = createSlot(name, types, root);
        } else if (kind.equals(XMLString.ElementName.FACET)) {
            frame = createFacet(name, types, root);
        } else if (kind.equals(XMLString.ElementName.SIMPLE_INSTANCE)) {
            frame = createSimpleInstance(name, types, root);
        } else {
            Log.getLogger().warning("bad frame type: " + kind);
            frame = null;
        }

        if (frame != null) {
            addOwnSlotValues(frame, root);
        }
    }

    private void addTemplateFacetValues(Cls cls, Element root) {
        Iterator i = root.getSubelements(XMLString.ElementName.TEMPLATE_FACET_VALUE).iterator();
        while (i.hasNext()) {
            Element node = (Element) i.next();
            addTemplateFacetValue(cls, node);
        }

    }

    private void addTemplateFacetValue(Cls cls, Element node) {
        Slot slot = null;
        Facet facet = null;
        Collection values = new ArrayList();
        Iterator i = node.getSubelements().iterator();
        while (i.hasNext()) {
            Element element = (Element) i.next();
            String elementName = element.getName();
            if (elementName.equals(XMLString.ElementName.SLOT_REFERENCE)) {
                slot = getSlot(element.getValue());
            } else if (elementName.equals(XMLString.ElementName.FACET_REFERENCE)) {
                facet = getFacet(element.getValue());
            } else {
                Object value = getValue(element);
                if (value != null) {
                    values.add(value);
                }
            }
        }
        cls.setTemplateFacetValues(slot, facet, values);
    }

    private void addOwnSlotValues(Frame frame, Element root) {
        Iterator i = root.getSubelements(XMLString.ElementName.OWN_SLOT_VALUE).iterator();
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
            if (element.getName().equals(XMLString.ElementName.SLOT_REFERENCE)) {
                slot = getSlot(element.getValue());
            } else {
                Object value = getValue(element);
                if (value != null) {
                    values.add(value);
                }
            }
        }
        frame.setOwnSlotValues(slot, values);
    }

    private Object getValue(Element element) {
        Object value;
        String valueString = element.getValue();
        String type = element.getAttributeValue(XMLString.AttributeName.VALUE_TYPE);
        if (type.equals(XMLString.AttributeValue.CLASS_TYPE)) {
            value = getCls(valueString);
        } else if (type.equals(XMLString.AttributeValue.SLOT_TYPE)) {
            value = getSlot(valueString);
        } else if (type.equals(XMLString.AttributeValue.FACET_TYPE)) {
            value = getFacet(valueString);
        } else if (type.equals(XMLString.AttributeValue.SIMPLE_INSTANCE_TYPE)) {
            value = getSimpleInstance(valueString);
        } else if (type.equals(XMLString.AttributeValue.STRING_TYPE)) {
            value = valueString;
        } else if (type.equals(XMLString.AttributeValue.BOOLEAN_TYPE)) {
            value = Boolean.valueOf(valueString);
        } else if (type.equals(XMLString.AttributeValue.INTEGER_TYPE)) {
            value = new Integer(valueString);
        } else if (type.equals(XMLString.AttributeValue.FLOAT_TYPE)) {
            value = new Float(valueString);
        } else {
            Log.getLogger().warning("bad value type: " + type);
            value = null;
        }
        return value;

    }

    private static void addSuperclasses(Cls cls, Collection superclasses) {
        if (!superclasses.isEmpty()) {
            Collection currentSuperclasses = cls.getDirectSuperclasses();
            Iterator i = superclasses.iterator();
            while (i.hasNext()) {
                Cls superclass = (Cls) i.next();
                if (!currentSuperclasses.contains(superclass)) {
                    cls.addDirectSuperclass(superclass);
                }
            }
        }
    }

    private static void addTypes(Instance instance, Collection types) {
        if (!types.isEmpty()) {
            Collection currentTypes = instance.getDirectTypes();
            Iterator i = types.iterator();
            while (i.hasNext()) {
                Cls type = (Cls) i.next();
                if (!currentTypes.contains(type)) {
                    instance.addDirectType(type);
                }
            }
        }
    }

    private Cls createCls(String name, Collection types, Element root) {
        Collection superclasses = getElementClsValues(root, XMLString.ElementName.SUPERCLASS);
        Cls cls = kb.getCls(name);
        if (cls == null) {
            cls = kb.createCls(new FrameID(name), superclasses, types, false);
            setIncluded(cls);
        } else {
            addTypes(cls, types);
            addSuperclasses(cls, superclasses);
        }
        Collection slots = getElementSlotValues(root, XMLString.ElementName.TEMPLATE_SLOT);
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            cls.addDirectTemplateSlot(slot);
        }
        addTemplateFacetValues(cls, root);
        return cls;
    }

    private Slot createSlot(String name, Collection types, Element root) {
        Slot slot = kb.getSlot(name);
        if (slot == null) {
            Cls type = (Cls) CollectionUtilities.getFirstItem(types);
            slot = kb.createSlot(name, type, Collections.EMPTY_LIST, false);
            setIncluded(slot);
        } else {
            addTypes(slot, types);
        }
        return slot;
    }

    private Facet createFacet(String name, Collection types, Element root) {
        Facet facet = kb.getFacet(name);
        if (facet == null) {
            Cls type = (Cls) CollectionUtilities.getFirstItem(types);
            facet = kb.createFacet(name, type, false);
            setIncluded(facet);
        } else {
            addTypes(facet, types);
        }
        return facet;
    }

    private SimpleInstance createSimpleInstance(String name, Collection types, Element root) {
        SimpleInstance simpleInstance = (SimpleInstance) kb.getInstance(name);
        if (simpleInstance == null) {
            simpleInstance = kb.createSimpleInstance(new FrameID(name), types, false);
            setIncluded(simpleInstance);
        } else {
            addTypes(simpleInstance, types);
        }
        return simpleInstance;
    }

    private void setIncluded(Frame frame) {
        if (isIncluded) {
            frame.setIncluded(true);
        }
    }

}
