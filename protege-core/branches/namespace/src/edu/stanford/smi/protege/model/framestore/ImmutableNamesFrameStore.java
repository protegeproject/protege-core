package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;

public class ImmutableNamesFrameStore extends FrameStoreAdapter {
  private KnowledgeBase kb;
  
  public ImmutableNamesFrameStore(KnowledgeBase kb) {
    this.kb = kb;
  }

  public String getFrameName(Frame frame) {
    return frame.getFrameID().getName();
  }
  
  protected void checkUniqueness(String name) {
    if (getFrame(name) != null) {
      throw new IllegalArgumentException(name + " not unique");
    }
  }
  
  private int nextName;
  
  protected String generateUniqueName(String baseName) {
    String uniqueName = null;
    baseName = kb.getName() + "_" + baseName;
    
    while (uniqueName == null) {
      String s = baseName + nextName;
      if (getFrame(s) == null) {
        uniqueName = s;
        ++nextName;
      } else {
        nextName += 10000;
      }
    }
    return uniqueName;
  }

  private boolean isNameSlot(Slot slot) {
    return slot.getFrameID().equals(Model.SlotID.NAME);
  }
  
  private void checkNotNameSlot(Slot slot) {
    if (isNameSlot(slot)) {
      throw new IllegalArgumentException("Should not be modifying name slot values");
    }
  }
  
  /*
   * ---------------------------------------------------------------------------
   * FrameStore calls.
   */

  public Cls createCls(FrameID id, 
                       Collection directTypes, 
                       Collection directSuperclasses, 
                       boolean loadDefaultValues) {
    if (id == null || id.getName() == null) {
      id = new FrameID(generateUniqueName("Class"));
    } else {
      checkUniqueness(id.getName());
    }
    return getDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues);
  }

  public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots, boolean loadDefaultValues) {
    if (id == null || id.getName() == null) {
      id = new FrameID(generateUniqueName("Slot"));
    } else {
      checkUniqueness(id.getName());
    }
    return getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaultValues);
  }

  public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
    if (id == null || id.getName() == null) {
      id = new FrameID(generateUniqueName("Facet"));
    } else {
      checkUniqueness(id.getName());
    }
    return getDelegate().createFacet(id, directTypes, loadDefaultValues);
  }

  public SimpleInstance createSimpleInstance(FrameID id,
                                             Collection directTypes,
                                             boolean loadDefaultValues) {
    if (id == null || id.getName() == null) {
      id = new FrameID(generateUniqueName("Instance"));
    } else {
      checkUniqueness(id.getName());
    }
    return getDelegate().createSimpleInstance(id, directTypes, loadDefaultValues);
  }

  @SuppressWarnings("unchecked")
  public Collection getOwnSlotValues(Frame frame, Slot slot) {
    if (slot.getFrameID().equals(Model.SlotID.NAME)) {
      Collection results = new ArrayList();
      results.add(frame.getFrameID().getName());
      return results;
    }
    return getDelegate().getOwnSlotValues(frame, slot);
  }

  @SuppressWarnings("unchecked")
  public List getDirectOwnSlotValues(Frame frame, Slot slot) {
    if (slot.getFrameID().equals(Model.SlotID.NAME)) {
      List results = new ArrayList();
      results.add(frame.getFrameID().getName());
      return results;
    }
    return getDelegate().getDirectOwnSlotValues(frame, slot);
  }


  public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom,
                                     int indexTo) {
    checkNotNameSlot(slot);
    getDelegate().moveDirectOwnSlotValue(frame, slot, indexFrom, indexTo);
  }

  public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
    checkNotNameSlot(slot);
    getDelegate().setDirectOwnSlotValues(frame, slot, values);
  }
}
