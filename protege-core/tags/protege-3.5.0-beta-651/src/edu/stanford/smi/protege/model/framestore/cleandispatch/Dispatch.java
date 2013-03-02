package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;


interface Dispatch {
    List getDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot);
    void setDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot, Collection values);
}