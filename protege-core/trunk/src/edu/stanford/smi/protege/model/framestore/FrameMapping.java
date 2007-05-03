package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;

/**
 * This interface maintains a mapping between frames in two 
 * Narrow Frame Stores (a local vs a global Narrow Frame Store).
 */
public interface FrameMapping {
  Frame mapGlobalFrame(Frame frame, boolean create);
  Frame mapLocalFrame(Frame frame);

  Slot mapGlobalSlot(Slot slot, boolean create);
  Slot mapLocalSlot(Slot slot);

  Facet mapGlobalFacet(Facet slot, boolean create);
  Facet mapLocalFacet(Facet slot);

  Object mapLocalObject(Object o);
  Object mapGlobalObject(Object o, boolean create);
  
  List mapGlobalList(List c, boolean create);
  List mapLocalList(List c);

  Collection mapGlobalCollection(Collection c, boolean create);
  Collection mapLocalCollection(Collection c);

  Set<Frame> mapLocalFrameSet(Set<Frame> localFrames);
  
}