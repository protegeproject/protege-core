package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;


public class FrameEvaluationStarted extends ValueUpdate implements Localizable {
  private final static long serialVersionUID = 4569166782833280291L;
  
  public FrameEvaluationStarted(Frame frame) {
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
    return "Started[" + super.toString() + "]";
  }
}
