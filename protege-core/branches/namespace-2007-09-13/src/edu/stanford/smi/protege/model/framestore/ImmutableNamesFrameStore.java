package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultSlot;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;

public class ImmutableNamesFrameStore extends FrameStoreAdapter {
  private KnowledgeBase kb;
  private Slot nameSlot;
  
  public ImmutableNamesFrameStore(KnowledgeBase kb) {
    this.kb = kb;
    nameSlot = new DefaultSlot(kb,Model.SlotID.NAME); //using system frames here leads to circularity
  }

  private boolean isNameSlot(Slot slot) {
    return slot.equals(nameSlot);
  }
  
  public FrameID adjustFrameId(FrameID id) {
    if (id == null || id.getName() == null) {
      return new FrameID(generateUniqueName("Class"));
    } else {
      checkUniqueness(id.getName());
      return id;
    }
  }
  
  private void checkNotNameSlot(Slot slot) {
    if (isNameSlot(slot)) {
      throw new IllegalArgumentException("Should not be modifying name slot values");
    }
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
  
  
  private void insertName(Frame frame) {
    /*
    List<String> names = new ArrayList<String>();
    names.add(frame.getFrameID().getName());
    getDelegate().setDirectOwnSlotValues(frame, nameSlot, names);
    */
  }
  

  
  /*
   * ---------------------------------------------------------------------------
   * FrameStore calls.
   */
  
  public String getFrameName(Frame frame) {
    return frame.getFrameID().getName();
  }

  public Cls createCls(FrameID id, 
                       Collection directTypes, 
                       Collection directSuperclasses, 
                       boolean loadDefaultValues) {
    id = adjustFrameId(id);
    Cls cls = getDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues);
    insertName(cls);
    return cls;
  }

  public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots, boolean loadDefaultValues) {
    id = adjustFrameId(id);
    Slot slot = getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaultValues);
    insertName(slot);
    return slot;
  }

  public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
    id = adjustFrameId(id);
    Facet facet =  getDelegate().createFacet(id, directTypes, loadDefaultValues);
    insertName(facet);
    return facet;
  }

  public SimpleInstance createSimpleInstance(FrameID id,
                                             Collection directTypes,
                                             boolean loadDefaultValues) {
    id = adjustFrameId(id);
    SimpleInstance instance = getDelegate().createSimpleInstance(id, directTypes, loadDefaultValues);
    insertName(instance);
    return instance;
  }

  @SuppressWarnings("unchecked")
  public Collection getOwnSlotValues(Frame frame, Slot slot) {
    if (isNameSlot(slot)) {
      Collection results = new ArrayList();
      results.add(frame.getFrameID().getName());
      return results;
    }
    return getDelegate().getOwnSlotValues(frame, slot);
  }

  @SuppressWarnings("unchecked")
  public List getDirectOwnSlotValues(Frame frame, Slot slot) {
    if (isNameSlot(slot)) {
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
