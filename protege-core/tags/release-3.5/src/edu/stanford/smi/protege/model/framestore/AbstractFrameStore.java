package edu.stanford.smi.protege.model.framestore;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

public abstract class AbstractFrameStore implements FrameStore {
    private static Logger log = Log.getLogger(AbstractFrameStore.class);
    private FrameStore delegate;
    private String name;

    protected AbstractFrameStore(String name) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Constructing abstract frame store name = " + name + " class = " + this.getClass());
            log.fine("FrameStore being constructed = " + this + "/" + this.hashCode());
        }
        this.name = name;
    }

    protected AbstractFrameStore() {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Constructing abstract frame store class = " + this.getClass());
            log.fine("FrameStore being constructed = " + this + "/" + this.hashCode());
        }
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
        if (log.isLoggable(Level.FINE)) {
            log.fine("Setting delegate for " + this + "/" + this.hashCode() 
                    + " delegate = " + delegate + "/" + (delegate == null ? -1 : delegate.hashCode()));
        }
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