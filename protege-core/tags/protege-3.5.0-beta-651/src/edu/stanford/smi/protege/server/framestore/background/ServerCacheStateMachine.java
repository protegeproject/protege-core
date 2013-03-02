package edu.stanford.smi.protege.server.framestore.background;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;

public interface ServerCacheStateMachine {
    ServerCachedState getInitialState();
    
    ServerCachedState nextState(ServerCachedState state, Frame before, Slot slot, Frame after);

}
