package edu.stanford.smi.protege.util;


import java.util.*;

/**
 * Maintains a collection of listeners for each of a set of sources (that is, each source has its own set of listeners).
 * Posting to these listeners can be enabled or disabled all at once.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ListenerMap extends ListenerCollection {
    private Set _postingSources = new HashSet();
    private Map _listeners = new HashMap();  // <Object, Collection<EventListener>>
    
    public ListenerMap(EventDispatcher d) {
        super(d);
    }

    public Collection getListeners(Object o) {
        return (Collection) _listeners.get(o);
    }

    public Collection getSources() {
        return _listeners.keySet();
    }

    public boolean hasListeners(Object source) {
        Collection c = getListeners(source);
        return c != null && !c.isEmpty();
    }

    protected boolean isPosting(Object o) {
        return _postingSources.contains(o);
    }

    public void remove(Object source, EventListener listener) {
        super.remove(source, listener);
        Collection c = getListeners(source);
        if (c != null && c.isEmpty()) {
            _listeners.remove(source);
        }
    }

    public void removeAllListeners(Object source) {
        _listeners.remove(source);
    }

    protected void saveListeners(Object source, Collection c) {
        _listeners.put(source, c);
    }

    protected void setFinishPosting(Object o) {
        _postingSources.remove(o);
    }

    protected boolean setStartPosting(Object o) {
        return !_postingSources.add(o);
    }
}
