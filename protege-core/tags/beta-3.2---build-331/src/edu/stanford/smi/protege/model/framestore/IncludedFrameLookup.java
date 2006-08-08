package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Slot;

public interface IncludedFrameLookup {
  
  Slot getNameSlot();
  
  boolean isEmpty();
  
  String getInherittedFrameName(Frame frame);
  
  Frame getInheritedFrame(String name);

  Frame getInheritedFrame(FrameID id);
}
