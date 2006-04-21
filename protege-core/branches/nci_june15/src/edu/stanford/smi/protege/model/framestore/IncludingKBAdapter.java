package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.util.Log;

/**
 * This class represents a Narrow Frame Store that allows its delegate 
 * Narrow Frame Store to include knowledge bases.  It assumes that the 
 * delegate has no knowledge of the included knowledge bases.  Thus the
 * delegate Narrow Frame Store will necessarily have its own frames representing
 * frames that have been included from other knowledge bases.  These frames 
 * have a problem because they are guaranteed to be different (they have a
 * different FrameID) than the frames that the included knowledge bases are 
 * using to represent the same entity.  To solve this problem, this class maintains
 * a mapping from the included frames generated by the delegate knowledge base (called local
 * frames) to the frames that come from the included knowledge bases (called 
 * global frames).  The reason that the delegates version of the included frames are
 * called local frames is that they do not appear in the set of frames visible at the top 
 * level.  The frames that the user interface sees come from the included knowledge bases.
 * So this class must take all the results generated by its delegate and translate
 * all local frames into their global counterparts.  We currently have a version
 * of this working for the DatabaseFrameDb but it could be used with any Narrow 
 * Frame Store that needs to be augmented to handle included knowledge bases.
 * 
 * To implement this class the developer must implement the method isLocalFrameIncluded.
 * But it is also likely that the developer should also implement the functions
 * 
 *      getFrameCount, getClsCount, getSlotCount, getFacetCount
 * 
 * because the implmentation here could be very inefficient.
 *
 * @author tredmond
 *
 */
public abstract class IncludingKBAdapter 
  implements NarrowFrameStore, IncludingKBSupport, FrameMapping {
  private static Logger log = Log.getLogger(IncludingKBAdapter.class);

  private IncludedFrameLookup iframes;
  
  private FrameFactory frameFactory;
  
  private NarrowFrameStore delegate;
  
  protected int memoryProjectId;
  
  private Set<Frame> frameSetCache;
  
  public IncludingKBAdapter(NarrowFrameStore delegate) {
    this.delegate = delegate;
  }
  
  public void initialize(FrameFactory frameFactory) {
    this.frameFactory = frameFactory;
    FrameID fid = delegate.generateFrameID();
    memoryProjectId = fid.getMemoryProjectPart();
  }
  
  /**
   * This function is used during the initialization of a fresh knowledge
   * base to set the memoryProjecetId to the memory Project id of the frames
   * being inserted into the knowledgebase.  It should not be called if the 
   * knowledgebase currently has any frames of its own.
   * 
   * @param memoryProjectId
   */
  protected void setMemoryProjectId(int memoryProjectId) {
    this.memoryProjectId = memoryProjectId;
  }

  
  public abstract boolean isLocalFrameIncluded(Frame frame);
  
  public boolean isGlobalFrameInherited(Frame frame) {
    return frame.getFrameID().isUser() &&
           frame.getFrameID().getMemoryProjectPart() != memoryProjectId;
  }
  
  public NarrowFrameStore getDelegate() {
    return delegate;
  }
  
  public IncludedFrameLookup getIncludedFrames() {
    return iframes;
  }
  
  public void setIncludedFrames(IncludedFrameLookup iframes) {
    this.iframes = iframes;
  }
  
  protected FrameFactory getFrameFactory() {
    return frameFactory;
  }
  
  public boolean noIncludedFrames() {
    return (iframes == null || iframes.isEmpty());
  }
  
  public Frame mapLocalFrame(Frame frame) {
    if (frame == null  || noIncludedFrames()
                       || frame.getFrameID().isSystem() 
                       || !isLocalFrameIncluded(frame)) {
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
    if (log.isLoggable(Level.FINEST) && frame != null) {
      log.finest("(" + memoryProjectId + ") Mapping global frame with id = " + 
                 frame.getFrameID() + " create flag = " + create);
    }
    if (frame == null || noIncludedFrames() 
                      || frame.getFrameID().isSystem() 
                      || !isGlobalFrameInherited(frame)) {
      if (log.isLoggable(Level.FINEST)) {
        log.finest("no mapping needed");
      }
      return frame;
    }
    String name = iframes.getInherittedFrameName(frame);
    if (log.isLoggable(Level.FINEST)) {
      log.finest("global frame has name = " + name);
    }
    if (name != null) {
      for (Frame localFrame : delegate.getFrames(iframes.getNameSlot(), 
                                                  (Facet) null, 
                                                  false, name)) {
        if (log.isLoggable(Level.FINEST)) {
          log.finest("found local frame with appropriate name with id = " + localFrame.getFrameID());
        }
        return localFrame;
      }
    }
    if (create) {
      Frame localFrame = createLocalFrame(frame, name);
      if (log.isLoggable(Level.FINEST)) {
        log.finest("Created mapped local frame with id = " + localFrame.getFrameID());
      }
      return localFrame;
    }
    return null;
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
    if (name != null) {
      delegate.addValues(localFrame, 
                         iframes.getNameSlot(), 
                         (Facet) null, 
                         false, 
                         Collections.singleton(name));
    }
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
    if (noIncludedFrames()) {
      return c;
    }
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
    if (noIncludedFrames()) {
      return c;
    }
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

  public Set<Frame> mapLocalFrameSet(Set<Frame> localFrames) {
    Set<Frame> globalFrames = new HashSet<Frame>();
    for (Frame localFrame : localFrames) {
      Frame globalFrame = mapLocalFrame(localFrame);
      globalFrames.add(globalFrame);
    }
    return globalFrames;
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
      if (!isLocalFrameIncluded(frame)) {
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
    if (isGlobalFrameInherited(frame)
          && slot.getFrameID().equals(Model.SlotID.NAME)) {
      return new ArrayList();
    }
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
    if (isGlobalFrameInherited(frame)
        && slot.getFrameID().equals(Model.SlotID.NAME)) {
      return 0;
    }
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
    Set<Frame> frames = delegate.getFrames(slot, facet, isTemplate, value);
    frames = mapLocalFrameSet(frames);
    return frames;
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
