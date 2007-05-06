package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for altering the frame name.  This differs from a simple text field in that it must check that the new
 * frame name is unique and valid.  We currently have no validity checks though.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceNameWidget extends TextFieldWidget {

    public void addNotify() {
        super.addNotify();
        if (isRuntime() && needsNameChange()) {
            selectAll();
        }
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.NAME);
    }

    private boolean needsNameChange() {
        boolean needsNameChange = false;
        String name = getInstance().getName();
        if (isEditable() && name != null) {
            int index = name.lastIndexOf('_');
            String possibleIntegerString = name.substring(index + 1);
            try {
                Integer.parseInt(possibleIntegerString);
                needsNameChange = true;
            } catch (Exception e) {
            }
        }
        return needsNameChange;
    }

    public void setAssociatedCls(Cls associatedCls) {
        super.setAssociatedCls(associatedCls);
        if (associatedCls != null) {
            setEditable(false);
        }
    }

    public void setEditable(boolean b) {
        super.setEditable(b && !isSlotAtCls());
    }

    public void setInstance(Instance instance) {
        super.setInstance(instance);
        if (needsNameChange()) {
            selectAll();
        }
    }

    public void setInstanceValues() {
        String name = getText();
        if (name != null && isValidName(name)) {
            getInstance().setName(name);
            markDirty(false);

        } else {
            ModalDialog.showMessageDialog(this, "Invalid frame name: Unable to change name.");
            setText(getInstance().getName());
            getTextField().requestFocus();
        }
    }

    protected String getInvalidTextDescription(String text) {
        String invalidText = null;
        if (text == null || !isValidName(text)) {
            invalidText = "Invalid frame name";

        }
        return invalidText;
    }

    private boolean isValidName(String name) {
        Frame currentFrame = getInstance();
        Frame frame = getKnowledgeBase().getFrame(name);
        boolean isDuplicate = (frame != null) && !frame.equals(currentFrame);
        boolean isValid = getKnowledgeBase().isValidFrameName(name, currentFrame);
        return isValid && !isDuplicate && name.length() > 0;
    }

    public void setWidgetValues() {
        if (isSlotAtCls()) {
            setText(getInstance().getName());
        } else {
            super.setWidgetValues();
        }
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Name", ResourceKey.NAME_SLOT_WIDGET_LABEL);
    }
}
