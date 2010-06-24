package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreAdapter;

public class CleanDispatchFrameStore extends FrameStoreAdapter {
    private Map _slotToDispatchMap = new HashMap();
    private Dispatch _defaultDispatch;

    public void setDelegate(FrameStore frameStore) {
        super.setDelegate(frameStore);
        _slotToDispatchMap.clear();
        if (frameStore != null) {
            loadDispatches();
        }
    }

    public void close() {
        _slotToDispatchMap = null;
        _defaultDispatch = null;
    }
    
    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        return getDispatch(slot).getDirectOwnSlotValues(getDelegate(), frame, slot);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        getDispatch(slot).setDirectOwnSlotValues(getDelegate(), frame, slot, values);
    }

    private void addDispatch(String slotName, Dispatch dispatch) {
        Slot slot = (Slot) getDelegate().getFrame(slotName);
        _slotToDispatchMap.put(slot, dispatch);
    }

    private void loadDispatches() {
        _defaultDispatch = new DefaultDispatch();
        addDispatch(Model.Slot.DIRECT_INSTANCES, new DirectInstancesDispatch());
        addDispatch(Model.Slot.DIRECT_TYPES, new DirectTypesDispatch());
        addDispatch(Model.Slot.DIRECT_SUBCLASSES, new DirectSubclassesDispatch());
        addDispatch(Model.Slot.DIRECT_SUPERCLASSES, new DirectSuperclassesDispatch());
        addDispatch(Model.Slot.DIRECT_SUBSLOTS, new DirectSubslotsDispatch());
        addDispatch(Model.Slot.DIRECT_SUPERSLOTS, new DirectSuperslotsDispatch());
        addDispatch(Model.Slot.DIRECT_TEMPLATE_SLOTS, new DirectTemplateSlotsDispatch());
        addDispatch(Model.Slot.DIRECT_DOMAIN, new DirectDomainDispatch());
    }

    private Dispatch getDispatch(Slot slot) {
        Dispatch dispatch = (Dispatch) _slotToDispatchMap.get(slot);
        if (dispatch == null) {
            dispatch = _defaultDispatch;
        }
        return dispatch;
    }
}
