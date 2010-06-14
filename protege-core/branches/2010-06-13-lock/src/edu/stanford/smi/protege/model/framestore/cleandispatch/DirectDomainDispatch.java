package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DirectDomainDispatch extends AbstractRedirectingDispatch {
    public List getCurrentValues(FrameStore delegate, Frame frame) {
        return delegate.getDirectDomain((Slot) frame);
    }

    public void addValue(FrameStore delegate, Frame frame, Object value) {
        delegate.addDirectTemplateSlot((Cls) value, (Slot) frame);
    }

    public void removeValue(FrameStore delegate, Frame frame, Object value) {
        delegate.removeDirectTemplateSlot((Cls) value, (Slot) frame);
    }

    boolean isCorrectFrameType(Frame frame) {
        return frame instanceof Slot;
    }

    boolean isCorrectValueType(Object value) {
        return value instanceof Cls;
    }
}