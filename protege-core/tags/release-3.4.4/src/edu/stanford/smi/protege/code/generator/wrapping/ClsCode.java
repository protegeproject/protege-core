package edu.stanford.smi.protege.code.generator.wrapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;

/**
 * Code generator for a class.
 */
public class ClsCode {

    private Cls cls;


    public ClsCode(Cls cls) {
        this.cls = cls;
    }


    public String getJavaName() {
        return getValidJavaName(cls.getName());
    }


    /**
     * @return a List of RDFPropertyAtClassCodes
     * @see SlotAtClassCode
     */
    @SuppressWarnings("unchecked")
	public List<SlotAtClassCode> getSlotCodes(boolean transitive) {
        Set<Slot> properties = new HashSet<Slot>();
        List<SlotAtClassCode> codes = new ArrayList<SlotAtClassCode>();


        Set<Slot> relevantSlots = new HashSet<Slot>(cls.getTemplateSlots());

        for (Slot slot : relevantSlots) {
            if (slot.isSystem()) {
            	//skip system properties
            	continue;
            }
            properties.add(slot);
            SlotAtClassCode code = new SlotAtClassCode(cls, slot);
            codes.add(code);

            Collection<Slot> subSlots = slot.getSubslots();
            Iterator<Slot> sit = subSlots.iterator();
            while (sit.hasNext()) {
                Slot subSlot = sit.next();
                if (!properties.contains(subSlot)) {
                    codes.add(new SlotAtClassCode(cls, subSlot));
                    properties.add(subSlot);
                }
            }
        }
        Collections.sort(codes);
        return codes;
    }


    public static String getValidJavaName(String name) {
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                name = name.replace(c, '_');
            }
        }
        return name;
    }
}
