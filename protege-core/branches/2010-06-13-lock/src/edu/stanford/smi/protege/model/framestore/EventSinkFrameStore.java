package edu.stanford.smi.protege.model.framestore;

import java.util.Collections;
import java.util.List;

import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.Log;

public class EventSinkFrameStore extends FrameStoreAdapter {
    
    @Override
    public List<AbstractEvent> getEvents() {
        super.getEvents();
        return Collections.emptyList();
    }
    
    @Override
    public void setDelegate(FrameStore delegate) {
        if (delegate == null && getDelegate() != null) {
            try {
                getEvents();
            }
            catch (Throwable t) {
                // probably a dispose?
                Log.emptyCatchBlock(t);
            }
        }
        super.setDelegate(delegate);
    }

}
