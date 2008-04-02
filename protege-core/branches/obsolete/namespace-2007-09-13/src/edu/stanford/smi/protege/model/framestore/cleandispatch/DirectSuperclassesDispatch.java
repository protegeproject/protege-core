package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DirectSuperclassesDispatch extends AbstractRedirectingDispatch {
    public List getCurrentValues(FrameStore delegate, Frame frame) {
        return delegate.getDirectSuperclasses((Cls) frame);
    }

    public void addValue(FrameStore delegate, Frame frame, Object value) {
        delegate.addDirectSuperclass((Cls) frame, (Cls) value);
    }

    public void removeValue(FrameStore delegate, Frame frame, Object value) {
        delegate.removeDirectSuperclass((Cls) frame, (Cls) value);
    }

    boolean isCorrectFrameType(Frame frame) {
        return frame instanceof Cls;
    }

    boolean isCorrectValueType(Object value) {
        return value instanceof Cls;
    }
}