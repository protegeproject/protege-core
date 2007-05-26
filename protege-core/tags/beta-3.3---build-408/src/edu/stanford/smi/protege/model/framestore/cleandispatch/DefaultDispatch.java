package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DefaultDispatch implements Dispatch {
    public List getDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot) {
        return delegate.getDirectOwnSlotValues(frame, slot);
    }
    public void setDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot, Collection values) {
        delegate.setDirectOwnSlotValues(frame, slot, values);
    }
}