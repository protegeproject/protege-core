package edu.stanford.smi.protege.widget;

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.ModalDialog;

/**
 * Slot widget for altering the frame name.  This differs from a simple text field in that it must check that the new
 * frame name is unique and valid.  We currently have no validity checks though.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceNameWidget extends TextFieldWidget {
  private static final long serialVersionUID = -1873474195346387238L;

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
        if (getInstance() == null) { return false;}
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
      Collection values = getValues();
      if (values == null) {
        throw new IllegalArgumentException("Illegal null value for frame");
      }
      else if (values.isEmpty()) {
        throw new IllegalArgumentException("Missing name for class");
      }
      else if (values.size() > 1) {
        throw new IllegalArgumentException("Too many names for frame " + values.size());
      }
      else if (!(values.iterator().next() instanceof String)) {
        throw new IllegalArgumentException("name should be a string");
      }
      String name = (String) values.iterator().next();
      Instance i = getInstance();
      if (i.getName().equals(name)) {
        return;
      }
      i.rename(name);
      markDirty(false);
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
