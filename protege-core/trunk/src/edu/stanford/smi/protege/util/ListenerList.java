package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Maintains a list of listeners to a particular source.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ListenerList extends ListenerCollection {
    private Collection _listeners; // <EventListener>
    private boolean _isPosting;
    private Object _source;

    public ListenerList(EventDispatcher dispatcher) {
        super(dispatcher);
    }

    public Collection getListeners(Object o) {
        return _listeners;
    }

    public Collection getSources() {
        return CollectionUtilities.createCollection(_source);
    }

    public boolean hasListeners(Object o) {
        return equals(o, _source) && _listeners != null && !_listeners.isEmpty();
    }

    protected boolean isPosting(Object o) {
        return _isPosting;
    }

    public void removeAllListeners(Object source) {
        _listeners = null;
    }

    protected void saveListeners(Object source, Collection c) {
        _source = source;
        _listeners = c;
    }

    protected void setFinishPosting(Object o) {
        _isPosting = false;
    }

    protected boolean setStartPosting(Object o) {
        boolean wasPosting = _isPosting;
        _isPosting = true;
        return wasPosting;
    }
}
