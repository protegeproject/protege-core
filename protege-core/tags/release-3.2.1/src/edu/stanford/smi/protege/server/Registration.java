package edu.stanford.smi.protege.server;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class Registration {
    private int _lastEvent;

    public Registration(int lastEvent) {
        setLastEvent(lastEvent);
    }

    public void setLastEvent(int lastEvent) {
        _lastEvent = lastEvent;
    }

    public int getLastEvent() {
        return _lastEvent;
    }
}
