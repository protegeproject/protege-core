package edu.stanford.smi.protege.server.update;

import java.util.List;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.LocalizeUtils;

public class FrameRead extends SftUpdate implements Localizable {
  private List values;
  
  public FrameRead(Frame frame, 
                   Slot slot, 
                   Facet facet, 
                   boolean isTemplate, 
                   List values) {
    super(frame, slot, facet, isTemplate);
    this.values = values;
  }

  public List getValues() {
    return values;
  }

  public void localize(KnowledgeBase kb) {
    super.localize(kb);
    LocalizeUtils.localize(values, kb);
  }
}
