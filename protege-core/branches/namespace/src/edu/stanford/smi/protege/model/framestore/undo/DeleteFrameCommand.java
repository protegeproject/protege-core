package edu.stanford.smi.protege.model.framestore.undo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;

public class DeleteFrameCommand extends SimpleCommand {
    private Frame frame;
    
    private Map<Slot, Collection> directSlotValues = new HashMap<Slot, Collection>();


    
    public DeleteFrameCommand(FrameStore delegate, Frame frame) {
        super(delegate);
        this.frame = frame;
    }
    
    public Frame getFrame() {
        return frame;
    }
    

    public Object doIt() {
        saveFrame();
        deleteFrame();
        frame.markDeleted(true);
        return null;
    }

    public void undoIt() {
        restoreFrame();
        frame.markDeleted(false);
    }
    
    @SuppressWarnings("unchecked")
    protected void saveFrame() {
        Set<Slot> slots = getDelegate().getOwnSlots(frame);
        for (Slot slot : slots) {
            Collection values = getDelegate().getDirectOwnSlotValues(frame, slot);
            if (values != null && !values.isEmpty()) {
                directSlotValues.put(slot, values);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void restoreFrame() {
        for (Entry<Slot, Collection> entry : directSlotValues.entrySet()) {
            getDelegate().setDirectOwnSlotValues(frame, entry.getKey(), entry.getValue());
        }
    }
    
    private void deleteFrame() {
        if (frame instanceof Cls) {
            getDelegate().deleteCls((Cls) frame);
        }
        else if (frame instanceof Slot) {
            getDelegate().deleteSlot((Slot) frame);
        }
        else if (frame instanceof Facet) {
            getDelegate().deleteFacet((Facet) frame);
        }
        else {
            getDelegate().deleteSimpleInstance((SimpleInstance) frame);
        }
    }
    


}
