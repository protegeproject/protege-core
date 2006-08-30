package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;

public class FrameEvaluationCompleted extends ValueUpdate implements Localizable {

  private static final long serialVersionUID = 7794961942298066290L;

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
  
  public String toString() {
    return "Completed[" + super.toString() + "]";
  }
  
}
