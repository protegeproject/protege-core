package edu.stanford.smi.protege.server.framestore.background;

import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.SessionEvent;

public class FrameEvaluationEvent extends ClientNotification implements Localizable {
  private Slot slot;
  private Facet facet;
  private boolean isTemplate;
  private List values;
  
  public FrameEvaluationEvent(Frame frame, 
                              Slot slot, 
                              Facet facet, 
                              boolean isTemplate, 
                              List values) {
    super(frame);
    this.slot = slot;
    this.facet = facet;
    this.isTemplate = isTemplate;
    this.values = values;
  }
  
  public Facet getFacet() {
    return facet;
  }
  
  public Slot getSlot() {
    return slot;
  }
  
  public boolean isTemplate() {
    return isTemplate;
  }
  
  public List getValues() {
    return values;
  }

  public void localize(KnowledgeBase kb) {
    super.localize(kb);
    LocalizeUtils.localize(slot, kb);
    LocalizeUtils.localize(facet, kb);
    LocalizeUtils.localize(values, kb);
  }
}
