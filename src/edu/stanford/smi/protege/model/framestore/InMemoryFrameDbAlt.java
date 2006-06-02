package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SimpleStringMatcher;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/**
 * This is a version of the InMemoryFrameDb that helps support the OWL problem that frames
 * may change their type in mid-parse.
 * 
 * The idea of this class is that all Frames kept in this class are simply viewed as a cache.  
 * A call to flush() will efficiently invalidate all frames held in storage and cause them to be recalculated as 
 * needed.  In addition, the type of a single frame can be updated and the cache for just that frame will be 
 * invalidated.
 * 
 * @author tredmond
 *
 */
public class InMemoryFrameDbAlt implements NarrowFrameStore {
  private static Logger log = Log.getLogger(InMemoryFrameDbAlt.class);
  
  private String name;
  
  private int flushCount = 0;
  
  private FrameFactory ff;
  private int projectId = FrameID.allocateMemoryProjectPart();
  private int counter = FrameID.INITIAL_USER_FRAME_ID;
  
  private Map<FrameID, FrameValue> frames = new HashMap<FrameID, FrameValue>();
  
  private Map<FrameSlotRequest, List<Value>> valueMap 
              = new HashMap<FrameSlotRequest, List<Value>>();
  
  public InMemoryFrameDbAlt(String name) {
    this.name = name;
  }
  
  /**
   * This call sets the frame factory for this Narrow Frame Store so that it can create
   * its own frames.
   * 
   * The usual process for creating this FrameStore for a KnowledgeBaseFactory2, is to first call 
   * the constructor via createNarrowFrameStore and then initialize the frame factory during loadKnowledgeBase
   * or includeKnowledgeBase.  A KnowledgeBaseFactory that is not a KnowledgeBaseFactory2 will have more
   * trouble with this process because it will not have access to the name during the includeKnowledgeBase or 
   * loadKnowledgeBase.
   * 
   * @param ff the frame factory that this frame store uses to create frames.
   */
  
  public void setFrameFactory(FrameFactory ff) {
    this.ff = ff;
  }
  
  /**
   * Flush all the cached frames in one quick operation.
   *
   */
  public void flush() {
    flushCount++;
  }

  /**
   * Set the javaType of the frame with frame id fid to javaType.
   * 
   * This will cause the existing cached frame of id fid (if any) to be flushed.
   * 
   * @param fid the frame id of the frame
   * @param javaType the new java type of the frame
   */
  public void setJavaType(FrameID fid, int javaType) {
    FrameValue fv = getFrameValue(fid, true);
    fv.setJavaType(javaType);
  }

  private FrameValue getFrameValue(Frame f) {
    FrameValue fv = frames.get(f.getFrameID());
    if (fv == null) {
      fv = new FrameValue(f);
      frames.put(f.getFrameID(), fv);
      return fv;
    } else {
      fv.setFrame(f);
      return fv;
    }
  }

  private FrameValue getFrameValue(FrameID fid, boolean create) {
    FrameValue fv = frames.get(fid);
    if (create && fv == null) {
      fv = new FrameValue(fid);
      frames.put(fid, fv);
    }
    return fv;
  }
  
  private List<Value> getValues(Frame frame, 
                                Slot slot, 
                                Facet facet, 
                                boolean isTemplate, 
                                boolean create) {
    return getValues(frame.getFrameID(), 
                     slot.getFrameID(), 
                     facet != null ? facet.getFrameID() : null, 
                     isTemplate, create);
  }
  
  private List<Value> getValues(FrameID frameId, 
                                FrameID slotId, 
                                FrameID facetId,
                                boolean isTemplate, 
                                boolean create) {
    FrameSlotRequest request = new FrameSlotRequest(frameId, slotId, facetId, isTemplate);
    List<Value> values = valueMap.get(request);
    if (create && values == null) {
      values = new ArrayList<Value>();
      valueMap.put(request, values);
    }
    return values;
  }
  
  private void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, List<Value> values) {
    setValues(frame.getFrameID(), 
              slot.getFrameID(), 
              facet != null ? facet.getFrameID() : null, 
              isTemplate, values);
  }
  
  private void setValues(FrameID frameId,
                         FrameID slotId,
                         FrameID facetId,
                         boolean isTemplate,
                         List<Value> values) {
    if (values == null) {
      values = new ArrayList<Value>();
    }
    FrameSlotRequest request = new FrameSlotRequest(frameId, slotId, facetId, isTemplate);
    valueMap.put(request, values);
  }
  
  private List<Value> buildValues(Collection values) {
    List<Value> result = new ArrayList<Value>();
    for (Object v : values) {
      if (v instanceof Frame) {
        result.add(getFrameValue((Frame) v));
      } else  {
        result.add(new Value(v));
      }
    }
    return result;
  }
  
  /**
   * A request for a (template) slot/facet value.
   * 
   * @author tredmond
   *
   */
  private class FrameSlotRequest {
    private FrameID frameId;
    private FrameID slotId;
    private FrameID facetId;
    private boolean isTemplate;

    public FrameSlotRequest(FrameID frameId, FrameID slotId, FrameID facetId, boolean isTemplate) {
      this.frameId = frameId;
      this.slotId = slotId;
      this.facetId = facetId;
      this.isTemplate = isTemplate;
    }

    public FrameID getFrameId() {
      return frameId;
    }

    public FrameID getSlotId() {
      return slotId;
    }
    
    public FrameID getFacetId() {
      return facetId;
    }
    
    public boolean isTemplate() {
      return isTemplate;
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof FrameSlotRequest)) {
        return false;
      }
      FrameSlotRequest other = (FrameSlotRequest) o;
      boolean equalFrame = false;
      if (frameId == null) {
        equalFrame = (other.frameId == null);
      } else {
        equalFrame = frameId.equals(other.frameId);
      }
      boolean equalSlot = false;
      if (slotId == null) {
        equalSlot = (other.slotId == null);
      } else {
        equalSlot = (slotId.equals(other.slotId));
      }
      boolean equalFacet = false;
      if (facetId == null) {
        equalFacet = (other.facetId == null);
      } else {
        equalFacet = (facetId.equals(other.facetId));
      }
      return equalFrame && equalSlot && equalFacet && (isTemplate == other.isTemplate);
    }
    
    public int hashCode() {
      int hashCode = 42;
      if (frameId != null) {
        hashCode += frameId.hashCode();
      }
      if (slotId != null) {
        hashCode *= slotId.hashCode();
      }
      if (facetId != null) {
        hashCode += 84 * facetId.hashCode();
      }
      hashCode += (isTemplate ? 22 : 5);
      return hashCode;
    }
    
    public String toString() {
      return "<Req: " + frameId + ", " + slotId + ", " + facetId + ", " + isTemplate  + ">";
    }
  };
  
  /**
   * A container of a value.
   * 
   * If the value being held is a Frame then one should use the FrameValue class
   * which extends this class.
   * 
   * @author tredmond
   *
   */
  private class Value {
    Object o;
    
    public Value(Object o) {
      this.o = o;
    }
    
    public Object getObject() {
      return o;
    }
    
    public boolean equals(Object x) {
      if (x == null || !(x.getClass().equals(getClass()))) {
        return false;
      }
      return getObject().equals(((Value) x).getObject());
    }
    
    public int hashCode() {
      return o.hashCode();
    }
    
    public String toString() {
      return o.toString();
    }
  }
  
  /**
   * A container for a frame.
   * 
   * The actual frame being contained is cached and is generated as needed.
   * 
   * @author tredmond
   *
   */
  private class FrameValue extends Value {
    int javaType = DefaultFrameFactory.DEFAULT_SLOT_JAVA_CLASS_ID;
    int localFlushCount;
    Frame frame = null;
    
    public FrameValue(FrameID frameId) {
      super(frameId);
    }
    
    public FrameValue(Frame f) {
      super(f.getFrameID());
      localFlushCount = flushCount;
      frame = f;
      javaType = determineJavaType(f);
    }
    
    FrameID getFrameID() {
      return (FrameID) super.getObject();
    }
     
    private int determineJavaType(Frame f) {
      if (f instanceof Cls) {
        return DefaultFrameFactory.DEFAULT_CLS_JAVA_CLASS_ID;
      } else if (f instanceof Slot) {
        return DefaultFrameFactory.DEFAULT_SLOT_JAVA_CLASS_ID;
      } else if (f instanceof Facet) {
        return DefaultFrameFactory.DEFAULT_FACET_JAVA_CLASS_ID;
      } else if (f instanceof SimpleInstance) {
        return DefaultFrameFactory.DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID;
      }
      throw new RuntimeException("Shouldn't be here...");
    }

    public int getJavaType() {
      return javaType;
    }

    public void setJavaType(int javaType) {
      this.javaType = javaType;
      frame = null;
    }
    
    public boolean cached() {
      return frame != null && localFlushCount == flushCount;
    }
    
    public Frame updateCache() {
      localFlushCount = flushCount;
      frame = ff.createFrameFromClassId(javaType, getFrameID());
      return frame;
    }
    
    public void setFrame(Frame f) {
      if (frame == f) {
        return;
      }
      javaType = determineJavaType(f);
      frame = f;
      localFlushCount = flushCount;
    }
    
    public Frame getObject() {
      if (cached()) {
        return frame;
      }
      return updateCache();
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof FrameValue)) {
        return false;
      }
      FrameValue fv = (FrameValue) o;
      return javaType == fv.javaType && getFrameID().equals(fv.getFrameID());
    }
    
    public int hashCode() {
      return o.hashCode() + 13 * javaType;
    }
    
    public String toString() {
      if (frame != null) {
        return frame.toString();
      }
      return getFrameID().toString();
    }
  }

  
  /*
   * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
   * Standard NarrowFrameStore Interfaces.
   */
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public NarrowFrameStore getDelegate() {
    return null;
  }

  public FrameID generateFrameID() {
    return FrameID.createLocal(projectId, counter++);
  }

  public int getFrameCount() {
    return frames.size();
  }

  public int getClsCount() {
    return getCountByType(DefaultFrameFactory.DEFAULT_CLS_JAVA_CLASS_ID);
  }


  public int getSlotCount() {
    return getCountByType(DefaultFrameFactory.DEFAULT_SLOT_JAVA_CLASS_ID);
  }

  public int getFacetCount() {
    return getCountByType(DefaultFrameFactory.DEFAULT_FACET_JAVA_CLASS_ID);
  }
 
  public int getSimpleInstanceCount() {
    return getCountByType(DefaultFrameFactory.DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID);
  }
  
  public int getCountByType(int javaType) {
    int count = 0;
    for (FrameValue fv : frames.values()) {
      if (fv.getJavaType() == javaType) {
        count++;
      }
    }
    return count;
  }

  public Set<Frame> getFrames() {
    Set<Frame> results = new HashSet<Frame>();
    for (FrameValue fv : frames.values()) {
      results.add(fv.getObject());
    }
    return results;
  }

  public Frame getFrame(FrameID id) {
    FrameValue fv = getFrameValue(id, false);
    if (fv != null) {
      return fv.getObject();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Entering getValues with frame id = " + frame.getFrameID() + 
           " slot id = " + slot.getFrameID() + 
           " facet id = " + (facet != null?facet.getFrameID():(FrameID) null) +
           " isTemplate = " + isTemplate);
    }
    List results = new ArrayList();
    List<Value> values = getValues(frame, slot, facet, isTemplate, false);
    if (values == null) {
      return results;
    }
    for (Value v : values) {
      results.add(v.getObject());
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Results = [");
      for (Object res : results) {
        if (res instanceof Frame) {
          log.fine(((Frame) res).getFrameID().toString() + "/ Frame");
        } else {
          log.fine(res.toString() + "/" + res.getClass());
        }
        log.fine(", ");
      }
      log.fine("]");
    }
    return results;
  }

  public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    List<Value> values = getValues(frame, slot, facet, isTemplate, false);
    if (values == null) {
      return 0;
    } else {
      return values.size();
    }
  }

  public void addValues(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, Collection values) {
    List<Value> oldValues = getValues(frame, slot, facet, isTemplate,true);
    oldValues.addAll(buildValues(values));
    setValues(frame, slot, facet, isTemplate, oldValues);
  }

  public void moveValue(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, int from, int to) {
    if (from == to || from < 0 || to < 0) {
      return;
    }
    List<Value> oldValues = getValues(frame, slot,facet, isTemplate,true);
    if (from >= oldValues.size() || to >= oldValues.size()) {
      return;
    }
    Value v = oldValues.remove(from);
    oldValues.add(to, v);
    setValues(frame, slot, facet, isTemplate, oldValues);
  }

  public void removeValue(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Object value) {
    int count = 0;
    List<Integer> counts = new ArrayList<Integer>();
    List<Value> values = getValues(frame, slot, facet, isTemplate, false);
    if (values == null) {
      return;
    }
    for (Value v : values) {
      if (v.getObject().equals(value)) {
        counts.add(count);
      }
      count++;
    }
    Collections.reverse(counts);
    for (int pt : counts) {
      values.remove(pt);
    }
    setValues(frame, slot, facet, isTemplate, values);
  }

  public void setValues(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, Collection values) {
    getFrameValue(frame);
    setValues(frame, slot, facet, isTemplate, buildValues(values));
  }

  /*
   * Not too efficient...
   */
  public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
                              Object value) {
    Set<Frame> frameSet = new HashSet<Frame>();
    FrameID slotId = slot.getFrameID();
    FrameID facetId = null;
    if (facet != null) {
      facetId = facet.getFrameID();
    }
    for (FrameID frameId : frames.keySet()) {
      boolean found = false;
      List<Value> values = getValues(frameId, slotId, facetId, isTemplate, false);
      if (values == null) {
        continue;
      }
      for (Value v : values) {
        if (v.getObject().equals(value)) {
          found = true;
          break;
        }
      }
      if (found) {
        frameSet.add(frames.get(frameId).getObject());
      }
    }
    return frameSet;
  }

  public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet,
                                          boolean isTemplate) {
    Set<Frame> frameSet = new HashSet<Frame>();
    FrameID slotId = slot.getFrameID();
    FrameID facetId = null;
    if (facet != null) {
      facetId = facet.getFrameID();
    }
    for (FrameID frameId : frames.keySet()) {
      List<Value> values = getValues(frameId, slotId, facetId, isTemplate, false);
      if (values != null && !values.isEmpty()) {
        frameSet.add(frames.get(frameId).getObject());
      }
    }
    return frameSet;
  }

  public Set<Frame> getMatchingFrames(Slot slot, Facet facet,
                                      boolean isTemplate, String regexp,
                                      int maxMatches) {
    Set<Frame> frameSet = new HashSet<Frame>();
    FrameID slotId = slot.getFrameID();
    FrameID facetId = null;
    if (facet != null) {
      facetId = facet.getFrameID();
    }
    SimpleStringMatcher matcher = new SimpleStringMatcher(regexp);
    for (FrameID frameId : frames.keySet()) {
      boolean found = false;
      List<Value> values = getValues(frameId, slotId, facetId, isTemplate, false);
      if (values == null) {
        continue;
      }
      for (Value v : values) {
        if (v.getObject() instanceof String) {
          String stringValue = (String) v.getObject();
          if (matcher.isMatch(stringValue)) {
            found = true;
            break;
          }
        }
      }
      if (found) {
        frameSet.add(frames.get(frameId).getObject());
      }
    }
    return frameSet;
  }

  public Set<Reference> getReferences(Object value) {
    Set<Reference> references = new HashSet<Reference>();
    
    for (FrameSlotRequest req : valueMap.keySet()) {
      if (valueMap.get(req).contains(value)) {
        Reference r = new ReferenceImpl(getFrame(req.getFrameId()),
                                        (Slot) getFrame(req.getSlotId()),
                                        (Facet) getFrame(req.getFacetId()),
                                        req.isTemplate());
        references.add(r);
      }
    }
    return references;
  }

  public Set<Reference> getMatchingReferences(String regexp, int maxMatches) {
    Set<Reference> references = new HashSet<Reference>();
    SimpleStringMatcher matcher = new SimpleStringMatcher(regexp);
    
    for (FrameSlotRequest req : valueMap.keySet()) {
      List<Value> values = valueMap.get(req);
      boolean found = false;
      for (Value v : values) {
        if (v.getObject() instanceof String) {
          String valueString = (String) v.getObject();
          if (matcher.isMatch(valueString)) {
            found = true;
            break;
          }
        }
      }
      if (found) {
        Reference r = new ReferenceImpl(getFrame(req.getFrameId()),
                                        ff.createSlot(req.getSlotId(), Collections.EMPTY_LIST),
                                        req.getFacetId() != null ?
                                            ff.createFacet(req.getFacetId(), Collections.EMPTY_LIST) :
                                              null,
                                        req.isTemplate());
        references.add(r);
      }
    }
    return references;
  }

  public Set executeQuery(Query query) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void deleteFrame(Frame frame) {
    FrameValue fv = frames.get(frame.getFrameID());
    if (fv == null) {
      return;
    }
    frames.remove(frame.getFrameID());
    for (List<Value> values : valueMap.values()) {
      values.remove(fv);
    }
  }

  public void close() {
    frames = new HashMap<FrameID, FrameValue>();
    valueMap = new HashMap<FrameSlotRequest, List<Value>>();
  }

  public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    return ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
  }

  public void replaceFrame(Frame frame) {
    FrameValue fv = frames.get(frame.getFrameID());
    if (fv == null) {
      getFrameValue(frame);
    } else {
      fv.setFrame(frame);
    }
  }

  public boolean beginTransaction(String name) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public boolean commitTransaction() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public boolean rollbackTransaction() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
 
  public TransactionMonitor getTransactionStatusMonitor() {
    return null;
  }


}
