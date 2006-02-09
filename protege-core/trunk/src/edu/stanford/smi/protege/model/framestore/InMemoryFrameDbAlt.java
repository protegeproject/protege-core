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
import edu.stanford.smi.protege.util.SimpleStringMatcher;

public class InMemoryFrameDbAlt implements NarrowFrameStore {
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
  
  public void setFrameFactory(FrameFactory ff) {
    this.ff = ff;
  }
  
  public void flush() {
    flushCount++;
  }

  public void setJavaType(FrameID fid, int javaType) {
    FrameValue fv = getFrameValue(fid, true);
    fv.setJavaType(javaType);
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
    return getValues(frame, slot, facet, isTemplate, create);
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
    setValues(frame, slot, facet, isTemplate, values);
  }
  
  private void setValues(FrameID frameId,
                         FrameID slotId,
                         FrameID facetId,
                         boolean isTemplate,
                         List<Value> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    FrameSlotRequest request = new FrameSlotRequest(frameId, slotId, facetId, isTemplate);
    valueMap.put(request, values);
  }
  
  private List<Value> buildValues(Collection values) {
    List<Value> result = new ArrayList<Value>();
    for (Object v : values) {
      if (v instanceof Frame) {
        result.add(new FrameValue(((Frame) v).getFrameID()));
      } else  {
        result.add(new Value(v));
      }
    }
    return result;
  }
  
  
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
      return equalFrame && equalSlot && (isTemplate == other.isTemplate);
    }
    
    public int hashCode() {
      int hashCode = 42;
      if (frameId != null) {
        hashCode += frameId.hashCode();
      }
      if (slotId != null) {
        hashCode *= slotId.hashCode();
      }
      hashCode += (isTemplate ? 22 : 5);
      return hashCode;
    }
  };
  
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
  }
  
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
      return (FrameID) getObject();
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
      javaType = determineJavaType(f);
      frame = f;
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
    List results = new ArrayList();
    List<Value> values = getValues(frame, slot, facet, isTemplate, false);
    if (values == null) {
      return results;
    }
    for (Value v : values) {
      results.add(v.getObject());
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
    setValues(frame, slot, facet, isTemplate, buildValues(values));
  }

  /*
   * Not too efficient...
   */
  public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
                              Object value) {
    Set<Frame> frameSet = new HashSet<Frame>();
    FrameID slotId = slot.getFrameID();
    FrameID facetId = facet.getFrameID();
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
    FrameID facetId = facet.getFrameID();
    for (FrameID frameId : frames.keySet()) {
      boolean found = false;
      List<Value> values = getValues(frameId, slotId, facetId, isTemplate, false);
      if (values != null || !values.isEmpty()) {
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
    FrameID facetId = facet.getFrameID();
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
    Value v = new Value(value);
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
                                        (Slot) getFrame(req.getSlotId()),
                                        (Facet) getFrame(req.getFacetId()),
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
    frames.remove(frame.getFrameID());
    FrameValue fv = new FrameValue(frame);
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
      fv = new FrameValue(frame);
      frames.put(frame.getFrameID(), fv);
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
 
}
