package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.util.*;

public abstract class AbstractFrameStore implements FrameStore {
    private FrameStore _delegate;

    public void close() {
        _delegate = null;
    }

    public FrameStore getDelegate() {
        return _delegate;
    }
    public void setDelegate(FrameStore delegate) {
        _delegate = delegate;
        onSetDelegate();
    }

    public void onSetDelegate() {
        // do nothing
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}