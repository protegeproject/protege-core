package edu.stanford.smi.protege.storage.xml;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
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

    // private Collection errors;

    public XMLStorer(KnowledgeBase kb, Writer writer, Collection errors) {
        this.kb = kb;
        this.writer = new PrintWriter(writer);
        initializeExcludeSlots();
        // this.errors = errors;
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

    private void unindent(int levels) {
        indentLevel -= levels;
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
        List clses = new ArrayList(kb.getClses());
        // Collections.sort(clses);
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            storeCls(cls);
        }
    }

    private void storeSlots() {
        List slots = new ArrayList(kb.getSlots());
        // Collections.sort(slots);
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            storeSlot(slot);
        }

    }

    private void storeFacets() {
        List facets = new ArrayList(kb.getFacets());
        // Collections.sort(facets);
        Iterator i = facets.iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            storeFacet(facet);
        }
    }

    private void storeSimpleInstances() {
        Set frames = new HashSet(kb.getFrames());
        frames.removeAll(kb.getClses());
        frames.removeAll(kb.getSlots());
        frames.removeAll(kb.getFacets());
        List simpleInstances = new ArrayList(frames);
        // Collections.sort(simpleInstances);
        Iterator i = simpleInstances.iterator();
        while (i.hasNext()) {
            SimpleInstance simpleInstance = (SimpleInstance) i.next();
            storeSimpleInstance(simpleInstance);
        }
    }

    private void printHeader() {
        println("<?xml version=\"1.0\" ?>");
    }

    private void printDtd() {
        println("<!DOCTYPE kb [");
        indent();
        println("<!ELEMENT kb (class | slot | facet | simple_instance)*>");
        println("<!ATTLIST kb xmlns CDATA #IMPLIED>");
        println();
        println("<!ELEMENT class (name, type*, superclass*, template_slot*, template_facet_value*, own_slot_value*)>");
        println("<!ATTLIST class id ID #REQUIRED>");
        println();
        println("<!ELEMENT slot (name, type*, superslot*, own_slot_value*)>");
        println("<!ATTLIST slot id ID #REQUIRED>");
        println();
        println("<!ELEMENT facet (name, type*, own_slot_value*)>");
        println("<!ATTLIST facet id ID #REQUIRED>");
        println();
        println("<!ELEMENT simple_instance (name, type*, own_slot_value*)>");
        println("<!ATTLIST simple_instance id ID #REQUIRED>");
        println();
        println("<!ELEMENT name (#PCDATA)>");
        println("<!ELEMENT type (#PCDATA)>");
        println("<!ELEMENT superclass (#PCDATA)>");
        println("<!ELEMENT template_slot (#PCDATA)>");
        println("<!ELEMENT superslot (#PCDATA)>");
        println("<!ELEMENT slot_reference (#PCDATA)>");
        println("<!ELEMENT facet_reference (#PCDATA)>");
        println();
        println("<!ELEMENT own_slot_value (slot_reference, (primitive_value | reference_value)*)>");
        println("<!ELEMENT template_facet_value (slot_reference, facet_reference, (primitive_value | reference_value)*)>");

        println("<!ELEMENT primitive_value (#PCDATA)>");
        println("<!ATTLIST primitive_value type CDATA #REQUIRED>");
        println("<!ELEMENT reference_value (#PCDATA)>");
        println("<!ATTLIST reference_value type CDATA #REQUIRED>");
        unindent();
        println("]>");
    }

    private void preamble() {
        printHeader();
        indent();
        printDtd();
        unindent();
        printStartTag("kb", "xmlns", "http://protge.stanford.edu/xml");

    }

    private void postscript() {
        printEndTag("kb");
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
        println("<" + element + ">");
    }

    private void printStartTag(String element, String attribute, String value) {
        println("<" + element + " " + attribute + "=\"" + value + "\">");
    }

    private void printEndTag(String element) {
        println("</" + element + ">");
    }

    private void beginFrame(Frame frame, String frameType) {
        printStartTag(frameType, "id", getId(frame));
        indent();
        printValue("name", frame.getName());
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
            printStartTag("own_slot_value");
            indent();
            printValue("slot_reference", slot.getName());
            printValues(frame.getDirectOwnSlotValues(slot));
            indent();
            printEndTag("own_slot_value");
            unindent(2);
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
            tag = "reference_value";
            attribute = frameAttribute(o);
            value = frameValue(o);
        } else {
            tag = "primitive_value";
            if (o instanceof String) {
                attribute = "string";
                value = (String) o;
            } else if (o instanceof Integer) {
                attribute = "integer";
                value = o.toString();
            } else if (o instanceof Float) {
                attribute = "float";
                value = o.toString();
            } else if (o instanceof Boolean) {
                attribute = "boolean";
                value = o.toString();
            } else {
                Log.getLogger().warning("Unexpected object: " + o);
                attribute = "string";
                value = o.toString();
            }
        }
        printValue(tag, value, "type", attribute);
    }

    private String frameValue(Object o) {
        return ((Frame) o).getName();
    }

    private String frameAttribute(Object o) {
        String attribute;
        if (o instanceof Cls) {
            attribute = "class";
        } else if (o instanceof Slot) {
            attribute = "slot";
        } else if (o instanceof Facet) {
            attribute = "facet";
        } else {
            attribute = "simple_instance";
        }
        return attribute;
    }

    private void endInstance(String frameType, Instance instance) {
        endFrame(frameType, instance);
    }

    private void beginInstance(String frameType, Instance instance) {
        beginFrame(instance, frameType);
        indent();
        printFrameValues("type", instance.getDirectTypes());
        unindent();
    }

    private void printFrameValues(String tag, Collection frames) {
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            printFrameReference(tag, frame);
        }
    }

    private void storeCls(Cls cls) {
        beginInstance("class", cls);
        indent();
        printFrameValues("superclass", cls.getDirectSuperclasses());
        printFrameValues("template_slot", cls.getDirectTemplateSlots());
        printTemplateFacetValues(cls);
        unindent();
        endInstance("class", cls);
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
        printStartTag("template_facet_value");
        indent();
        printFrameReference("slot_reference", slot);
        printFrameReference("facet_reference", facet);
        printValues(values);
        unindent();
        printEndTag("template_facet_value");
    }

    private void storeSlot(Slot slot) {
        beginInstance("slot", slot);
        printFrameValues("superslots", slot.getDirectSuperslots());
        endInstance("slot", slot);
    }

    private void storeFacet(Facet facet) {
        beginInstance("facet", facet);
        endInstance("facet", facet);
    }

    private void storeSimpleInstance(SimpleInstance simpleInstance) {
        beginInstance("simple_instance", simpleInstance);
        endInstance("simple_instance", simpleInstance);
    }

}
