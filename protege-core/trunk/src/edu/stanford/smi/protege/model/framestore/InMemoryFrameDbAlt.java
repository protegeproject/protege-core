package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
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
  
  private List<Value> getValues(FrameID frameId, 
                                FrameID slotId, 
                                boolean isTemplate, 
                                boolean create) {
    FrameSlotRequest request = new FrameSlotRequest(frameId, slotId, isTemplate);
    List<Value> values = valueMap.get(request);
    if (create && values == null) {
      values = new ArrayList<Value>();
      valueMap.put(request, values);
    }
    return values;
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
    private boolean isTemplate;

    public FrameSlotRequest(FrameID frameId, FrameID slotId, boolean isTemplate) {
      this.frameId = frameId;
      this.slotId = slotId;
      this.isTemplate = isTemplate;
    }

    public FrameID getFrameId() {
      return frameId;
    }

    public FrameID getSlotId() {
      return slotId;
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
      
      if (f instanceof Cls) {
        javaType = DefaultFrameFactory.DEFAULT_CLS_JAVA_CLASS_ID;
      } else if (f instanceof Slot) {
        javaType = DefaultFrameFactory.DEFAULT_SLOT_JAVA_CLASS_ID;
      } else if (f instanceof Facet) {
        javaType = DefaultFrameFactory.DEFAULT_FACET_JAVA_CLASS_ID;
      } else if (f instanceof SimpleInstance) {
        javaType = DefaultFrameFactory.DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID;
      }
    }
    
    FrameID getFrameID() {
      return (FrameID) getObject();
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
    
    public Frame getObject() {
      if (cached()) {
        return frame;
      }
      return updateCache();
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
    List<Value> values = getValues(frame.getFrameID(), slot.getFrameID(), isTemplate, false);
    if (values == null) {
      return results;
    }
    for (Value v : values) {
      results.add(v.getObject());
    }
    return results;
  }

  public int getValuesCount(Frame frame, Slot slot, Facet facet,
                            boolean isTemplate) {
    List<Value> values = getValues(frame.getFrameID(), slot.getFrameID(), isTemplate, false);
    if (values == null) {
      return 0;
    } else {
      return values.size();
    }
  }

  public void addValues(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, Collection values) {
    List<Value> oldValues = getValues(frame.getFrameID(), slot.getFrameID(),isTemplate,true);
    oldValues.addAll(buildValues(values));
    valueMap.put(new FrameSlotRequest(frame.getFrameID(), slot.getFrameID(), isTemplate),
                 oldValues);
  }

  public void moveValue(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, int from, int to) {
    if (from == to || from < 0 || to < 0) {
      return;
    }
    List<Value> oldValues = getValues(frame.getFrameID(), slot.getFrameID(),isTemplate,true);
    if (from >= oldValues.size() || to >= oldValues.size()) {
      return;
    }
    Value v = oldValues.remove(from);
    oldValues.add(to, v);
    valueMap.put(new FrameSlotRequest(frame.getFrameID(), slot.getFrameID(), isTemplate),
                 oldValues);
  }

  public void removeValue(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Object value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void setValues(Frame frame, Slot slot, Facet facet,
                        boolean isTemplate, Collection values) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
                              Object value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet,
                                          boolean isTemplate) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set<Frame> getMatchingFrames(Slot slot, Facet facet,
                                      boolean isTemplate, String value,
                                      int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set<Reference> getReferences(Object value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set<Reference> getMatchingReferences(String value, int maxMatches) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set executeQuery(Query query) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void deleteFrame(Frame frame) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void close() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void replaceFrame(Frame frame) {
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
 
}
