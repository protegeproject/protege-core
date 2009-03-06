package edu.stanford.smi.protege.model.framestore.undo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;

public class DeleteSlotCommand extends DeleteFrameCommand {
    
    private Map<Frame, Collection> directOwnSlotValues = new HashMap<Frame, Collection>();
    private Map<Cls, Collection> directTemplateSlotValues = new HashMap<Cls, Collection>();

    public DeleteSlotCommand(FrameStore delegate, Slot slot) {
        super(delegate, slot);
    }

    public Slot getSlot() {
        return (Slot) getFrame();
    }
    
    @Override
    protected void saveFrame() {
        super.saveFrame();
        saveSlot();
    }
    
    @Override
    protected void restoreFrame() {
        super.restoreFrame();
        restoreSlot();
    }
    
    private void saveSlot() {
        Slot slot = getSlot();
        for (Frame frame : getDelegate().getFramesWithAnyDirectOwnSlotValue(slot)) {
            Collection values = getDelegate().getDirectOwnSlotValues(frame, slot);
            if (values != null && !values.isEmpty()) {
                directOwnSlotValues.put(frame, values);
            }
        }
        for (Cls cls : getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot)) {
            Collection values = getDelegate().getDirectTemplateSlotValues(cls, slot);
            if  (values != null && !values.isEmpty()) {
                directTemplateSlotValues.put(cls, values);
            }
        }
    }
    
    private  void restoreSlot() {
        for (Entry<Frame, Collection> entry : directOwnSlotValues.entrySet()) {
            getDelegate().setDirectOwnSlotValues(entry.getKey(), getSlot(), entry.getValue());
        }
        for (Entry<Cls, Collection> entry : directTemplateSlotValues.entrySet()) {
            getDelegate().setDirectTemplateSlotValues(entry.getKey(), getSlot(), entry.getValue());
        }
    }
}
