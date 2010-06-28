package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public class BrowserSlotPattern {
    private List elements; // a list of strings and slots

    public BrowserSlotPattern(Slot slot) {
        this.elements = new ArrayList();
        this.elements.add(slot);
    }

    public BrowserSlotPattern(List elements) {
        this.elements = new ArrayList(elements);
    }

    public boolean isSimple() {
        return elements.size() == 1 && getFirstSlot() != null;
    }

    public Slot getFirstSlot() {
        Slot slot = null;
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Slot) {
                slot = (Slot) o;
                break;
            }
        }
        return slot;
    }

    public String getSerialization() {
        Object previous = null;
        StringBuffer buffer = new StringBuffer();
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof String) {
                buffer.append("{" + toSafeText((String) o) + "}");
            }
            if (o instanceof Slot) {
                if (previous instanceof Slot) {
                    buffer.append("{}");
                }
                Slot slot = (Slot) o;
                buffer.append(toSafeText(slot.getName()));
            }
            previous = o;
        }
        return buffer.toString();
    }

    private static String toSafeText(String s) {
        return FileUtilities.urlEncode(s);
    }

    private static String fromSafeText(String s) {
        return FileUtilities.urlDecode(s);
    }

    public static BrowserSlotPattern createFromSerialization(KnowledgeBase kb, String s) {
        boolean inText = false;
        List elements = new ArrayList();
        if (s != null) {
            StringTokenizer tokenizer = new StringTokenizer(s, "{}", true);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.equals("{")) {
                    inText = true;
                } else if (token.equals("}")) {
                    inText = false;
                } else if (inText) {
                    String text = fromSafeText(token);
                    elements.add(text);
                } else {
                    String name = fromSafeText(token);
                    Slot slot = kb.getSlot(name);
                    if (slot != null) {
                        elements.add(slot);
                    }
                }
            }

        }
        return (elements.isEmpty()) ? null : new BrowserSlotPattern(elements);
    }

    public boolean contains(Slot slot) {
        return elements.contains(slot);
    }

    public List getElements() {
        return new ArrayList(elements);
    }

    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof BrowserSlotPattern) {
            BrowserSlotPattern rhs = (BrowserSlotPattern) o;
            equals = CollectionUtilities.equalsList(elements, rhs.elements);
        }
        return equals;
    }

    public int hashCode() {
        return elements.get(0).hashCode();
    }

    public List<Slot> getSlots() {
        List<Slot> slots = new ArrayList<Slot>();
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Slot) {
                slots.add((Slot) o);
            }
        }
        return slots;
    }
    
    public void replaceSlot(Slot oldSlot, Slot newSlot) {
        if (elements.contains(oldSlot)) {
            List newElements = new ArrayList();
            for (Object o : elements) {
                if (oldSlot.equals(o)) {
                    newElements.add(newSlot);
                }
                else {
                    newElements.add(o);
                }
            }
            elements = newElements;
        }
    }

    public String getBrowserText(Instance instance) {
        StringBuffer buffer = new StringBuffer();
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Slot) {
                buffer.append(getText((Slot) o, instance));
            } else {
                buffer.append(o);
            }
        }
        return (buffer.length() == 0) ? null : buffer.toString();
    }

    private static String getText(Slot slot, Instance instance) {
        String text;
        Collection values = instance.getDirectOwnSlotValues(slot);
        if (values.size() > 1) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{");
            boolean isFirst = true;
            Iterator i = values.iterator();
            while (i.hasNext()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buffer.append(", ");
                }
                Object o = i.next();
                buffer.append(getText(o, instance));
            }
            buffer.append("}");
            text = buffer.toString();
        } else {
            Object o = CollectionUtilities.getFirstItem(values);
            text = getText(o, instance);
        }
        return text;
    }

    private static String getText(Object o, Instance instance) {
        String text;
        if (o == null) {
            text = "";
        } else if (o instanceof Frame) {
            if (o.equals(instance)) {
                text = "<recursive call>";
            } else {
                text = ((Frame) o).getBrowserText();
            }
        } else {
            text = o.toString();
        }
        return text;
    }

    public String toString() {
        return "BrowserSlotPattern(" + getSerialization() + ")";
    }
}
