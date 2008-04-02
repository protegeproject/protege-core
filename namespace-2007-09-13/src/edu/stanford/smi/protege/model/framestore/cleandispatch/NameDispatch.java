package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

class NameDispatch implements Dispatch {
    public List getDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot) {
        String name = delegate.getFrameName(frame);
        return CollectionUtilities.createList(name);
    }

    public void setDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot, Collection values) {
      throw new UnsupportedOperationException("Not allowed to change  name slots");
    }
}