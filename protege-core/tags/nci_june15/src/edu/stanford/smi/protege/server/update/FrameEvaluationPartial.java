package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;

public class FrameEvaluationPartial extends ValueUpdate {
  
  public FrameEvaluationPartial(Frame frame) {
    super(frame);
  }

  public void localize(KnowledgeBase kb) {
    super.localize(kb);
  }
  
  @Override
  public ValueUpdate getInvalidatingVariant() {
    return null;
  }

}
