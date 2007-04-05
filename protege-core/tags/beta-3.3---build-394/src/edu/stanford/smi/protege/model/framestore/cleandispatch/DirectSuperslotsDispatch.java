package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DirectSuperslotsDispatch extends AbstractRedirectingDispatch {
    public List getCurrentValues(FrameStore delegate, Frame frame) {
        return delegate.getDirectSuperslots((Slot) frame);
    }

    public void addValue(FrameStore delegate, Frame frame, Object value) {
        delegate.addDirectSuperslot((Slot) frame, (Slot) value);
    }

    public void removeValue(FrameStore delegate, Frame frame, Object value) {
        delegate.removeDirectSuperslot((Slot) frame, (Slot) value);
    }

    boolean isCorrectFrameType(Frame frame) {
        return frame instanceof Slot;
    }

    boolean isCorrectValueType(Object value) {
        return value instanceof Slot;
    }
}