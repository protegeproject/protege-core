package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;

public class ImmutableNamesFrameStore extends FrameStoreAdapter {

  public Frame getFrame(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public String getFrameName(Frame frame) {
    return frame.getFrameID().getName();
  }

  public Cls createCls(FrameID id, Collection directTypes, Collection directSuperclasses, boolean loadDefaultValues) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots, boolean loadDefaultValues) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public SimpleInstance createSimpleInstance(FrameID id,
                                             Collection directTypes,
                                             boolean loadDefaultValues) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void deleteCls(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void deleteSlot(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void deleteFacet(Facet facet) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void deleteSimpleInstance(SimpleInstance simpleInstance) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getOwnSlots(Frame frame) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Collection getOwnSlotValues(Frame frame, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectOwnSlotValues(Frame frame, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom,
                                     int indexTo) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getOwnFacets(Frame frame, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getTemplateSlots(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectTemplateSlots(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectDomain(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getDomain(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getOverriddenTemplateSlots(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void addDirectTemplateSlot(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void removeDirectTemplateSlot(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Collection getTemplateSlotValues(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getTemplateFacets(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet,
                                           Collection values) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectSuperclasses(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getSuperclasses(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectSubclasses(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getSubclasses(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void addDirectSuperclass(Cls cls, Cls superclass) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void removeDirectSuperclass(Cls cls, Cls superclass) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectSuperslots(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getSuperslots(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectSubslots(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getSubslots(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void addDirectSuperslot(Slot slot, Slot superslot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void removeDirectSuperslot(Slot slot, Slot superslot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectTypes(Instance instance) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getTypes(Instance instance) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List getDirectInstances(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getInstances(Cls cls) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void addDirectType(Instance instance, Cls type) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void removeDirectType(Instance instance, Cls type) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void moveDirectType(Instance instance, Cls type, int index) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public List<EventObject> getEvents() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set executeQuery(Query query) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getReferences(Object object) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getMatchingReferences(String string, int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClsesWithMatchingBrowserText(String string,
                                             Collection superclasses,
                                             int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getFramesWithAnyDirectOwnSlotValue(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value,
                                                     int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot,
                                                         String value,
                                                         int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet,
                                                  Object value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot,
                                                          Facet facet,
                                                          String value,
                                                          int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public boolean beginTransaction(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public boolean commitTransaction() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public boolean rollbackTransaction() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void close() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  protected String uniqueName(String name, String baseName) {
    String uniqueName;
    if (name == null) {
      uniqueName = generateUniqueName(_kb.getName() + "_" + baseName);
    } else {
      Frame frame = getFrame(name);
      if (frame != null) {
        throw new IllegalArgumentException(name + " not unique: " + frame);
      }
      uniqueName = name;
    }
    return uniqueName;
  }
  
  private int nextName;
  
  protected String generateUniqueName(String baseName) {
    String uniqueName = null;
    
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


}
