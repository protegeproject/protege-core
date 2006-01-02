package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Tree;

/**
 * This class provides the ability to lookup frames from subordinate knowledge 
 * bases.  The purpose of this class is to allow a narrow frame store to 
 * retrieve the frames of knowledge bases on which it depends.  Currently
 * instances of this class are constructed by the MergingNarrowFrameStore
 * (which is responsible for managing the heirarchy of knowledge bases) and is
 * used by Narrow Frame Stores which do not precalculate their frames at load
 * time.
 * 
 * @author tredmond
 */
public class InherittedFrameLookupImpl implements InherittedFrameLookup {
  
  Slot nameSlot;
  Collection<NarrowFrameStore> stores;
  Map<Integer, NarrowFrameStore> frameStoreMap;
  
  public InherittedFrameLookupImpl(Slot nameSlot,
                                   NarrowFrameStore nfs,
                                   Tree<NarrowFrameStore> heirarchy) {
    this.nameSlot = nameSlot;
    stores = heirarchy.getDescendents(nfs);
    fillFrameStoreMap();
  }
  
  private void fillFrameStoreMap() {
    frameStoreMap = new HashMap<Integer, NarrowFrameStore>();
    for (NarrowFrameStore nfs : stores) {
      FrameID fid = nfs.generateFrameID();
      int projectId = fid.getMemoryProjectPart();
      frameStoreMap.put(new  Integer(projectId), nfs);
    }
  }

  public Slot getNameSlot() {
    return nameSlot;
  }
  
  public String getInherittedFrameName(Frame frame) {
    if (frame == null) {
      return null;
    }
    int projectId = frame.getFrameID().getMemoryProjectPart();
    NarrowFrameStore store = frameStoreMap.get(new Integer(projectId));
    if (store == null) { 
      return null;
    }
    for (Object value : store.getValues(frame, getNameSlot(), null, false)) {
      if (value instanceof String) {
        return (String) value;
      }
    }
    return null;
  }

  public Frame getInheritedFrame(String name) {
    for (NarrowFrameStore nfs : stores) {
      Collection<Frame> frames = nfs.getFrames(nameSlot, null, false, name);
      for (Frame frame : frames) {
        return frame;
      }
    }
    return null;
  }

  public Frame getInheritedFrame(FrameID id) {
    int projectId = id.getMemoryProjectPart();
    NarrowFrameStore store = frameStoreMap.get(new Integer(projectId));
    if (store != null) {
      return store.getFrame(id);
    }
    return null;
  }
}
