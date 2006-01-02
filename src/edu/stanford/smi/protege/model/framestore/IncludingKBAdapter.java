package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;

/**
 * 
 * 
 * @author tredmond
 *
 */
public abstract class IncludingKBAdapter 
  implements NarrowFrameStore, IncludingKBSupport {

  private InherittedFrameLookup iframes;
  
  private FrameFactory frameFactory;
  
  private NarrowFrameStore delegate;
  
  private int memoryProjectId;
  
  private Set<Frame> frameSetCache;

  public IncludingKBAdapter(FrameFactory frameFactory, 
                            NarrowFrameStore delegate) {
    this.frameFactory = frameFactory;
    this.delegate = delegate;
    FrameID fid = delegate.generateFrameID();
    memoryProjectId = fid.getMemoryProjectPart();
  }

  
  public abstract boolean isLocalFrameInherited(Frame frame);
  
  public boolean isGlobalFrameInherited(Frame frame) {
    return frame.getFrameID().isUser() &&
           frame.getFrameID().getMemoryProjectPart() != memoryProjectId;
  }
  
  public NarrowFrameStore getDelegate() {
    return delegate;
  }
  
  public InherittedFrameLookup getInheritedFrames() {
    return iframes;
  }
  
  public void setInheritedFrames(InherittedFrameLookup iframes) {
    this.iframes = iframes;
  }
  
  public Frame mapLocalFrame(Frame frame) {
    if (frame == null  || frame.getFrameID().isSystem()|| !isLocalFrameInherited(frame)) {
      return frame;
    }
    String name = null;
    List values = delegate.getValues(frame, iframes.getNameSlot(), null, false);
    for (Object value : values) {
      if (value instanceof String) {
        name = (String) value;
        break;
      }
    }
    if (name == null) {
      return null;
    } else {
      return iframes.getInheritedFrame(name);
    }
  }
  
  public Frame mapGlobalFrame(Frame frame, boolean create) {
    if (frame == null || frame.getFrameID().isSystem() || !isGlobalFrameInherited(frame)) {
      return frame;
    }
    String name = iframes.getInherittedFrameName(frame);
    for (Frame localFrame : delegate.getMatchingFrames(iframes.getNameSlot(), 
                                                       (Facet) null, 
                                                       false, name, 1)) {
      return localFrame;
    }
    if (create) {
      return createLocalFrame(frame, name);
    } else {
      return null;
    }
  }
  
  protected Frame createLocalFrame(Frame global, String name) {
    int javaClassId = 0;
    if (global instanceof Cls) {
      javaClassId = DefaultFrameFactory.DEFAULT_CLS_JAVA_CLASS_ID;
    } else if (global instanceof Slot) {
      javaClassId = DefaultFrameFactory.DEFAULT_SLOT_JAVA_CLASS_ID;
    } else if (global instanceof Facet) {
      javaClassId = DefaultFrameFactory.DEFAULT_FACET_JAVA_CLASS_ID;
    } else {
      javaClassId = DefaultFrameFactory.DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID;
    }
    Frame localFrame = 
      frameFactory.createFrameFromClassId(javaClassId, 
                                          delegate.generateFrameID());
    delegate.addValues(localFrame, 
                       iframes.getNameSlot(), 
                       (Facet) null, 
                       false, 
                       Collections.singleton(name));
    return localFrame;
  }

  public Slot mapGlobalSlot(Slot slot, boolean create) {
    Frame localFrame = mapGlobalFrame(slot,create);
    if (localFrame == null) {
      return null;
    } else if (localFrame instanceof Slot) {
      return (Slot) localFrame;
    } else {
      throw new RuntimeException("Inconsistent ontologies - inherited frame " + 
                                 slot  + " is also know as a non-slot frame " +
                                 localFrame + " by narrow frame store " + this);
    }
  }
    
  public Facet mapGlobalFacet(Facet slot, boolean create) {
    Frame localFrame = mapGlobalFrame(slot,create);
    if (localFrame == null) {
      return null;
    } else if (localFrame instanceof Facet) {
      return (Facet) localFrame;
    } else {
      throw new RuntimeException("Inconsistent ontologies - inherited frame " + 
                                 slot  + " is also know as a non-slot frame " +
                                 localFrame + " by narrow frame store " + this);
    }
  }
  
  public Slot mapLocalSlot(Slot slot) {
    Frame globalFrame = mapLocalFrame(slot);
    if (globalFrame == null) {
      return null;
    } else if (globalFrame instanceof Slot) {
      return (Slot) globalFrame;
    } else {
      throw new RuntimeException("Inconsistent ontologies - inherited frame " + 
                                 globalFrame  + " is also know as a non-slot frame " +
                                 slot + " by narrow frame store " + this);
    }
  }
  
  public Facet mapLocalFacet(Facet slot) {
    Frame globalFrame = mapLocalFrame(slot);
    if (globalFrame == null) {
      return null;
    } else if (globalFrame instanceof Facet) {
      return (Facet) globalFrame;
    } else {
      throw new RuntimeException("Inconsistent ontologies - inherited frame " + 
                                 globalFrame  + " is also know as a non-slot frame " +
                                 slot + " by narrow frame store " + this);
    }
  }
  
  public Object mapLocalObject(Object o) {
    if (o instanceof Frame) {
      return mapLocalFrame((Frame) o);
    }
    return o;
  }
  
  public Object mapGlobalObject(Object o, boolean create) {
    if (o instanceof Frame) {
      return mapGlobalFrame((Frame) o, create);
    }
    return o;
  }
  
  public List mapLocalList(List c) {
    if (!containsFrames(c)) {
      return c;
    }
    List alst = new ArrayList();
    for (Object o : c) {
      alst.add(mapLocalObject(o));
    }
    return alst;
  }
  
  public List mapGlobalList(List c, boolean create) {
    if (!containsFrames(c)) {
      return c;
    }
    List alst = new ArrayList();
    for (Object o : c) {
      Object localObject = mapGlobalObject(o, create);
      if (localObject != null) {
        alst.add(localObject);
      }
    }
    return alst;
  }
  
  public Collection mapLocalCollection(Collection c) {
    if (!containsFrames(c)) {
      return c;
    }
    List alst = new ArrayList();
    for (Object o : c) {
      alst.add(mapLocalObject(o));
    }
    return alst;
  }
  
  public Collection mapGlobalCollection(Collection c, boolean create) {
    if (!containsFrames(c)) {
      return c;
    }
    List alst = new ArrayList();
    for (Object o : c) {
      Object localObject = mapGlobalObject(o, create);
      if (localObject != null) {
        alst.add(localObject);
      }
    }
    return alst;
  }
  
  
  private boolean containsFrames(Collection s) {
    for (Object o : s) {
      if (o instanceof Frame) {
        return true;
      }
    }
    return false;
  }

  private Set<Frame> mapLocalFrameSet(Set<Frame> localFrames) {
    Set<Frame> globalFrames = new HashSet<Frame>();
    for (Frame localFrame : localFrames) {
      Frame globalFrame = mapLocalFrame(localFrame);
      globalFrames.add(globalFrame);
    }
    return globalFrames;
  }
  
  private Set<Reference> mapLocalReferences(Set<Reference> localRefs) {
    Set<Reference> globalRefs = new HashSet<Reference>();
    for (Reference localRef : localRefs) {
      globalRefs.add(new ReferenceImpl(mapLocalFrame(localRef.getFrame()),
                                       mapLocalSlot(localRef.getSlot()),
                                       mapLocalFacet(localRef.getFacet()),
                                       localRef.isTemplate()));
    }
    return globalRefs;
  }

  /*
   * ---------------------------------------------------------------------------
   *              Narrow Frame Store Interfaces.
   */
  
  public String getName() {
    return delegate.getName();
  }

  public void setName(String name) {
    delegate.setName(name);
  }



  public FrameID generateFrameID() {
    return delegate.generateFrameID();
  }

  public int getFrameCount() {
    return getFrames().size();
  }

  public int getClsCount() {
    int count = 0;
    for (Frame fr : getFrames()) {
      if (fr instanceof Cls) {
        count++;
      }
    }
    return count;
  }

  public int getSlotCount() {
    int count = 0;
    for (Frame fr : getFrames()) {
      if (fr instanceof Slot) {
        count++;
      }
    }
    return count;
  }

  public int getFacetCount() {
    int count = 0;
    for (Frame fr : getFrames()) {
      if (fr instanceof Facet) {
        count++;
      }
    }
    return count;
  }

  public int getSimpleInstanceCount() {
    int count = 0;
    for (Frame fr : getFrames()) {
      if (fr instanceof SimpleInstance) {
        count++;
      }
    }
    return count;
  }

  public Set<Frame> getFrames() {
    if (frameSetCache != null) {
      return frameSetCache;
    }
    frameSetCache = new HashSet<Frame>();
    for (Frame frame : delegate.getFrames()) {
      if (!isLocalFrameInherited(frame)) {
        frameSetCache.add(frame);
      }
    }
    return frameSetCache;
  }

  public Frame getFrame(FrameID id) {
    if (id.getMemoryProjectPart() != memoryProjectId) {
      return iframes.getInheritedFrame(id);
    } else {
      return delegate.getFrame(id);
    }
  }

  public List getValues(Frame frame, 
                        Slot slot, 
                        Facet facet, 
                        boolean isTemplate) {
    frame = mapGlobalFrame(frame, false);
    slot  = mapGlobalSlot(slot, false);
    facet = mapGlobalFacet(facet, false);
    if (frame == null || slot == null) {
      return new ArrayList();
    }
    List values = delegate.getValues(frame, slot, facet, isTemplate);
    return mapLocalList(values);
  }

  public int getValuesCount(Frame frame, Slot slot, Facet facet,
                            boolean isTemplate) {
    frame = mapGlobalFrame(frame, false);
    slot =  mapGlobalSlot(slot, false);
    facet = mapGlobalFacet(facet, false);
    if (frame == null || slot == null) {
      return 0;
    }
    return delegate.getValuesCount(frame, slot,facet, isTemplate);
  }

  public void addValues(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, Collection values) {
    frame = mapGlobalFrame(frame, true);
    slot  = mapGlobalSlot(slot, true);
    facet = mapGlobalFacet(facet, true);
    delegate.addValues(frame, slot, facet, isTemplate, values);
  }

  public void moveValue(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, int from, int to) {
    frame = mapGlobalFrame(frame, true);
    slot  = mapGlobalSlot(slot, true);
    facet = mapGlobalFacet(facet, true);
    delegate.moveValue(frame, slot, facet, isTemplate, from, to);
  }

  public void removeValue(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Object value) {
    frame = mapGlobalFrame(frame, true);
    slot  = mapGlobalSlot(slot, true);
    facet = mapGlobalFacet(facet, true);
    value = mapGlobalObject(value, true);
    delegate.removeValue(frame, slot, facet, isTemplate, value);
  }

  public void setValues(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, Collection values) {
    frame  = mapGlobalFrame(frame, true);
    slot   = mapGlobalSlot(slot, true);
    facet  = mapGlobalFacet(facet, true);
    values = mapGlobalCollection(values, true);
    delegate.setValues(frame, slot,facet, isTemplate, values);
  }

  public Set<Frame> getFrames(Slot slot, 
                              Facet facet, 
                              boolean isTemplate,
                              Object value) {
    slot   = mapGlobalSlot(slot, false);
    facet  = mapGlobalFacet(facet, false);
    value  = mapGlobalObject(value, false);
    if (slot == null || value == null) {
      return new HashSet<Frame>();
    }
    return delegate.getFrames(slot, facet, isTemplate, value);
  }

  public Set<Frame> getFramesWithAnyValue(Slot slot,  Facet facet, 
                                          boolean isTemplate) {
    slot   = mapGlobalSlot(slot, false);
    facet  = mapGlobalFacet(facet, false);
    if (slot == null) {
      return new HashSet<Frame>();
    }
    Set<Frame> localFrames
      = delegate.getFramesWithAnyValue(slot, facet, isTemplate);
    return mapLocalFrameSet(localFrames);
  }


  public Set<Frame> getMatchingFrames(Slot slot, Facet facet, 
                                      boolean isTemplate,
                                      String value, 
                                      int maxMatches) {
    slot   = mapGlobalSlot(slot, false);
    facet  = mapGlobalFacet(facet, false);
    if (slot == null) {
      return new HashSet<Frame>();
    }
    Set<Frame> localFrames = 
      delegate.getMatchingFrames(slot, facet,  isTemplate, 
                                 value, maxMatches);
    return mapLocalFrameSet(localFrames);
  }

  public Set<Reference> getReferences(Object value) {
    return delegate.getReferences(value);
  }

  public Set<Reference> getMatchingReferences(String value, int maxMatches) {
    return delegate.getMatchingReferences(value, maxMatches);
  }

  public Set executeQuery(Query query) {
    return delegate.executeQuery(query);
  }

  public void deleteFrame(Frame frame) {
    delegate.deleteFrame(frame);
  }

  public void close() {
    delegate.close();
  }

  public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    return delegate.getClosure(frame,slot, facet, isTemplate);
  }

  public void replaceFrame(Frame frame) {
    delegate.replaceFrame(frame);
  }

  public boolean beginTransaction(String name) {
    return delegate.beginTransaction(name);
  }

  public boolean commitTransaction() {
    return delegate.commitTransaction();
  }

  public boolean rollbackTransaction() {
    return delegate.rollbackTransaction();
  }


}
