package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;

public class ChangeMonitorFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private boolean _changed;

    public Object handleInvoke(Method method, Object[] args) {
        Object o = invoke(method, args);
        if (isModification(method)) {
            _changed = true;
        }
        return o;
    }

    public boolean isChanged() {
        return _changed;
    }

    public void setChanged(boolean b) {
        _changed = b;
    }

    public void handleReinitialize() {
        _changed = false;
    }
}
