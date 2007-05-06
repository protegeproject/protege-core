package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.util.*;

public abstract class AbstractFrameStore implements FrameStore {
    private FrameStore delegate;
    private String name;

    public AbstractFrameStore(String name) {
        this.name = name;
    }
    
    public AbstractFrameStore() {
        this.name = getClass().getName();
    }
    
    public void close() {
        delegate = null;
    }
    
    public String getName() {
        return name;
    }

    public FrameStore getDelegate() {
        return delegate;
    }
    public void setDelegate(FrameStore delegate) {
        this.delegate = delegate;
        onSetDelegate();
    }

    public void onSetDelegate() {
        // do nothing
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}