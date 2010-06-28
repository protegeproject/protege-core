package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DirectTemplateSlotsDispatch extends AbstractRedirectingDispatch {
    public List getCurrentValues(FrameStore delegate, Frame frame) {
        return delegate.getDirectTemplateSlots((Cls) frame);
    }

    public void addValue(FrameStore delegate, Frame frame, Object value) {
        delegate.addDirectTemplateSlot((Cls) frame, (Slot) value);
    }

    public void removeValue(FrameStore delegate, Frame frame, Object value) {
        delegate.removeDirectTemplateSlot((Cls) frame, (Slot) value);
    }

    boolean isCorrectFrameType(Frame frame) {
        return frame instanceof Cls;
    }

    boolean isCorrectValueType(Object value) {
        return value instanceof Slot;
    }
}