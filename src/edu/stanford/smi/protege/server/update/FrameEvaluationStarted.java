package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.util.LocalizeUtils;


public class FrameEvaluationStarted extends ValueUpdate implements Localizable {
  
  public FrameEvaluationStarted(Frame frame) {
    super(frame);
  }
  
  public void localize(KnowledgeBase kb) {
    super.localize(kb);
  }
}
