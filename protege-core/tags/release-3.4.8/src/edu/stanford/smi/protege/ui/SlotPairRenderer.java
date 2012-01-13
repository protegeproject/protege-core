package edu.stanford.smi.protege.ui;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for displaying a slot at a class. Has icons to indicate overrides.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotPairRenderer extends DefaultRenderer implements Cloneable {
    private static final long serialVersionUID = -4381654272891062203L;
    private static SlotPairRenderer _prototypeInstance = new SlotPairRenderer();

    public static SlotPairRenderer createInstance() {
        SlotPairRenderer result;
        try {
            result = (SlotPairRenderer) _prototypeInstance.clone();
        } catch (CloneNotSupportedException e) {
            Log.getLogger().warning(e.toString());
            result = _prototypeInstance;
        }
        return result;
    }

    public void load(Object value) {
        FrameSlotCombination combination = (FrameSlotCombination) value;
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();
        String text = slot.getBrowserText();
        boolean isInherited = !cls.hasDirectTemplateSlot(slot);
        boolean isReadonly = !cls.isEditable() || !slot.isEditable();
        boolean isOverridden = cls.hasDirectlyOverriddenTemplateSlot(slot);
        boolean isHidden = !slot.isVisible();
        Icon icon = Icons.getSlotIcon(isInherited, isOverridden, isReadonly, isHidden);
        setMainText(text);
        setMainIcon(icon);
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }

    public String getToolTipText() {
        FrameSlotCombination combination = (FrameSlotCombination) getValue();
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();
        StringBuffer buffer = new StringBuffer();
        appendInheritanceInformation(cls, slot, buffer);
        appendFacetOverrideInformation(cls, slot, buffer);
        String text;
        if (buffer.length() == 0) {
            text = null;
        } else {
            text = "<html>" + buffer.toString() + "</html>";
        }
        return text;
    }

    private static void appendInheritanceInformation(Cls cls, Slot slot, StringBuffer buffer) {
        if (!cls.hasDirectTemplateSlot(slot)) {
            buffer.append("<b>");
            buffer.append(slot.getBrowserText());
            buffer.append("</b> is inherited from ");
            boolean first = true;
            Iterator i = cls.getSuperclasses().iterator();
            while (i.hasNext()) {
                Cls superClass = (Cls) i.next();
                if (superClass.hasDirectTemplateSlot(slot)) {
                    if (!first) {
                        buffer.append(" and ");
                    }
                    buffer.append("<b>");
                    buffer.append(superClass.getBrowserText());
                    buffer.append("</b>");
                    first = false;
                }
            }
        }
    }

    private static void appendFacetOverrideInformation(Cls cls, Slot slot, StringBuffer buffer) {
        Collection facets = cls.getDirectlyOverriddenTemplateFacets(slot);
        if (!facets.isEmpty()) {
            if (buffer.length() != 0) {
                buffer.append("<br>");
            }
            Iterator i = facets.iterator();
            while (i.hasNext()) {
                Facet facet = (Facet) i.next();
                buffer.append("<b>");
                buffer.append(facet.getBrowserText());
                buffer.append("</b> is directly overridden");
                buffer.append("</br>");
            }
        }
    }

    public static void setPrototypeInstance(SlotPairRenderer renderer) {
        _prototypeInstance = renderer;
    }
}