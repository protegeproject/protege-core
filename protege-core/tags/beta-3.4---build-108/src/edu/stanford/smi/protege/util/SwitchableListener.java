package edu.stanford.smi.protege.util;

/**
 * A listener that can be temporarily switched off.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class SwitchableListener {
    private boolean _isEnabled = true;

    public void disable() {
        _isEnabled = false;
    }

    public void enable() {
        _isEnabled = true;
    }

    public boolean isEnabled() {
        return _isEnabled;
    }
}
