package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DirectInstancesDispatch extends AbstractRedirectingDispatch {
    public List getCurrentValues(FrameStore delegate, Frame frame) {
        return delegate.getDirectInstances((Cls) frame);
    }

    public void addValue(FrameStore delegate, Frame frame, Object value) {
        delegate.addDirectType((Instance) value, (Cls) frame);
    }

    public void removeValue(FrameStore delegate, Frame frame, Object value) {
        delegate.removeDirectType((Instance) value, (Cls) frame);
    }

    boolean isCorrectFrameType(Frame frame) {
        return frame instanceof Cls;
    }

    boolean isCorrectValueType(Object value) {
        return value instanceof Instance;
    }
}