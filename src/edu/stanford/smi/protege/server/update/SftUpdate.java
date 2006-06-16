package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.LocalizeUtils;

public class SftUpdate extends ValueUpdate implements Localizable {
  private Slot slot;
  private Facet facet;
  private boolean isTemplate;

  public SftUpdate(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    super(frame);
    this.slot = slot;
    this.facet = facet;
    this.isTemplate = isTemplate;
  }
  
  public Facet getFacet() {
    return facet;
  }
  
  public boolean isTemplate() {
    return isTemplate;
  }

  public Slot getSlot() {
    return slot;
  }
  
  public void localize(KnowledgeBase kb) {
    super.localize(kb);
    LocalizeUtils.localize(slot, kb);
    LocalizeUtils.localize(facet, kb);
  }

  @Override
  public ValueUpdate getInvalidatingVariant() {
    return new InvalidateCacheUpdate(getFrame(), slot, facet, isTemplate);
  }
  
  public String toString() {
    return "[" + super.toString() + ", " + slot.getFrameID() + ", " 
              + (facet == null ? "null" : "" + facet.getFrameID()) + ", " + isTemplate + "]";
  }
  

}
