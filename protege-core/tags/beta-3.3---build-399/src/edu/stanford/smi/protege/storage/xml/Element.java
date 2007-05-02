package edu.stanford.smi.protege.storage.xml;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.stanford.smi.protege.util.*;

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
        //ESCA-JAVA0259 
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
