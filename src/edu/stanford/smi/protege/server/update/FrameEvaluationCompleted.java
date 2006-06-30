package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;

public class FrameEvaluationCompleted extends ValueUpdate implements Localizable {
  
  public FrameEvaluationCompleted(Frame frame) {
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
