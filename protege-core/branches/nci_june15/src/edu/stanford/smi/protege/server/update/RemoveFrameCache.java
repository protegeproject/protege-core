package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Frame;

public class RemoveFrameCache extends ValueUpdate {

  /**
   * This update tells the client to remove the entire cache for the whole frame.
   * 
   * @param frame
   */
  public RemoveFrameCache(Frame frame) {
    super(frame);
  }
  
  public ValueUpdate getInvalidatingVariant() {
    return new RemoveFrameCache(getFrame());
  }
  
  public String toString() {
    return "RemoveFrameCache[" + super.toString() + "]";
  }

}
