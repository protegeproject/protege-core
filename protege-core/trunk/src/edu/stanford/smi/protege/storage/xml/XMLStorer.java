package edu.stanford.smi.protege.storage.xml;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.xml.XMLString.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class XMLStorer {
    private KnowledgeBase kb;
    private PrintWriter writer;
    private Collection excludeSlots = new HashSet();
    private int indentLevel;

    public XMLStorer(KnowledgeBase kb, Writer writer, Collection errors) {
        this.kb = kb;
        this.writer = new PrintWriter(writer);
        initializeExcludeSlots();
    }

    private void initializeExcludeSlots() {
        exclude(Model.Slot.NAME);
        exclude(Model.Slot.DIRECT_INSTANCES);
        exclude(Model.Slot.DIRECT_TEMPLATE_SLOTS);
        exclude(Model.Slot.DIRECT_SUPERCLASSES);
        exclude(Model.Slot.DIRECT_SUBCLASSES);
        exclude(Model.Slot.DIRECT_TYPES);
        exclude(Model.Slot.DIRECT_INSTANCES);
        exclude(Model.Slot.DIRECT_SUBSLOTS);
        exclude(Model.Slot.DIRECT_SUPERSLOTS);
    }

    private void exclude(String slotName) {
        excludeSlots.add(kb.getSlot(slotName));
    }

    private void indent() {
        ++indentLevel;
    }

    private void unindent() {
        --indentLevel;
    }

    public void store() {
        preamble();
        indent();
        storeClses();
        storeSlots();
        storeFacets();
        storeSimpleInstances();
        unindent();
        postscript();
        writer.flush();
    }

    private void storeClses() {
        storeCls(kb.getRootCls(), new HashSet());
    }

    private void storeSlots() {
        List slots = new ArrayList(kb.getSlots());
        Collections.sort(slots);
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            storeSlot(slot);
        }
    }

    private void storeFacets() {
        List facets = new ArrayList(kb.getFacets());
        Collections.sort(facets);
        Iterator i = facets.iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            storeFacet(facet);
        }
    }

    private void storeSimpleInstances() {
        Set storedInstances = new HashSet();
        Iterator i = kb.getClses().iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            storeSimpleInstances(cls, storedInstances);
        }
    }

    private void storeSimpleInstances(Cls cls, Set storedInstances) {
        Iterator i = cls.getDirectInstances().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (instance instanceof SimpleInstance && !storedInstances.contains(instance)) {
                storeSimpleInstance((SimpleInstance) instance);
                storedInstances.add(instance);
            }
        }
    }

    private void printHeader() {
        println("<?xml version=\"1.0\" ?>");
    }

    //    private void printDtd() {
    //        println("<!DOCTYPE knowledge_base [");
    //        indent();
    //        println("<!ELEMENT knowledge_base (class | slot | facet | simple_instance)*>");
    //        println("<!ATTLIST knowledge_base xmlns CDATA #IMPLIED>");
    //        println();
    //        println("<!ELEMENT class (name, type*, superclass*, template_slot*, template_facet_value*, own_slot_value*)>");
    //        println("<!ATTLIST class id ID #REQUIRED>");
    //        println();
    //        println("<!ELEMENT slot (name, type*, superslot*, own_slot_value*)>");
    //        println("<!ATTLIST slot id ID #REQUIRED>");
    //        println();
    //        println("<!ELEMENT facet (name, type*, own_slot_value*)>");
    //        println("<!ATTLIST facet id ID #REQUIRED>");
    //        println();
    //        println("<!ELEMENT simple_instance (name, type*, own_slot_value*)>");
    //        println("<!ATTLIST simple_instance id ID #REQUIRED>");
    //        println();
    //        println("<!ELEMENT name (#PCDATA)>");
    //        println("<!ELEMENT type (#PCDATA)>");
    //        println("<!ELEMENT superclass (#PCDATA)>");
    //        println("<!ELEMENT template_slot (#PCDATA)>");
    //        println("<!ELEMENT superslot (#PCDATA)>");
    //        println("<!ELEMENT slot_reference (#PCDATA)>");
    //        println("<!ELEMENT facet_reference (#PCDATA)>");
    //        println();
    //        println("<!ELEMENT own_slot_value (slot_reference, (primitive_value | reference_value)*)>");
    //        println("<!ELEMENT template_facet_value (slot_reference, facet_reference, (primitive_value | reference_value)*)>");
    //
    //        println("<!ELEMENT primitive_value (#PCDATA)>");
    //        println("<!ATTLIST primitive_value type CDATA #REQUIRED>");
    //        println("<!ELEMENT reference_value (#PCDATA)>");
    //        println("<!ATTLIST reference_value type CDATA #REQUIRED>");
    //        unindent();
    //        println("]>");
    //    }

    private void preamble() {
        printHeader();
        // indent();
        // printDtd();
        // unindent();
        String[] attributes = new String[] { "xmlns=\"http://protege.stanford.edu/xml\"",
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                "xsi:schemaLocation=\"file:///c/temp protege.xsd\"" };
        println();
        printStartTag(XMLString.ElementName.KNOWLEDGE_BASE, attributes);
    }

    private void postscript() {
        printEndTag(XMLString.ElementName.KNOWLEDGE_BASE);
    }

    private void println(String s) {
        for (int i = 0; i < indentLevel; ++i) {
            writer.print("\t");
        }
        writer.println(s);
    }

    private void println() {
        writer.println();
    }

    private void printValue(String element, String value) {
        println("<" + element + ">" + value + "</" + element + ">");
    }

    private void printValue(String element, String elementValue, String attribute, String value) {
        println("<" + element + " " + attribute + "=\"" + value + "\">" + elementValue + "</" + element + ">");
    }

    private void printFrameReference(String tag, Frame frame) {
        printValue(tag, frame.getName());
    }

    private String getId(Frame frame) {
        String name = frame.getName();
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (!Character.isLetterOrDigit(c)) {
                chars[i] = '_';
            }
        }
        return new String(chars);
    }

    private void printStartTag(String element) {
        printStartTag(element, null);
    }

    private void printStartTag(String element, String[] attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<");
        buffer.append(element);
        if (attribute != null) {
            String spaces = null;
            for (int i = 0; i < attribute.length; ++i) {
                if (true || i == 0) {
                    buffer.append(" ");
                } else {
                    if (spaces == null) {
                        StringBuffer spaceBuffer = new StringBuffer();
                        // spaceBuffer.append('\n');
                        for (int j = 0; j < buffer.length(); ++j) {
                            spaceBuffer.append(' ');
                        }
                        spaces = spaceBuffer.toString();
                    }
                    buffer.append(spaces);
                }
                buffer.append(attribute[i]);
            }
        }
        buffer.append(">");
        println(buffer.toString());

    }

    private void printStartTag(String element, String attribute, String value) {
        printStartTag(element, new String[] { attribute + "=\"" + value + "\"" });
    }

    private void printEndTag(String element) {
        println("</" + element + ">");
    }

    private void beginFrame(Frame frame, String frameType) {
        printStartTag(frameType, XMLString.AttributeName.ID, getId(frame));
        indent();
        printValue(XMLString.ElementName.NAME, frame.getName());
        unindent();
    }

    private void endFrame(String frameType, Frame frame) {
        indent();
        printOwnSlotValues(frame);
        unindent();
        printEndTag(frameType);
        println("");
    }

    private void printOwnSlotValues(Frame frame) {
        Iterator i = frame.getOwnSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (printOwnSlot(frame, slot)) {
                printOwnSlotValues(frame, slot);
            }
        }
    }

    private boolean printOwnSlot(Frame frame, Slot slot) {
        return !excludeSlots.contains(slot);
    }

    private void printOwnSlotValues(Frame frame, Slot slot) {
        Collection values = frame.getDirectOwnSlotValues(slot);
        if (!values.isEmpty()) {
            printStartTag(XMLString.ElementName.OWN_SLOT_VALUE);
            indent();
            printValue(XMLString.ElementName.SLOT_REFERENCE, slot.getName());
            printValues(frame.getDirectOwnSlotValues(slot));
            unindent();
            printEndTag(XMLString.ElementName.OWN_SLOT_VALUE);
        }
    }

    private void printValues(Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            printValue(o);
        }
    }

    private void printValue(Object o) {
        String value;
        String tag;
        String attribute;
        if (o instanceof Frame) {
            tag = XMLString.ElementName.REFERENCE_VALUE;
            attribute = frameAttribute(o);
            value = frameValue(o);
        } else {
            tag = XMLString.ElementName.PRIMITIVE_VALUE;
            if (o instanceof String) {
                attribute = XMLString.AttributeValue.STRING_TYPE;
                value = (String) o;
            } else if (o instanceof Integer) {
                attribute = XMLString.AttributeValue.INTEGER_TYPE;
                value = o.toString();
            } else if (o instanceof Float) {
                attribute = XMLString.AttributeValue.FLOAT_TYPE;
                value = o.toString();
            } else if (o instanceof Boolean) {
                attribute = XMLString.AttributeValue.BOOLEAN_TYPE;
                value = o.toString();
            } else {
                Log.getLogger().warning("Unexpected object: " + o);
                attribute = XMLString.AttributeValue.STRING_TYPE;
                value = o.toString();
            }
        }
        printValue(tag, value, XMLString.AttributeName.VALUE_TYPE, attribute);
    }

    private String frameValue(Object o) {
        return ((Frame) o).getName();
    }

    private String frameAttribute(Object o) {
        String attribute;
        if (o instanceof Cls) {
            attribute = AttributeValue.CLASS_TYPE;
        } else if (o instanceof Slot) {
            attribute = XMLString.AttributeValue.SLOT_TYPE;
        } else if (o instanceof Facet) {
            attribute = XMLString.AttributeValue.FACET_TYPE;
        } else {
            attribute = XMLString.AttributeValue.SIMPLE_INSTANCE_TYPE;
        }
        return attribute;
    }

    private void endInstance(String frameType, Instance instance) {
        endFrame(frameType, instance);
    }

    private void beginInstance(String frameType, Instance instance) {
        beginFrame(instance, frameType);
        indent();
        printFrameValues(XMLString.ElementName.TYPE, instance.getDirectTypes());
        unindent();
    }

    private void printFrameValues(String tag, Collection frames) {
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            printFrameReference(tag, frame);
        }
    }

    private void storeCls(Cls cls, Set storedClses) {
        beginInstance(XMLString.ElementName.CLASS, cls);
        indent();
        printFrameValues(XMLString.ElementName.SUPERCLASS, cls.getDirectSuperclasses());
        printFrameValues(XMLString.ElementName.TEMPLATE_SLOT, cls.getDirectTemplateSlots());
        printTemplateFacetValues(cls);
        unindent();
        endInstance(XMLString.ElementName.CLASS, cls);

        storedClses.add(cls);
        storeSubclasses(cls, storedClses);
    }

    private void storeSubclasses(Cls cls, Set storedClses) {
        Iterator i = cls.getDirectSubclasses().iterator();
        while (i.hasNext()) {
            Cls subclass = (Cls) i.next();
            if (!storedClses.contains(subclass)) {
                storeCls(subclass, storedClses);
            }
        }
    }

    private void printTemplateFacetValues(Cls cls) {
        Iterator i = cls.getTemplateSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            printTemplateFacetValues(cls, slot);
        }
    }

    private void printTemplateFacetValues(Cls cls, Slot slot) {
        Iterator i = cls.getTemplateFacets(slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            Collection values = cls.getDirectTemplateFacetValues(slot, facet);
            if (!values.isEmpty()) {
                printTemplateFacetValues(slot, facet, values);
            }
        }
    }

    private void printTemplateFacetValues(Slot slot, Facet facet, Collection values) {
        printStartTag(XMLString.ElementName.TEMPLATE_FACET_VALUE);
        indent();
        printFrameReference(XMLString.ElementName.SLOT_REFERENCE, slot);
        printFrameReference(XMLString.ElementName.FACET_REFERENCE, facet);
        printValues(values);
        unindent();
        printEndTag(XMLString.ElementName.TEMPLATE_FACET_VALUE);
    }

    private void storeSlot(Slot slot) {
        beginInstance(XMLString.ElementName.SLOT, slot);
        printFrameValues(XMLString.ElementName.SUPERSLOT, slot.getDirectSuperslots());
        endInstance(XMLString.ElementName.SLOT, slot);
    }

    private void storeFacet(Facet facet) {
        beginInstance(XMLString.ElementName.FACET, facet);
        endInstance(XMLString.ElementName.FACET, facet);
    }

    private void storeSimpleInstance(SimpleInstance simpleInstance) {
        beginInstance(XMLString.ElementName.SIMPLE_INSTANCE, simpleInstance);
        endInstance(XMLString.ElementName.SIMPLE_INSTANCE, simpleInstance);
    }

}
