package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DirectTypesDispatch extends AbstractRedirectingDispatch {
    public List getCurrentValues(FrameStore delegate, Frame frame) {
        return delegate.getDirectTypes((Instance) frame);
    }

    public void addValue(FrameStore delegate, Frame frame, Object value) {
        delegate.addDirectType((Instance) frame, (Cls) value);
    }

    public void removeValue(FrameStore delegate, Frame frame, Object value) {
        delegate.removeDirectType((Instance) frame, (Cls) value);
    }

    boolean isCorrectFrameType(Frame frame) {
        return frame instanceof Instance;
    }

    boolean isCorrectValueType(Object value) {
        return value instanceof Cls;
    }
}